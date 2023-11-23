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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;

import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.build.quickscan.notification.QuickImportScanPhaseListener;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryPairing;
import com.braintribe.model.panther.SourceArtifact;

public class QuickImportScanMonitorListener implements QuickImportScanPhaseListener, ProcessAbortSignaller{	
	private SubMonitor monitor;
	private IProgressMonitor progressMonitor;
	private ProcessAbortSignaller abortSignaller;
	private SourceRepositoryPairing pairing;
	
	@Configurable
	public void setAbortSignaller(ProcessAbortSignaller abortSignaller) {
		this.abortSignaller = abortSignaller;
	}
	
	@Configurable @Required
	public void setPairing(SourceRepositoryPairing pairing) {
		this.pairing = pairing;
	}
	
	public QuickImportScanMonitorListener(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;		
	}
	
	@Override
	public void acknowledgeDetected(String arg0) {
		if (abortScan())
			return;
		
		monitor.subTask( "source artifact [" + arg0.toString() + "] detected in repository [" + pairing.getName() + "]");
		monitor.split(1);
	}

	@Override
	public void acknowledgeEnumerationPhase() {
		
		int expectedNumberOnSources = pairing.getNumberOfSourcesFound();
		if (expectedNumberOnSources > 0) {
			monitor = SubMonitor.convert(progressMonitor, expectedNumberOnSources);
		}
		else {
			monitor = SubMonitor.convert(progressMonitor);
		}
		monitor.setTaskName("Scanning ");	
			
	}

	@Override
	public void acknowledgeResolved(SourceArtifact sourceArtifact) {	
		if (abortScan())
			return;
		
		monitor.subTask( "resolved [" + sourceArtifact.getGroupId() + ":" + sourceArtifact.getArtifactId() + "#" + sourceArtifact.getVersion() + "]");
		monitor.split(1);
	}

	@Override
	public void acknowledgeScanError(String msg, String path) {
		if (abortScan())
			return;
		ArtifactContainerStatus status = new ArtifactContainerStatus("scanner reports error : " + msg + " in ["+ path + "] in repository [" + pairing.getName() + "]", IStatus.WARNING);
		ArtifactContainerPlugin.getInstance().log(status);	
	}
	

	@Override
	public void acknowledgeUnresolved(int phases, String path) {
		if (abortScan())
			return;
		ArtifactContainerStatus status = new ArtifactContainerStatus("remaining unresolved after : " + phases + " phases : ["+ path + "] in repository [" + pairing.getName() + "]", IStatus.WARNING);
		ArtifactContainerPlugin.getInstance().log(status);	
		
	}

	@Override
	public void acknowledgeScanPhase(int numPhase, int remaining) {
		if (abortScan())
			return;
		monitor = SubMonitor.convert(progressMonitor);
	}


	@Override
	public boolean abortScan() {
		if (abortSignaller != null && abortSignaller.abortScan())
			return true;
		return progressMonitor.isCanceled();
	}

	
	
	
}
