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
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.model.malaclypse.cfg.preferences.ac.container.DynamicContainerPreferences;

public class DynamicContainerPreferencesCodec implements Codec<IPreferenceStore, DynamicContainerPreferences> {
	private IPreferenceStore store;

	public DynamicContainerPreferencesCodec(IPreferenceStore store) {
		this.store = store;
	}

	@Override
	public IPreferenceStore decode(DynamicContainerPreferences preferences) throws CodecException {
		store.setValue( ArtifactContainerPreferenceConstants.PC_MAX_CONCURRENT_WALK_BATCH_SIZE, preferences.getConcurrentWalkBatchSize());
		ResolvingInstant instant = preferences.getClashResolvingInstant();
		store.setValue( ArtifactContainerPreferenceConstants.PC_CLASH_RESOLVING_INSTANT, instant.toString());
		store.setValue( ArtifactContainerPreferenceConstants.PC_CHAIN_WALKS, preferences.getChainArtifactSync());
		return store;
	}

	@Override
	public DynamicContainerPreferences encode(IPreferenceStore store) throws CodecException {
		DynamicContainerPreferences preferences = DynamicContainerPreferences.T.create();
		preferences.setConcurrentWalkBatchSize( store.getInt( ArtifactContainerPreferenceConstants.PC_MAX_CONCURRENT_WALK_BATCH_SIZE));
		
		String resolvingInstantAsString = store.getString( ArtifactContainerPreferenceConstants.PC_CLASH_RESOLVING_INSTANT);
		if (resolvingInstantAsString != null && resolvingInstantAsString.length() > 0) {
			preferences.setClashResolvingInstant( ResolvingInstant.valueOf(resolvingInstantAsString));
		}		
		preferences.setChainArtifactSync( store.getBoolean( ArtifactContainerPreferenceConstants.PC_CHAIN_WALKS));
		
		return preferences;
	}

	@Override
	public Class<IPreferenceStore> getValueClass() {
		return IPreferenceStore.class;
	}

}
