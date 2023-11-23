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
import com.braintribe.model.access.smood.collaboration.manager.model.StagedEntity;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativeStageStats;
import com.braintribe.model.cortexapi.access.collaboration.GetCollaborativeStageStats;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @see GetCollaborativeStageStats
 * @see CollaborativeAccessManager
 *
 * @author peter.gazdik
 */
public class GetStageStatsCsaTest extends AbstractCollaborativeAccessManagerTest {

	final String newStageName = "newStage";

	@Test
	public void emptyIsReallyEmpty() {
		CollaborativeStageStats stats = getStageStats(trunkStageName);
		assertStatsIsEmpty(stats);
	}

	@Test
	public void withEntityIsNotEmpty() {
		StagedEntity e = session.create(StagedEntity.T);
		e.setName("e");
		session.commit();

		CollaborativeStageStats stats = getStageStats(trunkStageName);
		assertStats(stats, 1, 2 /* name + globalId */, 0);
	}

	@Test
	public void deleteInNewStage() {
		StagedEntity e = session.create(StagedEntity.T);
		session.commit();

		pushNewStage(newStageName);
		session.deleteEntity(e);
		session.commit();

		CollaborativeStageStats stats = getStageStats(newStageName);
		assertStats(stats, 0, 0, 1);
	}

	public static void assertStatsIsEmpty(CollaborativeStageStats stats) {
		Assertions.assertThat(stats.isEmpty()).isTrue();
	}

	public static void assertStats(CollaborativeStageStats stats, int instantiations, int updates, int deletes) {
		Assertions.assertThat(stats.getInstantiations()).isEqualTo(instantiations);
		Assertions.assertThat(stats.getUpdates()).isEqualTo(updates);
		Assertions.assertThat(stats.getDeletes()).isEqualTo(deletes);
	}


}
