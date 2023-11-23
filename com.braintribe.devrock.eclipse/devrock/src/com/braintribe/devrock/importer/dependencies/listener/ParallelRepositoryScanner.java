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
package com.braintribe.devrock.importer.dependencies.listener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.bridge.eclipse.api.EnvironmentBridge;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.eclipse.model.identification.RemoteCompiledDependencyIdentification;
import com.braintribe.devrock.eclipse.model.reason.devrock.ScanDataDecodeFailure;
import com.braintribe.devrock.eclipse.model.reason.devrock.ScanDataDecodingNoData;
import com.braintribe.devrock.eclipse.model.reason.devrock.ScanDataEncodeFailure;
import com.braintribe.devrock.eclipse.model.scan.SourceRepositoryEntry;
import com.braintribe.devrock.importer.CamelCasePatternExpander;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.logging.Logger;

/**
 * the repository scanner, i.e. the thing that accesses RH and gets the content 
 * in form of {@link RemoteCompiledArtifactIdentification} instances
 * @author pit
 *
 */
public class ParallelRepositoryScanner implements ScanControl {
	private static Logger log = Logger.getLogger(ParallelRepositoryScanner.class);
	private static final String STORE_FILE = "remote-scan-result.zip";
	private static final String PAYLOAD = "payload.yaml";		

	private boolean scanIsActive;
	private List<RemoteRepositoryScanListener> listeners = new ArrayList<>();
	private CamelCasePatternExpander patternExpander = new CamelCasePatternExpander();
	private enum Notation { group,artifact,version, all}
	private final ForkJoinPool pool = new ForkJoinPool( 5);
	private List<RemoteCompiledDependencyIdentification> population;
	private static YamlMarshaller marshaller;
	static {
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}
	
	@Override
	public boolean isScanActive() {
		return scanIsActive;
	}

	@Override
	public void rescan(IProgressMonitor monitor) {
		scanIsActive = true;
		scanAndStorePopulation( monitor);
		scanIsActive = false;
		
		for (RemoteRepositoryScanListener listener : listeners) {
			listener.acknowledgeScanResult( population);
		}
	}
	

	/**
	 * runs a scan, extracts the data, and stores it
	 */
	private List<RemoteCompiledDependencyIdentification> scanAndStorePopulation(IProgressMonitor monitor) {
		if (monitor != null) {		
			monitor.beginTask("scanning remote repositories", IProgressMonitor.UNKNOWN);
		}
		List<RemoteCompiledDependencyIdentification> remoteArtifactPopulation = DevrockPlugin.mcBridge().retrieveCurrentRemoteArtifactPopulation();
		if (monitor != null) {
			monitor.beginTask("scanning local repository", IProgressMonitor.UNKNOWN);
		}
		List<RemoteCompiledDependencyIdentification> localArtifactPopulation = DevrockPlugin.mcBridge().retrieveCurrentLocalArtifactPopulation();
		if (monitor != null) {
			monitor.beginTask("scanning source repositories", IProgressMonitor.UNKNOWN);
		}
		Set<EnhancedCompiledArtifactIdentification> rawSourcesArtifactPopulation = DevrockPlugin.instance().quickImportController().acquirePopulation();
		if (monitor != null) {
			monitor.beginTask("collating results", IProgressMonitor.UNKNOWN);
		}
		// converting source result to compatible results
		List<SourceRepositoryEntry> scanRepositories = DevrockPlugin.envBridge().getScanRepositories();	
		List<RemoteCompiledDependencyIdentification> sourcesArtifactPopulation = rawSourcesArtifactPopulation.stream()
				.map( ecai -> ecaiToRcdi( ecai, scanRepositories))
				.collect( Collectors.toList());

		// build population
		population = new ArrayList<>( remoteArtifactPopulation.size() + localArtifactPopulation.size() + sourcesArtifactPopulation.size());
		
		population.addAll(remoteArtifactPopulation);
		population.addAll(localArtifactPopulation);
		population.addAll( sourcesArtifactPopulation);
		// persist
		encode(getPersistedScanResultFile(), population);
		
		if (monitor != null) {
			monitor.done();
		}
		return population;
	}
	
	private RemoteCompiledDependencyIdentification ecaiToRcdi(EnhancedCompiledArtifactIdentification ecai, List<SourceRepositoryEntry> scanRepositories) {
		RemoteCompiledDependencyIdentification rcid = RemoteCompiledDependencyIdentification.from(ecai);
		String origin = ecai.getOrigin();
		SourceRepositoryEntry entry = scanRepositories.stream().filter( se -> origin.startsWith( se.getActualFile())).findFirst().orElse( null);		
		if (entry != null) {
			File actualFile = new File(entry.getActualFile());
			File originFile = new File( origin);
			Path path = actualFile.toPath().relativize( originFile.toPath());			
			rcid.setSourceRepositoryOrigin( path.toString());
			rcid.setSourceRepositoryKey(entry.getKey());
			rcid.setSourceOrigin( origin);
		}
		return rcid;
	}
	
	@Override
	public void stop() {		
	}
	
