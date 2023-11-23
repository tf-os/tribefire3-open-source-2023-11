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
package com.braintribe.devrock.artifactcontainer.control.walk.wired.processing;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.devrock.artifactcontainer.control.container.ArtifactContainerRegistry;
import com.braintribe.devrock.artifactcontainer.control.monitor.WalkProgressMonitor;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerUpdateRequestType;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerUpdateRequest;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerWalkProcessor;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredThreadedArtifactContainerWalkProcessor;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.notification.ContainerProcessingNotificationListener;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.listener.MalaclypseAnalysisMonitor;


public class JobBasedUpdateRequestProcessor implements UpdateRequestProcessor {
	
	private Set<ContainerProcessingNotificationListener> listeners = new HashSet<ContainerProcessingNotificationListener>();
	private ArtifactContainerRegistry artifactContainerRegistry;

	@Override
	public void addListener(ContainerProcessingNotificationListener listener) {
		listeners.add(listener);		
	}

	@Override
	public void removeListener(ContainerProcessingNotificationListener listener) {
		listeners.remove(listener);	
	}

	@Override
	public void setArtifactContainerRegistry(ArtifactContainerRegistry registry) {
		this.artifactContainerRegistry = registry;				
	}

	@Override
	public void processRequest(final WiredArtifactContainerUpdateRequest updateRequest, final ArtifactContainer container, ArtifactContainerUpdateRequestType walkMode) {
		Job job = null;
		switch (walkMode) {
			case combined:
				WiredArtifactContainerUpdateRequest launchRequest = new WiredArtifactContainerUpdateRequest(updateRequest);
				launchRequest.setWalkMode( ArtifactContainerUpdateRequestType.launch);
				Job chainedLaunchedJob = generateJob("Sync launch:[" + container.getProject().getProject().getName() + "]", launchRequest, false, null);
				
				WiredArtifactContainerUpdateRequest compileRequest = new WiredArtifactContainerUpdateRequest(updateRequest);
				compileRequest.setWalkMode( ArtifactContainerUpdateRequestType.compile);
				job = generateJob("Sync compile:[" + container.getProject().getProject().getName() + "]", compileRequest, false, chainedLaunchedJob);				
				break;
			case compile:
				job = generateJob("Sync compile:[" + container.getProject().getProject().getName() + "]", updateRequest, false, null);						
				break;
			case launch:
				job = generateJob("Sync launch:[" + container.getProject().getProject().getName() + "]", updateRequest, false, null);
				break;
			case refresh:
				job = generateJob("Update:[" + container.getProject().getProject().getName() + "]", updateRequest, false, null);
				break;
			default:
				break;				 
		}						
		if (job != null) {
			try {
				job.schedule();
			} catch (Throwable t) {
				t.printStackTrace();
			}					
		}
	}
	
	private Job generateJob(String title, final WiredArtifactContainerUpdateRequest updateRequest, final boolean updateEclipse, final Job chainedJob) {
		final ArtifactContainer container = updateRequest.getContainer();
		Job job = new Job( title) {				
			@Override
			protected IStatus run(IProgressMonitor progressMonitor) {				
				try {
					// see if we have a runtime scope passed (multi project) or if we need to create one.
					ClasspathResolverContract contract = updateRequest.getClasspathResolverContract();
					
					
					MalaclypseAnalysisMonitor analysisMonitor = updateRequest.getAnalysisMonitor();
					if (analysisMonitor == null) {
						updateRequest.setAnalysisMonitor( new MalaclypseAnalysisMonitor("unidentified - created from [" + WiredThreadedArtifactContainerWalkProcessor.class.getName() + "]"));
					} 
					
					WiredArtifactContainerWalkProcessor artifactContainerWalkProcessor = new WiredArtifactContainerWalkProcessor( new WalkProgressMonitor(progressMonitor));
					artifactContainerWalkProcessor.setMalaclypseAnalysisMonitor(analysisMonitor);
					artifactContainerWalkProcessor.setClasspathResolverContract(contract);
					artifactContainerWalkProcessor.setContainerClasspathDiagnosticsListener( artifactContainerRegistry.getContainerClasspathDiagnosticsRegistry());
					artifactContainerWalkProcessor.processContainer( updateRequest);	
					ArtifactContainerPlugin.acknowledgeContainerProcessed(container, updateRequest.getWalkMode());
					// broadcast to listeners 
					for (ContainerProcessingNotificationListener listener : listeners) {
						listener.acknowledgeContainerProcessed( updateRequest);
					}
					// broadcast to eclipse
					if (updateEclipse) {
						System.out.println("TACWP: updating container [" + container.getId() + "]");
						
						ArtifactContainer.notifyEclipse(container);
					}
					// start chained job - mostly launch after sync
					if (chainedJob != null) {
						chainedJob.schedule(100);
					}
					return Status.OK_STATUS;
				} catch (Throwable t) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					t.printStackTrace(pw);					
					ArtifactContainerPlugin.acknowledgeContainerFailed(container, updateRequest.getWalkMode(), sw.toString());
					for (ContainerProcessingNotificationListener listener : listeners) {
						listener.acknowledgeContainerFailed( updateRequest);
					}
					t.printStackTrace();
					return Status.CANCEL_STATUS;
				}
			}					
		};
		return job;
	}

}
