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
import com.braintribe.devrock.artifactcontainer.plugin.preferences.ArtifactContainerPreferenceConstants;
import com.braintribe.model.malaclypse.cfg.preferences.ac.ClasspathPreferences;

public class ClasspathPreferencesCodec implements Codec<IPreferenceStore, ClasspathPreferences> {
	
	private IPreferenceStore store;

	public ClasspathPreferencesCodec(IPreferenceStore store) {
		this.store = store;
	}

	@Override
	public IPreferenceStore decode(ClasspathPreferences preferences) throws CodecException {
		store.setValue( ArtifactContainerPreferenceConstants.PC_MAVEN_CP_FILE, preferences.getStaticMavenClasspathFile());		
		return store;
	}

	@Override
	public ClasspathPreferences encode(IPreferenceStore store) throws CodecException {
		ClasspathPreferences preferences = ClasspathPreferences.T.create();
		preferences.setStaticMavenClasspathFile( store.getString( ArtifactContainerPreferenceConstants.PC_MAVEN_CP_FILE));
		return preferences;
	}

	@Override
	public Class<IPreferenceStore> getValueClass() {	
		return IPreferenceStore.class;
	}

	
}
