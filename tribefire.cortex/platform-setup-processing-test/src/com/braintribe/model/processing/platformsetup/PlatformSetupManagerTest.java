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

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.ManipulationPriming;
import com.braintribe.model.asset.natures.ModelPriming;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.platformsetup.api.request.MergeTrunkAsset;
import com.braintribe.model.platformsetup.api.request.TransferAsset;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.product.rat.imp.ImpApiFactory;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * Tests {@link PlatformSetupManager}.
 * 
 * @author christina.wilpernig
 */
//@Category(TribefireServices.class)
@Ignore // TODO
public class PlatformSetupManagerTest extends PlatformSetupManagerTestBase {

	private static final Logger log = Logger.getLogger(PlatformSetupManagerTest.class);
	private static PersistenceGmSession pfaSession = null;
	
	@BeforeClass
	public static void initialize() throws Exception {
		
		log.info("Preparing " + PlatformSetupManager.class.getSimpleName() + " related tests...");
		
		ImpApi imp = ImpApiFactory.with().build();
		
		// Create test smood access
		GmMetaModel platformSetupModel = imp.model("tribefire.cortex:platform-setup-model").get();
		GmMetaModel platformSetupServiceModel = imp.model("tribefire.cortex:platform-setup-service-model").get();
		CollaborativeSmoodAccess smoodAccess = imp.deployable().access()
					.createCsa("Smood Platform Setup Access", PFA_TEST_ACCESSID, platformSetupModel, platformSetupServiceModel).get();

		imp.deployable(smoodAccess).commitAndRedeploy();
		pfaSession = imp.switchToAccess(PFA_TEST_ACCESSID).session();

		log.info("Prepared " + PlatformSetupManager.class.getSimpleName() + " test configuration.");
	}
	
	// ###################################################
	// ## . . . . . . MergeTrunkAsset Tests . . . . . . ##
	// ###################################################

	@Test
	public void merge() {
		List<PlatformAsset> assets = createAssets(pfaSession, 1);
		
		EntityQuery query = EntityQueryBuilder.from(PlatformAsset.T).done();
		List<Object> pfassets = pfaSession.query().entities(query).list();
		
		Assertions.assertThat(pfassets).isNotNull();
		
		MergeTrunkAsset m = MergeTrunkAsset.T.create();
		m.setAsset(assets.get(0));
		
		m.eval(pfaSession).get();
	}

	// ##################################################
	// ## . . . . . . Install/Deploy Tests . . . . . . ##
	// ##################################################
	
	@Test
	public void deployAssetTest() {
		List<PlatformAsset> assets = createAssets(pfaSession, 1);
		
		EntityQuery query = EntityQueryBuilder.from(PlatformAsset.T).done();
		List<Object> pfassets = pfaSession.query().entities(query).list();
		
		Assertions.assertThat(pfassets).isNotNull();
		
		TransferAsset transfer = TransferAsset.T.create();
		transfer.setAsset(assets.get(0));
		transfer.setTransferOperation("deploy");
		
		transfer.eval(pfaSession).get();
	}
	

	// ###################################################
	// ## . . . . . . CloseTrunkAsset Tests . . . . . . ##
	// ###################################################
	
	@Test
	public void testNatureRecordings() {
		System.out.println(NatureRecording.stringify(getModelNature()));
		System.out.println("---");
		System.out.println(NatureRecording.stringify(getManNature()));
		
		// TODO (cwi) assertions
	}
	
	private static PlatformAssetNature getModelNature() {
		ModelPriming modelInitializer = ModelPriming.T.create();
		//modelInitializer.setConflictPriority(0.5);
		return modelInitializer;
	}
	
	private static PlatformAssetNature getManNature() {
		ManipulationPriming manInitializer = ManipulationPriming.T.create();
		manInitializer.setAccessId("foobar");
		return manInitializer;
	}
		
}
