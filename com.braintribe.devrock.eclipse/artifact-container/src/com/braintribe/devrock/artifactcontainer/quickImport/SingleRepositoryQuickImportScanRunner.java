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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.build.quickscan.agnostic.LocationAgnosticQuickImportScanner;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.quickImport.notification.QuickImportScanResultListener;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryPairing;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;

public class SingleRepositoryQuickImportScanRunner {
	private SourceRepositoryPairing sourceRepositoryPairing;
	private QuickImportScanResultListener resultListener;
	private ProcessAbortSignaller abortSignaller;
	boolean active;
	
	public void setSourceRepositoryPairing(SourceRepositoryPairing sourceRepositoryPairing) {
		this.sourceRepositoryPairing = sourceRepositoryPairing;
	}
	
	public void setResultListener(QuickImportScanResultListener resultListener) {
		this.resultListener = resultListener;
	}
	public void setAbortSignaller(ProcessAbortSignaller abortSignaller) {
		this.abortSignaller = abortSignaller;
	}



	public IStatus run(IProgressMonitor monitor) {					
		QuickImportScanMonitorListener monitorListener = new QuickImportScanMonitorListener(monitor);
		LocationAgnosticQuickImportScanner scanner = new LocationAgnosticQuickImportScanner();
		scanner.addListener(monitorListener);
		scanner.setScanAbortSignaller(abortSignaller);
		try {
			monitorListener.setPairing(sourceRepositoryPairing);
			SourceRepository sourceRepository = sourceRepositoryPairing.getLocalRepresentation();
			scanner.setSourceRepository( sourceRepository);
			
			URL url = new URL( sourceRepository.getRepoUrl());			
			active = true;
			
			List<SourceArtifact> result = scanner.scanLocalWorkingCopy( url.getFile());
			resultListener.acknowledgeScanResult( sourceRepositoryPairing, result);
		} catch (Exception e) {
			String msg="cannot scan source repository [" + sourceRepositoryPairing.getName() + "]'s working copy";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			ArtifactContainerPlugin.getInstance().log(status);
			return status;	
		}							
		active = false;
		monitor.done();
		return Status.OK_STATUS;
	}
}
