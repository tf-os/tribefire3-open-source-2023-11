// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.devrock.importer.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;

import com.braintribe.common.potential.Potential;
import com.braintribe.devrock.bridge.eclipse.api.EnvironmentBridge;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.eclipse.model.reason.devrock.ScanDataDecodingNoData;
import com.braintribe.devrock.eclipse.model.scan.SourceRepositoryEntry;
import com.braintribe.devrock.importer.CamelCasePatternExpander;
import com.braintribe.devrock.importer.scanner.listener.QuickImportScanListener;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.utils.lcd.LazyInitialized;

/**
 * the smart wrapper around the {@link SourceScanner}
 * 
 * @author pit
 *
 */
public class ParallelQuickImportControl implements QuickImportControl{
	private static Logger log = Logger.getLogger(ParallelQuickImportControl.class);
	private static final String STORE_FILE = "scan-result.zip";	
	private final CamelCasePatternExpander patternExpander = new CamelCasePatternExpander();
	private enum Notation { group,artifact,version, all}
	private final ForkJoinPool pool = new ForkJoinPool( 5);
	
	private final LazyInitialized<SourceScanner> scanner = new LazyInitialized<>( this::setup);
	private Set<EnhancedCompiledArtifactIdentification> population;
	private final List<QuickImportScanListener> listeners = new ArrayList<>();

	@Override
	public void stop() {				
		File persistedScanResultFile = getPersistedScanResultFile();
		if (population == null || population.size() == 0) {
			persistedScanResultFile.delete();
		}
		else {
			scanner.get().encode(persistedScanResultFile, population);
		}
	}
	
	/**
	 * @return - a lazily created and primed scanner 
	 */
	private SourceScanner setup() {		
		SourceScanner scanner = new SourceScanner();	
		return scanner;
	}
	
	public Set<EnhancedCompiledArtifactIdentification> acquirePopulation() {		
		
		if (population != null) { 
			return population;
		}
		// if any previous result has been stored, prime the scan manager with it
		Potential<Set<EnhancedCompiledArtifactIdentification>,Reason> pStoredResult = scanner.get().decode( getPersistedScanResultFile());
		
		if (pStoredResult.isFilled()) {
			population = pStoredResult.get(); 
			return population;
		}							
		else {
			Reason reason = pStoredResult.whyEmpty();
			// scanner reports a non-existing file, so if we want to raise the alarm, we must make sure it's another problem
			if (reason instanceof ScanDataDecodingNoData) {
				String msg = "error during decoding stored scan result :" + reason.stringify();
				log.error(msg);
				DevrockPluginStatus status = new DevrockPluginStatus( msg, IStatus.ERROR);
				DevrockPlugin.instance().log(status);
				return Collections.emptySet();		
			}			
		}
		population = scanner.get().scanSourceRepositories();
		return population;			
	}
	
	@Override
	public boolean isScanActive() {
		return scanner.get().isActive();
	}
		
	@Override
	public void rescan() {
		population = scanner.get().scanSourceRepositories();
		for (QuickImportScanListener listener : listeners) {
			listener.acknowledgeScanResult(population);
		}
	}
		
	@Override
	public void rescan(List<SourceRepositoryEntry> roots) {
		List<File> filesToScan = new ArrayList<>( roots.size());
		for (SourceRepositoryEntry entry : roots) {
			filesToScan.add( new File( entry.getActualFile()));
		}
		population = scanner.get().scanSourceRepositories( filesToScan);
		for (QuickImportScanListener listener : listeners) {
			listener.acknowledgeScanResult(population);
		}
	}
		
	public boolean abortScan() {
		return true;
	}
	
	/**
	 * uses the parameter to run a query against the session, and then
	 * let's the tree update itself in the UI thread 
	 * @param txt - the {@link String} with the expression 
	 */
	class ConjunctionPredicate implements Predicate<EnhancedCompiledArtifactIdentification> {
		List<Predicate<EnhancedCompiledArtifactIdentification>> predicates = new ArrayList<>();
		