	@Override
	public void addListener(RemoteRepositoryScanListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(RemoteRepositoryScanListener listener) {
		listeners.remove( listener);
	}

	@Override
	public List<RemoteCompiledDependencyIdentification> runQuery(String txt) {
		Predicate<RemoteCompiledDependencyIdentification> predicate = processExpression(txt);
		return filter(predicate);
	}
	
	/**
	 * @return - the {@link File} that is to be used to store the population
	 */
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
	public List<RemoteCompiledDependencyIdentification> runContainsQuery(String txt) {
		Predicate<RemoteCompiledDependencyIdentification> predicate = (rai) -> rai.asString().contains(txt);				
		return filter( predicate);
	}
	
	/**
	 * if a population has been scanned, it is returned, otherwise it will look for 
	 * a persistence file to load, and finally, if it's not present, get the data
	 * @return - returns a population, aka a {@link List} of {@link RemoteCompiledArtifactIdentification}
	 */
	private List<RemoteCompiledDependencyIdentification> acquirePopulation() {
		if (population != null) {
			return population;
		}
		File persistedScanResultFile = getPersistedScanResultFile();
		if (persistedScanResultFile.exists())  {
			Maybe<List<RemoteCompiledDependencyIdentification>> decode = decode(persistedScanResultFile);
			if (decode.isSatisfied()) {
				population = decode.get();
				return population;
			}
		}
		
		scanAndStorePopulation( null);		
		return population;
	}
	
	/**
	 * build a {@link Predicate} from the camel-case expression
	 * @param txt - the expressions as entered
	 * @return - a matching {@link Predicate}
	 */
	private Predicate<RemoteCompiledDependencyIdentification> processExpression( String txt) {
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
				
				return new Predicate<RemoteCompiledDependencyIdentification>() {

					@Override
					public boolean test(RemoteCompiledDependencyIdentification t) {
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
				
				return new Predicate<RemoteCompiledDependencyIdentification>() {

					@Override
					public boolean test(RemoteCompiledDependencyIdentification t) {
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
				
				return new Predicate<RemoteCompiledDependencyIdentification>() {

					@Override
					public boolean test(RemoteCompiledDependencyIdentification t) {
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
				return new Predicate<RemoteCompiledDependencyIdentification>() {
					
					@Override
					public boolean test(RemoteCompiledDependencyIdentification t) {
						if (!t.getArtifactId().matches( v))
							return false;
						return true;
					}						
				};					
			}
		
		}			
	}	

	/**
	 * filter the population in parallel 
	 * @param predicate - the {@link Predicate}
	 * @return - a filtered {@link List} of {@link RemoteCompiledArtifactIdentification}
	 */
	private List<RemoteCompiledDependencyIdentification> filter( Predicate<RemoteCompiledDependencyIdentification> predicate) {
		Callable<List<RemoteCompiledDependencyIdentification>> result = () -> acquirePopulation().parallelStream().filter(predicate).collect( Collectors.toList());	
		try {
			return pool.submit( result).get();
		} catch (Exception e) {
			return Collections.emptyList();
		} 
		  
	}
	
	
	/**
	 * decodes a zipped file with the stored {@link RemoteCompiledArtifactIdentification}s from a earlier scan 
	 * @param file - the {@link File} that contains the data 
	 * @return - a {@link List} of {@link RemoteCompiledArtifactIdentification} or null if nothing's returnable
	 */
	@SuppressWarnings("unchecked")
	public Maybe<List<RemoteCompiledDependencyIdentification>> decode(File file) {
		if (!file.exists()) {
			ScanDataDecodingNoData sddnd = Reasons.build( ScanDataDecodingNoData.T).text( "no file [" + file.getAbsolutePath() + "] exists").toReason();			
			return Maybe.empty( sddnd);
		}
		List<RemoteCompiledDependencyIdentification> payload = null;
		long before = System.currentTimeMillis();
		try ( 
				FileInputStream in = new FileInputStream(file);
				BufferedInputStream bin = new BufferedInputStream(in);
                ZipInputStream zin = new ZipInputStream(bin);
			) {
			ZipEntry ze;
			do {
				ze = zin.getNextEntry();
				if (ze == null) {
					String msg="cannot unmarshall file [" + file.getAbsolutePath() + "] because no compressed data is found it zip"; 
					DevrockPluginStatus status = new DevrockPluginStatus(msg, IStatus.ERROR);
					DevrockPlugin.instance().log(status);	
				}
				if (ze.getName().equals( PAYLOAD)) {
					payload = (List<RemoteCompiledDependencyIdentification>) marshaller.unmarshall( zin);
					break;
				}
			} while (true);			
		}
		catch (Exception e) {
			String msg="cannot unmarshall persisted scan data from [" + file.getAbsolutePath() + "]"; 
			log.error( msg, e);
			ScanDataDecodeFailure sddf = Reasons.build( ScanDataDecodeFailure.T).text(msg).toReason();			
			return Maybe.empty( sddf);
				
		}		
		long after = System.currentTimeMillis();		
		log.debug("unmarshalling source repository data from [" + file.getAbsolutePath() + "] took [" + (after-before) + "] ms");
		return Maybe.complete( payload);
	}
	
	/**
	 * encodes a previous scan result to a zipped file 
	 * @param file - the {@link File} to store the data 
	 * @param ecais - a {@link List} of {@link RemoteCompiledArtifactIdentification}
	 */
	public Reason encode(File file, List<RemoteCompiledDependencyIdentification> ecais) {
		long before = System.currentTimeMillis();
		try (
				OutputStream out = new FileOutputStream( file);
				BufferedOutputStream bout = new BufferedOutputStream(out);
				ZipOutputStream zout = new ZipOutputStream(bout);
		) {
			zout.putNextEntry(new ZipEntry(PAYLOAD));
			marshaller.marshall( zout, ecais, GmSerializationOptions.deriveDefaults().outputPrettiness( OutputPrettiness.low).build());
			zout.closeEntry();
		}
		catch (Exception e) {
			String msg ="cannot marshall container data to [" + file.getAbsolutePath() + "]";		
			log.error( msg,e);
			ScanDataEncodeFailure sdef = Reasons.build(ScanDataEncodeFailure.T).text(msg).toReason();			
			return sdef;
		}
		long after = System.currentTimeMillis();		
		log.debug("marshalling source repository data to [" + file.getAbsolutePath() + "] took [" + (after-before) + "] ms");
		return null;
	}
	
	
	

}
