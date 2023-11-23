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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

import com.braintribe.model.access.smood.collaboration.deployment.CsaBuilder;
import com.braintribe.model.access.smood.collaboration.deployment.CsaDeployedUnit;
import com.braintribe.model.access.smood.collaboration.manager.model.CsaTestModel;
import com.braintribe.model.access.smood.collaboration.tools.CsaTestTools;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;

/**
 * @author peter.gazdik
 */
public class CorruptManFileCsaSmokeTest {

	/* Note the man files don't contain line-breaks but spaces, so the marker is valid for both Windows and Linux */
	private static final File PROTOTYPE_FOLDER = new File("res/CorruptManFilesCsaSmokeTest");

	private CsaDeployedUnit csaUnit;

	@Test
	public void trimsManFilesBeforeStartingUp() throws Exception {
		csaUnit = deployCsa();

		PersistenceGmSession session = csaUnit.session;

		GmMetaModel manModelEntity = session.findEntityByGlobalId("test.smoke.SimpleSmokeTestModel");
		checkEntity(manModelEntity, "trunk");

		Resource manDataEntity = session.findEntityByGlobalId("resource.smoke.test");
		checkEntity(manDataEntity, "trunk");
	}

	private CsaDeployedUnit deployCsa() throws Exception {
		File workingFolder = CsaTestTools.createWorkingFolder(PROTOTYPE_FOLDER);

		return CsaBuilder.create() //
				.baseFolder(workingFolder) //
				.cortex(true) //
				.model(CsaTestModel.raw()) //
				.done();
	}

	private void checkEntity(GenericEntity entity, String expectedStageName) {
		assertThat(entity).isNotNull();

		PersistenceStage persitenceStage = csaUnit.csa.findStageForReference(entity.reference());
		assertThat(persitenceStage).isNotNull();
		assertThat(persitenceStage.getName()).isEqualTo(expectedStageName);
	}

}
