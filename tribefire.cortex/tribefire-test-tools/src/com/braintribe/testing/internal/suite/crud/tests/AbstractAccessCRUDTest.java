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
package com.braintribe.testing.internal.suite.crud.tests;

import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;

/**
 * A class that automatically creates a new session to an access (set in constructor), test some CRUD operations and verify the
 * result in the end with another automatically newly created session
 * 
 * @author Neidhart
 *
 */
public abstract class AbstractAccessCRUDTest extends AbstractAccessInspector {
	private static Logger logger = Logger.getLogger(AbstractAccessCRUDTest.class);

	public AbstractAccessCRUDTest(String accessId, PersistenceGmSessionFactory factory) {
		super(accessId, factory);
	}

	/**
	 * 
	 * @param session session to access. provided automatically when calling start()
	 * @return manipulated entities
	 */
	abstract protected List<GenericEntity> run(PersistenceGmSession session);
	
	/**
	 * 
	 * @param verificator provided automatically when calling start() - helps with result verification
	 * @param testResult which was returned by your custom implementation of run()
	 */
	abstract protected void verifyResult(Verificator verificator, List<GenericEntity> testResult);

	public List<GenericEntity> start() {
		PersistenceGmSession session = sessionFactory.newSession(accessId);

		List<GenericEntity> testResult = run(session);

		Verificator verificator = new Verificator(accessId, sessionFactory);
		verificator.setFilterPredicate(getFilterPredicate());

		verifyResult(verificator, testResult);

		return testResult;
	}
}
