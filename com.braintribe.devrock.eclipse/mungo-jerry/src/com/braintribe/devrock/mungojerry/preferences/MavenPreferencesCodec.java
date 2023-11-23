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
import com.braintribe.model.malaclypse.cfg.preferences.mv.MavenPreferences;

public class MavenPreferencesCodec implements Codec<IPreferenceStore, MavenPreferences> {
	
	private IPreferenceStore store;

	public MavenPreferencesCodec(IPreferenceStore store) {
		this.store = store;
	}

	@Override
	public IPreferenceStore decode(MavenPreferences preferences) throws CodecException {		
		String confSettingsOverride = preferences.getConfSettingsOverride();
		if (confSettingsOverride == null)
			confSettingsOverride = "";
		store.setValue( MungojerryPreferencesConstants.PC_MAVEN_CONF_OVERRIDE, confSettingsOverride);
		
		String userSettingsOverride = preferences.getUserSettingsOverride();
		if (userSettingsOverride == null)
			userSettingsOverride = "";
		store.setValue( MungojerryPreferencesConstants.PC_MAVEN_USER_OVERRIDE, userSettingsOverride);
		
		String localRepositoryOverride = preferences.getLocalRepositoryOverride();
		if (localRepositoryOverride == null)
			localRepositoryOverride = "";
		store.setValue( MungojerryPreferencesConstants.PC_MAVEN_LOCAL_OVERRIDE, localRepositoryOverride);
		
		return store;
	}

	@Override
	public MavenPreferences encode(IPreferenceStore store) throws CodecException {
		MavenPreferences preferences = MavenPreferences.T.create();
		
		String overrideConf = store.getString( MungojerryPreferencesConstants.PC_MAVEN_CONF_OVERRIDE);
		if (overrideConf != null && overrideConf.length() > 0) {
			preferences.setConfSettingsOverride(overrideConf);
		}
		else {
			preferences.setConfSettingsOverride( null);
		}
		
		String overrideUser = store.getString( MungojerryPreferencesConstants.PC_MAVEN_USER_OVERRIDE);
		if (overrideUser != null && overrideUser.length() > 0) {
			preferences.setUserSettingsOverride(overrideUser);
		}
		else {
			preferences.setUserSettingsOverride( null);
		}
		
		String overrideLocal = store.getString( MungojerryPreferencesConstants.PC_MAVEN_LOCAL_OVERRIDE);
		if (overrideLocal != null && overrideLocal.length() > 0) {
			preferences.setLocalRepositoryOverride(overrideLocal);
		}
		else {
			preferences.setLocalRepositoryOverride( null);
		}
		return preferences;
	}

	@Override
	public Class<IPreferenceStore> getValueClass() {
		return IPreferenceStore.class;
	}

	
}
