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
package com.braintribe.devrock.greyface;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;
import org.osgi.framework.BundleContext;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.cfg.Configurable;
import com.braintribe.codec.CodecException;
import com.braintribe.devrock.greyface.process.ProcessControl;
import com.braintribe.devrock.greyface.process.retrieval.TempFileHelper;
import com.braintribe.devrock.greyface.scope.GreyfaceScope;
import com.braintribe.devrock.greyface.settings.codecs.GreyfacePreferencesCodec;
import com.braintribe.devrock.greyface.settings.preferences.GreyfacePreferenceInitializer;
import com.braintribe.devrock.greyface.view.GreyfaceView;
import com.braintribe.model.malaclypse.cfg.preferences.gf.GreyFacePreferences;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;
import com.braintribe.plugin.commons.properties.PropertyResolver;


/**
 * 
 *   Greyface and his followers took the game of playing at life more seriously than they took life itself and 
 *   were known even to destroy other living beings whose ways of life differed from their own.
 *   	Malaclypse the Younger, Principia Discordia, Page 00042
 *   
 * 	 An alternative is to view disorder as preferable at all costs. 
 *   To quote: "To choose order over disorder, or disorder over order, is to accept a trip composed of both the creative and the destructive. 
 *   But to choose the creative over the destructive is an all-creative trip composed of both order and disorder" 
 *   	Malaclypse the Younger, K.S.C.
 *   
 * 	 This plugin is named Greyface as it deals with Maven.
 * 
 * 	 Hail Eris!  
 * 
 *   pit, K.S.C, Setting Orange, 45th Chaos, 3178 YOLD
 *   
 */
public class GreyfacePlugin extends AbstractUIPlugin implements GreyfaceViewLauncher, HasGreyfaceToken {

	// The shared instance
	private static GreyfacePlugin plugin;
		
	private List<String> initialViewScanParameters = new ArrayList<String>();
	
	private ProcessControl processControl;
	private PreferenceStore store;
	private GreyFacePreferences greyfacePreferences;

	
		
	/**
	 * The constructor
	 */
	public GreyfacePlugin() {
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
		System.out.println("GF start initializing : " + new Date());
		
		//VirtualEnvironmentPlugin.getInstance().addListener( this);
					
		// 
		//VirtualEnvironmentPlugin.getInstance().addContributerImplementation(plugin);
		
		// house keeping 
		Job houseKeepingJob = new Job( "Internal housekeeping") {				
			@Override
			protected IStatus run(IProgressMonitor progressMonitor) {
													
				//
				TempFileHelper.purge();
										
				return Status.OK_STATUS;
			}
		};					
		
		houseKeepingJob.schedule();
		
		
		long endTime = System.nanoTime();
		System.out.println("GF initialized in " + (endTime - startTime) / 1000 + " ms");
		
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		/*
		VirtualEnvironmentPlugin virtualEnvironmentPlugin = VirtualEnvironmentPlugin.getInstance();
		if (virtualEnvironmentPlugin != null) {
			virtualEnvironmentPlugin.removeContributionDeclaration( this);
			virtualEnvironmentPlugin.removeContributerImplementation( this);
			virtualEnvironmentPlugin.removeListener( this);
		}	
		*/	
		if (processControl != null) {
			processControl.cancelAllProcesses();
		}
		persistPreferenceStore();
		plugin = null;	
		super.stop(context);
	}
	
