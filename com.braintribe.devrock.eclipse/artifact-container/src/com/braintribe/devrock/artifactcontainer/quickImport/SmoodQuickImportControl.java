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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.runtime.IStatus;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.common.lcd.EmptyReadWriteLock;
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
import com.braintribe.model.processing.query.fluent.ConditionBuilder;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.JunctionBuilder;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.plugin.commons.selection.PantherSelectionHelper;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;

public class SmoodQuickImportControl implements QuickImportControl, QuickImportScanResultBroadcaster, QuickImportScanResultListener, ProcessAbortSignaller {	
	private static final String PAYLOAD = "payload.xml";
	private ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
	private QuickImportScanManager scanManager;
	private List<QuickImportScanResultListener> listeners = new ArrayList<QuickImportScanResultListener>();
	private CamelCasePatternExpander patternExpander = new CamelCasePatternExpander();
	private enum Notation { group,artifact,version, all}
	private boolean stopped = false;

	private Smood smood;
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
	
		File scanResultFile = getPersitedScanResultFile();
		if (scanResultFile.exists()) {			
			try (InputStream in = Archives.zip().from(getPersitedScanResultFile()).getEntry(PAYLOAD).getPayload()) {
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
		File scanResultFile = getPersitedScanResultFile();
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
	
	public Smood getSmood() {
		Lock lock = smoodInitializationlock.writeLock();				
		if (smood == null || reinitializeSmood) {
			try {
				lock.lock();		
				smood = new Smood( EmptyReadWriteLock.INSTANCE);
				smood.initialize( getScannedSourceArtifacts());
				reinitializeSmood = false;
			}
			finally {
				lock.unlock();
			}				
		}			
		return smood;		
	}
	
	
	@Override
	public boolean abortScan() {
		synchronized ( scanManager) {
			return stopped;
		}
	}
	
	/**
	 * build a query from the expression in the editbox 
	 * @param txt - the expression as {@link String}
	 * @param junctionBuilder - {@link JunctionBuilder} to append the query condition to. 
	 */
	private void processExpression( String txt, JunctionBuilder<EntityQueryBuilder> junctionBuilder) {
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
			
			JunctionBuilder<JunctionBuilder<EntityQueryBuilder>> builder = junctionBuilder.conjunction();
			
			switch (notation) {
				case all: { // all given 
					String grp = txt.substring(0, groupIndex);
					String vrsn = txt.substring( versionIndex + 1);
					String artf = txt.substring(groupIndex+1, versionIndex);									 
					
					
					
					setPatternExpression( builder, "groupId", grp);
					setPatternExpression( builder, "artifactId", artf);
					setPatternExpression( builder, "version", vrsn);
					
					
					break;
				}
				case group: { // group and artifact
					String grp = txt.substring(0, groupIndex);
					String artf = txt.substring(groupIndex +1);				
					
					setPatternExpression( builder, "groupId", grp);
					setPatternExpression( builder, "artifactId", artf);
									
					
					break;
				}
				case version: { // version and artifact
					String artf = txt.substring( 0, versionIndex);
					String vrsn = txt.substring( versionIndex + 1);
					
					setPatternExpression( builder, "artifactId", artf);
					setPatternExpression( builder, "version", vrsn);
					
					break;
				}
				case artifact: // artifact only 
				default:
					if (
							txt.contains( "*") == false &&
							patternExpander.isPrecise(txt) == false
						)
						txt = "*" + txt + "*";
					
					setPatternExpression(builder, "artifactId", txt);
					break;
			}
			
			builder.close();
		
	}
	
	private void setPatternExpression(JunctionBuilder<JunctionBuilder<EntityQueryBuilder>> builder, String property, String value) {
		if (patternExpander.isPrecise(value) == true) {			
			builder.property( property).eq( patternExpander.sanitize(value));
			return;
		}					
		builder.property( property).like( patternExpander.expand( patternExpander.sanitize(value)));		
	}

	/**
	 * uses the parameter to run a query against the session, and then
	 * let's the tree update itself in the UI thread 
	 * @param txt - the {@link String} with the expression 
	 */
	public List<SourceArtifact> runCoarseSourceArtifactQuery( String txt) {
		
		EntityQueryBuilder entityQueryBuilder = EntityQueryBuilder.from( SourceArtifact.class);
		ConditionBuilder<EntityQueryBuilder> conditionBuilder = entityQueryBuilder.where();
		
		JunctionBuilder<EntityQueryBuilder> junctionBuilder = conditionBuilder.disjunction();
		String [] txts = txt.split( "\\|");
		if (txts.length > 1) {		
			for (String value : txts) {
				processExpression(value, junctionBuilder);
			}			
		} else {
			processExpression(txts[0], junctionBuilder);
		}
		junctionBuilder.close();
		
		EntityQuery query = entityQueryBuilder.done();
		
		try {			
			 List<?> bland = getSmood().queryEntities(query).getEntities();
			 @SuppressWarnings("unchecked")
			List<SourceArtifact> result = (List<SourceArtifact>) bland; 						 				
			return result != null ? new ArrayList<SourceArtifact>(result) : new ArrayList<SourceArtifact>();					
		} catch (Exception e) {			
		}		
		return new ArrayList<SourceArtifact>();
	}
	

	
	/**	
	 * run a query for a fully qualified {@link SourceArtifact}
	 * @param txt
	 * @return
	 */
	public List<SourceArtifact> runSourceArtifactQuery( String txt) {
		
		EntityQueryBuilder entityQueryBuilder = EntityQueryBuilder.from( SourceArtifact.class);
		ConditionBuilder<EntityQueryBuilder> conditionBuilder = entityQueryBuilder.where();
		
		JunctionBuilder<EntityQueryBuilder> junctionBuilder = conditionBuilder.disjunction();
		String [] txts = txt.split( "\\|");
		if (txts.length > 1) {		
			for (String value : txts) {
				processSourceArtifact(value, junctionBuilder);
			}			
		} else {
			processSourceArtifact(txts[0], junctionBuilder);
		}
		junctionBuilder.close();
		
		EntityQuery query = entityQueryBuilder.done();
		
		try {			
			 List<?> bland = getSmood().queryEntities(query).getEntities();
			 @SuppressWarnings("unchecked")
			List<SourceArtifact> result = (List<SourceArtifact>) bland; 						 				
			return result != null ? new ArrayList<SourceArtifact>(result) : new ArrayList<SourceArtifact>();					
		} catch (Exception e) {
			
		}		
		return new ArrayList<SourceArtifact>();
	}
	
	/**
	 * run a query for a partial {@link SourceArtifact} - groupId, artifactId
	 * @param txt
	 * @return
	 */
	public List<SourceArtifact> runPartialSourceArtifactQuery( String txt) {
		
		EntityQueryBuilder entityQueryBuilder = EntityQueryBuilder.from( SourceArtifact.class);
		ConditionBuilder<EntityQueryBuilder> conditionBuilder = entityQueryBuilder.where();
		
		JunctionBuilder<EntityQueryBuilder> junctionBuilder = conditionBuilder.disjunction();
		String [] txts = txt.split( "\\|");
		if (txts.length > 1) {		
			for (String value : txts) {
				processPartialSourceArtifact(value, junctionBuilder);
			}			
		} else {
			processPartialSourceArtifact(txts[0], junctionBuilder);
		}
		junctionBuilder.close();
		
		EntityQuery query = entityQueryBuilder.done();
		
		try {			
			 List<?> bland = getSmood().queryEntities(query).getEntities();
			 @SuppressWarnings("unchecked")
			List<SourceArtifact> result = (List<SourceArtifact>) bland; 						 				
			return result != null ? new ArrayList<SourceArtifact>(result) : new ArrayList<SourceArtifact>();					
		} catch (Exception e) {
			
		}		
		return new ArrayList<SourceArtifact>();
	}
	
	/**
	 * local pomfile, local repository 
	 * @param pomFile
	 * @return
	 */
	public List<SourceArtifact> runPomFileToSourceArtifactQuery( File pomFile) {
		
		EntityQueryBuilder entityQueryBuilder = EntityQueryBuilder.from( SourceArtifact.class);
		String absolutePath = pomFile.getParentFile().getAbsolutePath();
		SourceRepository backingLocalSourceRepository = PantherSelectionHelper.findMatchingLocalRepresentationSourceRepositoryFromPath(absolutePath);
		if (backingLocalSourceRepository != null) {			
			int protcolLength = "file:".length();
			int cutlength = backingLocalSourceRepository.getRepoUrl().length() - protcolLength;
			absolutePath = absolutePath.substring( cutlength+1);
		}
		
		EntityQuery query = entityQueryBuilder.where().property("path").eq( absolutePath).done();
					
		try {			
			 List<?> bland = getSmood().queryEntities(query).getEntities();
			 @SuppressWarnings("unchecked")
			List<SourceArtifact> result = (List<SourceArtifact>) bland;
			 if (result != null) {
				 return new ArrayList<SourceArtifact>(result); 
			 }
			 else {
				String msg = "query for source artifact with path of [" + absolutePath + "] returned no hits";
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
				ArtifactContainerPlugin.getInstance().log(status);			
			 }
		} catch (Exception e) {
			;
		}		
		return Collections.emptyList();

	}
	
	private void processSourceArtifact( String txt, JunctionBuilder<EntityQueryBuilder> junctionBuilder) {
	
		Artifact artifact = NameParser.parseCondensedArtifactName(txt);
		JunctionBuilder<JunctionBuilder<EntityQueryBuilder>> builder = junctionBuilder.conjunction();
		builder.property( "groupId").eq( artifact.getGroupId());
		builder.property( "artifactId").eq( artifact.getArtifactId());
		builder.property( "version").eq( VersionProcessor.toString(artifact.getVersion()));
		builder.value( ProjectNature.eclipse).in().property( "natures");			
		builder.close();	
	}
	
	private void processPartialSourceArtifact( String txt, JunctionBuilder<EntityQueryBuilder> junctionBuilder) {	
		String [] parts = txt.split( ":");
		if (parts.length != 2)
			return;
		
		JunctionBuilder<JunctionBuilder<EntityQueryBuilder>> builder = junctionBuilder.conjunction();
		builder.property( "groupId").eq( parts[0]);
		builder.property( "artifactId").eq( parts[1]);
		builder.value( ProjectNature.eclipse).in().property( "natures");			
		builder.close();	
	}
	
	private File getPersitedScanResultFile() {
		String path = plugin.getStateLocation().toOSString();
		return new File( path + File.separator + ArtifactContainerPlugin.PLUGIN_ID + ".scanResult.zip");
	}
}
