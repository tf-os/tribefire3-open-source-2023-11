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
package com.braintribe.model.access.security.query;

import static com.braintribe.model.access.security.testdata.acl.AclFactory.ALLOWED_ACL_USER;
import static com.braintribe.model.access.security.testdata.acl.AclFactory.UNKNOWN_ACL_USER;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.security.testdata.acl.AclFactory;
import com.braintribe.model.access.security.testdata.query.AclEntity;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.PropertyQuery;

/**
 * The names of the tests use the following pattern:
 * 
 * <pre>
 * ${aclConfig}_${userConfig}_${expectedAccess}
 * </pre>
 * 
 * <b>Example:</b> <tt>anyAdmin_AdminAndUser_Allow</tt> <br/>
 * This the access should be allowed for any <tt>admin</tt>, the user doing the query has both <tt>admin</tt> and
 * <tt>user</tt> roles, so the access should be allowed.
 * <p>
 * Tests prefixed with <tt>property_</tt> are the same, but the configuration applies to the owner of the queried
 * property.
 */
public class Acl_DirectQueryingTests extends AbstractQueryingTest {

	private AclFactory acls;

	@Override
	protected Map<Class<? extends GenericEntity>, TraversingCriterion> defaultTc() {
		return allTopLevelPropsTcMap();
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		acls = new AclFactory(delegateSession);
	}

	@Test
	public void aclOff_Allow() {
		acls.create_NoAcl();

		setUserRoles(VOLUNTEER_ROLE);
		assert_Allow();
	}

	// ################################################
	// ## . . . . . . . . Owner only . . . . . . . . ##
	// ################################################

	@Test
	public void justOwner_IsOwner_Allow() {
		acls.create_JustOwner();

		setUserName(ALLOWED_ACL_USER);
		assert_Allow();
	}

	@Test
	public void justOwner_IsNotOwner_Deny() {
		acls.create_JustOwner();

		setUserName("other-user");
		assert_Deny();
	}

	@Test
	public void justOwner_CanAdministerHasAcl_Allow() {
		acls.create_JustOwner();
		
		setUserRoles(ADMINISTERING_HAS_ACL_ROLE);
		assert_Allow();
	}
	
	// Property version

	@Test
	public void property_justOwner_IsOwner_Allow() {
		acls.create_JustOwner("ID-1");

		setUserName(ALLOWED_ACL_USER);
		assert_Allowed_Property();
	}

	@Test
	public void property_justOwner_IsNotOwner_Deny() {
		acls.create_JustOwner("ID-1");

		setUserName(UNKNOWN_ACL_USER);
		assert_Deny_Property();
	}

	// ################################################
	// ## . . . . . . . . ACL only . . . . . . . . . ##
	// ################################################

	@Test
	public void anyAdmin_Admin_Allow() {
		acls.create_AnyAdmin();

		setUserRoles(ADMIN_ROLE);
		assert_Allow();
	}

	@Test
	public void anyAdmin_AdminAndVolunteer_Allow() {
		acls.create_AnyAdmin();

		setUserRoles(ADMIN_ROLE, VOLUNTEER_ROLE);
		assert_Allow();
	}

	@Test
	public void anyAdmin_Volunteer_Deny() {
		acls.create_AnyAdmin();

		setUserRoles(VOLUNTEER_ROLE);

		assert_Deny();
	}

	@Test
	public void adminNotVolunteer_OnlyAdmin_Allow() {
		acls.create_AdminNotVolunteer();

		setUserRoles(ADMIN_ROLE);
		assert_Allow();
	}

	@Test
	public void adminNotVolunteer_AdminAndVolunteer_Deny() {
		acls.create_AdminNotVolunteer();

		setUserRoles(ADMIN_ROLE, VOLUNTEER_ROLE);
		assert_Deny();
	}

	// Property version

	@Test
	public void property_adminNotVolunteer_OnlyAdmin_Allow() {
		acls.create_AdminNotVolunteer("ID-1");

		setUserRoles(ADMIN_ROLE);
		assert_Allowed_Property();
	}

	@Test
	public void property_adminNotVolunteer_AdminAndVolunteer_Deny() {
		acls.create_AdminNotVolunteer("ID-1");

		setUserRoles(ADMIN_ROLE, VOLUNTEER_ROLE);
		assert_Deny_Property();
	}

	// ################################################
	// ## . . . . . . . . Owner + ACL . . . . . . . .##
	// ################################################

	@Test
	public void ownerOrAdmin_IsOwner_Allow() {
		acls.create_ownerOrAdmin();

		setUserName(ALLOWED_ACL_USER);
		assert_Allow();
	}

	@Test
	public void ownerOrAdmin_IsAdmin_Allow() {
		acls.create_ownerOrAdmin();

		setUserRoles(ADMIN_ROLE);
		assert_Allow();
	}

	@Test
	public void ownerOrAdmin_JustSomeVolunteer_Deny() {
		acls.create_ownerOrAdmin();

		setUserRoles(VOLUNTEER_ROLE);
		assert_Deny();
	}

	// Property version

	@Test
	public void property_ownerOrAdmin_IsOwner_Allow() {
		acls.create_ownerOrAdmin("ID-1");

		setUserName(ALLOWED_ACL_USER);
		assert_Allowed_Property();
	}

	@Test
	public void property_ownerOrAdmin_IsAdmin_Allow() {
		acls.create_ownerOrAdmin("ID-1");

		setUserRoles(ADMIN_ROLE);
		assert_Allowed_Property();
	}

	@Test
	public void property_ownerOrAdmin_JustSomeVolunteer_Deny() {
		acls.create_ownerOrAdmin("ID-1");

		setUserRoles(VOLUNTEER_ROLE);
		assert_Deny_Property();
	}

	// ################################################
	// ## . . . . . . . . . Helpers . . . . . . . . .##
	// ################################################

	private void assert_Allow() {
		EntityQuery query = EntityQueryBuilder.from(AclEntity.T).done();
		assertReturnedEntities(query, 1);
	}

	private void assert_Deny() {
		EntityQuery query = EntityQueryBuilder.from(AclEntity.T).done();
		assertResultIsEmpty(query);
	}

	private void assert_Allowed_Property() {
		PropertyQuery query = PropertyQueryBuilder.forProperty(AclEntity.T, "ID-1", "id").done();
		assertQueriedProperty(query, "ID-1");
	}

	private void assert_Deny_Property() {
		PropertyQuery query = PropertyQueryBuilder.forProperty(AclEntity.T, "ID-1", "id").done();
		assertQueriedPropertyNull(query);
	}

}
