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
package com.braintribe.devrock.artifactcontainer.control.container;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;

import com.braintribe.build.artifact.retrieval.multi.coding.CaseInsensitiveHashSupportWrapperCodec;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.devrock.artifactcontainer.container.diagnostics.ContainerClasspathDiagnosticsRegistry;
import com.braintribe.devrock.artifactcontainer.views.dependency.ContainerMode;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.WalkMonitoringResult;
import com.braintribe.model.malaclypse.cfg.container.ArtifactContainerConfiguration;
import com.braintribe.model.malaclypse.cfg.container.ArtifactContainerSolutionTuple;
import com.braintribe.model.malaclypse.container.ContainerPersistence;

/**
 * a registry of all {@link ArtifactContainer} in the workspaces, handles lookups and persistence 
 * @author pit
 *
 */
public class ArtifactContainerRegistry {
	private static Logger log = Logger.getLogger(ArtifactContainerRegistry.class);
	private static int POOL_SIZE = 5;
	private Map<IProject, ArtifactContainer> projectToContainerMap = new ConcurrentHashMap<>();
	private Map<ArtifactContainer, IProject> containerToProjectMap = new ConcurrentHashMap<>();
	private Map<IProject, ArtifactContainerConfiguration> projectToContainerConfigurationMap = new ConcurrentHashMap<>();
	private Map<IProject, WalkMonitoringResult> projectToCompileWalkResult = new ConcurrentHashMap<>();
	private Map<IProject, WalkMonitoringResult> projectToRuntimeWalkResult = new ConcurrentHashMap<>();
	private Map<IJavaProject, ArtifactContainerConfiguration> projectToUnassignedConfiguration = new ConcurrentHashMap<IJavaProject, ArtifactContainerConfiguration>(); 
	private ContainerClasspathDiagnosticsRegistry _containerDiagnosticsRegistry;
	
	private ArtifactContainerConfigurationPersistenceExpert persistenceExpertForConfiguration;
	private ArtifactContainerCompressedSolutionTuplePersistenceExpert _persistenceExpertForSolutionTuple;
	private ArtifactContainerCompressedWalkMonitorResultPersistenceExpert _persistenceExpertForWalkResult;
	
	private Map<String, Solution> jarToArtifactMap = CodingMap.createHashMapBased( new CaseInsensitiveHashSupportWrapperCodec());
	private boolean cacheWalkMonitoringResults = true;
	private ExecutorService executorService =  Executors.newFixedThreadPool( POOL_SIZE);
	private Map<String, Future<?>> futures = new ConcurrentHashMap<>();
	private boolean deferContainerSave = false;
	private boolean deferMonitorSave = false;
	
	
	public ArtifactContainerRegistry() {				
		persistenceExpertForConfiguration = new ArtifactContainerConfigurationPersistenceExpert();
		
		getContainerDiagnosticsRegistry();
		 
	}

	private Object registryInitializationMonitor = new Object();
	private ContainerClasspathDiagnosticsRegistry getContainerDiagnosticsRegistry() {
		if (_containerDiagnosticsRegistry != null)
			return _containerDiagnosticsRegistry;
		
		synchronized (registryInitializationMonitor) { 
			if (_containerDiagnosticsRegistry != null)
				return _containerDiagnosticsRegistry;
			
			ContainerClasspathDiagnosticsRegistry bean = new ContainerClasspathDiagnosticsRegistry();
			bean.setContainerRegistry( this);			
			_containerDiagnosticsRegistry = bean;
		}				
		return _containerDiagnosticsRegistry;
	}

	private ArtifactContainerCompressedWalkMonitorResultPersistenceExpert getPersistenceExpertForWalkResult() {
		if (_persistenceExpertForWalkResult == null) {
			_persistenceExpertForWalkResult = new ArtifactContainerCompressedWalkMonitorResultPersistenceExpert();
		}
		return _persistenceExpertForWalkResult;
	}

