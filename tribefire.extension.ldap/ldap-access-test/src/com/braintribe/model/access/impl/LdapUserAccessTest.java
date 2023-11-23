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
package com.braintribe.model.access.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.accessory.impl.BasicModelAccessory;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.user.Group;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.testing.category.BtNetwork;
import com.braintribe.testing.category.Slow;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;
import com.braintribe.utils.ldap.LdapConnectionStack;

@Category({BtNetwork.class, Slow.class})
public class LdapUserAccessTest {

	protected LdapUserAccess ldapUserAccess = null;
	protected BasicPersistenceGmSession session = null;

	@Before
	public void prepare() throws Exception {

		// User Model
		Model userModel = GMF.getTypeReflection().getModel("com.braintribe.gm:user-model");
		GmMetaModel userMetaModel = userModel.getMetaModel();

		BasicModelAccessory userBma = new BasicModelAccessory() {
			@Override
			protected GmMetaModel loadModel() {
				return userMetaModel;
			}
			@Override
			protected boolean adoptLoadedModel() {
				return false;
			}
		};
		userBma.setCortexSessionProvider(() -> null);
		userBma.setModelName(userMetaModel.getName());
		userBma.setSessionProvider(() -> userBma);
		// End of User Model
		
		LdapConnectionStack ldapStack = new LdapConnectionStack();
		ldapStack.setConnectionUrl("ldap://inf-dom-bt.braintribe:389");
		ldapStack.setUsername(AuthenticationUtils.getFullDn());
		ldapStack.setPassword(AuthenticationUtils.getWebConnectPassword());

		this.ldapUserAccess = new LdapUserAccess();
		this.ldapUserAccess.setLdapConnectionStack(ldapStack);
		this.ldapUserAccess.setModelName("LdapAuthAccess");

		this.ldapUserAccess.setMetaModelProvider(() -> userMetaModel);
		this.ldapUserAccess.setUserBase("OU=Accounts,OU=BTT,DC=Braintribe");
		this.ldapUserAccess.setGroupBase("OU=Groups,OU=BTT,DC=Braintribe");

		List<String> userObjectClasses = new ArrayList<String>();
		userObjectClasses.add("user");
		this.ldapUserAccess.setUserObjectClasses(userObjectClasses);

		List<String> groupObjectClasses = new ArrayList<String>();
		groupObjectClasses.add("group");
		this.ldapUserAccess.setGroupObjectClasses(groupObjectClasses);

		this.session = new BasicPersistenceGmSession(this.ldapUserAccess);
	}
	

