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
package com.braintribe.qa.tribefire.qatests.deployables.access;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.testing.internal.suite.crud.AccessTester;

public class SmoodGenericAccessTest extends AbstractPersistenceTest {

	@Test
	public void test() {
		ImpApi imp = apiFactory().build();

		PersistenceGmSessionFactory factory = apiFactory().buildSessionFactory();

		IncrementalAccess familyAccess = createAndDeployFamilyAccessWithTimestamp(imp);

		logger.info("Created access " + familyAccess.getExternalId());

		AccessTester tester = new AccessTester(familyAccess.getExternalId(), factory, familyAccess.getMetaModel());
		tester.executeTests();
	}

	@Before
	@After
	public void tearDown() {
		eraseTestEntities();
	}
}