		public ConjunctionPredicate( @SuppressWarnings("unchecked") Predicate<EnhancedCompiledArtifactIdentification> ... predicates) {
			this.predicates.addAll( Arrays.asList( predicates));
		}
		
		public ConjunctionPredicate(Collection<Predicate<EnhancedCompiledArtifactIdentification>> predicates) {
			this.predicates.addAll( predicates);
		}
		
		@Override
		public boolean test(EnhancedCompiledArtifactIdentification t) {
			for (Predicate<EnhancedCompiledArtifactIdentification> predicate : predicates) {
				if (!predicate.test( t))
					return false;
			}
			return true;
		}		
	}
	
	class DisjunctionPredicate implements Predicate<EnhancedCompiledArtifactIdentification> {
		List<Predicate<EnhancedCompiledArtifactIdentification>> predicates = new ArrayList<>();
		
		public DisjunctionPredicate( @SuppressWarnings("unchecked") Predicate<EnhancedCompiledArtifactIdentification> ... predicates) {
			this.predicates.addAll( Arrays.asList( predicates));
		}
		public DisjunctionPredicate(Collection<Predicate<EnhancedCompiledArtifactIdentification>> predicates) {
			this.predicates.addAll( predicates);
		}
		
		@Override
		public boolean test(EnhancedCompiledArtifactIdentification t) {
			
			for (Predicate<EnhancedCompiledArtifactIdentification> predicate : predicates) {
				if (predicate.test( t))
					return true;
			}
			return false;
		}		
	}
	
	enum QueryMode {standard, coarse, partial, pom}

	/**	
	 * run a query for a fully qualified {@link EnhancedCompiledArtifactIdentification}
	 * @param txt
	 * @return
	 */
	public List<EnhancedCompiledArtifactIdentification> runSourceArtifactQuery( String txt, QueryMode mode) {
		Predicate<EnhancedCompiledArtifactIdentification> predicate = null;
		
		String [] txts = txt.split( "\\|");
		if (txts.length > 1) {		
			List<Predicate<EnhancedCompiledArtifactIdentification>> predicates = new ArrayList<>();
			for (String value : txts) {
				switch (mode) {
					case coarse:
						predicates.add( processExpression(value));
						break;
					case partial:
						predicates.add( processPartialSourceArtifact(value));
						break;
					case pom :
						predicates.add( processPomQuery(value));
						break;
					case standard:			
					default:
						predicates.add( processSourceArtifact(value));
						break;				
				}				
			}			
			predicate = new DisjunctionPredicate(predicates);
		} else {
			String value = txts[0];
			switch (mode) {
				case coarse:
					predicate = processExpression( value);
					break;
				case partial:
					predicate = processPartialSourceArtifact(value);
					break;				
				case pom:
					predicate = processPomQuery(value);
					break;
				case standard:				
				default:
					predicate = processSourceArtifact( value);
					break;				
				}
		}

		return filter(predicate);
	}
	
	private List<EnhancedCompiledArtifactIdentification> filter( Predicate<EnhancedCompiledArtifactIdentification> predicate) {
		Callable<List<EnhancedCompiledArtifactIdentification>> result = () -> acquirePopulation().parallelStream().filter(predicate).collect( Collectors.toList());	
		try {
			return pool.submit( result).get();
		} catch (Exception e) {
			return Collections.emptyList();
		} 
		  
	}
	
//	@Override
//	public List<EnhancedCompiledArtifactIdentification> runCoarseSourceArtifactQuery( String txt) {
//		return runSourceArtifactQuery(txt, QueryMode.coarse);
//	}
	
	
	@Override
	public List<EnhancedCompiledArtifactIdentification> runProjectToSourceQuery(String condensedArtifact) {
		return runSourceArtifactQuery(condensedArtifact, QueryMode.standard);
	}

