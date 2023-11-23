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
package com.braintribe.model.access.smood.collaboration.smoke;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.braintribe.model.access.smood.collaboration.deployment.CsaBuilder;
import com.braintribe.model.access.smood.collaboration.deployment.CsaDeployedUnit;
import com.braintribe.model.access.smood.collaboration.manager.model.CsaTestModel;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializer;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;

/**
 * @author peter.gazdik
 */
public class CortexCsa_NonSplitting_SmokeTest {

	private CsaDeployedUnit csaUnit;

	private static final String MODEL_GLOBAL_ID  = "static.SimpleNonSplittingSmokeTestModel";
	private static final String MODEL_NAME = "SimpleNonSplittingSmokeTestModel";

	@Test
	public void startsIfModelStageBuildsUponPreviousInitializerDataStage() throws Exception {
		csaUnit = deployCsa();

		PersistenceGmSession session = csaUnit.session;

		GmMetaModel staticModelEntity = session.findEntityByGlobalId(MODEL_GLOBAL_ID);
		checkEntity(staticModelEntity, ModelInitializer1.class.getName());

	}

	private CsaDeployedUnit deployCsa() {
		return CsaBuilder.create() //
				.baseFolder(new File("res/SimpleCsaSmokeTest")) //
				.cortex(true) //
				.mergeModelAndData(true) //
				.configurationSupplier(this::prepareNewConfiguration) // CONFIG
				.staticInitializers(staticInitializers()) // PRE
				.model(CsaTestModel.raw()) //
				.done();
	}

	private void checkEntity(GenericEntity entity, String expectedStageName) {
		assertThat(entity).isNotNull();

		PersistenceStage persitenceStage = csaUnit.csa.findStageForReference(entity.reference());
		assertThat(persitenceStage).isNotNull();
		assertThat(persitenceStage.getName()).isEqualTo(expectedStageName);
	}

	// ##########################################
	// ## . . . . . . . CONFIG . . . . . . . . ##
	// ##########################################

	private CollaborativeSmoodConfiguration prepareNewConfiguration() {
		return CollaborativeSmoodConfiguration.T.create();
	}

	// ##########################################
	// ## . . . . . . . . PRE . . . . . . . . .##
	// ##########################################

	private List<PersistenceInitializer> staticInitializers() {
		return asList(new ModelInitializer1(), new ModelInitializer2());
	}

	private static class ModelInitializer1 extends SimplePersistenceInitializer {


		@Override
		public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
			ManagedGmSession session = context.getSession();
			session.create(GmMetaModel.T, MODEL_GLOBAL_ID);
		}
	}

	private static class ModelInitializer2 extends SimplePersistenceInitializer {


		@Override
		public void initializeModels(PersistenceInitializationContext context) throws ManipulationPersistenceException {
			ManagedGmSession session = context.getSession();
			GmMetaModel model = session.getEntityByGlobalId(MODEL_GLOBAL_ID);
			model.setName(MODEL_NAME);
		}

	}

}
