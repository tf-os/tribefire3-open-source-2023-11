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
package com.braintribe.devrock.plugin;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.api.logging.LoggingCommons;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.listeners.PreferencesChangeBroadcaster;
import com.braintribe.devrock.api.ui.listeners.PreferencesChangeListener;
import com.braintribe.devrock.api.ve.listeners.VirtualEnvironmentNotificationListener;
import com.braintribe.devrock.bridge.eclipse.ScopingMcBridge;
import com.braintribe.devrock.bridge.eclipse.api.EnvironmentBridge;
import com.braintribe.devrock.bridge.eclipse.api.McBridge;
import com.braintribe.devrock.bridge.eclipse.environment.BasicStorageLocker;
import com.braintribe.devrock.bridge.eclipse.environment.InternalEnvironmentBridge;
import com.braintribe.devrock.bridge.eclipse.internal.InternalMcBridge;
import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectView;
import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectViewSupplier;
import com.braintribe.devrock.eclipse.model.identification.RemoteCompiledDependencyIdentification;
import com.braintribe.devrock.eclipse.model.scan.SourceRepositoryEntry;
import com.braintribe.devrock.importer.dependencies.listener.ParallelRepositoryScanner;
import com.braintribe.devrock.importer.scanner.ParallelQuickImportControl;
import com.braintribe.devrock.importer.scanner.QuickImportControl;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.api.event.EntityEventListener;
import com.braintribe.devrock.mc.api.event.EventBroadcaster;
import com.braintribe.devrock.mc.api.event.EventEmitter;
import com.braintribe.devrock.mc.api.event.EventHub;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.plugin.listener.ResourceChangeDocumentingListener;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.ve.api.VirtualEnvironment;

/**
 * the main devrock plugin, sporting its own features, but also acting as common supplier
 * for basic functionality to the various plugins
 *  
 * @author pit
 *
 */
