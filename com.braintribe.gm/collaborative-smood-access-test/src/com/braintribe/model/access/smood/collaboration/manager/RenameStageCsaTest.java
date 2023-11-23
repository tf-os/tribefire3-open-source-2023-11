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
package com.braintribe.model.access.smood.collaboration.manager;

import org.junit.Test;

import com.braintribe.model.access.collaboration.CollaborativeAccessManager;
import com.braintribe.model.access.collaboration.persistence.AbstractManipulationPersistence;
import com.braintribe.model.access.smood.collaboration.manager.model.StagedEntity;
import com.braintribe.model.cortexapi.access.collaboration.RenameCollaborativeStage;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @see RenameCollaborativeStage
 * @see CollaborativeAccessManager
 *
 * @author peter.gazdik
 */
public class RenameStageCsaTest extends AbstractCollaborativeAccessManagerTest {

	final String newStageName = "new:Stage";
	final String newStageFolderName = AbstractManipulationPersistence.toLegalFileName(newStageName);

	/**
	 * Test the rename functionality - renaming the only existing stage.
	 */
	@Test
	public void renameStage() {
		StagedEntity entity = session.create(StagedEntity.T);
		session.commit();

		renameTrunk();

		assertEntityStage(entity, newStageName);

		// @formatter:off
		baseFolderFsAssert
			.sub("config.json").isExistingFile_()
			.sub(newStageFolderName).isDirectory()
				.sub("data.man").isExistingFile_();
		// @formatter:on
	}

	/**
	 * Check that we have updated the current stage correctly so that new entities will have the new stage as their
	 * current stage.
	 */
	@Test
	public void renameStage_NewEntitiesHaveNewStageName() {
		StagedEntity entity = session.create(StagedEntity.T);
		session.commit();

		renameTrunk();

		StagedEntity entity2 = session.create(StagedEntity.T);
		session.commit();

		assertEntityStage(entity, newStageName);
		assertEntityStage(entity2, newStageName);
	}

	/** The point is to have a stage with "problematic" name that is also being renamed. */
	@Test
	public void renameStage_Twice() {
		StagedEntity entity = session.create(StagedEntity.T);
		session.commit();

		renameTrunk();

		StagedEntity entity2 = session.create(StagedEntity.T);
		session.commit();

		String newerStagename = "newer:Stage";

		renameStage(newStageName, newerStagename);

		assertEntityStage(entity, newerStagename);
		assertEntityStage(entity2, newerStagename);
	}

	/**
	 * By re-deploying we check that we have persisted the configuration correctly - cause we know from
	 * {@link #renameStage()} that the file system structure was changed.
	 */
	@Test
	public void renameStage_ConfigurationPersistedCorrectly() {
		StagedEntity entity = session.create(StagedEntity.T);
		session.commit();

		renameTrunk();

		redeploy();

		// Now if we have read the property configuration the query works, but gives us a different entity, of course.
		GenericEntity entity2 = session.query().entity(entity.entityType(), entity.getId()).require();
		Assertions.assertThat(entity2).isNotSameAs(entity);
	}

	@Test(expected = GenericModelException.class)
	public void cannotRenameNonExistentStage() {
		renameStage("non-existing", "newName");
	}

	@Test
	public void cannotRenameToAnAlreadyExistingStage() {
		session.create(StagedEntity.T);
		session.commit();

		pushNewStage(newStageName);

		try {
			renameStage(newStageName, trunkStageName);
			Assertions.fail("Exception should have been thrown");

		} catch (Exception e) {
			Assertions.assertThat(e.getMessage()).contains("stage already exists");
		}
	}

	private void renameTrunk() {
		renameStage(trunkStageName, newStageName);
	}

}
