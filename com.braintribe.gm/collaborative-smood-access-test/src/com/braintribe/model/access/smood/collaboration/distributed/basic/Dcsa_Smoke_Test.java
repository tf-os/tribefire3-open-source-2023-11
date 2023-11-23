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
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @author peter.gazdik
 */
public class Dcsa_Smoke_Test extends AbstractDcsaTestBase {

	private DcsaDeployedUnit dcsaUnit;
	private PersistenceGmSession session;

	@Before
	public void setup() {
		dcsaUnit = deployDcsa("access.dcsa", 1);
		session = dcsaUnit.session;
	}

	@After
	public void cleanup() {
		cleanup(dcsaUnit);
	}

	@Test
	public void createEntity() throws Exception {
		DcsaEntity dcsaEntity = session.create(DcsaEntity.T);
		dcsaEntity.setName("DCSA");
		session.commit();

		String markerPersistence = dcsaUnit.markerPersistence.get();
		Assertions.assertThat(markerPersistence).isNotNull();
		
		DcsaEntity dcsaEntity2 = session.findEntityByGlobalId(dcsaEntity.getGlobalId());
		Assertions.assertThat(dcsaEntity2).isSameAs(dcsaEntity);
		
		redeploy();

		DcsaEntity dcsaEntity3 = session.findEntityByGlobalId(dcsaEntity.getGlobalId());
		Assertions.assertThat(dcsaEntity3).isNotNull();

	}

	private void redeploy() {
		dcsaUnit = redeploy(dcsaUnit);
		session = dcsaUnit.session;
	}

}
