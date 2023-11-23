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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.build.quickscan.QuickImportScanner;
import com.braintribe.build.quickscan.standard.StandardQuickImportScanner;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.project.validator.ProjectSettingsValidator;
import com.braintribe.devrock.artifactcontainer.quickImport.notification.QuickImportScanResultBroadcaster;
import com.braintribe.devrock.artifactcontainer.quickImport.notification.QuickImportScanResultListener;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryPairing;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.plugin.commons.preferences.validator.ValidationResult;

/**
 * a runner for the quick import scanner 
 * 
 * @author pit
 *
 */
public class QuickImportScanManager implements QuickImportScanResultBroadcaster, QuickImportScanResultListener, ProcessAbortSignaller {

	private boolean active;
	private Map<SourceRepositoryPairing, List<SourceArtifact>> sourceArtifacts = CodingMap.createHashMapBased( new SourceRepositoryPairingWrapperCodec());
	private List<QuickImportScanResultListener> quickImportScanResultListeners;
	private ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
	private ProcessAbortSignaller abortSignaller;
	private boolean primed = false;
	
	@Configurable
	public void setAbortSignaller(ProcessAbortSignaller abortSignaller) {
		this.abortSignaller = abortSignaller;
	}

	public Map<SourceRepositoryPairing, List<SourceArtifact>> getSourceArtifacts() {
		return sourceArtifacts;
	}
	
	
	public boolean isActive() {
		return active;
	}
	
	public boolean isPrimed(){
		return primed;
	}
	

	
	@Override
	public void addQuickImportScanResultListener( QuickImportScanResultListener listener) {
		if (quickImportScanResultListeners == null)
			quickImportScanResultListeners = new ArrayList<QuickImportScanResultListener>();
		quickImportScanResultListeners.add( listener);
		
	}

	@Override
	public void removeQuickImportScanResultListener( QuickImportScanResultListener listener) {
		if (quickImportScanResultListeners == null || quickImportScanResultListeners.size() == 0)
			return;
		quickImportScanResultListeners.remove(listener);		
	}
	
	

	@Override
	public void acknowledgeScanResult(SourceRepositoryPairing pairing, List<SourceArtifact> result) {
		pairing.setNumberOfSourcesFound( result.size());
		sourceArtifacts.put(pairing, result);
		
		if (quickImportScanResultListeners == null || quickImportScanResultListeners.size() == 0)
			return;
		
		for (QuickImportScanResultListener listener : quickImportScanResultListeners) {
			listener.acknowledgeScanResult(pairing, result);
		}
		
	}
	


	/**
	 * scans the drive for local .project files - single threaded, modal. 
	 * @return - the {@link List} of {@link SourceArtifact} as a result of the scan. 
	 */
	public Map<SourceRepositoryPairing, List<SourceArtifact>> run() {
		QuickImportScanner scanner = new StandardQuickImportScanner();
		active = true;		
		Map<SourceRepositoryPairing, List<SourceArtifact>> sourceArtifacts = new HashMap<SourceRepositoryPairing, List<SourceArtifact>>();
		List<SourceRepositoryPairing> sourceRepositoriesToScan = plugin.getArtifactContainerPreferences(false).getSvnPreferences().getSourceRepositoryPairings(); 
		if (sourceRepositoriesToScan.size() == 0)
			return sourceArtifacts;
		
		for (SourceRepositoryPairing sourceRepositoryPairing : sourceRepositoriesToScan) {
			try {
				SourceRepository sourceRepository = sourceRepositoryPairing.getLocalRepresentation();
				scanner.setSourceRepository( sourceRepository);
				URL url = new URL( sourceRepository.getRepoUrl());								
				List<SourceArtifact> result = scanner.scanLocalWorkingCopy( url.getFile());
				// only store if scan wasn't aborted 
				if (!abortScan()) {
					acknowledgeScanResult( sourceRepositoryPairing, result);
					sourceArtifacts.put( sourceRepositoryPairing, result);
				}
				else {
					// abort is signaled, so don't scan anything else 
					break;
				}
			} catch (Exception e) {
				String msg="cannot scan source repository [" + sourceRepositoryPairing.getName() + "]'s working copy ";
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
				plugin.log(status);	
			}
		}
		
		active = false;		
		return sourceArtifacts;
	}
	
	
	
	public void scanAllSourceRepositoryAsJob() {
		ProjectSettingsValidator validator = new ProjectSettingsValidator();
		ValidationResult result = validator.validate();
		if (!result.getValidationState()) {
			String msg="Validator says that the settings are incorrect. Scan is inhibited";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
			plugin.log(status);	
			return;
		}
		
		MultipleRepositoryQuickImportScanner runner = new MultipleRepositoryQuickImportScanner();
		runner.setResultListener(this);
		
		Job job = Job.create( "Running scan", runner::run);
		job.schedule();
		
	}
	

	
	
	/**
	 * scans the the drive for local .project files - concurrent run, no return value<br/>
	 * only sends a notification that it's done. 
	 */
	public void scanSingleSourceRepositoryAsJob(SourceRepositoryPairing sourceRepositoryPairing) {
		SingleRepositoryQuickImportScanRunner runner = new SingleRepositoryQuickImportScanRunner();
		runner.setAbortSignaller(abortSignaller);
		runner.setSourceRepositoryPairing(sourceRepositoryPairing);
		runner.setResultListener(this);
	
		Job job = Job.create( "Running scan", runner::run);	
		job.schedule();
	}
	
	

	

	@Override
	public boolean abortScan() {	
		if (abortSignaller != null)
			return abortSignaller.abortScan();
		else
			return false;
	}
	
	public void primeWith(Map<SourceRepositoryPairing, List<SourceArtifact>> storedResult) {
		sourceArtifacts.putAll(storedResult);
		primed = true;
	}
	
	

}
