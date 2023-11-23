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
package com.braintribe.devrock.artifactcontainer.quickImport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.quickImport.notification.QuickImportScanResultBroadcaster;
import com.braintribe.devrock.artifactcontainer.quickImport.notification.QuickImportScanResultListener;
import com.braintribe.devrock.artifactcontainer.quickImport.ui.CamelCasePatternExpander;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.QuickImportPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryPairing;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SvnPreferences;
import com.braintribe.model.panther.ProjectNature;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.plugin.commons.selection.PantherSelectionHelper;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;

public class ParallelQuickImportControl implements QuickImportControl, QuickImportScanResultBroadcaster, QuickImportScanResultListener, ProcessAbortSignaller {	
	private static final String PAYLOAD = "payload.xml";
	private ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
	private QuickImportScanManager scanManager;
	private List<QuickImportScanResultListener> listeners = new ArrayList<QuickImportScanResultListener>();
	private CamelCasePatternExpander patternExpander = new CamelCasePatternExpander();
	private enum Notation { group,artifact,version, all}
	private boolean stopped = false;
	private ForkJoinPool pool = new ForkJoinPool( 5);
	private List<SourceArtifact> population;
	private boolean reinitializeSmood = false;
	private ReentrantReadWriteLock smoodInitializationlock = new ReentrantReadWriteLock();
	
	private StaxMarshaller marshaller = new StaxMarshaller();
	private boolean isSetup = false;

	@Override
	public void stop() {
		if (scanManager == null)
			return;
		synchronized (scanManager) {
			stopped = true;
		}
	}
	
	@Override
	public void setup() {
		// only setup once
		if (isSetup)
			return;
		// 
		try {
			PantherSelectionHelper.primeRepositoryInformation();
		}
		catch (Exception e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "Cannot prime repository information", IStatus.WARNING);
			ArtifactContainerPlugin.getInstance().log(status);
			return;
		}
		
