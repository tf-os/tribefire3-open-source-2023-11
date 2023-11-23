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
package com.braintribe.tribefire.scheduling.test;

import org.junit.After;
import org.junit.Before;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.product.rat.imp.ImpApiFactory;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;

import tribefire.extension.scheduling.SchedulingConstants;

public abstract class AbstractSchedulingTest extends AbstractTribefireQaTest {

	public static final String SERVICES_URL = "https://localhost:9443/tribefire-services/";
	public static final String CORTEX_USER = "cortex";
	public static final String CORTEX_PASSWORD = "cortex"; // NOSONAR: it is a test
	public static final String CORTEX_ID = "cortex";

	protected PersistenceGmSessionFactory sessionFactory;

	protected static PersistenceGmSession cortexSession;
	protected PersistenceGmSession schedulingSession;

	protected static ImpApi globalImp;

	// -----------------------------------------------------------------------
	// SETUP & TEARDOWN
	// -----------------------------------------------------------------------

	@Before
	public void before() throws Exception {
		if (globalImp == null) {
			globalImp = ImpApiFactory.with().credentials(CORTEX_USER, CORTEX_PASSWORD).build();
			// globalImp = ImpApiFactory.with().credentials(CORTEX_USER, CORTEX_PASSWORD).url(SERVICES_URL).build();

			cortexSession = globalImp.switchToAccess(CORTEX_ID).session();

		}
		if (sessionFactory == null) {
			sessionFactory = accessId -> globalImp.switchToAccess(accessId).session();
		}

	}

	@After
	public void after() throws Exception {
		// nothing
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	protected PersistenceGmSession getSession() {
		return sessionFactory.newSession(SchedulingConstants.ACCESS_ID);
	}
}