	@Override
	public List<EnhancedCompiledArtifactIdentification> runPartialSourceArtifactQuery( String txt) {
		return runSourceArtifactQuery(txt, QueryMode.partial);
	}
	
	
	/**
	 * local pomfile, local repository 
	 * @param pomFile
	 * @return
	 */
	//@Override
	public List<EnhancedCompiledArtifactIdentification> runPomFileToSourceArtifactQuery( File pomFile) {
		String absolutePath = pomFile.getParentFile().getAbsolutePath();		
		return runSourceArtifactQuery(absolutePath, QueryMode.pom);				
	}
	
	/**
	 * @param value
	 * @return
	 */
	private Predicate<EnhancedCompiledArtifactIdentification> processPomQuery(String value) {
		
		return new Predicate<EnhancedCompiledArtifactIdentification>() {

			@Override
			public boolean test(EnhancedCompiledArtifactIdentification t) {
				if (t.getOrigin().equalsIgnoreCase(value))
					return true;
				return false;
			}			
		};
	}
		
	/**
	 * @param txt
	 * @return
	 */
	private Predicate<EnhancedCompiledArtifactIdentification> processSourceArtifact( String txt) {
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse(txt);
		return new Predicate<EnhancedCompiledArtifactIdentification>() {

			@Override
			public boolean test(EnhancedCompiledArtifactIdentification t) {
				if (!t.getGroupId().equalsIgnoreCase( cai.getGroupId()))
					return false;
				if (!t.getArtifactId().equalsIgnoreCase(cai.getArtifactId()))
					return false;
				if (!t.getVersion().matches(cai.getVersion()))
					return false;				
				return true;
			}
			
		};			
	}
	
	/**
	 * build a {@link Predicate} from the passed {@link String} for a partial query 
	 * @param txt - the {@link String}, i.e. groupId, artifactId
	 * @return - the {@link Predicate}
	 */
	private Predicate<EnhancedCompiledArtifactIdentification> processPartialSourceArtifact( String txt) {
		String [] parts = txt.split( ":");
		if (parts.length != 2)
			return new Predicate<EnhancedCompiledArtifactIdentification>() {

				@Override
				public boolean test(EnhancedCompiledArtifactIdentification t) {			
					return true;
				}
			
			};
			
		return new Predicate<EnhancedCompiledArtifactIdentification>() {

			@Override
			public boolean test(EnhancedCompiledArtifactIdentification t) {
				if (!t.getGroupId().equalsIgnoreCase( parts[0]))
					return false;
				if (!t.getArtifactId().equalsIgnoreCase( parts[1]))
					return false;
				/*
				if (!t.getNatures().contains(ProjectNature.eclipse))				
					return false;
				*/
				return true;
			}
			
		};				
	}
	
