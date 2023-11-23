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
package com.braintribe.model.processing.securityservice.ldap;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.access.impl.LdapUserAccess;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.accessory.impl.BasicModelAccessory;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.securityservice.UserAuthenticationResponse;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.user.Group;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.testing.category.BtNetwork;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;
import com.braintribe.utils.ldap.LdapConnectionStack;

@Category({BtNetwork.class})
public class LdapAuthenticationServiceTest {

	protected LdapUserAccess ldapUserAccess = null;
	protected BasicPersistenceGmSession session = null;
	protected LdapAuthenticationService service = null;

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
		this.ldapUserAccess.setAccessId("ldapUserAccess");
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
		
		service = new LdapAuthenticationService();
		service.setGroupsAreRoles(true);
		service.setLdapAccessId("ldapUserAccess");
		service.setSessionFactory(accessId -> {
			BasicPersistenceGmSession session = new BasicPersistenceGmSession(this.ldapUserAccess);
			return session;
		});
	}
	
	@Test
	@Ignore //Only works if the user "allgroups" exists
	public void testManyGroupsRolesAreGroups() throws Exception {
		
		service.setGroupsAreRoles(true);
		
		UserPasswordCredentials credentials = UserPasswordCredentials.T.create();
		credentials.setPassword("Operating2010!");
		UserNameIdentification userId = UserNameIdentification.T.create();
		userId.setUserName("allgroups");
		credentials.setUserIdentification(userId);
		
		Instant start = NanoClock.INSTANCE.instant();
		UserAuthenticationResponse response = service.authenticate(credentials);
		System.out.println("Authentication: "+StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS));
		User user = response.getUser();
		
		start = NanoClock.INSTANCE.instant();
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
	
	@Test
	@Ignore //Only works if the user "allgroups" exists
	public void testManyGroupsRolesAreNotGroups() throws Exception {
		
		service.setGroupsAreRoles(false);
		
		UserPasswordCredentials credentials = UserPasswordCredentials.T.create();
		credentials.setPassword("Operating2010!");
		UserNameIdentification userId = UserNameIdentification.T.create();
		userId.setUserName("allgroups");
		credentials.setUserIdentification(userId);

		Instant start = NanoClock.INSTANCE.instant();
		UserAuthenticationResponse response = service.authenticate(credentials);
		System.out.println("Authentication: "+StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS));
		User user = response.getUser();
		
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
}
