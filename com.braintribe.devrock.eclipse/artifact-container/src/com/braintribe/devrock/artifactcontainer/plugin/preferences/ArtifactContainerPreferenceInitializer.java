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
package com.braintribe.devrock.artifactcontainer.plugin.preferences;

import java.io.File;
import java.util.List;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;

import com.braintribe.build.process.repository.process.SourceRepositoryAccessException;
import com.braintribe.build.process.repository.process.svn.SvnInfo;
import com.braintribe.codec.CodecException;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.codec.ArtifactContainerPluginPreferencesCodec;
import com.braintribe.model.malaclypse.cfg.AntTarget;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.model.malaclypse.cfg.preferences.ac.AntRunnerPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.ArtifactContainerPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.ClasspathPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.container.DynamicContainerPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.profile.ProfilePreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.QuickImportPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.VersionModificationAction;
import com.braintribe.model.malaclypse.cfg.preferences.ac.ravenhurst.RavenhurstPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.views.dependency.DependencyViewPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.views.dependency.FilterType;
import com.braintribe.model.malaclypse.cfg.preferences.gwt.GwtPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.mv.MavenPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SvnPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.tb.TbRunnerPreferences;

public class ArtifactContainerPreferenceInitializer {

	private static final String BIN_ANT_SH = "bin/ant.sh";
	private static final String BIN_ANT_BAT = "bin/ant.bat";
	
	public static void initializeDefaultPreferences(IPreferenceStore store) {				
		ArtifactContainerPreferences preferences = initializeArtifactContainerPreferences();
		ArtifactContainerPluginPreferencesCodec codec = new ArtifactContainerPluginPreferencesCodec( store);
		try {
			codec.decode(preferences);
		} catch (CodecException e) {
			String msg = "cannot prime preferences";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			ArtifactContainerPlugin.getInstance().log(status);						
		}						
	}

	/**
	 * initialize the preferences 	
	 */
	public static ArtifactContainerPreferences initializeArtifactContainerPreferences() {
		ArtifactContainerPreferences preferences = ArtifactContainerPreferences.T.create();
		preferences.setDynamicContainerPreferences( initializeDynamicContainerPreferences());
		preferences.setAntRunnerPreferences( initializeAntRunnerPreferences());
		preferences.setClasspathPreferences( initializeClasspathPreferences());
		preferences.setDependencyViewPreferences( initializeDependencyViewFilterPreferences());
		preferences.setGwtPreferences(initializeGwtPreferences());
		preferences.setProfilePreferences( initializeProfilePreferences());
		preferences.setQuickImportPreferences( initializedQuickImportPreferences());
		preferences.setRavenhurstPreferences( initializeRavenhurstPreferences());
		preferences.setSvnPreferences(initializeSvnPreferences());
		preferences.setMavenPreferences( initializeMavenPreferences());
		preferences.setTbRunnerPreferences( initializeTbRunnerPreferences());
		return preferences;
	}
	
	public static DynamicContainerPreferences initializeDynamicContainerPreferences() {
		DynamicContainerPreferences dynContainerPreferences = DynamicContainerPreferences.T.create();
		// 10.10.2019 : moved from 10 down to 5
		dynContainerPreferences.setConcurrentWalkBatchSize(5);
		// from now on, it's adhoc per default : 3.12.2018, pit
		dynContainerPreferences.setClashResolvingInstant( ResolvingInstant.adhoc);
		// 22.10.2019 introduced that 
		dynContainerPreferences.setChainArtifactSync(true);
		return dynContainerPreferences;
	}

	public static SvnPreferences initializeSvnPreferences() {
		SvnPreferences preferences = SvnPreferences.T.create();
		String workingCopyVariable = "${BT__ARTIFACTS_HOME}";			
		preferences.setWorkingCopy(workingCopyVariable);
		
		String workingCopy = ArtifactContainerPlugin.getInstance().getVirtualPropertyResolver().getEnvironmentProperty(workingCopyVariable);
		String url = "https://svn.braintribe.com/repo/master/Development/artifacts";
		SvnInfo svnInfo = new SvnInfo();
		try {
			svnInfo.read( workingCopy);
			url = svnInfo.getUrl();			
		} catch (SourceRepositoryAccessException e) {		
		}
		preferences.setUrl(url);
		
		return preferences;
	}
	
	public static ProfilePreferences initializeProfilePreferences() {
		ProfilePreferences preferences = ProfilePreferences.T.create();
		return preferences;
	}
	
