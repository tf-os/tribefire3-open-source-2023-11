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
package com.braintribe.model.processing.platformsetup;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.ManipulationPriming;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class PlatformSetupManagerTestBase {

	protected static final String PFA_TEST_ACCESSID = "test.platformsetup.smood.access";

	// #####################################
	// ## . . . . . . Helpers . . . . . . ##
	// #####################################

	protected static List<PlatformAsset> createAssets(PersistenceGmSession session, int amount) {
		List<PlatformAsset> assets = new ArrayList<>();
		for (int i = 0; i < amount; i++) {
			PlatformAsset a = session.create(PlatformAsset.T);
			a.setName("" + i);

			session.commit();

			assets.add(a);
		}

		return assets;
	}

	protected static PlatformAssetNature createNature(PersistenceGmSession session, String accessId) {
		ManipulationPriming m = session.create(ManipulationPriming.T);
		m.setAccessId(accessId);
		return m;
	}

}
