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
package com.braintribe.model.processing.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.securityservice.credentials.ExistingSessionCredentials;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.user.User;

//@Category(TribefireServices.class)
@Ignore // TODO
public class GmSessionFactoriesTest {

	@Test
	public void testStandaloneResourceAccess() throws Exception {

		PersistenceGmSessionFactory sessionFactory = GmSessionFactories.remote("https://localhost:8443/tribefire-services")
				.authentication("cortex", "cortex").done();
		PersistenceGmSession session = sessionFactory.newSession("auth");

		EntityQuery query = EntityQueryBuilder.from(User.class).done();
		List<User> users = session.query().entities(query).list();

		assertThat(users).isNotNull();
		assertThat(users.size()).isGreaterThan(0);

	}

	@Test
	public void testStandaloneResourceAccessWithCredentials() throws Exception {

		UserPasswordCredentials credentials = UserPasswordCredentials.T.create();
		UserNameIdentification ui = UserNameIdentification.T.create();
		ui.setUserName("cortex");
		credentials.setUserIdentification(ui);
		credentials.setPassword("cortex");

		PersistenceGmSessionFactory sessionFactory = GmSessionFactories.remote("https://localhost:8443/tribefire-services")
				.authentication(credentials).done();
		PersistenceGmSession session = sessionFactory.newSession("auth");

		EntityQuery query = EntityQueryBuilder.from(User.class).done();
		List<User> users = session.query().entities(query).list();

		assertThat(users).isNotNull();
		assertThat(users.size()).isGreaterThan(0);

	}

	@Test
	public void testStandaloneResourceAccessWithExistingSessionId() throws Exception {

		PersistenceGmSessionFactory sessionFactoryAuth = GmSessionFactories.remote("https://localhost:8443/tribefire-services")
				.authentication("cortex", "cortex").done();
		PersistenceGmSession sessionAuth = sessionFactoryAuth.newSession("auth");
		String sessionid = sessionAuth.getSessionAuthorization().getSessionId();

		ExistingSessionCredentials credentials = ExistingSessionCredentials.T.create();
		credentials.setExistingSessionId(sessionid);

		PersistenceGmSessionFactory sessionFactory = GmSessionFactories.remote("https://localhost:8443/tribefire-services")
				.authentication(credentials).done();
		PersistenceGmSession session = sessionFactory.newSession("auth");

		EntityQuery query = EntityQueryBuilder.from(User.class).done();
		List<User> users = session.query().entities(query).list();

		assertThat(users).isNotNull();
		assertThat(users.size()).isGreaterThan(0);

	}
}
