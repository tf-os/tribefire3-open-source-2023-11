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

import com.braintribe.model.access.collaboration.persistence.DirectGmmlInitializer;
import com.braintribe.model.access.collaboration.persistence.SimpleGmmlInitializer;
import com.braintribe.model.access.smood.collaboration.deployment.CsaBuilder;
import com.braintribe.model.access.smood.collaboration.deployment.CsaDeployedUnit;
import com.braintribe.model.access.smood.collaboration.manager.model.CsaTestModel;
import com.braintribe.model.access.smood.collaboration.manager.model.StagedEntity;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.csa.ManInitializer;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializer;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;

/**
 * @author peter.gazdik
 */
public class SimpleCsaSmokeTest {

	private CsaDeployedUnit csaUnit;

	@Test
	public void testDoesNotCauseFireOrNuclearExplosionOrSimilar() throws Exception {
		csaUnit = deployCsa();

		PersistenceGmSession session = csaUnit.session;

		GmMetaModel manModelEntity = session.findEntityByGlobalId("test.smoke.SimpleSmokeTestModel");
		checkEntity(manModelEntity, "ManipulationStage");

		Resource manDataEntity = session.findEntityByGlobalId("resource.smoke.test");
		checkEntity(manDataEntity, "ManipulationStage");

		// pre
		GmMetaModel staticModelEntity = session.findEntityByGlobalId("static.SimpleSmokeTestModel");
		checkEntity(staticModelEntity, SmokeTestStaticInitializer.class.getName());

		StagedEntity staticStagedEntity = session.findEntityByGlobalId("static.StagedEntity");
		checkEntity(staticStagedEntity, SmokeTestStaticInitializer.class.getName());

		// post
		GmMetaModel postModelEntity = session.findEntityByGlobalId("post.SimpleSmokeTestModel");
		checkEntity(postModelEntity, "env-file:model");

		StagedEntity postStagedEntity = session.findEntityByGlobalId("post.StagedEntity");
		checkEntity(postStagedEntity, "env-file:data");

		StagedEntity postDirectStagedEntity = session.findEntityByGlobalId("post.direct.StagedEntity");
		checkEntity(postDirectStagedEntity, "env:direct");
	}

	private CsaDeployedUnit deployCsa() {
		return CsaBuilder.create() //
				.baseFolder(new File("res/SimpleCsaSmokeTest")) //
				.cortex(true) //
				.configurationSupplier(this::prepareNewConfiguration) // CONFIG
				.staticInitializers(staticInitializers()) // PRE
				.staticPostInitializers(staticPostInitializers()) // POST
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
		ManInitializer manInitializer = ManInitializer.T.create();
		manInitializer.setName("ManipulationStage");

		CollaborativeSmoodConfiguration result = CollaborativeSmoodConfiguration.T.create();
		result.getInitializers().addAll(asList( //
				manInitializer //
		));

		return result;
	}

	// ##########################################
	// ## . . . . . . . . PRE . . . . . . . . .##
	// ##########################################

	private List<PersistenceInitializer> staticInitializers() {
		return asList(new SmokeTestStaticInitializer());
	}

	private static class SmokeTestStaticInitializer extends SimplePersistenceInitializer {

		@Override
		public void initializeModels(PersistenceInitializationContext context) throws ManipulationPersistenceException {
			ManagedGmSession session = context.getSession();
			session.create(GmMetaModel.T, "static.SimpleSmokeTestModel");
		}
		@Override
		public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
			ManagedGmSession session = context.getSession();
			session.create(StagedEntity.T, "static.StagedEntity");
		}
	}

	// ##########################################
	// ## . . . . . . . . POST . . . . . . . . ##
	// ##########################################

	private List<PersistenceInitializer> staticPostInitializers() {
		return asList( //
				gmmlInitializer("env-file:model", "res/SimpleCsaSmokeTest/PostStage/model.man"), //
				gmmlInitializer("env-file:data", "res/SimpleCsaSmokeTest/PostStage/data.man"), //
				directDataInitializer());
	}

	private SimpleGmmlInitializer gmmlInitializer(String stageName, String fileName) {
		SimpleGmmlInitializer result = new SimpleGmmlInitializer();
		result.setStageName(stageName);
		result.setGmmlFile(new File(fileName));

		return result;
	}

	private DirectGmmlInitializer directDataInitializer() {
		DirectGmmlInitializer result = new DirectGmmlInitializer();
		result.setStageName("env:direct");
		result.setDataManSupplier(this::directDataGmmlScript);

		return result;
	}

	private String directDataGmmlScript() {
		return "$0=(StagedEntity=com.braintribe.model.access.smood.collaboration.manager.model.StagedEntity)() " + //
				".globalId='post.direct.StagedEntity'";
	}
}
