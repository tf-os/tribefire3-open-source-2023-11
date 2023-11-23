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

import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.ArtifactContainerPreferenceConstants;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.QuickImportPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.VersionModificationAction;

public class QuickImportPreferencesCodec implements Codec<IPreferenceStore, QuickImportPreferences> {
	
	private IPreferenceStore store;

	public QuickImportPreferencesCodec( IPreferenceStore store) {
		this.store = store;		
	}

	@Override
	public IPreferenceStore decode(QuickImportPreferences quickImportPreferences) throws CodecException {		
		Boolean value = quickImportPreferences.getAlternativeUiNature(); 
		store.setValue(ArtifactContainerPreferenceConstants.PC_QI_CONDENSED_IMPORT_STYLE, Boolean.TRUE.equals(value));
		value = quickImportPreferences.getLocalOnlyNature();
		store.setValue(ArtifactContainerPreferenceConstants.PC_QI_CONDENSED_IMPORT_LOCAL, !Boolean.FALSE.equals(value));	
		value = quickImportPreferences.getAttachToCurrentProject();
		store.setValue( ArtifactContainerPreferenceConstants.PC_QI_CONDENSED_IMPORT_ATTACH, Boolean.TRUE.equals(value));
		
		int num = quickImportPreferences.getExpectedNumberOfSources();
		store.setValue(ArtifactContainerPreferenceConstants.PC_QI_NUM_OF_SOURCES, "" + num);
		
		VersionModificationAction copyMode = quickImportPreferences.getLastDependencyCopyMode();
		store.setValue( ArtifactContainerPreferenceConstants.PC_LAST_DEPENDENCY_COPY_MODE, copyMode.toString());
		
		VersionModificationAction pasteMode = quickImportPreferences.getLastDependencyPasteMode();
		store.setValue( ArtifactContainerPreferenceConstants.PC_LAST_DEPENDENCY_PASTE_MODE, pasteMode.toString());
		
		Map<String,String> archetypeMap = quickImportPreferences.getArchetypeToAssetMap();
		if (archetypeMap != null && !archetypeMap.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, String> entry : archetypeMap.entrySet()) {
				if (sb.length() > 0) {
					sb.append(";");
				}
				sb.append( entry.getKey() + ":" + entry.getValue());
			}
			store.setValue( ArtifactContainerPreferenceConstants.PC_ARCHETYPE_MAP, sb.toString());
		}
		
		value = quickImportPreferences.getPrimeWithSelection();
		store.setValue(ArtifactContainerPreferenceConstants.PC_QI_PRIMEWITH_SELECTION, Boolean.TRUE.equals(value));
		return store;
	}

	@Override
	public QuickImportPreferences encode(IPreferenceStore store) throws CodecException {
		QuickImportPreferences quickImportPreferences = QuickImportPreferences.T.create();
		quickImportPreferences.setLocalOnlyNature( store.getBoolean( ArtifactContainerPreferenceConstants.PC_QI_CONDENSED_IMPORT_LOCAL));
		quickImportPreferences.setAlternativeUiNature( store.getBoolean( ArtifactContainerPreferenceConstants.PC_QI_CONDENSED_IMPORT_STYLE));
		quickImportPreferences.setAttachToCurrentProject( store.getBoolean( ArtifactContainerPreferenceConstants.PC_QI_CONDENSED_IMPORT_ATTACH));
		
		quickImportPreferences.setExpectedNumberOfSources( store.getInt(ArtifactContainerPreferenceConstants.PC_QI_NUM_OF_SOURCES));
		
		
		String copyModeAsString = store.getString( ArtifactContainerPreferenceConstants.PC_LAST_DEPENDENCY_COPY_MODE);
		if (copyModeAsString != null) {
			quickImportPreferences.setLastDependencyCopyMode( VersionModificationAction.valueOf( copyModeAsString));
		}
		
		String pasteModeAsString = store.getString( ArtifactContainerPreferenceConstants.PC_LAST_DEPENDENCY_PASTE_MODE);
		if (pasteModeAsString != null) {
			quickImportPreferences.setLastDependencyPasteMode( VersionModificationAction.valueOf( pasteModeAsString));
		}
		
		String archetypeMapAsString = store.getString( ArtifactContainerPreferenceConstants.PC_ARCHETYPE_MAP);
		if (archetypeMapAsString != null && archetypeMapAsString.length() > 0) {
			String [] tokens = archetypeMapAsString.split(",");
			for (String token : tokens) {
				int c = token.indexOf( ':');
				String archetype = token.substring(0, c);
				String tagValue = token.substring(c+1);
				quickImportPreferences.getArchetypeToAssetMap().put( archetype, tagValue);
			}
		}
		
		quickImportPreferences.setPrimeWithSelection( store.getBoolean( ArtifactContainerPreferenceConstants.PC_QI_PRIMEWITH_SELECTION));
		
		return quickImportPreferences;
	}

	@Override
	public Class<IPreferenceStore> getValueClass() {
		return IPreferenceStore.class;
	}

}
