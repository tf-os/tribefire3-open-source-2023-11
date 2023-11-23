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
package com.braintribe.model.access.smood.collaboration.basic;

import static com.braintribe.testing.junit.assertions.gm.assertj.core.api.GmAssertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Test;

import com.braintribe.model.access.smood.collaboration.deployment.AbstractCsaDeployedUnit;
import com.braintribe.model.access.smood.collaboration.manager.model.StagedEntity;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeAccess;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.testing.junit.assertions.assertj.core.api.FileSystemAssert;

/**
 * 
 * @see CollaborativeAccess
 * 
 * @author peter.gazdik
 */
public class SimpleCsaTest extends AbstractCollaborativePersistenceTest {

	/**
	 * We simply create a Collaborative setup and test that we have wired it correctly. So just check that after creating an entity the persistence
	 * resources have the right structure.
	 */
	@Test
	public void singleStageInitialization() {
		session.create(StagedEntity.T);
		session.commit();

		// @formatter:off
		baseFolderFsAssert
			.sub("config.json").isExistingFile_()
			.sub(trunkStageName).isDirectory()
				.sub("data.man").isExistingFile_()
				.sub("marker.txt").notExists_();
		// @formatter:on
	}

	/** Here we test that we can actually read the configuration written with our setup. */
	@Test
	public void singleStageInitialization_AndRedeployment() {
		StagedEntity entity = session.create(StagedEntity.T);
		session.commit();

		redeploy();

		GenericEntity entity2 = session.query().entity(entity.entityType(), entity.getId()).require();
		Assertions.assertThat(entity2).isNotSameAs(entity);
	}

	/** Just test that the entity has a correct stage. */
	@Test
	public void entityStageIsRecognized() {
		StagedEntity entity = session.create(StagedEntity.T);
		session.commit();

		assertEntityStage(entity, AbstractCsaDeployedUnit.trunkStageName);
	}

	@Test
	public void globalIdCanBeAssigned() {
		StagedEntity entity = session.create(StagedEntity.T);
		entity.setGlobalId("stagedEntity");
		session.commit();
	}

	@Test(expected = IllegalArgumentException.class)
	public void globalIdCannotBeChanged() {
		StagedEntity entity = session.create(StagedEntity.T);
		entity.setGlobalId("stagedEntity");
		session.commit();

		entity.setGlobalId("otherId");
		session.commit();
	}

	@Test
	public void propertyReAssignmentIsNotTracked() {
		StagedEntity entity = session.create(StagedEntity.T);
		entity.setName("name");
		session.commit();

		FileSystemAssert trunkDataFsa = baseFolderFsAssert.sub(trunkStageName).sub("data.man");
		long sizeBeforeReAssignment = trunkDataFsa.toFile().length();

		entity.setName("name");
		session.commit();

		trunkDataFsa.hasSize(sizeBeforeReAssignment);
	}

	@Test
	public void getCreatedEntities() {
		StagedEntity e = session.create(StagedEntity.T);
		session.commit();

		Set<?> entities = csa.getCreatedEntitiesForStage("trunk");
		GenericEntity internalE = first(entities);

		assertThat(entities).hasSize(1);
		assertThat(csa.getStageForReference(internalE.reference())).isNotNull();

		session.deleteEntity(e);
		session.commit();

		assertThat(csa.getCreatedEntitiesForStage("trunk")).isEmpty();
		assertThat(csa.findStageForEntity(internalE)).isNull();
	}

	@Test
	public void deletionIgnoringReferences_WorksIfNotReferenced() throws Exception {
		StagedEntity e = session.create(StagedEntity.T);
		e.setGlobalId("SE");
		session.commit();

		redeploy();

		e = session.findEntityByGlobalId("SE");
		assertThat(e).isNotNull();

		session.deleteEntity(e, DeleteMode.ignoreReferences);
		session.commit();
	}

	@Test
	public void deletionIgnoringReferencesFailsIfReferenced() throws Exception {
		StagedEntity e1 = session.create(StagedEntity.T);
		StagedEntity e2 = session.create(StagedEntity.T);
		e1.setGlobalId("SE1");
		e2.setGlobalId("SE2");

		e1.setEntity(e2);
		session.commit();

		redeploy();

		e2 = session.findEntityByGlobalId("SE2");
		assertThat(e2).isNotNull();

		try {
			session.deleteEntity(e2, DeleteMode.ignoreReferences);
			session.commit();
			fail("Exception was expected, entity SE1 cannot be deleted as it is referenced by SE1.entity!!!");

		} catch (Exception e) {
			// empty
		}
	}

}
