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
package com.braintribe.swagger.test.utils;

import org.junit.After;
import org.junit.Before;

import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;

/**
 * Creates a new Smood access and provides
 * subclasses with <br>
 * a session to it: {@link #demoAccessSession}<br>
 * a session factory: {@link #factory}<br>
 * a {@link CortexMetaModelManipulationHelper}: {@link #cortex}<br>
 * <br>
 * <br>
 * undeploys the created accesss in the end
 * 
 *
 */
public abstract class AbstractSwaggerImportTest extends AbstractTribefireQaTest {
	public static final String CORTEX_ACCESS_ID = "cortex";

	protected PersistenceGmSession swaggerImportAccessSession;
	protected GmMetaModel configuredDemoModel;
	protected ImpApi globalImp;
	protected PersistenceGmSessionFactory globalSessionFactory;
	
	@Before
	public void initBase() throws GmSessionException {
		logger.info("Preparing Test for Swagger Model Import module");

		globalImp = apiFactory().build();
		globalSessionFactory = apiFactory().buildSessionFactory();
		
		swaggerImportAccessSession = globalImp.session();
		
		logger.info("##################################### actual test begins #############################");
	}

	@After
	public void tearDown() {
		logger.info("##################################### tear down #############################");
		eraseTestEntities();
	}
}
