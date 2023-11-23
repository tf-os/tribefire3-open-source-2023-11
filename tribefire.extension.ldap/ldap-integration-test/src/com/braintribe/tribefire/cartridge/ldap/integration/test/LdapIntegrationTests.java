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
package com.braintribe.tribefire.cartridge.ldap.integration.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.protocol.shared.store.LdifFileLoader;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.user.Group;
import com.braintribe.model.user.User;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;

import software.apacheds.embedded.EmbeddedLdapServer;

@Category(KnownIssue.class)
public class LdapIntegrationTests extends AbstractTribefireQaTest {

	private PersistenceGmSession userSession = null;
	private ImpApi imp;

	private static EmbeddedLdapServer embeddedLdapServer;

	@BeforeClass
	public static void beforeClass() throws Exception {
		System.out.println("Starting LDAP Server.");
		embeddedLdapServer = new EmbeddedLdapServer();
		embeddedLdapServer.init();

		DirectoryService service = embeddedLdapServer.getDirectoryService();

		LdifFileLoader ldifLoader = new LdifFileLoader(service.getAdminSession(), "res/ldap/users.ldif");
		ldifLoader.execute();
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (embeddedLdapServer != null) {
			System.out.println("Tearing down LDAP Server.");
			embeddedLdapServer.destroy();
		}
	}

	@Before
	public void before() throws Exception {
		if (userSession == null) {
			imp = apiFactory().build();
			PersistenceGmSessionFactory sessionFactory = apiFactory().buildSessionFactory();
			userSession = sessionFactory.newSession("ldap-user-access.ldap.default.ldap-templates-space.ldap-user-access");
		}
	}

	@Test
	public void testSearch() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class).where().property("name").eq("roman.kurmanowytsch").done();

		List<User> list = userSession.query().entities(inputProcessQuery).list();

		for (User user : list) {

			System.out.println("Simple: Name: " + user.getFirstName() + " " + user.getLastName());
		}

		assertThat(list.size()).isEqualTo(1);
	}

	@Test
	public void testSearch2() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class).where().property("name").eq("peter.brandner").done();

		List<User> list = userSession.query().entities(inputProcessQuery).list();

		for (User user : list) {

			String lastName = user.getLastName();
			System.out.println("Simple: Name: " + user.getFirstName() + " " + lastName);
		}

		assertThat(list.size()).isGreaterThan(0);
	}

	@Test
	public void testSearchConjunction() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class).where().conjunction().property("name").eq("roman.kurmanowytsch")
				.property("email").eq("roman*").close().done();

		List<User> list = userSession.query().entities(inputProcessQuery).list();

		for (User user : list) {

			System.out.println("Conjunction: Name: " + user.getFirstName() + " " + user.getLastName());
		}

		assertThat(list.size()).isGreaterThan(0);
	}

	@Test
	public void testSearchDisjunction() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class).where().disjunction().property("name").eq("roman.kurmanowytsch")
				.property("email").eq("peter*").negation().property("lastName").eq("Brandner").close().done();

		List<User> list = userSession.query().entities(inputProcessQuery).list();

		for (User user : list) {

			System.out.println(
					"Disjunction: Name: " + user.getFirstName() + " " + user.getLastName() + " (" + user.getName() + "/" + user.getId() + ")");
		}

		assertThat(list.size()).isGreaterThan(0);
	}

	@Test
	public void testSearchConjunctionWithNegation() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class).where().conjunction().property("firstName").eq("Peter").negation()
				.property("lastName").eq("Brandner").close().done();

		List<User> list = userSession.query().entities(inputProcessQuery).list();

		for (User user : list) {

			System.out.println("Conjunction with Negation: Name: " + user.getFirstName() + " " + user.getLastName() + " (" + user.getName() + "/"
					+ user.getId() + ")");

			assertThat(user.getFirstName().equals("Peter") && user.getLastName().equals("Brandner")).isEqualTo(false);

		}

		assertThat(list.size()).isGreaterThan(0);
	}

	@Test
	public void testNegation() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class).where().negation().property("lastName").eq("Brandner").done();

		List<User> list = userSession.query().entities(inputProcessQuery).list();

		for (User user : list) {

			System.out
					.println("Negation: Name: " + user.getFirstName() + " " + user.getLastName() + " (" + user.getName() + "/" + user.getId() + ")");

			if (user.getLastName() != null) {
				assertThat(user.getLastName().equals("Brandner")).isEqualTo(false);
			}
		}

		assertThat(list.size()).isGreaterThan(0);
	}

	@Test
	// @Ignore //Security aspect does not want password searches
	public void testAuthentication() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class).where().conjunction().property("name").eq("peter.brandner")
				.property("password").eq("12345").close().done();

		List<User> list = userSession.query().entities(inputProcessQuery).list();

		for (User user : list) {

			System.out.println("Simple: Name: " + user.getFirstName() + " " + user.getLastName());
		}

		assertThat(list.size()).isGreaterThan(0);
	}

	@Test
	public void testGroupRetrieval() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(Group.class).done();
		List<Group> list = userSession.query().entities(inputProcessQuery).list();

		for (Group group : list) {

			Set<User> users = group.getUsers();
			System.out.println("Group: " + group.getName() + " has " + users.size() + " members.");

		}

		assertThat(list.size()).isGreaterThan(0);
	}

	@Test
	public void testPaging() throws Exception {

		int startIndex = 0;
		int pageSize = 5;
		Set<String> pagedUserIds = new HashSet<String>();
		int iterations = 3;

		for (int i = 0; i < iterations; ++i) {

			System.out.println("Page " + (i + 1) + ":");

			EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class).paging(pageSize, startIndex + (pageSize * i)).done();
			EntityQueryResult result = userSession.query().entities(inputProcessQuery).result();
			List<GenericEntity> entities = result.getEntities();

			for (GenericEntity entity : entities) {

				User user = (User) entity;
				String userId = user.getId();

				if (pagedUserIds.contains(userId)) {
					Assert.fail("The user " + userId + " has been returned twice.");
				} else {
					pagedUserIds.add(userId);
					System.out.println("Paged User: Name: " + user.getFirstName() + " " + user.getLastName());
				}
			}

		}

		Set<String> singlePageUserIds = new HashSet<String>();

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class).paging(pageSize * iterations, startIndex).done();
		EntityQueryResult result = userSession.query().entities(inputProcessQuery).result();
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			User user = (User) entity;
			String userId = user.getId();

			singlePageUserIds.add(userId);
		}

		Assert.assertEquals(pagedUserIds, singlePageUserIds);
	}

}