	/**
	 * build a predicate from the value of the QuickImporter's edit box,
	 * analyze the expression and derive the level of the query's predicate strictness
	 * - possible strictnesses :
	 * 	all : groupId, artifactId, version
	 *  group : groupId, artifactId
	 *  version : artifactId, version  
	 * @param txt - the expression as {@link String}
	 * @return - the matching {@link Predicate} 
	 */
	private Predicate<EnhancedCompiledArtifactIdentification> processExpression( String txt) {
		Notation notation = Notation.artifact;
			
		int groupIndex = txt.indexOf(":");	
		int versionIndex = txt.indexOf("#");
		
		if (groupIndex < 0 && versionIndex < 0) {
			notation = Notation.artifact;
		} else {
			if (groupIndex >= 0) {
				notation=Notation.group;
				if (versionIndex > 0)
					notation=Notation.all;
			} else {
				if (versionIndex >= 0) {
					notation = Notation.version;
				}
			}			
		}
		
		switch (notation) {
			case all: {
				final String grp = patternExpander.expand( txt.substring(0, groupIndex));
				final String vrsn = patternExpander.expand( txt.substring( versionIndex + 1));
				final String artf = patternExpander.expand( txt.substring(groupIndex+1, versionIndex));		
				
				return new Predicate<EnhancedCompiledArtifactIdentification>() {

					@Override
					public boolean test(EnhancedCompiledArtifactIdentification t) {
						if (!t.getGroupId().matches( grp))
							return false;
						if (!t.getArtifactId().matches( artf))
							return false;
						if (!t.getVersion().asString().matches( vrsn))
							return false;				
						return true;
					}
					
				};
			}
			case group: {
				final String grp = patternExpander.expand(txt.substring(0, groupIndex));
				final String artf = patternExpander.expand(txt.substring(groupIndex +1));				
				
				return new Predicate<EnhancedCompiledArtifactIdentification>() {

					@Override
					public boolean test(EnhancedCompiledArtifactIdentification t) {
						if (!t.getGroupId().matches( grp))
							return false;
						if (!t.getArtifactId().matches( artf))
							return false;							
						return true;
					}
					
				};
			}
			case version : {
				final String artf = patternExpander.expand( txt.substring( 0, versionIndex));
				final String vrsn = patternExpander.expand(txt.substring( versionIndex + 1));
				
				return new Predicate<EnhancedCompiledArtifactIdentification>() {

					@Override
					public boolean test(EnhancedCompiledArtifactIdentification t) {
						if (!t.getArtifactId().matches( artf))
							return false;
						if (!t.getVersion().asString().matches( vrsn))
							return false;				
						return true;
					}
					
				};
			}				
			case artifact: 					
			default: {
				if (
						txt.contains( "*") == false &&
						patternExpander.isPrecise(txt) == false
					)					
					txt = patternExpander.expand(txt);
				else 
					txt = patternExpander.sanitize(txt);
				
				final String v = txt;
				return new Predicate<EnhancedCompiledArtifactIdentification>() {
					
					@Override
					public boolean test(EnhancedCompiledArtifactIdentification t) {
						if (!t.getArtifactId().matches( v))
							return false;
						return true;
					}						
				};					
			}
		
		}			
	}		
	private File getPersistedScanResultFile() {
		// just required to run the main function below
		if (DevrockPlugin.instance() == null) {
			return new File("non-existing-file-just-to-enable-tests");
		}
		
		// location of the stored scan data is retrieved from the enviroment
		EnvironmentBridge envBridge = DevrockPlugin.envBridge();
		File location = envBridge.workspaceSpecificStorageLocation();
		return new File( location, STORE_FILE);		
	}



	@Override
	public void addListener(QuickImportScanListener listener) {
		listeners.add( listener);		
	}

	@Override
	public void removeListener(QuickImportScanListener listener) {
		listeners.remove( listener);
		
	}

	@Override
	public List<EnhancedCompiledArtifactIdentification> runQuery(String expression) {
		return runSourceArtifactQuery(expression, QueryMode.coarse);
	}
	
	

	@Override
	public List<EnhancedCompiledArtifactIdentification> runSourceArtifactQuery(String expression) {
		return runSourceArtifactQuery(expression, QueryMode.partial);
	}
		
	@Override
	public List<EnhancedCompiledArtifactIdentification> runContainsQuery(String txt) {
		Callable<List<EnhancedCompiledArtifactIdentification>> result = () -> acquirePopulation().parallelStream()
				.filter( ecai -> {
					return ecai.asString().contains( txt);
				}).collect( Collectors.toList());	
		
		try {
			return pool.submit( result).get();
		} catch (Exception e) {
			return Collections.emptyList();
		} 				
	}

	public static void main(String [] args) {
		ParallelQuickImportControl ic = new ParallelQuickImportControl();
		
		List<EnhancedCompiledArtifactIdentification> runQuery = ic.runQuery("com.braintribe.devrock:artifact-model");
		String result = runQuery.stream().map( e -> e.asString()).collect(Collectors.joining(","));
		System.out.println("-> " + result);
	}
	
}

