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
import com.braintribe.model.malaclypse.cfg.preferences.gwt.GwtPreferences;

public class GwtPreferencesCodec implements Codec<IPreferenceStore, GwtPreferences> {
	
	private IPreferenceStore store;

	public GwtPreferencesCodec(IPreferenceStore store) {
		this.store = store;
	}

	@Override
	public IPreferenceStore decode(GwtPreferences preferences) throws CodecException {		

		String gwtLibrary = preferences.getAutoInjectLibrary();
		store.setValue(ArtifactContainerPreferenceConstants.PC_GWT_AUTOINJECT, gwtLibrary);
		return store;
	}

	@Override
	public GwtPreferences encode(IPreferenceStore store) throws CodecException {		
		GwtPreferences preferences = GwtPreferences.T.create();
		preferences.setAutoInjectLibrary( store.getString( ArtifactContainerPreferenceConstants.PC_GWT_AUTOINJECT));				
		return preferences;
	}

	@Override
	public Class<IPreferenceStore> getValueClass() {	
		return IPreferenceStore.class;
	}

	
}
