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
package com.braintribe.devrock.virtualenvironment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.braintribe.codec.CodecException;
import com.braintribe.devrock.preferences.contributer.PreferencesContributerImplementation;
import com.braintribe.devrock.preferences.contributer.PreferencesContributerRegistry;
import com.braintribe.devrock.preferences.contributer.PreferencesContributionDeclaration;
import com.braintribe.devrock.preferences.contributer.implementation.PreferencesContributerRegistryImpl;
import com.braintribe.devrock.virtualenvironment.listener.VirtualEnvironmentNotificationBroadcaster;
import com.braintribe.devrock.virtualenvironment.listener.VirtualEnvironmentNotificationListener;
import com.braintribe.devrock.virtualenvironment.plugin.preferences.VirtualEnvironmentPreferencesCodec;
import com.braintribe.devrock.virtualenvironment.plugin.preferences.VirtualEnvironmentPreferencesInitializer;
import com.braintribe.model.malaclypse.cfg.preferences.ve.EnvironmentOverride;
import com.braintribe.model.malaclypse.cfg.preferences.ve.VirtualEnvironmentPreferences;
import com.braintribe.utils.IOTools;

/**
 * The activator class controls the plug-in life cycle
 */
public class VirtualEnvironmentPlugin extends AbstractUIPlugin implements 	VirtualEnvironmentNotificationBroadcaster, 
																			VirtualEnvironmentNotificationListener,
																			PreferencesContributerRegistry,
																			PreferencesContributionDeclaration,
																			PreferencesContributerImplementation {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.braintribe.devrock.VirtualEnvironment"; //$NON-NLS-1$

	// The shared instance
	private static VirtualEnvironmentPlugin plugin;
	private VirtualEnvironmentPreferences preferences;
	
	private PreferenceStore store;
	
	private Set<VirtualEnvironmentNotificationListener> listeners = new HashSet<VirtualEnvironmentNotificationListener>();

	private PreferencesContributerRegistryImpl preferencesContributionRegistry = new PreferencesContributerRegistryImpl();
	/**
	 * The constructor
	 */
	public VirtualEnvironmentPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		long startTime = System.nanoTime();
		System.out.println("VE start initializing : " + new Date());
		// 
		plugin = this;
		// 
		preferencesContributionRegistry.addContributionDeclaration( this);
		preferencesContributionRegistry.addContributerImplementation( this);
		
		long endTime = System.nanoTime();
		System.out.println("VE initialized in " + (endTime - startTime) / 1000 + " ms");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		preferencesContributionRegistry.removeContributerImplementation( this);
		preferencesContributionRegistry.removeContributionDeclaration( this);
		
		persistPreferenceStore();		
		plugin = null;
		super.stop(context);
	} 
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static VirtualEnvironmentPlugin getInstance() {
		return plugin;
	}
		
	private File getPreferencesFile() {
		String path = getStateLocation().toOSString();
		return new File( path + File.separator + PLUGIN_ID + ".prefs");
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#getPreferenceStore()
	 */
	@Override
	public IPreferenceStore getPreferenceStore() {
		// lazy load of preference store 
		if (store != null)
			return store;
		File file = getPreferencesFile();
		store = new PreferenceStore( file.getAbsolutePath());
		// always initialize to get the defaults 
		VirtualEnvironmentPreferencesInitializer.initializeDefaultPreferences(store);
		// if a persisted store exists load it
		if (file.exists()) {
			try {
				store.load();
			} catch (IOException e) {
				String msg="cannot load preferences from [" + file.getAbsolutePath() + "]";	
				VirtualEnvironmentStatus status = new VirtualEnvironmentStatus(msg, e);
				VirtualEnvironmentPlugin.getInstance().getLog().log(status);	
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
				VirtualEnvironmentStatus status = new VirtualEnvironmentStatus(msg, e);
				VirtualEnvironmentPlugin.getInstance().getLog().log(status);	
			}
		}
	}

	/**
	 * retrieve a new instance of the property resolver.	 
	 */
	public static Map<String, String> getEnvironmentOverrides() {
		Map<String, String> transfer = new HashMap<String, String>();

		VirtualEnvironmentPreferences virtualEnvironmentPreferences = getInstance().getPreferences(false);
		if (virtualEnvironmentPreferences.getActivation()) {
			Map<String, EnvironmentOverride> environmentOverrides = virtualEnvironmentPreferences.getEnvironmentOverrides();
			for (Entry<String, EnvironmentOverride> entry : environmentOverrides.entrySet()) {
				EnvironmentOverride environmentOverride = entry.getValue();
				if (environmentOverride.getActive()) {
					transfer.put( entry.getKey(), environmentOverride.getValue());
				}
			}
		}
		return transfer; 
	}
	
	public static Map<String, String> getPropertyOverrides() {
		Map<String, String> transfer = new HashMap<String, String>();

		VirtualEnvironmentPreferences virtualEnvironmentPreferences = getInstance().getPreferences(false);
		if (virtualEnvironmentPreferences.getActivation()) {
			Map<String, EnvironmentOverride> environmentOverrides = virtualEnvironmentPreferences.getPropertyOverrides();
			for (Entry<String, EnvironmentOverride> entry : environmentOverrides.entrySet()) {
				
				EnvironmentOverride environmentOverride = entry.getValue();
				if (environmentOverride.getActive()) {
					transfer.put( entry.getKey(), environmentOverride.getValue());
				}
			}
		}
		return transfer;
	}
	
	public static boolean getOverrideActivation() {
		return getInstance().getPreferences(false).getActivation();
	}
	
	/**
	 * retrieve the preferences if not present or just return them 	 
	 */
	public VirtualEnvironmentPreferences getPreferences(boolean reload) {
		try {
			if (preferences == null || reload) {
				IPreferenceStore store = getPreferenceStore();
				preferences = new VirtualEnvironmentPreferencesCodec( store).encode(store);			
			}
			return preferences;
		} catch (CodecException e) {
			String msg = "cannot decode IPreferenceStore";
			VirtualEnvironmentStatus status = new VirtualEnvironmentStatus(msg, e);
			VirtualEnvironmentPlugin.getInstance().getLog().log(status);	
			return VirtualEnvironmentPreferences.T.create();
		}
	}

	@Override
	public void addListener(VirtualEnvironmentNotificationListener listener) {		
		listeners.add(listener);
	}

	@Override
	public void removeListener(VirtualEnvironmentNotificationListener listener) {
		listeners.remove(listener);
		
	}

	@Override
	public void acknowledgeOverrideChange() {
		for (VirtualEnvironmentNotificationListener listener : listeners) {
			listener.acknowledgeOverrideChange();
		}
	}
	
	public Set<PreferencesContributionDeclaration> getPreferenceContributers() {
		return preferencesContributionRegistry.getDeclarations();
	}
	public Set<PreferencesContributerImplementation> getActivePreferenceContributers() {
		return preferencesContributionRegistry.getImplementations();
	}

	@Override
	public void addContributionDeclaration(PreferencesContributionDeclaration contributer) {
		preferencesContributionRegistry.addContributionDeclaration(contributer);		
	}

	@Override
	public void removeContributionDeclaration(PreferencesContributionDeclaration contributer) {
		preferencesContributionRegistry.removeContributionDeclaration(contributer);		
	}

	@Override
	public void addContributerImplementation(PreferencesContributerImplementation contributer) {
		preferencesContributionRegistry.addContributerImplementation(contributer);
		
	}

	@Override
	public void removeContributerImplementation(PreferencesContributerImplementation contributer) {
		preferencesContributionRegistry.removeContributerImplementation(contributer);
		
	}

	@Override
	public String getName() {
		return PLUGIN_ID;
	}

	@Override
	public String exportContents() {
		try {
			VirtualEnvironmentPreferences preferences = getPreferences(false);
			VirtualEnvironmentPreferencesCodec codec = new VirtualEnvironmentPreferencesCodec( getPreferenceStore());
			PreferenceStore store = (PreferenceStore) codec.decode(preferences);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();		
			store.save( stream, PLUGIN_ID);
			String contents = stream.toString( "UTF-8");
			IOTools.closeQuietly(stream);				
			return contents;
		} catch (Exception e) {			
			String msg = "cannot export preferences as [" + e + "]";
			VirtualEnvironmentStatus status = new VirtualEnvironmentStatus(msg, e);
			VirtualEnvironmentPlugin.getInstance().getLog().log(status);	
		} 
		return null;
	}

	@Override
	public void importContents(String contents) {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream( contents.getBytes("UTF-8"));		
			store.load(stream);		
			preferences = null;
			getPreferences(true);
			// broadcast changes to any others 
			for (VirtualEnvironmentNotificationListener listener : listeners) {
				listener.acknowledgeOverrideChange();
			}
		} catch (Exception e) {
			String msg = "cannot import preferences as [" + e + "], restoring from persistence";
			VirtualEnvironmentStatus status = new VirtualEnvironmentStatus(msg, e);
			VirtualEnvironmentPlugin.getInstance().getLog().log(status);	
			store = null;
			getPreferenceStore();
		}
		
	}

	@Override
	public String getTooltip() {
		return "Devrock Virtual Environment : overrides enviroment settings for Devrock plugins and implements variable resolvers";
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

	
	

}