	private ArtifactContainerCompressedSolutionTuplePersistenceExpert getPersistenceExpertForSolutionTuple() {
		if (_persistenceExpertForSolutionTuple == null) {
			ArtifactContainerSolutionTuplePersistenceExpert delegate = new ArtifactContainerSolutionTuplePersistenceExpert();
			_persistenceExpertForSolutionTuple = new ArtifactContainerCompressedSolutionTuplePersistenceExpert();
			_persistenceExpertForSolutionTuple.setDelegate(delegate);
		}
		return _persistenceExpertForSolutionTuple;
	}
	/**
	 * returns the associated {@link ArtifactContainer} of a {@link IProject}, if any 
	 * @param project - the {@link IProject} passed
	 * @return - the associated {@link ArtifactContainer} or null
	 */
	public ArtifactContainer getContainerOfProject( IProject project) {
		return projectToContainerMap.get(project);
	}
	
	/**
	 * returns the {@link IProject} associated with the {@link ArtifactContainer}, if any 
	 * @param container - the {@link ArtifactContainer} passed
	 * @return - the associated {@link IProject} or null 
	 */
	public IProject getProjectOfContainer( ArtifactContainer container) {
		return containerToProjectMap.get(container);
	}
	
	/**
	 * adds a {@link ArtifactContainer} to a {@link IProject} and initializes the {@link ArtifactContainerConfiguration}
	 * @param project - the {@link IProject}
	 * @param container - the {@link ArtifactContainer}
	 */
	public void addContainer( IProject project, ArtifactContainer container) {
		projectToContainerMap.put( project, container);
		containerToProjectMap.put(container, project);
		container.setConfiguration( projectToContainerConfigurationMap.get( project));
		
//		if (!deferContainerSave) {
//			persistContainerData(container);
//		}
	}
	
	/**
	 * removes a {@link ArtifactContainer} from the registry via the {@link IProject} 
	 * @param project - the {@link IProject}
	 */
	public void removeContainer( IProject project) {
		ArtifactContainer container = projectToContainerMap.get(project);
		if (container == null) {
			return;
		}
		projectToContainerMap.remove(project);
		containerToProjectMap.remove(container);
		projectToContainerConfigurationMap.remove(project);		
	}
	
	/**
	 * removes a {@link ArtifactContainer} from the registry
	 * @param container - the passed {@link ArtifactContainer}
	 */
	public void removeContainer( ArtifactContainer container) {
		IProject project = containerToProjectMap.get(container);
		if (project == null) {
			return;
		}
		projectToContainerMap.remove(project);
		containerToProjectMap.remove(container);
		projectToContainerConfigurationMap.remove(project);		
	}
	
	/**
	 * reassigns a new {@link ArtifactContainer} to a {@link IProject}
	 * @param project - the {@link IProject} to associate with 
	 * @param container - the {@link ArtifactContainer}
	 */
	public void reassignContainerToProject( IProject project, ArtifactContainer container) {
		ArtifactContainer obsoleteContainer = projectToContainerMap.get(project);
		ArtifactContainerConfiguration configuration;
		if (obsoleteContainer != null) {
			containerToProjectMap.remove(obsoleteContainer);
			configuration = obsoleteContainer.getConfiguration();
		}
		else {
			configuration = ArtifactContainerConfigurationPersistenceExpert.generateDefaultContainerConfiguration();
		}
		containerToProjectMap.put(container, project);
		projectToContainerMap.put(project, container);
		container.setConfiguration(configuration);		
//		if (!deferContainerSave) {
//			persistContainerData(container);
//		}
//		System.out.println("REASSIGN");
	}

