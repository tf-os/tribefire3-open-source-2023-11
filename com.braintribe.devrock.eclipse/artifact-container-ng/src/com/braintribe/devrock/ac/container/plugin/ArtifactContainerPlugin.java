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
package com.braintribe.devrock.ac.container.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.braintribe.devrock.ac.container.plugin.listener.ResourceChangeListener;
import com.braintribe.devrock.ac.container.registry.WorkspaceContainerRegistry;
import com.braintribe.devrock.api.logging.LoggingCommons;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.listeners.PreferencesChangeListener;
import com.braintribe.devrock.mc.core.commons.ArtifactRemover;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.logging.Logger;

/**
 * the backing plugin 
 * @author pit
 *
 */
public class ArtifactContainerPlugin extends AbstractUIPlugin implements PreferencesChangeListener {

	private static Logger log = Logger.getLogger(ArtifactContainerPlugin.class);

	public static final String PLUGIN_ID = "com.braintribe.devrock.ArtifactContainerPlugin"; //$NON-NLS-1$
	public static final String PLUGIN_RESOURCE_PREFIX = "platform:/plugin/" + PLUGIN_ID;
	
	private static ArtifactContainerPlugin instance;
	
	private WorkspaceContainerRegistry containerRegistry = new WorkspaceContainerRegistry();
	private ResourceChangeListener resourceChangeListener = new ResourceChangeListener();
	
	private UiSupport uiSupport = new UiSupport();
	
	private boolean connected = false;

	// this is the default for event logging. If not set in the storage locker, this is value is used. 
	private boolean eventLoggingDefault = true;

	public static ArtifactContainerPlugin instance() {
		return instance;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {	
		super.start(context);
		instance = this;
		
		LoggingCommons.initializeWithFallback(PLUGIN_ID);
		
		long startTime = System.nanoTime();		
		log.info("ArtifactContainerPlugin: starting : " + new Date());

		// listener installation 
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE );

		// add additional initial setup here
		DevrockPlugin.instance().addPreferencesChangeListener(instance);
		connected = true;
		
		// clear files..
		purgeUndeletedFilesFromFilesystemRepositories();
				
				
		
		long endTime = System.nanoTime();
		String msg = "ArtifactContainerPlugin : started after " + (endTime - startTime) / 1E6 + " ms";

		log.info(msg);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		long startTime = System.nanoTime();		
		log.info("ArtifactContainerPlugin: stopping : " + new Date());
		
		if (connected) {
			DevrockPlugin.instance().removePreferencesChangeListener(instance);
			connected = false;
		}
		
		// de-initialization tasks here
		uiSupport.dispose();
				
		long endTime = System.nanoTime();
		String msg = "ArtifactContainerPlugin : stopped after " + (endTime - startTime) / 1E6 + " ms";
		log.info(msg);
		
	}

	private void purgeUndeletedFilesFromFilesystemRepositories() {
		// clear 
		List<String> filesToPurge = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ARTIFACT_VIEWER_PC_PURGE_NONDELETES, null);
		if (filesToPurge != null && !filesToPurge.isEmpty()) {
			Set<String> uniques = new HashSet<>( filesToPurge);
			List<String> remainder = new ArrayList<>();
			List<String> repopulated = new ArrayList<>();
			List<String> deleted = new ArrayList<>();
			for (String fileName : uniques) {
				File file = new File( fileName);
				if (file.exists()) {
					if (!ArtifactRemover.canBeSafelyDeleted(file)) {
						repopulated.add(fileName);
						continue;
					}
					List<File> delete = ArtifactRemover.delete(file);				
					if (delete != null && delete.size() > 0) {
						remainder.add( fileName);
					}
					else {
						deleted.add(fileName);
					}				
				}
				else {
					deleted.add(fileName); // already purged somehow
				}
			}
			System.out.println( "repopulated : " + repopulated.stream().collect(Collectors.joining(",")));
			System.out.println("deleted : " + deleted.stream().collect(Collectors.joining(",")));
			System.out.println("remaining : " + remainder.stream().collect(Collectors.joining(",")));
			
			if (remainder.size() > 0) { 
				DevrockPlugin.instance().storageLocker().setValue( StorageLockerSlots.SLOT_ARTIFACT_VIEWER_PC_PURGE_NONDELETES, remainder);
			}
			else {
				DevrockPlugin.instance().storageLocker().setValue( StorageLockerSlots.SLOT_ARTIFACT_VIEWER_PC_PURGE_NONDELETES, null);
			}
			
			String msg = "purge : deleted [" + deleted.size() + "], skipped [" + repopulated.size() + "], can't delete [" + remainder.size() + "]"; 			
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.INFO);
			ArtifactContainerPlugin.instance().log(status);
			
		}
	}
	
	

	@Override
	public void acknowledgePreferencesChange() {
			uiSupport.dispose();
	}

	@Override
	public void acknowledgeBroadcasterShutdown() {		
		connected = false;
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
	
	public UiSupport uiSupport() {
		return uiSupport;
	}
	
	public WorkspaceContainerRegistry containerRegistry() {
		return containerRegistry;
	}
	
	public boolean isDebugEventLoggingActive() {
		return DevrockPlugin.envBridge().storageLocker().getValue(StorageLockerSlots.SLOT_AC_DEBUG_EVENT_LOGGING, eventLoggingDefault);
	}

	
	
}
