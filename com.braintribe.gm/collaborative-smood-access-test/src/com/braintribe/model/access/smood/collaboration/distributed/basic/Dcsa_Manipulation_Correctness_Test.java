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

import static com.braintribe.model.generic.manipulation.DeleteMode.ignoreReferences;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.BiConsumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.smood.collaboration.deployment.DcsaDeployedUnit;
import com.braintribe.model.access.smood.collaboration.distributed.AbstractDcsaTestBase;
import com.braintribe.model.access.smood.collaboration.distributed.model.DcsaEntity;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.processing.manipulation.parser.impl.listener.GmmlManipulatorParserListener;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @author peter.gazdik
 */
public class Dcsa_Manipulation_Correctness_Test extends AbstractDcsaTestBase {

	private DcsaDeployedUnit dcsaUnit1;
	private DcsaDeployedUnit dcsaUnit2;

	private PersistenceGmSession session1;
	private PersistenceGmSession session2;

	@Before
	public void setup() {
		dcsaUnit1 = deployDcsa("access.dcsa", 1);
		session1 = dcsaUnit1.session;

		dcsaUnit2 = deployDcsa("access.dcsa", 2);
		session2 = dcsaUnit2.session;
	}

	@After
	public void cleanup() {
		cleanup(dcsaUnit1);
		cleanup(dcsaUnit2);
	}

	@Test
	public void applyManipulationDistributedCorrectly() throws Exception {
		DcsaEntity dcsaEntity1 = session1.create(DcsaEntity.T);
		dcsaEntity1.setName("DCSA");
		session1.commit();

		DcsaEntity dcsaEntity2 = session2.findEntityByGlobalId(dcsaEntity1.getGlobalId());
		Assertions.assertThat(dcsaEntity2).isNotNull();
		Assertions.assertThat(dcsaEntity2.getName()).isEqualTo(dcsaEntity1.getName());
	}

	/**
	 * When deleting entities by {@link GmmlManipulatorParserListener#exitDeleteManipulation}, it for performance reasons important that the entities
	 * created in the relevant stage are provided. This tests that the correct delete manipulation mode was picked.
	 * <p>
	 * In the future we should add a test to see if dropReferences is also picked when needed!!!
	 */
	@Test
	public void updateWithDeleteManipulationsDoesNotQueryForReferences() throws Exception {
		// create in DCSA1
		DcsaEntity dcsaEntity1 = session1.create(DcsaEntity.T);
		dcsaEntity1.setName("DCSA");
		session1.commit();

		// update DCSA2
		DcsaEntity dcsaEntity2 = session2.findEntityByGlobalId(dcsaEntity1.getGlobalId());
		Assertions.assertThat(dcsaEntity2).isNotNull();
		Assertions.assertThat(dcsaEntity2.getName()).isEqualTo(dcsaEntity1.getName());

		// delete in DCSA1
		session1.deleteEntity(dcsaEntity1);
		session1.commit();

		dcsaUnit2.csa.getSmoodSession().deleteEntityInterceptor = this::throwExceptionOnDelete;

		// update DCSA2
		PersistenceGmSession newSession2 = dcsaUnit2.newSession();
		List<?> list2 = newSession2.query().entities(EntityQueryBuilder.from(DcsaEntity.T).done()).list();
		assertThat(list2).isEmpty();
	}

	private void throwExceptionOnDelete(GenericEntity entity, DeleteMode mode, BiConsumer<GenericEntity, DeleteMode> superDeletor) {
		if (mode != ignoreReferences)
			throw new RuntimeException("Deleting entity that was created in this same stage should use " + ignoreReferences + " mode, not: " + mode);
		superDeletor.accept(entity, mode);
	}

}
