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
package com.braintribe.devrock.mungojerry.preferences;

import org.eclipse.jface.preference.IPreferenceStore;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.model.malaclypse.cfg.preferences.mj.MungojerryPreferences;

public class MungojerryPreferencesCodec implements Codec<IPreferenceStore, MungojerryPreferences> {

	private IPreferenceStore store;
	private GwtPreferencesCodec gwtPreferencesCodec;
	private MavenPreferencesCodec mvPreferencesCodec;

	public MungojerryPreferencesCodec(IPreferenceStore store) {
		this.store = store;
		gwtPreferencesCodec = new GwtPreferencesCodec(store);
		mvPreferencesCodec = new MavenPreferencesCodec(store);

	}

	@Override
	public IPreferenceStore decode(MungojerryPreferences mjPreferences) throws CodecException {
		// gwt		
		gwtPreferencesCodec.decode( mjPreferences.getGwtPreferences());
		mvPreferencesCodec.decode(mjPreferences.getMavenPreferences());
		return store;
	}

	@Override
	public MungojerryPreferences encode(IPreferenceStore store) throws CodecException {
		MungojerryPreferences mjPreferences = MungojerryPreferences.T.create();
		mjPreferences.setGwtPreferences( gwtPreferencesCodec.encode(store));
		mjPreferences.setMavenPreferences( mvPreferencesCodec.encode(store));
		return mjPreferences;
	}

	@Override
	public Class<IPreferenceStore> getValueClass() {
		return IPreferenceStore.class;
	}

	
}
