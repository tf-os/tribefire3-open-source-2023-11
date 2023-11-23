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
package com.braintribe.devrock.artifactcontainer.control.walk.wired;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerDevloaderUpdateExpert;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerUpdateRequestType;
import com.braintribe.devrock.artifactcontainer.control.walk.scope.ContainerProcessingExpectationRegistry;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.notification.ContainerProcessingNotificationBroadcaster;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.notification.ContainerProcessingNotificationListener;
import com.braintribe.devrock.artifactcontainer.control.workspace.WorkspaceProjectRegistry;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.logging.Logger;
import com.braintribe.model.malaclypse.cfg.container.ContainerGenerationMode;
import com.braintribe.plugin.commons.container.ContainerNatureExpert;

/**
 * the actual place where you can fire an update/sync/refresh on a container 
 * 
 * @author pit
 *
 */
public class WiredArtifactContainerWalkController implements ContainerProcessingNotificationListener, ContainerProcessingNotificationBroadcaster {
	private static Logger log = Logger.getLogger(WiredArtifactContainerWalkController.class);
	private BlockingQueue<WiredArtifactContainerUpdateRequest> containerQueue;
	private Set<String> queuedContainerIds = new HashSet<String>();
	private WiredThreadedArtifactContainerWalkProcessor walkProcessor;	
	private Map<String, ArtifactContainer> idToContainerMap = new HashMap<String, ArtifactContainer>();
	private ContainerProcessingExpectationRegistry expectationRegistry = new ContainerProcessingExpectationRegistry();
	private Set<ContainerProcessingNotificationListener> listeners = new HashSet<ContainerProcessingNotificationListener>();	
	private List<WiredArtifactContainerUpdateRequest> deferredRequests = new ArrayList<WiredArtifactContainerUpdateRequest>();
	
	private static WiredArtifactContainerWalkController instance;
	private boolean inhibitDirectArtifactContainerInitializing = false;
	
	public static WiredArtifactContainerWalkController getInstance() {
		if (instance == null) {
			instance = new WiredArtifactContainerWalkController();
		}
		return instance;
	}
	
	public WiredArtifactContainerWalkController() {	
		containerQueue = new LinkedBlockingQueue<WiredArtifactContainerUpdateRequest>();
		
		walkProcessor = new WiredThreadedArtifactContainerWalkProcessor();
		walkProcessor.setQueue(containerQueue);		
		walkProcessor.addListener( this);
		walkProcessor.start();
	}
	
	public void setContainerInitializingInhibited( boolean inhibited) {
		inhibitDirectArtifactContainerInitializing = inhibited;
	}
	public boolean getContainerInitializingInhibited() {
		return inhibitDirectArtifactContainerInitializing;
	}

	@Override
	public void addListener(ContainerProcessingNotificationListener listener) {
		listeners.add(listener);
	}



	@Override
	public void removeListener(ContainerProcessingNotificationListener listener) {
		listeners.remove(listener);
	}



	public void updateContainer( ArtifactContainer container, ArtifactContainerUpdateRequestType mode) {
		if (inhibitDirectArtifactContainerInitializing) 
			return;
		List<ArtifactContainer> containers = new ArrayList<ArtifactContainer>();
		containers.add(container);
		updateContainers(containers, mode);
	}
	
	public void updateContainers( ArtifactContainerUpdateRequestType mode) {
		if (inhibitDirectArtifactContainerInitializing) 
			return;
		List<ArtifactContainer> containers = new ArrayList<ArtifactContainer>();
		Collection<ArtifactContainer> currentContainers = ArtifactContainerPlugin.getArtifactContainerRegistry().getContainers();
		if (currentContainers != null) {
			containers.addAll(currentContainers);
		}
		updateContainers( containers, mode);	
	}
	
