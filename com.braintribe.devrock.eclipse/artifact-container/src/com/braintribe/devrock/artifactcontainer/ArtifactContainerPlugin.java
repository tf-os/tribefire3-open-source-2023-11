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
package com.braintribe.devrock.artifactcontainer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.codec.CodecException;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.devrock.artifactcontainer.control.container.ArtifactContainerRegistry;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerUpdateRequestType;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerWalkController;
import com.braintribe.devrock.artifactcontainer.control.workspace.ResourceChangeListener;
import com.braintribe.devrock.artifactcontainer.control.workspace.WorkspaceProjectRegistry;
import com.braintribe.devrock.artifactcontainer.housekeeping.HouseKeepingTask;
import com.braintribe.devrock.artifactcontainer.housekeeping.RavenhurstPersistencePurgeTask;
import com.braintribe.devrock.artifactcontainer.housekeeping.TemporaryFilePurgeTask;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.ArtifactContainerPreferenceInitializer;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.codec.ArtifactContainerPluginPreferencesCodec;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.maven.validator.MavenSettingsValidator;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.project.validator.ProjectSettingsValidator;
import com.braintribe.devrock.artifactcontainer.quickImport.ParallelQuickImportControl;
import com.braintribe.devrock.artifactcontainer.quickImport.QuickImportControl;
import com.braintribe.devrock.artifactcontainer.validator.ArtifactContainerPluginValidator;
import com.braintribe.devrock.preferences.contributer.PreferencesContributerImplementation;
import com.braintribe.devrock.preferences.contributer.PreferencesContributionDeclaration;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.logging.Logger;
import com.braintribe.logging.LoggerInitializer;
import com.braintribe.model.malaclypse.cfg.preferences.ac.ArtifactContainerPreferences;
import com.braintribe.plugin.commons.console.ConsoleLogger;
import com.braintribe.plugin.commons.preferences.validator.CompoundValidator;
import com.braintribe.plugin.commons.properties.PropertyResolver;
import com.braintribe.plugin.commons.selection.SelectionServiceListener;
import com.braintribe.utils.IOTools;

/**
 * The activator class controls the plug-in life cycle
 */
public class ArtifactContainerPlugin extends AbstractUIPlugin implements PreferencesContributionDeclaration, PreferencesContributerImplementation { 

	private static Logger log = Logger.getLogger(ArtifactContainerPlugin.class);
	
	// The plug-in ID
	public static final String PLUGIN_ID = "com.braintribe.ArtifactContainer"; //$NON-NLS-1$
	
	// 
	public static final String PLUGIN_DEBUG = "AC_DEBUG";
	
	//
	public static final String PLUGIN_RESOURCE_PREFIX = "platform:/plugin/" + PLUGIN_ID;
	
	public static final String ERRORVIEW_ID = "org.eclipse.pde.runtime.LogView";

	// The shared instance
	private static ArtifactContainerPlugin plugin;
	
	private ArtifactContainerPreferences artifactContainerPreferences;
	
	
	private QuickImportControl quickImportController;
	
	private ResourceChangeListener resourceChangeListener = new ResourceChangeListener();
	private SelectionServiceListener selectionListener = new SelectionServiceListener();
	
	//private boolean inhibitDirectArtifactContainerInitializing;
	
	private PreferenceStore store;
	
	private ConsoleLogger consoleLogger;
	
	
	private static ArtifactContainerRegistry artifactContainerRegistry;
	private static WorkspaceProjectRegistry workspaceProjectRegistry;

	private ArtifactContainerPluginValidator validator;
	
	/**
	 * The constructor
	 */
	public ArtifactContainerPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		long startTime = System.nanoTime();		
		log.info("AC start initializing : " + new Date());
	
		// attach the hook into the workspace
		attachResourceChangeListener();
				
		// attach selection service listener
	//	attachSelectionListener();
		
		// setup quick import controller instance - defer it, so it runs after the plugin has been initialized
		quickImportController = new ParallelQuickImportControl();
		
		// validation job definition 
		Job validationJob = new Job( "Internal validation") {				
			@Override
			protected IStatus run(IProgressMonitor progressMonitor) {
				// prime preferences.. 
				getArtifactContainerPreferences(false);
				
				quickImportController.setup();
				//
				if (getValidator().validate( progressMonitor)) {
					// only start the scanner if everything's ok
					quickImportController.rescan();					
				}
										
				return Status.OK_STATUS;
			}
		};				
		
		
		// house keeping 
		Job houseKeepingJob = new Job( "Internal housekeeping") {				
			@Override
			protected IStatus run(IProgressMonitor progressMonitor) {
				// prime preferences.. 
				getArtifactContainerPreferences(false);
								
				//
				getHouseKeepingTasks().stream().forEach( t -> t.execute());
										
				return Status.OK_STATUS;
			}
		};					
		
		
		// create a console logger 
		consoleLogger = new ConsoleLogger("Artifact Container Plugin");
		