	/**
	 * finds and loads all persisted {@link ArtifactContainerConfiguration}
	 * @param iProjects - an {@link Array} of {@link IProject}
	 */
	public void initializeContainerConfiguration( IProject ... iProjects) {
		for (IProject project : iProjects) {
			ArtifactContainerConfiguration containerConfiguration = persistenceExpertForConfiguration.getConfiguration(project);
			if (containerConfiguration != null) {
				projectToContainerConfigurationMap.put(project, containerConfiguration);
			}
		}
	}
	
	/**
	 * load a persisted configuration of a container 
	 * @param project - the project the container's assigned to 
	 * @return - the {@link ArtifactContainerConfiguration}
	 */
	public ArtifactContainerConfiguration getPersistedConfigurationOfProject( IProject project) {
		return persistenceExpertForConfiguration.getConfiguration(project);
	}
	
	/**
	 * persists all {@link ArtifactContainerConfiguration} of the currently know {@link ArtifactContainer}
	 */
	public void persistContainerConfiguration() {
		for (Entry<ArtifactContainer, IProject> entry : containerToProjectMap.entrySet()) {
			ArtifactContainerConfiguration configuration = entry.getKey().getConfiguration();
			if (configuration.getModified()) {
				IProject project = entry.getValue();
				if (project.isAccessible()) {
					persistenceExpertForConfiguration.saveConfiguration(project, configuration);
				}
			}
		}
	}
	
	public void persistContainerData( ArtifactContainer container) {
		ContainerPersistence tuple = container.getContainerPersistence();
		String key = UUID.randomUUID().toString();

		Future<?> future = executorService.submit( () -> {
			IProject prj = container.getProject().getProject();
			getPersistenceExpertForSolutionTuple().encode( prj, tuple);
			futures.remove(key);
		});
		 
		futures.put( key, future);					
	}
	
	/**
	 * persists the {@link ArtifactContainerSolutionTuple} of all {@link ArtifactContainer} in the registry 
	 */
	public void persistContainerData() {		 		

		Set<Entry<ArtifactContainer,IProject>> entrySet = containerToProjectMap.entrySet();
		ExecutorService es =  Executors.newFixedThreadPool( Math.max( entrySet.size(), POOL_SIZE));
		List<Future<?>> futures = new ArrayList<>();
		
		log.debug( "start persisting container data: found [" + entrySet.size() + "] containers to persist");
		
		long start = System.currentTimeMillis();
		
		for (Map.Entry<ArtifactContainer, IProject> e : entrySet) {
			Future<?> future = es.submit( () -> {
				IProject prj = null;
				try {
					ArtifactContainer container = e.getKey();
					prj = container.getProject().getProject();
					ContainerPersistence tuple = container.getContainerPersistence();
					getPersistenceExpertForSolutionTuple().encode( e.getValue(), tuple);
				} catch (Throwable e1) {				
					log.error( "cannot encode container data of [" + prj.getName() + "]", e1);
				}	
			});
			futures.add(future);
		}
		
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (Throwable e1) {				
				;
			} 
		}
		long end = System.currentTimeMillis();
		log.debug( "done persisting [" + entrySet.size() + "] containers in [" + (end - start) + " ms]");
		