	public void updateContainers( List<ArtifactContainer> containers, ArtifactContainerUpdateRequestType mode) {
		
		synchronized (containers) {			 
			ClasspathResolverContract mcContract = MalaclypseWirings.fullClasspathResolverContract().contract(); 			
			String id = expectationRegistry.createNewProcessingScope();
			List<ArtifactContainer> artifactContainersInScope = new ArrayList<ArtifactContainer>(); 
			
			for (ArtifactContainer container : containers) {
				// check if this is a duplicate 
				synchronized (queuedContainerIds) {
					String containerId = container.getId();
					if (queuedContainerIds.contains(containerId)) {
						if (ArtifactContainerPlugin.isDebugActive()) {
							String msg = "Container with id [" + containerId + "] adhering to [" + container.getProject().getProject().getName() + "] is already being processed";
							ArtifactContainerPlugin.log(msg);
						}						
						continue;
					}
				
					//check if the container's project's open
					if (!container.getProject().getProject().isAccessible()) {
						continue;
					}				
					queuedContainerIds.add( containerId);			
					log.debug("adding processing expectation for [" + container.getProject().getProject().getName() + "]" );
					expectationRegistry.addProcessorExpectationToScope(id, container);
					idToContainerMap.put(containerId, container);
					artifactContainersInScope.add(container);
				}
			}
			// control the number of containers
			int i = 0;
			int batchSize = ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getDynamicContainerPreferences().getConcurrentWalkBatchSize();
			for (ArtifactContainer container : artifactContainersInScope) {
				WiredArtifactContainerUpdateRequest updateRequest = new WiredArtifactContainerUpdateRequest(id, container, mode, false);
				updateRequest.setContainer(container);
				updateRequest.setClasspathResolverContract(mcContract);
				if (batchSize < 0 || i++ < batchSize) {
					// only drop max numbers 
					containerQueue.offer( updateRequest);
				}
				else {
					deferredRequests.add(updateRequest);
				}
			}
			
		}						
	}
	
	public void stop() {
		walkProcessor.signalDone();
		try {
			walkProcessor.join();
		} catch (InterruptedException e) {			
		}
	}
	
	private void inject() {
		if (deferredRequests != null && deferredRequests.size() > 0) {
			WiredArtifactContainerUpdateRequest updateRequest = deferredRequests.remove(0);
			if (updateRequest != null) {
				containerQueue.offer( updateRequest);
			}
		}
	}
	

	@Override
	public void acknowledgeContainerFailed(WiredArtifactContainerUpdateRequest request) {
		for (ContainerProcessingNotificationListener listener : listeners) {
			listener.acknowledgeContainerFailed(request);
		}
		final String id = request.getRequestId();
		ArtifactContainer container = request.getContainer();
		String projectName = container.getProject().getProject().getName();
		String containerId  = container.getId();
		synchronized (queuedContainerIds) {		
			System.out.println("removing [" + container.getProject().getProject().getName() + "(" + request.getWalkMode().toString() + ")] as failed");
			queuedContainerIds.remove(containerId);
		}
		
		String msg="request [" + id + "] failed as container [" + containerId + "] of [" + projectName + "] could not be processed";		
		ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
		ArtifactContainerPlugin.getInstance().log(status);
		
		inject();
	}



