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
package com.braintribe.devrock.greyface.settings.codecs;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.json.genericmodel.GenericModelJsonStringCodec;
import com.braintribe.devrock.greyface.settings.preferences.GreyfacePreferenceConstants;
import com.braintribe.model.malaclypse.cfg.preferences.gf.GreyFacePreferences;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;

public class GreyfacePreferencesCodec implements Codec<IPreferenceStore, GreyFacePreferences> {
	
	private IPreferenceStore store;
	GenericModelJsonStringCodec<List<RemoteRepository>> codec = new GenericModelJsonStringCodec<List<RemoteRepository>>(); 

	public GreyfacePreferencesCodec(IPreferenceStore store) {
		this.store = store;	
	}

	@Override
	public IPreferenceStore decode(GreyFacePreferences preferences) throws CodecException {
		
	 store.setValue( GreyfacePreferenceConstants.PC_TEMPORARY_DIRECTORY, preferences.getTempDirectory());
	 
	 store.setValue( GreyfacePreferenceConstants.PC_FAKE_UPLOAD, preferences.getFakeUpload());
	 store.setValue( GreyfacePreferenceConstants.PC_FAKE_UPLOAD_TARGET, preferences.getFakeUploadTarget());
	 store.setValue( GreyfacePreferenceConstants.PC_ASYNC_SCAN_MODE, preferences.getAsyncScanMode());
	 store.setValue( GreyfacePreferenceConstants.PC_SIMULATE_ERRORS, preferences.getSimulateErrors());

	 store.setValue( GreyfacePreferenceConstants.PC_SETTING_EXCLUDE_OPTIONAL, preferences.getExcludeOptionals());
	 store.setValue( GreyfacePreferenceConstants.PC_SETTING_EXCLUDE_TEST, preferences.getExcludeTest());
	 store.setValue( GreyfacePreferenceConstants.PC_SETTING_EXCLUDE_EXISTING, preferences.getExcludeExisting());
	 store.setValue( GreyfacePreferenceConstants.PC_SETTING_OVERWRITE, preferences.getOverwrite());
	 store.setValue( GreyfacePreferenceConstants.PC_SETTING_REPAIR, preferences.getRepair());
	 store.setValue( GreyfacePreferenceConstants.PC_SETTING_PURGE_POMS, preferences.getPurgePoms());
	 store.setValue( GreyfacePreferenceConstants.PC_SETTING_ACCEPT_COMPILE, preferences.getApplyCompileScope());
	 store.setValue( GreyfacePreferenceConstants.PC_SETTING_ENFORCE_LICENSES, preferences.getEnforceLicenses());
	 store.setValue( GreyfacePreferenceConstants.PC_VALIDATE_POMS, preferences.getValidatePoms());
					
	 store.setValue(GreyfacePreferenceConstants.PC_REPO_SETTINGS, codec.encode(preferences.getSourceRepositories()));
	 
	 store.setValue(GreyfacePreferenceConstants.PC_LAST_SELECTED_TARGET_REPO, preferences.getLastSelectedTargetRepo());
	 return store;
	}

	@Override
	public GreyFacePreferences encode(IPreferenceStore store) throws CodecException {
		GreyFacePreferences gfPreferences = GreyFacePreferences.T.create();
		gfPreferences.setTempDirectory( store.getString( GreyfacePreferenceConstants.PC_TEMPORARY_DIRECTORY));
		
		gfPreferences.setFakeUpload( store.getBoolean( GreyfacePreferenceConstants.PC_FAKE_UPLOAD));
		gfPreferences.setFakeUploadTarget( store.getString( GreyfacePreferenceConstants.PC_FAKE_UPLOAD_TARGET));
		gfPreferences.setAsyncScanMode( store.getBoolean( GreyfacePreferenceConstants.PC_ASYNC_SCAN_MODE));
		gfPreferences.setSimulateErrors( store.getBoolean( GreyfacePreferenceConstants.PC_SIMULATE_ERRORS));
		
		gfPreferences.setExcludeOptionals( store.getBoolean( GreyfacePreferenceConstants.PC_SETTING_EXCLUDE_OPTIONAL));
		gfPreferences.setExcludeTest( store.getBoolean( GreyfacePreferenceConstants.PC_SETTING_EXCLUDE_TEST));
		gfPreferences.setExcludeExisting( store.getBoolean( GreyfacePreferenceConstants.PC_SETTING_EXCLUDE_EXISTING));
		gfPreferences.setOverwrite( store.getBoolean( GreyfacePreferenceConstants.PC_SETTING_OVERWRITE));
		gfPreferences.setPurgePoms( store.getBoolean( GreyfacePreferenceConstants.PC_SETTING_PURGE_POMS));
		gfPreferences.setApplyCompileScope( store.getBoolean( GreyfacePreferenceConstants.PC_SETTING_ACCEPT_COMPILE));
		gfPreferences.setSourceRepositories( codec.decode( store.getString( GreyfacePreferenceConstants.PC_REPO_SETTINGS)));
		gfPreferences.setEnforceLicenses( store.getBoolean( GreyfacePreferenceConstants.PC_SETTING_ENFORCE_LICENSES));
		gfPreferences.setValidatePoms( store.getBoolean( GreyfacePreferenceConstants.PC_VALIDATE_POMS));
		
		gfPreferences.setLastSelectedTargetRepo( store.getString( GreyfacePreferenceConstants.PC_LAST_SELECTED_TARGET_REPO));
						
		return gfPreferences;
	}

	@Override
	public Class<IPreferenceStore> getValueClass() {		
		return IPreferenceStore.class;
	}
	
	

}