public class DevrockPlugin extends AbstractUIPlugin implements McBridge, 
																EnvironmentBridge,
																VirtualEnvironmentNotificationListener,
																EventEmitter,  
																EventBroadcaster,
																PreferencesChangeBroadcaster {
	private static Logger log = Logger.getLogger(DevrockPlugin.class);

	public static final String PLUGIN_ID = "com.braintribe.devrock.DevrockPlugin"; //$NON-NLS-1$
	public static final String PLUGIN_RESOURCE_PREFIX = "platform:/plugin/" + PLUGIN_ID;
	
	
	private static DevrockPlugin instance;
	
	private final EventHub eventHub = new EventHub();
	private McBridge mcBridge;
	private final InternalEnvironmentBridge envBridge = new InternalEnvironmentBridge();	

	private final ParallelQuickImportControl quickImportControl = new ParallelQuickImportControl();
	private final ParallelRepositoryScanner repoImportControl = new ParallelRepositoryScanner();
	
	private WorkspaceProjectViewSupplier projectView = new WorkspaceProjectViewSupplier();

	private ResourceChangeDocumentingListener resourceChangeListener;
	
	// this is the default for event logging. If not set in the storage locker, this is value is used. 
	private final boolean eventLoggingDefault = true;
	
	private UiSupport uiSupport = new UiSupport();
	
	private List<PreferencesChangeListener> preferencesChangeListeners = new ArrayList<>();
	
	public static DevrockPlugin instance() {
		return instance;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {	
		super.start(context);
		instance = this;
		
		LoggingCommons.initializeWithFallback( PLUGIN_ID);
		
		long startTime = System.nanoTime();		
		log.info("DevrockPlugin: starting : " + new Date());
		
		//
		// direct initialization tasks here
		//
		
		//activate scoping bridge 
		mcBridge = new ScopingMcBridge( InternalMcBridge::new);
		
		
		//
		//  deferred initialization tasks here
		//
		quickImportControl.scheduleRescan();
		repoImportControl.scheduleRescan();
		
		
		// attach listener to workspace
		if (isDebugEventLoggingActive()) {
			resourceChangeListener = new ResourceChangeDocumentingListener();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener, 
					IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | //IResourceChangeEvent.PRE_REFRESH |
					IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.POST_BUILD | IResourceChangeEvent.PRE_BUILD );
		}
				
		long endTime = System.nanoTime();
		String msg = "DevrockPlugin : started after " + (endTime - startTime) / 1E6 + " ms";
		
		log.info(msg);


	}

	
	


	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		long startTime = System.nanoTime();		
		log.info("DevrockPlugin: stopping : " + new Date());
		
		if (resourceChangeListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		}
		
		// broad cast preferences listener 
		for (PreferencesChangeListener listener : preferencesChangeListeners) {
			listener.acknowledgeBroadcasterShutdown();
		}
		
		// de-initialization tasks here
		quickImportControl.stop();
		repoImportControl.stop();

		storageLocker().save();

		// calls context.close in bridge.. is that graceful? should we call it here now?
		close();
		
		uiSupport.dispose();
		
		
		long endTime = System.nanoTime();
		String msg = "DevrockPlugin : stopped after " + (endTime - startTime) / 1E6 + " ms";
		log.info(msg);
		
	}

	/**
	 * @param status - the {@link IStatus} to log
	 */
	public void log( IStatus status) {
		getLog().log( status);
		
		// redirect to logs 
		switch (status.getSeverity()) {
			case 0 : // OK
				log.trace(status.getMessage());
				break;
			case 1 : // INFO
				log.info(status.getMessage());
				break;
			case 2 : // WARN
				log.warn(status.getMessage());
				break;
			case 4: // ERROR
				log.error(status.getMessage(), status.getException());
				break;
			case 8:	// CANCEL ?? 	
				log.warn(status.getMessage());
				break;							
		}
	}
	
	/**
	 * @return - the singleton handling the API of {@link McBridge} (the plugin redirecting to delegate)
	 */
	public static McBridge mcBridge() {
		return instance;
	}
	
	/**
	 * @return - the singleton handling the API of {@link EnvironmentBridge} (the plugin redirecting to delegate)
	 */
	public static EnvironmentBridge envBridge() {
		return instance;
	}
	
	/**
	 * @return - the singleton handling the scanning of source repositories
	 */
	public QuickImportControl quickImportController() {
		return quickImportControl;
	}
	
	/**
	 * @return - the singleton handling the scanning if remote repositories
	 */
	public ParallelRepositoryScanner repositoryImportController() {
		return repoImportControl;
	}
	
	/**
	 * @return - the {@link WorkspaceProjectView} singleton
	 */
	public WorkspaceProjectView getWorkspaceProjectView() {	
		return projectView.get();
	}
	
	/**
	 * @return - true if the workspace is 'dirty' i.e. has changed after last check
	 * on being dirty.
	 */
	public boolean isWorkspaceDirty() {
		return projectView.dirtied();
	}
	
	/*
	 * 
	 * delegations - McBridge
	 * 
	 */

	@Override	
	public Maybe<AnalysisArtifactResolution> resolveClasspath(CompiledTerminal ct, ClasspathResolutionScope resolutionScope) {		
		return mcBridge.resolveClasspath(ct, resolutionScope);
	}
	
	@Override
	public McBridge customBridge(RepositoryConfiguration repositoryConfiguration) {
		return mcBridge.customBridge(repositoryConfiguration);		
	}

	@Override	
	public Maybe<AnalysisArtifactResolution> resolveClasspath(Collection<CompiledTerminal> cts, ClasspathResolutionScope resolutionScope) {		
		return mcBridge.resolveClasspath(cts, resolutionScope);
	}

	@Override
	public Maybe<CompiledArtifact> readPomFile(File pomFile) {
		return mcBridge.readPomFile(pomFile);
	}
	
	@Override
	public Maybe<CompiledArtifact> resolve(CompiledArtifactIdentification cai) {				
		return mcBridge.resolve(cai);
	}

	@Override
	public Maybe<CompiledArtifactIdentification> resolve(CompiledDependencyIdentification cdi) {
		return mcBridge.resolve( cdi);
	}

	@Override
	public Maybe<File> resolve(CompiledPartIdentification cpi) {
		return mcBridge.resolve(cpi);
	}

	
	@Override
	public Maybe<RepositoryReflection> reflectRepositoryConfiguration() {	
		return mcBridge.reflectRepositoryConfiguration();
	}
	
	@Override
	public List<CompiledArtifactIdentification> matchesFor(CompiledDependencyIdentification cdi) {
		return mcBridge.matchesFor(cdi);
	}
	
	@Override
	public List<RemoteCompiledDependencyIdentification> retrieveCurrentRemoteArtifactPopulation() {	
		return mcBridge.retrieveCurrentRemoteArtifactPopulation();
	}
		
	@Override
	public List<RemoteCompiledDependencyIdentification> retrieveCurrentLocalArtifactPopulation() {	
		return mcBridge.retrieveCurrentLocalArtifactPopulation();
	}

	@Override
	public void close() {
		if (mcBridge != null)
			mcBridge.close();
	}

	/*
	 * 
	 * delegates InternalEnvironmentBridge
	 * 
	 */
	
	@Override
	public File workspaceSpecificStorageLocation() {
		return envBridge.workspaceSpecificStorageLocation();
	}

	@Override
	public Map<String, String> archetypeToTagMap() {
		return envBridge.archetypeToTagMap();
	}
	
	@Override
	public Optional<List<Pair<String, File>>> getDevEnvScanRoots() {	
		return envBridge.getDevEnvScanRoots();
	}
		
	@Override
	public Optional<File> getDevEnvBuildRoot() {	
		return envBridge.getDevEnvBuildRoot();
	}

	@Override
	public Optional<List<File>> getWorkspaceScanDirectories() {
		return envBridge.getWorkspaceScanDirectories();
	}
	
	@Override
	public List<SourceRepositoryEntry> getScanRepositories() {	
		return envBridge.getScanRepositories();
	}

	@Override
	public List<File> getScanDirectories() {
		return envBridge.getScanDirectories();
	}

	@Override
	public Optional<File> getDevEnvironmentRoot() {	
		return envBridge.getDevEnvironmentRoot();
	}
	
	

	@Override
	public Optional<File> getRepositoryConfiguration() {	
		return envBridge.getRepositoryConfiguration();
	}
	

	@Override
	public Optional<File> getLocalCache() {
		return envBridge.getLocalCache();
	}

	@Override
	public BasicStorageLocker storageLocker() {	
		return envBridge.storageLocker();
	}
	
	
	@Override
	public VirtualEnvironment virtualEnviroment() {	
		return envBridge.virtualEnviroment();
	}

	@Override
	public void sendEvent(GenericEntity event) {
		eventHub.sendEvent(event);
	}

	@Override
	public <E extends GenericEntity> void addListener(EntityType<E> type, EntityEventListener<? super E> listener) {
		eventHub.addListener(type, listener);		
	}

	@Override
	public <E extends GenericEntity> void removeListener(EntityType<E> type, EntityEventListener<? super E> listener) {
		eventHub.removeListener(type, listener);		
	}

	@Override
	public void acknowledgeOverrideChange() {
	}
	
	

	@Override
	public void addPreferencesChangeListener(PreferencesChangeListener listener) {
		preferencesChangeListeners.add(listener);		
	}

	@Override
	public void removePreferencesChangeListener(PreferencesChangeListener listener) {
		preferencesChangeListeners.remove( listener);		
	}
	
	

	@Override
	public void broadcastPreferencesChanged() {
		uiSupport.dispose();
		for (PreferencesChangeListener listener : preferencesChangeListeners) {
			listener.acknowledgePreferencesChange();
		}
	}

	/*
	 * misc
	 */
	public boolean isDebugEventLoggingActive() {
		return DevrockPlugin.envBridge().storageLocker().getValue(StorageLockerSlots.SLOT_DR_DEBUG_EVENT_LOGGING, eventLoggingDefault);
	}

	public UiSupport uiSupport() {
		return uiSupport;
	}

	
}