	@Override
	public void acknowledgeContainerProcessed(WiredArtifactContainerUpdateRequest request) {
		for (ContainerProcessingNotificationListener listener : listeners) {
			listener.acknowledgeContainerProcessed(request);
		}
		final String id = request.getRequestId();
		String containerId  = request.getContainer().getId();
		synchronized (queuedContainerIds) {		
			System.out.println("removing [" + request.getContainer().getProject().getProject().getName() + "(" + request.getWalkMode().toString() + ")] as succeeded");
			queuedContainerIds.remove(containerId);
		}
		if (id == null) {		
			String msg = "No id passed with the request.";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
			ArtifactContainerPlugin.getInstance().log(status);				
			return;
		}
		ArtifactContainerUpdateRequestType mode = request.getWalkMode();
		// if we're done a compile walk (compile or combined) or a refresh, we might want to update Eclipse
		if (mode == ArtifactContainerUpdateRequestType.compile || mode == ArtifactContainerUpdateRequestType.combined || mode == ArtifactContainerUpdateRequestType.refresh) {
			expectationRegistry.markProcessorExpectationAsResolved(id, containerId);
			
			if (expectationRegistry.isProcessed(id)) {
				
				Job job = new Job( "Notifying eclipse about modified containers") {
					@Override
					protected IStatus run(IProgressMonitor progressMonitor) {
						if (ArtifactContainerPlugin.isDebugActive()) {
							ArtifactContainerPlugin.log("All containers of request scope [" + id + "] have been processed, notifying Eclipse");
						}
						
						// deactivate auto build
						expectationRegistry.inhibitAutoBuild(id);
						try {
							List<ArtifactContainer> containers = expectationRegistry.getContainers(id);
							// sort .. build order style .. 
							containers = sortContainersByProjectReferences( containers);
							for (ArtifactContainer container : containers) {
						
								// make sure at this point that a tomcat/module-carrier project gets its runtime container now
								IProject containerProject = container.getProject().getProject();
								if (ContainerNatureExpert.hasTomcatNature(containerProject) && !ArtifactContainerDevloaderUpdateExpert.hasDevloader(containerProject)) {
									String msg = "forcing runtime initializer on tomcat project [" + containerProject.getName() + "]";
									ArtifactContainerPlugin.log(msg);
									log.debug(msg);
									WiredArtifactContainerWalkProcessor artifactContainerWalkProcessor = new WiredArtifactContainerWalkProcessor();
									artifactContainerWalkProcessor.initializeLaunchContainer(container, MalaclypseWirings.fullClasspathResolverContract().contract(), ContainerGenerationMode.standard);								
								}
															
								
								// broadcast to eclipse that container's changed.
								ArtifactContainerPlugin.acknowledgeContainerNotification(container);
								ArtifactContainer sibling = ArtifactContainer.notifyEclipse(container);
							    try {
									JavaCore.setClasspathContainer( sibling.getPath(), new IJavaProject[] { sibling.getProject()}, new IClasspathContainer[] {new ArtifactContainer(sibling)},null);
									
									IProject projectOfContainer = sibling.getProject().getProject();
									if (projectOfContainer != null) {
										projectOfContainer.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, progressMonitor);
									}
									
									
								} catch (JavaModelException e) {
									String emsg="cannot replace current artifact container with sibling";								
									ArtifactContainerStatus status = new ArtifactContainerStatus(emsg, e);
									ArtifactContainerPlugin.getInstance().log(status);	
								} catch (CoreException e) {
									String emsg="cannot compile project";								
									ArtifactContainerStatus status = new ArtifactContainerStatus(emsg, e);
									ArtifactContainerPlugin.getInstance().log(status);
								}
							}
						}
						finally { // to make sure all releases happen here 
							//reactivate auto build 
							expectationRegistry.releaseAutoBuild(id);							
							expectationRegistry.release( id);
						}
						return Status.OK_STATUS;
					}

				};
				job.schedule();				
			}
			else {
				// still need to schedule? 
				inject();
			}
		}
		
	}
	
	
	
	
	/**
	 * sort the list of containers by letting the {@link WorkspaceProjectRegistry} build a build order
	 * @param containers - {@link List} of {@link ArtifactContainer} within the expectation
	 * @return - the sorted {@link List} of {@link ArtifactContainer} in build order
	 */
	private List<ArtifactContainer> sortContainersByProjectReferences( List<ArtifactContainer> containers) {
		// 
		Map<IProject, ArtifactContainer> containerMap = new HashMap<>();
		containers.stream().forEach( c -> {
			containerMap.put( c.getProject().getProject(), c);
		});
		
		List<IProject> projectSequence = ArtifactContainerPlugin.getWorkspaceProjectRegistry().sortProjects( containerMap.keySet());
				
		return projectSequence.stream().map( p -> containerMap.get(p)).collect( Collectors.toList());
	}
	
	
}
