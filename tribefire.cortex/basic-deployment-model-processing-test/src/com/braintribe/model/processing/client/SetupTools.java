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
package com.braintribe.model.processing.client;

import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.GmSessionFactories;
import com.braintribe.model.processing.session.GmSessionFactoryBuilderException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;

/**
 * 
 */
public class SetupTools {

	private static final String user = "cortex";
	private static final String password = "cortex";

	private static final String SERVER_URL = "http://localhost:8080/tribefire-services";

	public static PersistenceGmSession createNewSession(String accessId) {
		try {
			PersistenceGmSessionFactory sessionFactory = setupRemoteSessionFactory(SERVER_URL, user, password);
			return newSession(sessionFactory, accessId);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static PersistenceGmSessionFactory setupRemoteSessionFactory(String baseUrl, String user, String password) {
		try {
			PersistenceGmSessionFactory sessionFactory = GmSessionFactories.remote(baseUrl).authentication(user, password).done();
			return sessionFactory;
		} catch (GmSessionFactoryBuilderException e) {
			throw new RuntimeException("Could not create a session to "+baseUrl+" using user "+user, e);
		}
	}

	private static PersistenceGmSession newSession(PersistenceGmSessionFactory factory, String accessId) {
		try {
			return factory.newSession(accessId);
		} catch (GmSessionException e) {
			throw new RuntimeException("Error while creating new session for access '" + accessId + "'!", e);
		}
	}

}