		log.debug( "shutting down thread pool");
		es.shutdown();
		try {
			es.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "exception while waiting for thread pool shutdown", e);					
			ArtifactContainerPlugin.getInstance().log(status);
		}
		log.debug( "done persisting container data");
	}
	
	public void synchronuouslyPersistContainerData(IProject project, ArtifactContainer container) {
		ContainerPersistence tuple = container.getContainerPersistence();
		getPersistenceExpertForSolutionTuple().encode( project, tuple);
	}
	
	
	/**
	 * loads the persisted {@link ArtifactContainerSolutionTuple} of the {@link ArtifactContainer} attached to the {@link IProject}
	 * @param project - the {@link IProject} which is associated with the container 
	 * @return - {@link ArtifactContainerSolutionTuple} (or null if not found) 
	 */
	public ContainerPersistence loadPersistedContainerData( IProject project) {
		return getPersistenceExpertForSolutionTuple().decode(project);
	}
	
	/**
	 * returns all currently known {@link ArtifactContainer}
	 * @return - a {@link Collection} of {@link ArtifactContainer}
	 */
	public Collection<ArtifactContainer> getContainers() {
		return projectToContainerMap.values();
	}

	public ContainerClasspathDiagnosticsRegistry getContainerClasspathDiagnosticsRegistry() {
		return getContainerDiagnosticsRegistry();
	}
	
	public void setPreConfiguredConfigurationOfProject( IJavaProject javaProject, ArtifactContainerConfiguration configuration) {
		projectToUnassignedConfiguration.put( javaProject, configuration);
	}
	
	public ArtifactContainerConfiguration getPreconfiguredConfiguration( IJavaProject javaProject){
		return projectToUnassignedConfiguration.get(javaProject);
	}
	
	public void relateJarToArtifact( String jar, Solution artifact) {
		jarToArtifactMap.put(jar, artifact);
		
	}
	
	public Solution getArtifactRelatedToJar( String jar) {
		return jarToArtifactMap.get(jar);
	}
		

	//
	// walk monitoring result management 
	// 
	private WalkMonitoringResult getWalkResult( IProject project, ContainerMode mode){
		WalkMonitoringResult result;
		switch (mode) {
		case runtime:
			result = projectToRuntimeWalkResult.get(project);
			if (result == null) {
				result = getPersistenceExpertForWalkResult().decode(project, mode);
				if (result != null && cacheWalkMonitoringResults) {
					projectToRuntimeWalkResult.put(project, result);
				}
			}
			return result;
		default:
		case compile:
			result = projectToCompileWalkResult.get(project);
			if (result == null) {
				result = getPersistenceExpertForWalkResult().decode(project, mode);
				if (result != null && cacheWalkMonitoringResults) {
					projectToCompileWalkResult.put(project, result);
				}
			}
			return result;
		}
	}
	
	/**
	 * returns the current walk monitor information for compile mode
	 * @param project - the {@link IProject}
	 * @return - the {@link WalkMonitoringResult}
	 */
	public WalkMonitoringResult getCompileWalkResult( IProject project) {
		return getWalkResult(project, ContainerMode.compile);
	}
	
	/**
	 * returns the current walk monitor for runtime mode 
	 * @param project - the {@link IProject}
	 * @return - the {@link WalkMonitoringResult}
	 */
	public WalkMonitoringResult getRuntimeWalkResult( IProject project) {
		return getWalkResult(project, ContainerMode.runtime);
	}
	
	private void updateWalkMonitorResult( IProject project, WalkMonitoringResult result, ContainerMode mode) {
		switch( mode) {
			case runtime:
				projectToRuntimeWalkResult.put( project, result);
				break;
			default:
			case compile:
				projectToCompileWalkResult.put(project, result);
				break;		
		}
		if (!deferMonitorSave) {
			persistWalkMonitorResult(project, result, mode);
		}
	}
	/**
	 * update the compile walk monitor data in the registry  
	 * @param project - the {@link IProject}
	 * @param result - the {@link WalkMonitoringResult}
	 */
	public void updateCompileWalkMonitorResult( IProject project, WalkMonitoringResult result) {
		updateWalkMonitorResult(project, result, ContainerMode.compile);
	}
	/**
	 * update the runtime walk monitor data in the registry 
	 * @param project - the {@link IProject}
	 * @param result - the associated {@link WalkMonitoringResult}
	 */
	public void updateRuntimeWalkMonitorResult( IProject project, WalkMonitoringResult result) {
		updateWalkMonitorResult(project, result, ContainerMode.runtime);
	}
	 
	/**
	 * persist the current walk monitor data 
	 */
	public void persistWalkMonitorResults() {
		
		Set<Entry<IProject, WalkMonitoringResult>> compileEntrySet = projectToCompileWalkResult.entrySet();
		Set<Entry<IProject, WalkMonitoringResult>> runtimeEntrySet = projectToRuntimeWalkResult.entrySet();
		int numC = compileEntrySet.size();
		int numR = runtimeEntrySet.size();
		
		log.debug( "start persisting monitor data: found [" + numC + "] compile & [" + numR + "] runtime monitor data to persist");
		
		ExecutorService es =  Executors.newFixedThreadPool( Math.max( Math.max( numC, numR), POOL_SIZE));
		List<Future<?>> futures = new ArrayList<>();
		
		long start = System.currentTimeMillis();
		
		for (Map.Entry<IProject, WalkMonitoringResult> e : compileEntrySet) {
			futures.add( es.submit( () -> {
				try {
					WalkMonitoringResult result = e.getValue();
					if (result != null){
						getPersistenceExpertForWalkResult().encode(e.getKey(), result, ContainerMode.compile);
					}
				} catch (Throwable e1) {
					String tag = buildMessage(e);					
					log.error("error during parallel dump of compile monitor data for [" + tag + "]", e1);
				}				
			}));
		}
		
		for (Map.Entry<IProject, WalkMonitoringResult> e : runtimeEntrySet) {
			futures.add( es.submit( () -> {
				try {
					WalkMonitoringResult result = e.getValue();
					if (result != null){
						getPersistenceExpertForWalkResult().encode(e.getKey(), result, ContainerMode.runtime);
					}
				} catch (Throwable e1) {
					String tag = buildMessage(e);					
					log.error("error during parallel dump of compile monitor data for [" + tag + "]", e1);
				}				
			}));
		}
		
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (Throwable e1) {				
				;
			} 
		}
		
		long end = System.currentTimeMillis();
		log.debug( "done persisting [" + (numC + numR) + "] containers in [" + (end - start) + " ms]");		
		
		log.debug( "shutting down thread pool");
		
		es.shutdown();
		try {
			es.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "exception while waiting for thread pool shutdown", e);					
			ArtifactContainerPlugin.getInstance().log(status);
		}		
		
		log.debug( "done persisting container data");
		
	}

	private String buildMessage(Entry<IProject, WalkMonitoringResult> e) {
		String name = "null-entry";
		if (e != null) {
			IProject prj = e.getKey();
			if (prj != null) {
				name = prj.getName();
			}
			else {
				name = "null-project";
			}
		}
		return name;
	}
	
	public void persistWalkMonitorResult( IProject project, WalkMonitoringResult result, ContainerMode containerMode) {
		String key = UUID.randomUUID().toString();
		Future<?> future = executorService.submit( () -> {
			getPersistenceExpertForWalkResult().encode(project, result, containerMode);
			futures.remove(key);
		});
				
		futures.put(key, future);
				
	}

	public void shutdown() {
		if (deferContainerSave) {
			// save the containers' solutions 
			persistContainerData();
		}
		if (deferMonitorSave) {
			// save the container's walk results 
			persistWalkMonitorResults();
		}


		
		long before = System.currentTimeMillis();
		log.debug("ensuring all data has been persisted");
		synchronized (futures) {
			for (Future<?> future : futures.values()) {
				try {
					future.get();
				} catch (Exception e) {
					ArtifactContainerStatus status = new ArtifactContainerStatus( "exception while making sure all container data is persisted", e);					
					ArtifactContainerPlugin.getInstance().log(status);
				} 
			}
		}
		long afterFutureGet = System.currentTimeMillis();
		
		log.debug("ensuring all container registry has been persisted took [" + (afterFutureGet - before) + "] ms");
		
		
		executorService.shutdown();
		try {
			executorService.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "exception while waiting for thread pool shutdown", e);					
			ArtifactContainerPlugin.getInstance().log(status);
		}		
		
		long after = System.currentTimeMillis();		
		log.debug( "shutting down thread pool took [" + (after - afterFutureGet) + "] ms");
	}
	
}