		LoggerInitializer loggerInitializer = new LoggerInitializer();
		try {					
			String path = ArtifactContainerPlugin.getInstance().getStateLocation().toOSString();
			File file = new File(path + File.separator + "logger.properties");
			if (file.exists()) {
				loggerInitializer.setLoggerConfigUrl( file.toURI().toURL());		
				loggerInitializer.afterPropertiesSet();
			}
		} catch (Exception e) {		
			String msg = "cannot initialize logging";
			log.info(msg, e);
		}
		
		
		
		// attach contribution		
		VirtualEnvironmentPlugin.getInstance().addContributionDeclaration( plugin);
		VirtualEnvironmentPlugin.getInstance().addContributerImplementation(plugin);		
	
		// run the declared jobs, validation & house keeping
		validationJob.schedule();
		houseKeepingJob.schedule();
		
		long endTime = System.nanoTime();
		String msg = "AC initialized in " + (endTime - startTime) / 1E6 + " ms";
		//System.out.println(msg);
		log.info(msg);
	}

	public CompoundValidator getValidator() {
		if (validator == null) {
			validator = new ArtifactContainerPluginValidator();
			validator.addValidator( new ProjectSettingsValidator());
			validator.addValidator( new MavenSettingsValidator());	
		}
		return validator;
	}
	
	public List<HouseKeepingTask> getHouseKeepingTasks() {
		List<HouseKeepingTask> tasks = new ArrayList<>();
		
		tasks.add( new RavenhurstPersistencePurgeTask());
		tasks.add( new TemporaryFilePurgeTask());
		
		return tasks;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		// detach contribution		
		VirtualEnvironmentPlugin virtualEnvironmentPlugin = VirtualEnvironmentPlugin.getInstance();
		if (virtualEnvironmentPlugin != null) {
			virtualEnvironmentPlugin.removeContributerImplementation(plugin);
			virtualEnvironmentPlugin.removeContributionDeclaration(plugin);
			
			// TODO : Find out what this is about
			//virtualEnvironmentPlugin.removeListener(defaultRuntimeScopeFactory);
		}
		
		// stop all walks 
		WiredArtifactContainerWalkController.getInstance().stop();
		quickImportController.stop();
		
		// write back preferences
		persistPreferenceStore();
			
		// detach our hook into the workspace 
		detachResourceChangeListener();
		
		// detach the selection service listener
		detachSelectionListener();
		
		// dump the workspace registry
		workspaceProjectRegistry.dump();
		
		// save containers' configuration 		
		artifactContainerRegistry.persistContainerConfiguration();
		
		
		// shutdown the registry 
		artifactContainerRegistry.shutdown();
		
		// close the console logger
		consoleLogger.close();
		
		// kill the instance 
		plugin = null;
		// delegate 
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ArtifactContainerPlugin getInstance() {
		return plugin;
	}	
	
	
	public static void log(String msg) {
		if (plugin != null && ArtifactContainerPlugin.isDebugActive()) {
			plugin.consoleLogger.log(msg);
		}
	}
	
	private File getPreferencesFile() {
		String path = ArtifactContainerPlugin.getInstance().getStateLocation().toOSString();
		return new File( path + File.separator + ArtifactContainerPlugin.PLUGIN_ID + ".prefs");
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#getPreferenceStore()
	 */
	@Override
	public IPreferenceStore getPreferenceStore() {
		// lazy load of preference store 
		if (store != null) {
			return store;
		}
		File file = getPreferencesFile();
		store = new PreferenceStore( file.getAbsolutePath());
		// always initialize to get the defaults 
		ArtifactContainerPreferenceInitializer.initializeDefaultPreferences(store);
		// if a persisted store exists load it
		if (file.exists()) {
			try {
				store.load();
			} catch (IOException e) {
				String msg="cannot load preferences from [" + file.getAbsolutePath() + "]";				
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
				log.error( msg,e);
				ArtifactContainerPlugin.getInstance().log(status);	
			}
		}	
		return store;
	}
	
	/**
	 * if the {@link PreferenceStore} has been read and changed, write it back to disk
	 */
	private void persistPreferenceStore() {
		if (store != null && store.needsSaving()) {
			try {
				store.save();
			} catch (IOException e) {
				File file = getPreferencesFile();
				String msg="cannot write preferences to [" + file.getAbsolutePath() + "]";
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
				log.error( msg,e);
				ArtifactContainerPlugin.getInstance().log(status);	
			}
		}
	}
	/**
	 * get the current preferences from the {@link IPreferenceStore}
	 * @return - the stored {@link ArtifactContainerPreferences}
	 */
	public synchronized ArtifactContainerPreferences getArtifactContainerPreferences(boolean reload) {
		if (artifactContainerPreferences == null || reload) {
			try { 								
				IPreferenceStore store = getPreferenceStore();				
				artifactContainerPreferences = new ArtifactContainerPluginPreferencesCodec(store).encode(store);
			} catch (CodecException e) {
				String msg = "cannot decode IPreferenceStore";				
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
				log.error( msg,e);
				ArtifactContainerPlugin.getInstance().log(status);	
				return null;
			}
		}
		return artifactContainerPreferences;
	}
				
	public VirtualPropertyResolver getVirtualPropertyResolver() {
		return new PropertyResolver();
	}	
	
	
	public static WorkspaceProjectRegistry getWorkspaceProjectRegistry() {
		if (workspaceProjectRegistry == null) {
			workspaceProjectRegistry = new WorkspaceProjectRegistry();			
		}
		return workspaceProjectRegistry;
	}
	
	public static ArtifactContainerRegistry getArtifactContainerRegistry() {
		if (artifactContainerRegistry == null) {
			artifactContainerRegistry = new ArtifactContainerRegistry();			
		}
		return artifactContainerRegistry;
	}
	
	
	public QuickImportControl getQuickImportScanController() {
		return quickImportController;
	}
	
	public void attachResourceChangeListener() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
		WiredArtifactContainerWalkController.getInstance().setContainerInitializingInhibited( false);
	}
	
	public void detachResourceChangeListener(){
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		WiredArtifactContainerWalkController.getInstance().setContainerInitializingInhibited( true);
	}
	
	
	public void detachSelectionListener() {
		selectionListener.detach();
	}
	
	public ISelection getCurrentSelection() {
		return selectionListener.getSelection();
	}
	
	
	public static void acknowledgeContainerProcessed( ArtifactContainer container, ArtifactContainerUpdateRequestType walkMode) {
		String msg="Processing container [" + container.getId()  + "] (" + walkMode.toString() + ") attached to [" + container.getProject().getProject().getName() + "] has succeeded";
		log(msg);
		log.debug(msg);
	}
	public static void acknowledgeContainerFailed( ArtifactContainer container, ArtifactContainerUpdateRequestType walkMode, String error) {
		String msg="Processing container [" + container.getId()  + "] (" + walkMode.toString() + ") attached to [" + container.getProject().getProject().getName() + "] has failed.\n [" + error + "]";		
		ArtifactContainerPlugin containerPlugin = ArtifactContainerPlugin.getInstance();
		log(msg);
		log.error( msg);
		
		ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
		containerPlugin.log(status);	
	}
	
	public static void acknowledgeContainerNotification( ArtifactContainer container) {
		String msg="notifying Eclipse about container [" + container.getId()  + "] (compile) attached to [" + container.getProject().getProject().getName() + "]";
		log(msg);		
		log.debug(msg);	
	}

	@Override
	public String getName() {	
		return PLUGIN_ID;
	}

	@Override
	public String getTooltip() {	
		return "Devrock Artifact Container : implements dynamic dependency containers and analysis";
	}

	@Override
	public String getLocalFileName() {
		return PLUGIN_ID + ".prefs";
	}

	@Override
	public String getFullFilePath() {
		return getPreferencesFile().getAbsolutePath();
	}
	
	@Override
	public String getPartialFilePath() {
		String fullpath = getFullFilePath().replace("\\", "/");
		String wsPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString().replace( "\\", "/");		
		return fullpath.substring( wsPath.length());
	}


	@Override
	public String exportContents() {
		try {
			ArtifactContainerPreferences preferences = getArtifactContainerPreferences(false);
			ArtifactContainerPluginPreferencesCodec codec = new ArtifactContainerPluginPreferencesCodec( getPreferenceStore());
			PreferenceStore store = (PreferenceStore) codec.decode(preferences);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();		
			store.save( stream, PLUGIN_ID);
			String contents = stream.toString( "UTF-8");
			IOTools.closeQuietly(stream);				
			return contents;
		} catch (Exception e) {
			
			String msg = "cannot export preferences";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			log.error(msg,e);
			ArtifactContainerPlugin.getInstance().log(status);	
			
		} 
		return null;
	}

	@Override
	public void importContents(String contents) {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream( contents.getBytes("UTF-8"));
			// make sure, the store's ready 
			getPreferenceStore();
			store.load(stream);
			getArtifactContainerPreferences(true);
		} catch (Exception e) {
			String msg = "cannot import preferences";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			ArtifactContainerPlugin.getInstance().log(status);
			log.error( msg, e);
			// force reload from old store 
			store = null;
			getPreferenceStore();
		}
	}
	
	
	public void log( IStatus status) {		
		getLog().log(status);		
	}
	
	public static boolean isDebugActive() {//
		String debugSwitch = plugin.getVirtualPropertyResolver().getEnvironmentProperty(PLUGIN_DEBUG);
		if (debugSwitch != null && debugSwitch.equalsIgnoreCase("ON")) {
			return true;
		}
		return false;
	}
		
}

