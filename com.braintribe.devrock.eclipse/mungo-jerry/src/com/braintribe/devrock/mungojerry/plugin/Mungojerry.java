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
package com.braintribe.devrock.mungojerry.plugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.codec.CodecException;
import com.braintribe.commons.environment.MungojerryEnvironment;
import com.braintribe.commons.environment.properties.PropertyResolver;
import com.braintribe.devrock.mungojerry.preferences.MungojerryPreferencesCodec;
import com.braintribe.devrock.mungojerry.preferences.MungojerryPreferencesInitializer;
import com.braintribe.devrock.preferences.contributer.PreferencesContributerImplementation;
import com.braintribe.devrock.preferences.contributer.PreferencesContributionDeclaration;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.logging.Logger;
import com.braintribe.model.malaclypse.cfg.preferences.mj.MungojerryPreferences;
import com.braintribe.utils.IOTools;


public class Mungojerry extends AbstractUIPlugin implements HasPluginTokens, PreferencesContributionDeclaration, PreferencesContributerImplementation {
	private static Logger log = Logger.getLogger(Mungojerry.class);
	private static Mungojerry plugin;
	private PreferenceStore store;
	private MungojerryPreferences mungojerryPreferences;
	private MungojerryEnvironment mungojerryEnvironment;

	public Mungojerry() {	
	}
	
	@Override
	public void start(BundleContext context) throws Exception {	
		super.start(context);
		plugin = this;
		VirtualEnvironmentPlugin virtualEnvironmentPlugin = VirtualEnvironmentPlugin.getInstance();
		if (virtualEnvironmentPlugin != null) {
			virtualEnvironmentPlugin.addContributerImplementation( plugin);
		}		
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		VirtualEnvironmentPlugin virtualEnvironmentPlugin = VirtualEnvironmentPlugin.getInstance();
		if (virtualEnvironmentPlugin != null) {
			virtualEnvironmentPlugin.removeContributerImplementation(plugin);
			virtualEnvironmentPlugin.removeContributionDeclaration(plugin);
		}
		
		persistPreferenceStore();
	}

	public static Mungojerry getInstance() {
		return plugin;
	}
	
	public static void log(int severity, String msg) {
		plugin.getLog().log( new Status( severity, PLUGIN_ID, msg));
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
		MungojerryPreferencesInitializer.initializeDefaultPreferences(store);
		// if a persisted store exists load it
		if (file.exists()) {
			try {
				store.load();
			} catch (IOException e) {
				String msg="cannot load preferences from [" + file.getAbsolutePath() + "]";
				log.error(msg, e);
				log( IStatus.ERROR, msg + " as " + e.getMessage());
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
				log.error(msg, e);
				log( IStatus.ERROR, msg + " as " + e.getMessage());
			}
		}
	}
	
	public MungojerryPreferences getMungojerryPreferences( boolean reload) {
		if (mungojerryPreferences == null || reload) {
			try { 								
				IPreferenceStore store = getPreferenceStore();				
				mungojerryPreferences = new MungojerryPreferencesCodec(store).encode(store);
			} catch (CodecException e) {
				String msg = "cannot decode IPreferenceStore";
				log.error( msg, e);
				log( Status.ERROR, msg + "[" + e.getMessage() + "]");
				return null;
			}
		}
		return mungojerryPreferences;
	}
	
	public MungojerryEnvironment getEnvironment() {
		if (mungojerryEnvironment == null) {
			mungojerryEnvironment = new MungojerryEnvironment();
		}
		return mungojerryEnvironment;
	}
	
	public VirtualPropertyResolver getVirtualPropertyResolver() {
		return new PropertyResolver();
	}

	@Override
	public String getName() {
		return PLUGIN_ID;
	}

	@Override
	public String exportContents() {
		try {
			MungojerryPreferences preferences = getMungojerryPreferences( false);
			MungojerryPreferencesCodec codec = new MungojerryPreferencesCodec( getPreferenceStore());
			PreferenceStore store = (PreferenceStore) codec.decode(preferences);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();		
			store.save( stream, PLUGIN_ID);
			String contents = stream.toString( "UTF-8");
			IOTools.closeQuietly(stream);				
			return contents;
		} catch (Exception e) {			
			log(IStatus.ERROR, "cannot export preferences as [" + e + "]");			
		} 
		return null;
	}

	@Override
	public void importContents(String contents) {	
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream( contents.getBytes("UTF-8"));
			// make sure store's ready
			getPreferenceStore();
			store.load(stream);									
		} catch (Exception e) {
			Mungojerry.log(IStatus.ERROR, "cannot import preferences as [" + e + "], restoring from persistence");
			// force reload from old store 			
			getMungojerryPreferences(true);
		}
	}

	@Override
	public String getTooltip() {
		return "Devrock Mungojerry : analyzes and validates GWT projects";
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
