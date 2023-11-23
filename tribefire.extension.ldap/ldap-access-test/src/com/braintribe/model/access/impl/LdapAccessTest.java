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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.ldap.Computer;
import com.braintribe.model.ldap.LdapAttribute;
import com.braintribe.model.ldap.LdapObjectClasses;
import com.braintribe.model.ldap.User;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.accessory.impl.BasicModelAccessory;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.testing.category.BtNetwork;
import com.braintribe.testing.category.Slow;
import com.braintribe.utils.ldap.LdapConnectionStack;

@Category({BtNetwork.class, Slow.class})
public class LdapAccessTest {

	protected LdapConnectionStack ldapStack = null;
	protected LdapAccess ldapAccess = null;
	protected BasicPersistenceGmSession session = null;

	@Before
	public void prepare() throws Exception {

		// LDAP Model
		Model ldapModel = GMF.getTypeReflection().getModel("tribefire.extension.ldap:ldap-model");
		GmMetaModel ldapMetaModel = ldapModel.getMetaModel();

		BasicModelAccessory ldapBma = new BasicModelAccessory() {
			@Override
			protected GmMetaModel loadModel() {
				return ldapMetaModel;
			}
			@Override
			protected boolean adoptLoadedModel() {
				return false;
			}
		};
		ldapBma.setCortexSessionProvider(() -> null);
		ldapBma.setModelName(ldapMetaModel.getName());
		ldapBma.setSessionProvider(() -> ldapBma);
		// End of LDAP Model


		this.ldapStack = new LdapConnectionStack();
		this.ldapStack.setConnectionUrl("ldap://inf-dom-bt.braintribe:389");
		this.ldapStack.setUsername(AuthenticationUtils.getFullDn());
		this.ldapStack.setPassword(AuthenticationUtils.getWebConnectPassword());

		this.ldapAccess = new LdapAccess();
		this.ldapAccess.setAccessId("ldapAccess");
		this.ldapAccess.setLdapConnectionStack(ldapStack);
		this.ldapAccess.setModelName("LdapAccess");

		this.ldapAccess.setMetaModelProvider(() -> ldapMetaModel);
		this.ldapAccess.setBase("DC=Braintribe");
		this.ldapAccess.postConstruct();

		BasicModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(ldapMetaModel);
		
		this.assignLdapObjectClass(mdEditor, User.T, "user");
		this.assignLdapAttribute(mdEditor, User.T, User.givenName, "givenName");
		this.assignLdapAttribute(mdEditor, User.T, User.mail, "mail");
		this.assignLdapAttribute(mdEditor, User.T, User.sn, "sn");
		this.assignLdapAttribute(mdEditor, User.T, User.dn, "dn");

		this.assignLdapObjectClass(mdEditor, Computer.T, "computer");
		this.assignLdapAttribute(mdEditor, Computer.T, Computer.cn, "cn");
		this.assignLdapAttribute(mdEditor, Computer.T, Computer.dnsHostName, "dnsHostName");
		this.assignLdapAttribute(mdEditor, Computer.T, Computer.dn, "dn");

		this.session = new BasicPersistenceGmSession(this.ldapAccess);
		this.session.setModelAccessory(ldapBma);
	}
	
	@After
	public void shutdown() {
		this.ldapStack.preDestroy();
	}

	@Ignore
	protected void assignLdapObjectClass(BasicModelMetaDataEditor modelEditor, EntityType<?> type, String... objectClasses) {
				
		LdapObjectClasses ldapObjectClasses = LdapObjectClasses.T.create();
		Set<String> ldapObjectClassesSet = new HashSet<String>();
		for (String oc : objectClasses) {
			ldapObjectClassesSet.add(oc);
		}
		ldapObjectClasses.setObjectClasses(ldapObjectClassesSet);
		
		modelEditor.onEntityType(type).addMetaData(ldapObjectClasses);
	}
	@Ignore
	protected void assignLdapAttribute(BasicModelMetaDataEditor modelEditor, EntityType<?> type, String propertyName, String attributeName) {

		LdapAttribute ldapAttribute = LdapAttribute.T.create();
		ldapAttribute.setAttributeName(attributeName);
		
		modelEditor.onEntityType(type).addPropertyMetaData(propertyName, ldapAttribute);
	}