	@Test
	@Ignore //Only works if the user "allgroups" exists
	public void testManyGroups() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class)
				.where()
				.conjunction()
				.property("name").eq("allgroups")
				.property("password").eq("Operating2010!")
				.close()
				.done();
		List<User> entities = this.session.query().entities(inputProcessQuery).list();

		for (User user : entities) {
			
			Instant start = NanoClock.INSTANCE.instant();
			Set<Group> groups = user.getGroups();
			System.out.println("Getting groups: "+StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS));
			StringBuilder sb = new StringBuilder();
			start = NanoClock.INSTANCE.instant();
			for (Group g : groups) {
				sb.append(g.getName());
			}
			System.out.println("Iterating groups: "+StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS));
			
			start = NanoClock.INSTANCE.instant();
			Set<Role> roles = user.getRoles();
			System.out.println("Getting roles: "+StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS));
			start = NanoClock.INSTANCE.instant();
			for (Role r : roles) {
				sb.append(r.getName());
			}
			System.out.println("Iterating roles: "+StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS));
			
			System.out.println("Simple: Name: "+user.getFirstName()+" "+user.getLastName()+" (#groups: "+user.getGroups().size()+", #roles: "+user.getRoles().size()+")");
			
		}

		assertThat(entities.size()).isGreaterThan(0);
	}

	@Test
	public void testSearch() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class)
				.where()
				.property("name").eq("roman.kurmanowytsch")
				.done();
		EntityQueryResult result = this.ldapUserAccess.queryEntities(inputProcessQuery);
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			User user = (User) entity;
			System.out.println("Simple: Name: "+user.getFirstName()+" "+user.getLastName());
		}

		assertThat(entities.size()).isGreaterThan(0);
	}

	@Test
	public void testSearch2() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class)
				.where()
				.property("name").eq("peter.hoenigschnabel")
				.done();
		EntityQueryResult result = this.ldapUserAccess.queryEntities(inputProcessQuery);
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			User user = (User) entity;
			String lastName = user.getLastName();
			System.out.println("Simple: Name: "+user.getFirstName()+" "+lastName);
		}

		assertThat(entities.size()).isGreaterThan(0);
	}

	@Test
	public void testSearchConjunction() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class)
				.where()
				.conjunction()
				.property("name").eq("roman.kurmanowytsch")
				.property("email").eq("roman*")
				.close()
				.done();
		EntityQueryResult result = this.ldapUserAccess.queryEntities(inputProcessQuery);
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			User user = (User) entity;
			System.out.println("Conjunction: Name: "+user.getFirstName()+" "+user.getLastName());
		}

		assertThat(entities.size()).isGreaterThan(0);
	}

	@Test
	public void testSearchDisjunction() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class)
				.where()
				.disjunction()
				.property("name").eq("roman.kurmanowytsch")
				.property("email").eq("peter*")
				.negation()
				.property("lastName").eq("Brandner")
				.close()
				.done();
		EntityQueryResult result = this.ldapUserAccess.queryEntities(inputProcessQuery);
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			User user = (User) entity;
			System.out.println("Disjunction: Name: "+user.getFirstName()+" "+user.getLastName()+" ("+user.getName()+"/"+user.getId()+")");
		}

		assertThat(entities.size()).isGreaterThan(0);
	}

	@Test
	public void testSearchConjunctionWithNegation() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class)
				.where()
				.conjunction()
				.property("firstName").eq("Peter")
				.negation()
				.property("lastName").eq("Brandner")
				.close()
				.done();
		EntityQueryResult result = this.ldapUserAccess.queryEntities(inputProcessQuery);
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			User user = (User) entity;
			System.out.println("Conjunction with Negation: Name: "+user.getFirstName()+" "+user.getLastName()+" ("+user.getName()+"/"+user.getId()+")");
		}

		assertThat(entities.size()).isGreaterThan(0);
	}

	@Test
	public void testNegation() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class)
				.where()
				.negation()
				.property("lastName").eq("Brandner")
				.done();
		EntityQueryResult result = this.ldapUserAccess.queryEntities(inputProcessQuery);
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			User user = (User) entity;
			System.out.println("Negation: Name: "+user.getFirstName()+" "+user.getLastName()+" ("+user.getName()+"/"+user.getId()+")");
		}

		assertThat(entities.size()).isGreaterThan(0);
	}

	@Test
	public void testUserObjectClasses() throws Exception {

		List<String> userObjectClasses = new ArrayList<String>();
		userObjectClasses.add("user");
		userObjectClasses.add("person");
		this.ldapUserAccess.setUserObjectClasses(userObjectClasses);

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class)
				.where()
				.negation()
				.property("lastName").eq("Brandner")
				.done();
		EntityQueryResult result = this.ldapUserAccess.queryEntities(inputProcessQuery);
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			User user = (User) entity;
			System.out.println("UserObjectClasses: Name: "+user.getFirstName()+" "+user.getLastName()+" ("+user.getName()+"/"+user.getId()+")");
		}

		assertThat(entities.size()).isGreaterThan(0);
	}


	@Test
	public void testGroupObjectClasses() throws Exception {

		List<String> groupObjectClasses = new ArrayList<String>();
		groupObjectClasses.add("group");
		this.ldapUserAccess.setGroupObjectClasses(groupObjectClasses);

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(Group.class)
				.where()
				.negation()
				.property("name").eq("Confluence_Customer*")
				.done();
		EntityQueryResult result = this.ldapUserAccess.queryEntities(inputProcessQuery);
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			Group group = (Group) entity;
			System.out.println("GroupObjectClasses: Name: "+group.getName());
		}

		assertThat(entities.size()).isGreaterThan(0);
	}

	@Test
	public void testAuthentication() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class)
				.where()
				.conjunction()
				.property("name").eq(AuthenticationUtils.getLoginName())
				.property("password").eq(AuthenticationUtils.getWebConnectPassword())
				.close()
				.done();
		EntityQueryResult result = this.ldapUserAccess.queryEntities(inputProcessQuery);
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			User user = (User) entity;
			System.out.println("Simple: Name: "+user.getFirstName()+" "+user.getLastName());
		}

		assertThat(entities.size()).isGreaterThan(0);
	}

	@Test
	public void testEntityPaging() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class).done();
		this.ldapUserAccess.setSearchPageSize(5);
		EntityQueryResult result = this.ldapUserAccess.queryEntities(inputProcessQuery);
		List<GenericEntity> entities = result.getEntities();

		Assert.assertTrue(entities.size() > 5);

		for (GenericEntity entity : entities) {
			User user = (User) entity;
			System.out.println("Simple: Name: "+user.getFirstName()+" "+user.getLastName());
		}

		assertThat(entities.size()).isGreaterThan(0);
	}

	@Test
	public void testGroupRetrieval() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(Group.class).done();
		EntityQueryResult result = this.session.query().entities(inputProcessQuery).result();
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			Group group = (Group) entity;
			Set<User> users = group.getUsers();
			System.out.println("Group: "+group.getName()+" has "+users.size()+" members.");

		}

		assertThat(entities.size()).isGreaterThan(0);
	}

	@Test
	public void testPaging() throws Exception {

		int startIndex = 0;
		int pageSize = 5;
		Set<String> pagedUserIds = new HashSet<String>();
		int iterations = 3;
		
		for (int i=0; i<iterations; ++i) {
			
			System.out.println("Page "+(i+1)+":");
			
			EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class).paging(pageSize, startIndex+(pageSize*i)).done();
			EntityQueryResult result = this.session.query().entities(inputProcessQuery).result();
			List<GenericEntity> entities = result.getEntities();

			for (GenericEntity entity : entities) {

				User user = (User) entity;
				String userId = user.getId();
				
				if (pagedUserIds.contains(userId)) {
					Assert.fail("The user "+userId+" has been returned twice.");
				} else {
					pagedUserIds.add(userId);
					System.out.println("Paged User: Name: "+user.getFirstName()+" "+user.getLastName());
				}
			}
			
		}
		
		Set<String> singlePageUserIds = new HashSet<String>();
		
		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class).paging(pageSize*iterations, startIndex).done();
		EntityQueryResult result = this.session.query().entities(inputProcessQuery).result();
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			User user = (User) entity;
			String userId = user.getId();
			
			singlePageUserIds.add(userId);
		}

		Assert.assertEquals(pagedUserIds, singlePageUserIds);
	}
	
	@Test
	public void testFullPaging() throws Exception {

		int page = 0;
		int startIndex = 0;
		int pageSize = 10;
		Set<String> pagedUserIds = new HashSet<String>();

		this.ldapUserAccess.setSearchPageSize(100);
		
		boolean hasMore = false;

		do {
			
			System.out.println("Page "+(page+1)+":");
			
			EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class).paging(pageSize, startIndex+(pageSize*page)).done();
			EntityQueryResult result = this.session.query().entities(inputProcessQuery).result();
			List<GenericEntity> entities = result.getEntities();

			for (GenericEntity entity : entities) {

				User user = (User) entity;
				String userId = user.getId();
				
				if (pagedUserIds.contains(userId)) {
					Assert.fail("The user "+userId+" has been returned twice.");
				} else {
					pagedUserIds.add(userId);
					System.out.println("Paged User: Name: "+user.getFirstName()+" "+user.getLastName());
				}
			}
			
			hasMore = result.getHasMore();
			
			page++;
		} while(hasMore);
		
		Assert.assertTrue(page > 1);
	}
}