	@Configurable
	public void setProcessControl(ProcessControl processControl) {
		this.processControl = processControl;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static GreyfacePlugin getInstance() {
		return plugin;
	}

	
	
	public Set<RepositorySetting> getHomeRepositorySettings() {
		
		MavenSettingsReader reader = GreyfaceScope.getScope().getSettingsReader();
		
		try {
			List<RemoteRepository> repositories = reader.getAllRemoteRepositories();
			Set<RepositorySetting> settings = new HashSet<RepositorySetting>( repositories.size());
			for (RemoteRepository repository : repositories) {
				RepositorySetting setting = RepositorySetting.T.create();
				setting.setHomeRepository(true);
				setting.setUrl( repository.getUrl());
				setting.setUser( repository.getUser());
				setting.setPassword( repository.getPassword());
				setting.setName( repository.getName());
				settings.add(setting);				
			}			
			return settings;
		} catch (RepresentationException e) {		
		}		
		return null;
	}
		
	@Override
	public void addDependency(String dependency) {
		initialViewScanParameters.add( dependency);
	}

	@Override
	public void activateGreyface() {
		//
		IWorkbench wb = PlatformUI.getWorkbench();
		IViewRegistry viewRegistry = wb.getViewRegistry();
		IViewDescriptor desc = viewRegistry.find( VIEW_ID);
		if (desc != null) {
			try {
				GreyfaceView view = (GreyfaceView) wb.getActiveWorkbenchWindow().getActivePage().showView( VIEW_ID);
				if (initialViewScanParameters.size() > 0) {
					StringBuffer buffer = new StringBuffer();
					for (String param : initialViewScanParameters) {
						if (buffer.length() > 0) {
							buffer.append( "\n");
						}
						buffer.append( param);
					}
					view.setArtifactExpression( buffer.toString());
					initialViewScanParameters.clear();
				}				
			} catch (PartInitException e) {
				String msg = "cannot active Greyface view with ID [" + GreyfacePlugin.VIEW_ID + "]";
				GreyfaceStatus status = new GreyfaceStatus( msg, e);
				GreyfacePlugin.getInstance().getLog().log(status);
			}
		}			
		else {
			String msg =  "no view with ID [" + GreyfacePlugin.VIEW_ID + "] found in registry";
			GreyfaceStatus status = new GreyfaceStatus( msg, IStatus.ERROR);
			GreyfacePlugin.getInstance().getLog().log(status);
		}
	}
	
	public List<String> getInitialViewScanParameters() {
		return initialViewScanParameters;
	}
		
	
	private File getPreferencesFile() {
		String path = getStateLocation().toOSString();
		return new File( path + File.separator + GreyfacePlugin.PLUGIN_ID + ".prefs");
		
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
		GreyfacePreferenceInitializer.initializeDefaultPreferences(store);
		// if a persisted store exists load it
		if (file.exists()) {
			try {
				store.load();
			} catch (IOException e) {
				String msg="cannot load preferences from [" + file.getAbsolutePath() + "]";				
				GreyfaceStatus status = new GreyfaceStatus( msg, e);
				GreyfacePlugin.getInstance().getLog().log(status);
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
				GreyfaceStatus status = new GreyfaceStatus( msg, e);
				GreyfacePlugin.getInstance().getLog().log(status);
			}
		}
	}
	
	public GreyFacePreferences getGreyfacePreferences(boolean reload) {
		if (greyfacePreferences == null || reload) {						
			GreyfacePreferencesCodec codec = new GreyfacePreferencesCodec(getPreferenceStore());
			try {
				greyfacePreferences = codec.encode(store);
			} catch (CodecException e) {
				String msg="cannot encode preferences from store";
				GreyfaceStatus status = new GreyfaceStatus( msg, e);
				GreyfacePlugin.getInstance().getLog().log(status);
			}
		}
		return greyfacePreferences;
	}
	
	public void setGreyfacePreferences( GreyFacePreferences gfPreferences) {
		greyfacePreferences = gfPreferences;
	}
	
	public VirtualPropertyResolver getVirtualPropertyResolver() {
		return new PropertyResolver();
	}
	
	public boolean isDebugActive() {//
		String debugSwitch = getVirtualPropertyResolver().getEnvironmentProperty(PLUGIN_DEBUG);
		if (debugSwitch != null && debugSwitch.equalsIgnoreCase("ON")) {
			return true;
		}
		return false;
	}
	/*
	@Override
	public void acknowledgeOverrideChange() {	
	}
		

	@Override
	public String getName() {
		return PLUGIN_ID;
	}

	@Override
	public String getTooltip() {
		return "Devrock Greyface : scans and uploads third party artifacts to remote repositories";
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
			GreyFacePreferences preferences = getGreyfacePreferences(false);
			GreyfacePreferencesCodec codec = new GreyfacePreferencesCodec( getPreferenceStore());
			PreferenceStore store = (PreferenceStore) codec.decode(preferences);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();		
			store.save( stream, PLUGIN_ID);
			String contents = stream.toString( "UTF-8");
			IOTools.closeQuietly(stream);				
			return contents;
		} catch (Exception e) {			
			String msg = "cannot export preferences as [" + e + "]";			
			GreyfaceStatus status = new GreyfaceStatus( msg, e);
			GreyfacePlugin.getInstance().getLog().log(status);
		} 
		return null;
	}

	@Override
	public void importContents(String contents) {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream( contents.getBytes("UTF-8"));
			// make sure preferences store's ready 
			getPreferenceStore();
			store.load(stream);
			getGreyfacePreferences( true);
		} catch (Exception e) {
			String msg ="cannot import preferences as [" + e + "], restoring from persistence";
			GreyfaceStatus status = new GreyfaceStatus( msg, e);
			GreyfacePlugin.getInstance().getLog().log(status);
			// force reload from old store 
			store = null;
			getPreferenceStore();
		}
	}
	*/
	
}