	@Test
	public void testUserSearch() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class)
				.where()
				.property("mail").eq("roman.kurmanowytsch@braintribe.com")
				.done();
		EntityQueryResult result = this.ldapAccess.queryEntities(inputProcessQuery);
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			User user = (User) entity;
			System.out.println("Simple: dn="+user.getDn()+", Name: "+user.getGivenName()+" "+user.getSn());
		}
		
		assertThat(entities.size()).isGreaterThan(0);

	}

	@Test
	public void testUserSearch2() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(User.class)
				.where()
				.disjunction()
					.property("givenName").eq("Roman")
					.property("sn").eq("Kurmanowytsch")
				.close()
				.done();
		EntityQueryResult result = this.ldapAccess.queryEntities(inputProcessQuery);
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			User user = (User) entity;
			System.out.println("Simple: dn="+user.getDn()+", Name: "+user.getGivenName()+" "+user.getSn()+", Mail: "+user.getMail());
		}

		assertThat(entities.size()).isGreaterThan(0);
	}

	@Test
	public void testComputerSearch() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(Computer.class)
				.where()
				.property("dnsHostName").eq("APP-CONV1.Braintribe")
				.done();
		EntityQueryResult result = this.ldapAccess.queryEntities(inputProcessQuery);
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			Computer computer = (Computer) entity;
			System.out.println("Computer: dn="+computer.getDn()+", "+computer.getDnsHostName()+", "+computer.getCn());
		}

		assertThat(entities.size()).isGreaterThan(0);
	}
	
	@Test
	public void testQueryProperty() throws Exception {

		PropertyQuery propQuery = PropertyQueryBuilder.forProperty(Computer.T, "CN=APP-CONV1,CN=Computers,DC=Braintribe", "dnsHostName").done();
		PropertyQueryResult result = this.ldapAccess.queryProperty(propQuery);
		
		Object value = result.getPropertyValue();
		
		System.out.println("dnsHostName: "+value);
		
		assertThat(value).isNotNull();
	}
	
	@Test
	public void testAllComputersSearch() throws Exception {

		EntityQuery inputProcessQuery = EntityQueryBuilder.from(Computer.class).done();
		this.ldapAccess.setSearchPageSize(5);
		EntityQueryResult result = this.ldapAccess.queryEntities(inputProcessQuery);
		List<GenericEntity> entities = result.getEntities();

		assertThat(entities.size()).isGreaterThan(5);
		
		for (GenericEntity entity : entities) {

			Computer computer = (Computer) entity;
			System.out.println("Computer: dn="+computer.getDn()+", "+computer.getDnsHostName()+", "+computer.getCn());
		}

	}

	@Test
	public void testPaging() throws Exception {

		int startIndex = 0;
		int pageSize = 5;
		Set<String> pagedComputerDns = new HashSet<String>();
		int iterations = 3;
		
		for (int i=0; i<iterations; ++i) {
			
			System.out.println("Page "+(i+1)+":");
			
			EntityQuery inputProcessQuery = EntityQueryBuilder.from(Computer.class).paging(pageSize, startIndex+(pageSize*i)).done();
			EntityQueryResult result = this.session.query().entities(inputProcessQuery).result();
			List<GenericEntity> entities = result.getEntities();

			for (GenericEntity entity : entities) {

				Computer computer = (Computer) entity;
				String dn = computer.getDn();
				
				if (pagedComputerDns.contains(dn)) {
					Assert.fail("The computer "+dn+" has been returned twice.");
				} else {
					pagedComputerDns.add(dn);
					System.out.println("Computer: "+computer.getDnsHostName()+" (dn: "+dn+")");
				}
			}
			
		}
		
		Set<String> singlePageComputerDns = new HashSet<String>();
		
		EntityQuery inputProcessQuery = EntityQueryBuilder.from(Computer.class).paging(pageSize*iterations, startIndex).done();
		EntityQueryResult result = this.session.query().entities(inputProcessQuery).result();
		List<GenericEntity> entities = result.getEntities();

		for (GenericEntity entity : entities) {

			Computer computer = (Computer) entity;
			String dn = computer.getDn();
			
			singlePageComputerDns.add(dn);
		}

		Assert.assertEquals(pagedComputerDns, singlePageComputerDns);
	}
	
	@Test
	public void testFullPaging() throws Exception {

		int page = 0;
		int startIndex = 0;
		int pageSize = 10;
		Set<String> pagedComputerDns = new HashSet<String>();

		boolean hasMore = false;
		
		do {
			
			System.out.println("Page "+(page+1)+":");
			
			EntityQuery inputProcessQuery = EntityQueryBuilder.from(Computer.class).paging(pageSize, startIndex+(pageSize*page)).done();
			EntityQueryResult result = this.session.query().entities(inputProcessQuery).result();
			List<GenericEntity> entities = result.getEntities();

			for (GenericEntity entity : entities) {

				Computer computer = (Computer) entity;
				String dn = computer.getDn();
				
				if (pagedComputerDns.contains(dn)) {
					Assert.fail("The computer "+dn+" has been returned twice.");
				} else {
					pagedComputerDns.add(dn);
					System.out.println("Computer: "+computer.getDnsHostName());
				}
			}
			
			hasMore = result.getHasMore();
			
			page++;
			
		} while(hasMore);
		
		Assert.assertTrue(page > 1);
	}
}
