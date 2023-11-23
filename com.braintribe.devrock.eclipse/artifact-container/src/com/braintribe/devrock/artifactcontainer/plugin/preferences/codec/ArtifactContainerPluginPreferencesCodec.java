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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.codec;

import org.eclipse.jface.preference.IPreferenceStore;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.model.malaclypse.cfg.preferences.ac.ArtifactContainerPreferences;

public class ArtifactContainerPluginPreferencesCodec implements Codec<IPreferenceStore, ArtifactContainerPreferences> {
	
	private AntPreferencesCodec antPreferencesCodec;
	private SvnPreferencesCodec svnPreferencesCodec;
	private QuickImportPreferencesCodec quickImportPreferencesCodec;
	private RavenhurstPreferencesCodec ravenhurstPreferencesCodec;
	private ProfilePreferencesCodec profilePreferencesCodec;
	private DependencyViewFilerPreferencesCodec dependencyFilterPreferencesCodec;
	private GwtPreferencesCodec gwtPreferencesCodec;
	private ClasspathPreferencesCodec classpathPreferencesCodec;
	private MavenPreferencesCodec mavenPreferencesCodec;
	private DynamicContainerPreferencesCodec dynamicContainerCodec;
	private TbRunnerPreferencesCodec tbRunnerCodec;

	
	private IPreferenceStore iPreferenceStore; 
	
	public ArtifactContainerPluginPreferencesCodec( IPreferenceStore store) {
		this.iPreferenceStore = store;
		
		antPreferencesCodec = new AntPreferencesCodec(store);
		svnPreferencesCodec = new SvnPreferencesCodec(store);
		quickImportPreferencesCodec = new QuickImportPreferencesCodec(store);
		ravenhurstPreferencesCodec = new RavenhurstPreferencesCodec(store);
		profilePreferencesCodec = new ProfilePreferencesCodec(store);
		dependencyFilterPreferencesCodec = new DependencyViewFilerPreferencesCodec(store);
		gwtPreferencesCodec = new GwtPreferencesCodec(store);
		classpathPreferencesCodec = new ClasspathPreferencesCodec(store);
		mavenPreferencesCodec = new MavenPreferencesCodec(store);
		dynamicContainerCodec = new DynamicContainerPreferencesCodec(store);
		tbRunnerCodec = new TbRunnerPreferencesCodec(store);
		
	}

	@Override
	public IPreferenceStore decode(ArtifactContainerPreferences preferences) throws CodecException {
		
		// do our stuff directly 
		dynamicContainerCodec.decode(preferences.getDynamicContainerPreferences());
		
		// static maven 
		classpathPreferencesCodec.decode( preferences.getClasspathPreferences());
		// gwt		
		gwtPreferencesCodec.decode( preferences.getGwtPreferences());

		// svn 
		svnPreferencesCodec.decode( preferences.getSvnPreferences());
		
		// ant
		antPreferencesCodec.decode( preferences.getAntRunnerPreferences());
				
		// quick import 
		quickImportPreferencesCodec.decode(preferences.getQuickImportPreferences());
		
		// ravenhurst.. 
		ravenhurstPreferencesCodec.decode(preferences.getRavenhurstPreferences());		
 		
 		// dependency view filter
 		dependencyFilterPreferencesCodec.decode(preferences.getDependencyViewPreferences());
 		
 		// profile settings
 		profilePreferencesCodec.decode(preferences.getProfilePreferences());
 		
 		//maven 
 		mavenPreferencesCodec.decode( preferences.getMavenPreferences());
 		
 		// tb 
 		tbRunnerCodec.decode(preferences.getTbRunnerPreferences());
 		
		return iPreferenceStore;
	}

	@Override
	public ArtifactContainerPreferences encode(IPreferenceStore store) throws CodecException {
		//
		// read the store data
		//
		ArtifactContainerPreferences preferences = ArtifactContainerPreferences.T.create();
		
		// only direct settings here 
		preferences.setDynamicContainerPreferences( dynamicContainerCodec.encode(store));
		
		// static maven 		
		preferences.setClasspathPreferences( classpathPreferencesCodec.encode(store));
		
		// gwt
		preferences.setGwtPreferences( gwtPreferencesCodec.encode(store));
		// svn		
		preferences.setSvnPreferences(svnPreferencesCodec.encode(store));
				
		// ant 
		preferences.setAntRunnerPreferences(antPreferencesCodec.encode(store));
		
		// quick import				
		preferences.setQuickImportPreferences( quickImportPreferencesCodec.encode(store));
		
		// ravenhurst
		preferences.setRavenhurstPreferences( ravenhurstPreferencesCodec.encode(store));
		
		// dependency view filter		
		preferences.setDependencyViewPreferences( dependencyFilterPreferencesCodec.encode(store));

		// profile 	
		preferences.setProfilePreferences( profilePreferencesCodec.encode(store));
		
		// maven
		preferences.setMavenPreferences( mavenPreferencesCodec.encode(store));
		
		//tb
		preferences.setTbRunnerPreferences( tbRunnerCodec.encode(store));
		return preferences;
	}

	@Override
	public Class<IPreferenceStore> getValueClass() {
		return IPreferenceStore.class;
	}

	
}
