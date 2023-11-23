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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.collaboration.CollaborativeAccessManager;
import com.braintribe.model.access.smood.collaboration.manager.model.StagedEntity;
import com.braintribe.model.cortexapi.access.collaboration.MergeCollaborativeStage;
import com.braintribe.model.csa.SmoodInitializer;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @see MergeCollaborativeStage
 * @see CollaborativeAccessManager
 *
 * @author peter.gazdik
 */
public abstract class MergeStageCsaTest_Common extends AbstractCollaborativeAccessManagerTest {

	protected static final String oldStageName = "oldStage";

	protected StagedEntity oldStageEntity;

	@Before
	public void prePopulateOldStageAndCreateNewTrunk() {
		oldStageEntity = session.create(StagedEntity.T);
		session.commit();

		renameStage(trunkStageName, oldStageName);
		pushNewStage(trunkStageName);
	}

	@Test
	public void mergeEmptyStage() {
		// Trunk
		// <EMPTY>

		// Merge
		mergeStage(trunkStageName, oldStageName);

		// Assertions
		assertOldOnly_DataOnly();
		assertEntityStage(oldStageEntity, oldStageName);
	}

	@Test
	public void mergeSimpleData() {
		// Trunk
		StagedEntity trunkEntity = session.create(StagedEntity.T);
		session.commit();

		// Merge
		mergeStage(trunkStageName, oldStageName);

		// Assertions
		assertOldDataFileContains("$0", "$1");

		assertOldOnly_DataOnly();
		assertEntityStage(trunkEntity, oldStageName);
	}

	@Test
	public void mergeSimpleDataMultipleTimes() {
		// Trunk
		StagedEntity trunk_E1 = session.create(StagedEntity.T);
		session.commit();

		// Merge
		mergeStage(trunkStageName, oldStageName);

		StagedEntity trunk_E2 = session.create(StagedEntity.T);
		session.commit();

		// Merge
		mergeStage(trunkStageName, oldStageName);

		// Assertions
		assertOldDataFileContains("$0", "$1", "$2");

		assertOldOnly_DataOnly();
		assertEntityStage(trunk_E1, oldStageName);
		assertEntityStage(trunk_E2, oldStageName);
	}

	@Test
	public void nonTrunkStageIsDeletedAfterMerge() {
		final String newerStageName = "newerStage";

		// Trunk (will later be newerStage)
		StagedEntity trunk_E1 = session.create(StagedEntity.T);
		session.commit();

		//
		renameStage(trunkStageName, newerStageName);
		pushNewStage(trunkStageName);

		// Merge
		mergeStage(newerStageName, oldStageName);

		// Assertions
		assertEntityStage(trunk_E1, oldStageName);

		// @formatter:off
		baseFolderFsAssert
			.sub(oldStageName).isDirectory()
				.sub("data.man").isExistingFile_()
				.sup()
			.sub(newerStageName).notExists_()
			.sub(trunkStageName).isEmptyDirectory()
			;
		// @formatter:on

		List<SmoodInitializer> initializers = getInitializers();
		assertThat(initializers).hasSize(2);

		Iterator<SmoodInitializer> it = initializers.iterator();
		assertThat(it.next().getName()).isEqualTo(oldStageName);
		assertThat(it.next().getName()).isEqualTo(trunkStageName);
	}

	// #################################################
	// ## . . . . . . . . . Helpers . . . . . . . . . ##
	// #################################################

	protected SelectQuery queryStagedEntityByName(String name) {
		// @formatter:off
		return new SelectQueryBuilder()
				.from(StagedEntity.T, "e")
				.where()
					.property("e", "name").eq(name)
				.done();
		// @formatter:on
	}

	protected void assertOldOnly_DataOnly() {
		// @formatter:off
		baseFolderFsAssert
			.sub(oldStageName).isDirectory()
				.sub("data.man").isExistingFile_()
				.sup()
			.sub(trunkStageName).isEmptyDirectory()
			;
		// @formatter:on
	}

	protected void assertOldOnly_ModelAndData() {
		// @formatter:off
		baseFolderFsAssert
			.sub(oldStageName).isDirectory()
				.sub("data.man").isExistingFile_()
				.sub("model.man").isExistingFile_()
			.sup()
			.sub(trunkStageName).isEmptyDirectory()
			;
		// @formatter:on
	}

	protected void assertOldDataFileContains(String... values) {
		String oldDataFileContent = getStageFileContent(oldStageName, false);
		Assertions.assertThat(oldDataFileContent).containsAll(values);
	}

	protected void assertOldDataFileNotContains(String... values) {
		String oldDataFileContent = getStageFileContent(oldStageName, false);
		Assertions.assertThat(oldDataFileContent).doesNotContain(asList(values));
	}

}
