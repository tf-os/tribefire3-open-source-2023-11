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
package com.braintribe.model.access.smood.collaboration.distributed.basic;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.smood.collaboration.deployment.DcsaDeployedUnit;
import com.braintribe.model.access.smood.collaboration.distributed.AbstractDcsaTestBase;
import com.braintribe.model.access.smood.collaboration.distributed.model.DcsaEntity;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativeStageStats;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @author peter.gazdik
 */
public class Dcsa_MergeStage_Correctness_Test extends AbstractDcsaTestBase {

	private DcsaDeployedUnit dcsaUnit1;
	private DcsaDeployedUnit dcsaUnit2;

	@Before
	public void setup() {
		dcsaUnit1 = deployDcsa("access.dcsa", 1);
		dcsaUnit2 = deployDcsa("access.dcsa", 2);
	}

	@After
	public void cleanup() {
		cleanup(dcsaUnit1);
		cleanup(dcsaUnit2);
	}

	@Test
	public void mergeDistributedCorrect_AssertionsByStats() throws Exception {
		// create entity in trunk on DCSA1
		DcsaEntity dcsaEntity1 = dcsaUnit1.session.create(DcsaEntity.T);
		dcsaEntity1.setName("DCSA-JOHN");
		dcsaUnit1.session.commit();

		// check stats before merge in DCSA1
		assertStageInstantiations(dcsaUnit1, "stage0", 0);
		assertStageInstantiations(dcsaUnit1, "trunk", 1);

		// check stats before merge in DCSA2
		assertStageInstantiations(dcsaUnit2, "stage0", 0);
		assertStageInstantiations(dcsaUnit2, "trunk", 1);

		// merge entity from trunk to stage0 in DCSA2
		mergeStage(dcsaUnit2, "trunk", "stage0");

		// check stats after merge in DCSA2
		assertStageInstantiations(dcsaUnit2, "stage0", 1);
		assertStageInstantiations(dcsaUnit2, "trunk", 0);

		// check stats after merge in DCSA1
		assertStageInstantiations(dcsaUnit1, "stage0", 1);
		assertStageInstantiations(dcsaUnit1, "trunk", 0);
	}

	@Test
	public void mergeDistributedCorrect_AssertionsByEntityStage() throws Exception {
		// create entity in trunk on DCSA1
		DcsaEntity dcsaEntity1 = dcsaUnit1.session.create(DcsaEntity.T);
		dcsaEntity1.setName("DCSA-MORPHEUS");
		dcsaUnit1.session.commit();

		// check entity stage in DCSA1
		assertEntityStage(dcsaEntity1, "trunk");

		// merge entity from trunk to stage0 in DCSA2
		mergeStage(dcsaUnit2, "trunk", "stage0");

		// check entity stage in DCSA1
		assertEntityStage(dcsaEntity1, "stage0");
	}

	private void assertEntityStage(DcsaEntity dcsaEntity, String stageName) {
		PersistenceStage stage = dcsaUnit1.csa.findStageForReference(dcsaEntity.reference());
		Assertions.assertThat(stage.getName()).isEqualTo(stageName);
	}

	private void assertStageInstantiations(DcsaDeployedUnit dcsaUnit, String stageName, int number) {
		CollaborativeStageStats stats = getStageStats(dcsaUnit, stageName);
		Assertions.assertThat(stats.getInstantiations()).isEqualTo(number);
	}

	@Override
	protected CollaborativeSmoodConfiguration prepareNewConfiguration() {
		return configForStages("stage0", "trunk");
	}

}