	public static RavenhurstPreferences initializeRavenhurstPreferences() {
		RavenhurstPreferences preferences = RavenhurstPreferences.T.create();
		preferences.setPrunePeriod(5);
		return preferences;
	}
	
	public static QuickImportPreferences initializedQuickImportPreferences() {
		QuickImportPreferences preferences = QuickImportPreferences.T.create();
		preferences.setAlternativeUiNature( false);
		preferences.setFilterOnWorkingSet(true);
		preferences.setLocalOnlyNature( true);
		preferences.setAttachToCurrentProject( false);
		preferences.setLastDependencyCopyMode( VersionModificationAction.referenced);
		preferences.setLastDependencyPasteMode( VersionModificationAction.referenced);
		
		preferences.getArchetypeToAssetMap().put("model", "asset"); 
	
		return preferences;
	}
	
	public static GwtPreferences initializeGwtPreferences() {
		GwtPreferences preferences = GwtPreferences.T.create();
		preferences.setAutoInjectLibrary( "com.google.gwt:gwt-user#2.8.0.jar");
		return preferences;
	}
	public static ClasspathPreferences initializeClasspathPreferences() {
		ClasspathPreferences preferences = ClasspathPreferences.T.create();
		preferences.setStaticMavenClasspathFile( ".static.classpath");
		return preferences;
	}
	
	public static DependencyViewPreferences initializeDependencyViewFilterPreferences() {
		DependencyViewPreferences preferences = DependencyViewPreferences.T.create();
		preferences.setFilterType(FilterType.simple);
		preferences.setFilterExpression("");
		return preferences;
	}
	
	public static TbRunnerPreferences initializeTbRunnerPreferences() {
		TbRunnerPreferences preferences = TbRunnerPreferences.T.create();
		preferences.setTransitiveBuild(true);
		return preferences;
	}
	
	public static AntRunnerPreferences initializeAntRunnerPreferences() {
		AntRunnerPreferences preferences = AntRunnerPreferences.T.create();
		// targets 
		String [] targets = new String []  {"Install dependencies,install-deps,true",
				"Download dependencies,download-deps,true",
				"Install dependencies,install-deps,true",
				"Build from SVN,buildFromSvn,true",
				"Build,build,true",
				"Install all, install-all, true",
				"Install artifact,install,false",
				"Update classpath,update-classpath.AC,true",
				"Deploy artifact,deploy,false",
		};
		
		List<AntTarget> antTargets = preferences.getTargets();
		for (String target : targets) {
			String [] singleTargetValues = target.split(",");
			String targetName = singleTargetValues[0];
			String targetTask = singleTargetValues[1];
			String targetTransitiveNature= singleTargetValues[2];
			
			AntTarget antTarget = AntTarget.T.create();
			antTarget.setName(targetName);
			antTarget.setTarget(targetTask);
			antTarget.setTransitiveNature( Boolean.parseBoolean(targetTransitiveNature));				
			antTargets.add(antTarget);
		}			
		
		// ant home 
		String home = ArtifactContainerPlugin.getInstance().getVirtualPropertyResolver().getEnvironmentProperty( "ANT_HOME");
		if (home == null) {
			try {
				AntCorePlugin antCorePlugin = AntCorePlugin.getPlugin();		
				if (antCorePlugin != null) {
					home = antCorePlugin.getPreferences().getAntHome();
				}									
			} catch (Exception e1) {
				String msg = "Cannot find ant core plugin";
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e1);
				ArtifactContainerPlugin.getInstance().log(status);			
			}
		}
		// fall back.. 		
		if (home == null) {
			String msg = "Cannot determine ant location neither via ${ANT_HOME} nor via Eclipse's AntCorePlugin";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
			ArtifactContainerPlugin.getInstance().log(status);
			preferences.setAntHome("");
			preferences.setAntRunner("");
			return preferences;
		}
		
		
		preferences.setAntHome(home);
		
		// ant runner 
		preferences.setAntRunner( BIN_ANT_BAT);		
		File runner = new File( home, BIN_ANT_BAT);
		if (runner.exists() == false) {
			runner = new File( home, BIN_ANT_SH);
			if (runner.exists()) 
				preferences.setAntRunner( BIN_ANT_SH);
		}
	
		
		
		return preferences;
	}
	
	public static MavenPreferences initializeMavenPreferences() {
		return MavenPreferences.T.create();
	}
		
}
