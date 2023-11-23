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
package com.braintribe.qa.tribefire.qatests.cartridges;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.qa.cartridge.main.model.data.Address;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;

/**
 * This class tests the creation and persistence of a {@code SmoodAccess} based on the {@code DataModel}, which was
 * synchronized from the {@code MainQaCartridge}.
 *
 *
 */
public class CartridgeModelSmoodPersistenceTest extends AbstractTribefireQaTest {

	protected final String DATA_MODEL_NAME = Address.T.getModel().name();

	@Before
	@After
	public void cleanup() {
		eraseTestEntities();
	}

	@Test
	public void testPersistenceModelWithSmood() {
		logger.info("Starting DevQA-test: cartridge detection and cartridge model synchronization and persist instances to Smood access ...");

		ImpApi imp = apiFactory().build();

		GmMetaModel dataModel = imp.model(DATA_MODEL_NAME).get();
		String accessName = nameWithTimestamp("Access");
		CollaborativeSmoodAccess smoodAccess = imp.deployable().access().createCsa(accessName, accessName, dataModel).get();
		imp.commit();
		imp.service().deployRequest(smoodAccess).call();

		PersistenceGmSession crudSession = apiFactory().newSessionForAccess(smoodAccess.getExternalId());
		PersistenceGmSession accessCheckerSession = apiFactory().newSessionForAccess(smoodAccess.getExternalId());

		Address address = crudSession.create(Address.T);
		String street = "Address " + smoodAccess.getExternalId();
		address.setStreet(street);
		crudSession.commit();

		SelectQuery selectQuery = new SelectQueryBuilder().from(Address.class, "a").done();
		List<Address> result = accessCheckerSession.query().select(selectQuery).list();

		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0).getStreet()).isEqualTo(street);

		logger.info("All assertions have completed succefully!");
		logger.info("Completed DevQA-test: cartridge detection and cartridge model synchronization and persist instances to Smood access.");
	}
}
