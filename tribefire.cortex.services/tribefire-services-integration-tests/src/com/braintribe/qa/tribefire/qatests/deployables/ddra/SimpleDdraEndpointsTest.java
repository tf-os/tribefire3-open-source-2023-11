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
package com.braintribe.qa.tribefire.qatests.deployables.ddra;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.user.User;
import com.braintribe.testing.internal.tribefire.helpers.http.HttpPostHelperEx;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;

public class SimpleDdraEndpointsTest extends AbstractTribefireQaTest {

	private static CloseableHttpClient httpClient;
	private static TestHttpRequestFactory factory;
	private static String sessionId;

	protected final static String TRIBEFIRE_SERVICES_URL = apiFactory().getURL();

	@BeforeClass
	public static void beforeClass() throws MalformedURLException {
		httpClient = HttpClients.createDefault();
		factory = new TestHttpRequestFactory(httpClient, new URL(TRIBEFIRE_SERVICES_URL));
		sessionId = authenticate();
	}

	public static String authenticate() {
		try {
			Map<String, Object> authRequest = new HashMap<>();
			authRequest.put("user", "cortex");
			authRequest.put("password", "cortex");
			HttpPostHelperEx httpHelper = new HttpPostHelperEx(TRIBEFIRE_SERVICES_URL + "/api/v1/authenticate", authRequest);
	
			String result = httpHelper.getContent();
			System.out.println(result);
			return result.replaceAll("\"", "").trim();
		} catch (IOException e) {
			throw new RuntimeException("Could not authenticate in tribefire via rest", e);
		}
	}

	@Before @After
	public void cleanUp() {
		PersistenceGmSession session = apiFactory().build().session();

		List<User> entities = session.query().entities(EntityQueryBuilder.from(User.T).done()).list();
		for(User entity : entities) {
			session.deleteEntity(entity);
		}
		session.commit();
	}
	
	@Test
	public void testSimpleAuthenticate() {
		// @formatter:off
		String sessionId = factory.post()
				.path("/api/v1/authenticate")
				.body("{ \"user\": \"cortex\", \"password\": \"cortex\" }")
				.execute(200);
		// @formatter:on
		
		Assert.assertNotNull(sessionId);
	}

	@Test
	public void testCreateReadUpdateDelete() {
		
		String path = "/rest/v2/entities/cortex/" + User.T.getTypeSignature();
		
		// QUERY
		
		User entity = queryViaRest();
		Assert.assertNull(entity);

		// CREATE

		// @formatter:off
		factory.post()
				.path(path)
				.urlParameter("sessionId", sessionId)
				.accept("application/json")
				.contentType("application/json")
				.body("{ \"name\": \"test\" }")
				.execute(200);
		// @formatter:on
		
		// QUERY
		
		entity = queryViaRest();
		
		Assert.assertNotNull(entity);
		Assert.assertNotNull(entity.getId());
		Assert.assertEquals("test", entity.getName());
		
		// DELETE

		// @formatter:off
		factory.delete()
				.path(path + "/" + entity.getId())
				.urlParameter("sessionId", sessionId)
				.header("Accept", "application/json")
				.execute(200);
		// @formatter:on

		// QUERY
		
		entity = queryViaRest();
		Assert.assertNull(entity);

	}
	
	private User queryViaRest() {
		// QUERY

		// @formatter:off
		List<User> entities = factory.get()
				.path("/rest/v2/entities/cortex/" + User.T.getTypeSignature())
				.urlParameter("sessionId", sessionId)
				.header("Accept", "application/json")
				.execute(200);
		// @formatter:on

		if(entities.isEmpty()) {
			return null;
		}
		
		Assert.assertEquals(1, entities.size());
		return entities.get(0);
	}
}