		QuickImportPreferences qiPreferences = plugin.getArtifactContainerPreferences(false).getQuickImportPreferences();
		if (qiPreferences.getLocalOnlyNature()) {			
			scanManager = new QuickImportScanManager();
			// if any previous result has been stored, prime the scan manager with it
			Map<SourceRepositoryPairing, List<SourceArtifact>> storedResult = loadResult();
			if (storedResult != null) {
				scanManager.primeWith( storedResult);
			}
			scanManager.addQuickImportScanResultListener( this);						
		}
		isSetup = true;
	}
	
	@Override
	public boolean isScanActive() {
		if (scanManager != null)
			return scanManager.isActive();
		return false;
	}
	
	private Map<SourceRepositoryPairing, List<SourceArtifact>> getScannedSourceArtifacts() {
		if (scanManager == null)
			return new HashMap<SourceRepositoryPairing, List<SourceArtifact>>();		
		if (scanManager.isActive() == false) {
			return scanManager.getSourceArtifacts();			
		}		
		if (scanManager.isPrimed()) {
			return scanManager.getSourceArtifacts();
		}
		
		return new HashMap<SourceRepositoryPairing, List<SourceArtifact>>();
	}
	@Override
	public void rescan() {
		if (scanManager == null)
			return;
		if (scanManager.isActive() == false) {
			scanManager.scanAllSourceRepositoryAsJob();			
		}
	}
	
	@Override
	public void rescan(SourceRepositoryPairing sourceRepositoryPairing) {
		if (scanManager == null)
			return;
		if (scanManager.isActive() == false) {
			scanManager.scanSingleSourceRepositoryAsJob( sourceRepositoryPairing);			
		}
	}
	

	@Override
	public void acknowledgeScanResult(SourceRepositoryPairing pairing, List<SourceArtifact> result) {
		if (stopped) {
			return;
		}
		reinitializeSmood = true;					
		// store the data 
		storeScanResults( scanManager.getSourceArtifacts());								
		for (QuickImportScanResultListener listener : listeners) {
			listener.acknowledgeScanResult(pairing, result);
		}
	}
	
	private Map<SourceRepositoryPairing, List<SourceArtifact>> loadResult(){		
	
		File scanResultFile = getPersistedScanResultFile();
		if (scanResultFile.exists()) {			
			try (InputStream in = Archives.zip().from(getPersistedScanResultFile()).getEntry(PAYLOAD).getPayload()) {
				@SuppressWarnings("unchecked")
				Map<SourceRepositoryPairing, List<SourceArtifact>> result = (Map<SourceRepositoryPairing, List<SourceArtifact>>) marshaller.unmarshall(in);
				return result;
			} catch (Exception e) {				
				ArtifactContainerStatus status = new ArtifactContainerStatus( "cannot load stored scan result", e);
				ArtifactContainerPlugin.getInstance().log(status);		
			}				
		}			
		return null;
	}

	private void storeScanResults(Map<SourceRepositoryPairing, List<SourceArtifact>> result) {
		//
		// clear the pairing no longer in the preferences
		//	
		SvnPreferences svnPreferences = plugin.getArtifactContainerPreferences(false).getSvnPreferences();
		List<SourceRepositoryPairing> sourceRepositoryPairings = svnPreferences.getSourceRepositoryPairings();
		Map<SourceRepositoryPairing, List<SourceArtifact>> toBeStored = new HashMap<SourceRepositoryPairing, List<SourceArtifact>>();
		
		for (Entry<SourceRepositoryPairing, List<SourceArtifact>> entry :result.entrySet()) {
			for (SourceRepositoryPairing pairing : sourceRepositoryPairings) {
				SourceRepositoryPairing suspect = entry.getKey();
				// name is still listed 
				if (suspect.getName().equalsIgnoreCase( pairing.getName())) {
					toBeStored.put( entry.getKey(), entry.getValue());
					break;
				}
			}
		}
		
		
		
		File dumpFile;
		try {
			dumpFile = File.createTempFile("ac_scanresult", ".xml");
		} catch (IOException e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "cannot store scan result", e);
			ArtifactContainerPlugin.getInstance().log(status);
			return;
		}

		try (OutputStream out = new FileOutputStream(dumpFile)) {
			marshaller.marshall(out, toBeStored, GmSerializationOptions.defaults.outputPrettiness( OutputPrettiness.high));
		}
		catch (Exception e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "cannot store scan result", e);
			ArtifactContainerPlugin.getInstance().log(status);
		}		
		File scanResultFile = getPersistedScanResultFile();
		try {
			Archives.zip().add(PAYLOAD, dumpFile).to( scanResultFile).close();
		} catch (ArchivesException e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "cannot store scan result", e);
			ArtifactContainerPlugin.getInstance().log(status);		
		}		
		finally  {
			dumpFile.delete();
		}
		
	}

	@Override
	public void addQuickImportScanResultListener(QuickImportScanResultListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeQuickImportScanResultListener(QuickImportScanResultListener listener) {
		listeners.remove( listener);
	}
	
	private List<SourceArtifact> getPopulation() {
		Lock lock = smoodInitializationlock.writeLock();				
		if (population == null || reinitializeSmood) {
			try {
				lock.lock();						
				population = new ArrayList<>();
				Map<SourceRepositoryPairing, List<SourceArtifact>> scannedSourceArtifacts = getScannedSourceArtifacts();
				for (List<SourceArtifact> sourceArtifact : scannedSourceArtifacts.values()) {
					population.addAll(sourceArtifact);
				}
				reinitializeSmood = false;
			}
			finally {
				lock.unlock();
			}				
		}			
		return population;		
	}
	
	
	@Override
	public boolean abortScan() {
		synchronized ( scanManager) {
			return stopped;
		}
	}
	
	

	/**
	 * uses the parameter to run a query against the session, and then
	 * let's the tree update itself in the UI thread 
	 * @param txt - the {@link String} with the expression 
	 */
	class ConjunctionPredicate implements Predicate<SourceArtifact> {
		List<Predicate<SourceArtifact>> predicates = new ArrayList<>();
		
		public ConjunctionPredicate( @SuppressWarnings("unchecked") Predicate<SourceArtifact> ... predicates) {
			this.predicates.addAll( Arrays.asList( predicates));
		}
		
		public ConjunctionPredicate(Collection<Predicate<SourceArtifact>> predicates) {
			this.predicates.addAll( predicates);
		}
		
		@Override
		public boolean test(SourceArtifact t) {
			for (Predicate<SourceArtifact> predicate : predicates) {
				if (!predicate.test( t))
					return false;
			}
			return true;
		}		
	}
	
	class DisjunctionPredicate implements Predicate<SourceArtifact> {
		List<Predicate<SourceArtifact>> predicates = new ArrayList<>();
		
		public DisjunctionPredicate( @SuppressWarnings("unchecked") Predicate<SourceArtifact> ... predicates) {
			this.predicates.addAll( Arrays.asList( predicates));
		}
		public DisjunctionPredicate(Collection<Predicate<SourceArtifact>> predicates) {
			this.predicates.addAll( predicates);
		}
		
		@Override
		public boolean test(SourceArtifact t) {
			
			for (Predicate<SourceArtifact> predicate : predicates) {
				if (predicate.test( t))
					return true;
			}
			return false;
		}		
	}
	
	enum QueryMode {standard, coarse, partial, pom}

	/**	
	 * run a query for a fully qualified {@link SourceArtifact}
	 * @param txt
	 * @return
	 */
	public List<SourceArtifact> runSourceArtifactQuery( String txt, QueryMode mode) {
		Predicate<SourceArtifact> predicate = null;
		
		String [] txts = txt.split( "\\|");
		if (txts.length > 1) {		
			List<Predicate<SourceArtifact>> predicates = new ArrayList<>();
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
		List<SourceArtifact> result = filter(predicate);
		return result != null ? new ArrayList<SourceArtifact>( result) : Collections.emptyList();							
	}
	
	private List<SourceArtifact> filter( Predicate<SourceArtifact> predicate) {
		Callable<List<SourceArtifact>> result = () -> getPopulation().parallelStream().filter(predicate).collect( Collectors.toList());	
		try {
			return pool.submit( result).get();
		} catch (Exception e) {
			return Collections.emptyList();
		} 
		  
	}
	
	@Override
	public List<SourceArtifact> runCoarseSourceArtifactQuery( String txt) {
		return runSourceArtifactQuery(txt, QueryMode.coarse);
	}
	@Override
	public List<SourceArtifact> runSourceArtifactQuery( String txt) {
		return runSourceArtifactQuery(txt, QueryMode.standard);
	}
	@Override
	public List<SourceArtifact> runPartialSourceArtifactQuery( String txt) {
		return runSourceArtifactQuery(txt, QueryMode.partial);
	}
	
	
	/**
	 * local pomfile, local repository 
	 * @param pomFile
	 * @return
	 */
	@Override
	public List<SourceArtifact> runPomFileToSourceArtifactQuery( File pomFile) {
		String absolutePath = pomFile.getParentFile().getAbsolutePath();
		SourceRepository backingLocalSourceRepository = PantherSelectionHelper.findMatchingLocalRepresentationSourceRepositoryFromPath(absolutePath);
		if (backingLocalSourceRepository != null) {			
			int protcolLength = "file:".length();
			int cutlength = backingLocalSourceRepository.getRepoUrl().length() - protcolLength;
			absolutePath = absolutePath.substring( cutlength+1);
		}
		return runSourceArtifactQuery(absolutePath, QueryMode.pom);				
	}
	
	/**
	 * @param value
	 * @return
	 */
	private Predicate<SourceArtifact> processPomQuery(String value) {
		
		return new Predicate<SourceArtifact>() {

			@Override
			public boolean test(SourceArtifact t) {
				if (t.getPath().equalsIgnoreCase(value))
					return true;
				return false;
			}			
		};
	}
		
	/**
	 * @param txt
	 * @return
	 */
	private Predicate<SourceArtifact> processSourceArtifact( String txt) {
		Artifact artifact = NameParser.parseCondensedArtifactName(txt);
		return new Predicate<SourceArtifact>() {

			@Override
			public boolean test(SourceArtifact t) {
				if (!t.getGroupId().equalsIgnoreCase( artifact.getGroupId()))
					return false;
				if (!t.getArtifactId().equalsIgnoreCase(artifact.getArtifactId()))
					return false;
				if (!t.getVersion().equalsIgnoreCase( VersionProcessor.toString( artifact.getVersion())))
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
	private Predicate<SourceArtifact> processPartialSourceArtifact( String txt) {
		String [] parts = txt.split( ":");
		if (parts.length != 2)
			return new Predicate<SourceArtifact>() {

				@Override
				public boolean test(SourceArtifact t) {			
					return true;
				}
			
			};
			
		return new Predicate<SourceArtifact>() {

			@Override
			public boolean test(SourceArtifact t) {
				if (!t.getGroupId().equalsIgnoreCase( parts[0]))
					return false;
				if (!t.getArtifactId().equalsIgnoreCase( parts[1]))
					return false;
				if (!t.getNatures().contains(ProjectNature.eclipse))				
					return false;
				return true;
			}
			
		};				
	}
	
	/**
	 * build a predicate from the value of the QuickImporter's edit box  
	 * @param txt - the expression as {@link String}
	 * @return - the matching {@link Predicate} 
	 */
	private Predicate<SourceArtifact> processExpression( String txt) {
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
				
				return new Predicate<SourceArtifact>() {

					@Override
					public boolean test(SourceArtifact t) {
						if (!t.getGroupId().matches( grp))
							return false;
						if (!t.getArtifactId().matches( artf))
							return false;
						if (!t.getVersion().matches( vrsn))
							return false;				
						return true;
					}
					
				};
			}
			case group: {
				final String grp = patternExpander.expand(txt.substring(0, groupIndex));
				final String artf = patternExpander.expand(txt.substring(groupIndex +1));				
				
				return new Predicate<SourceArtifact>() {

					@Override
					public boolean test(SourceArtifact t) {
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
				
				return new Predicate<SourceArtifact>() {

					@Override
					public boolean test(SourceArtifact t) {
						if (!t.getArtifactId().matches( artf))
							return false;
						if (!t.getVersion().matches( vrsn))
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
				return new Predicate<SourceArtifact>() {
					
					@Override
					public boolean test(SourceArtifact t) {
						if (!t.getArtifactId().matches( v))
							return false;
						return true;
					}						
				};					
			}
		
		}			
	}
		
	private File getPersistedScanResultFile() {
		String path = plugin.getStateLocation().toOSString();
		return new File( path + File.separator + ArtifactContainerPlugin.PLUGIN_ID + ".scanResult.zip");
	}
}
