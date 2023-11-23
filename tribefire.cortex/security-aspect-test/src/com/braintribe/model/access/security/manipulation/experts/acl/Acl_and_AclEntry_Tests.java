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
package com.braintribe.model.access.security.manipulation.experts.acl;

import static com.braintribe.model.access.security.common.AbstractSecurityAspectTest.ADMIN_ROLE;
import static com.braintribe.model.access.security.common.AbstractSecurityAspectTest.VOLUNTEER_ROLE;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.first;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.security.common.AbstractSecurityAspectTest;
import com.braintribe.model.access.security.manipulation.experts.AbstractAclIlsValidatorTest;
import com.braintribe.model.acl.Acl;
import com.braintribe.model.acl.AclEntry;
import com.braintribe.model.acl.AclOperation;
import com.braintribe.model.acl.AclStandardEntry;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteMode;

/**
 * 
 */
public class Acl_and_AclEntry_Tests extends AbstractAclIlsValidatorTest {

	private Acl volunteerCanModify;
	private Acl volunteerCanRw;
	private Acl adminCanModify;

	@Override
	@Before
	public void setUp() {
		super.setUp();

		setUserRoles(VOLUNTEER_ROLE);

		acls.setAssignedAclOps(asList(AclOperation.MODIFY_ACL));
		volunteerCanModify = acls.createAcl(VOLUNTEER_ROLE);
		adminCanModify = acls.createAcl(ADMIN_ROLE);

		acls.setAssignedAclOps(asList(AclOperation.READ, AclOperation.WRITE, AclOperation.REPLACE_ACL));
		volunteerCanRw = acls.createAcl(VOLUNTEER_ROLE);
	}

	// ###################################
	// ## . . . . . CreateAcl . . . . . ##
	// ###################################

	// Creating Acl which cannot be touched by anyone (except if Administrable MD). Useless but not forbidden
	// If this works, I assume anything else works as well
	@Test
	public void createEmptyAcl_Allowed() {
		flushAndValidate(() -> session.create(Acl.T));
		assertOk();
	}

	// ###################################
	// ## . . . . . ModifyAcl . . . . . ##
	// ###################################

	// MODIFY_ACL || Acl is Administrable

	@Test
	public void modify_CanModify_Allowed() {
		modify(volunteerCanModify);
		assertOk();
	}

	@Test
	public void modify_NoModifyAcl_Deny() {
		modify(volunteerCanRw);
		assertAclError();
	}

	@Test // Just in case there was a bug that this was granting access on Acl
	public void modify_NoModifyAcl_CanAdminister_HAS_ACL_Deny() {
		setUserRoles(AbstractSecurityAspectTest.ADMINISTERING_HAS_ACL_ROLE);
		modify(volunteerCanRw);
		assertAclError();
	}

	@Test
	public void modify_NoModifyAcl_CanAdministerAcl_Allow() {
		setUserRoles(AbstractSecurityAspectTest.ADMINISTERING_ACL_ROLE);
		modify(volunteerCanRw);
		assertOk();
	}

	@Test
	public void modify_ThisRoleCannotModify_Deny() {
		modify(adminCanModify);
		assertAclError();
	}

	private void modify(Acl acl) {
		flushAndValidate(() -> acl.getEntries().clear());
	}

	// ###################################
	// ## . . . . . DeleteAcl . . . . . ##
	// ###################################

	@Test
	public void delete_CanModify_Allowed() {
		delete(volunteerCanModify);
		assertOk();
	}

	@Test
	public void delete_NoModifyAcl_Deny() {
		delete(volunteerCanRw);
		assertAclError();
	}

	@Test
	public void deleteNoModifyAcl_CanAdministerAcl_Allow() {
		setUserRoles(AbstractSecurityAspectTest.ADMINISTERING_ACL_ROLE);
		delete(volunteerCanRw);
		assertOk();
	}

	@Test
	public void delete_ThisRoleCannotModify_Deny() {
		delete(adminCanModify);
		assertAclError();
	}

	@Test
	public void delete_HasRole_WrongMode_Deny() {
		flushAndValidate(() -> session.deleteEntity(volunteerCanModify));
		assertAclError();
	}

	// ###################################
	// ## . . . . DeleteAclEntry . . . .##
	// ###################################

	@Test
	public void deleteEntry_WrongMode_Deny() {
		AclEntry aclEntry = first(acls.createAcl(VOLUNTEER_ROLE).getEntries());

		flushAndValidate(() -> session.deleteEntity(aclEntry));
		assertAclEntryError();
	}

	@Test
	public void deleteEntry_CorrectMode_Allow() {
		AclEntry aclEntry = first(acls.createAcl(VOLUNTEER_ROLE).getEntries());

		delete(aclEntry);
		assertOk();
	}

	// ###################################
	// ## . . . . . Helpers . . . . . . ##
	// ###################################

	private void delete(GenericEntity entity) {
		flushAndValidate(() -> session.deleteEntity(entity, DeleteMode.failIfReferenced));
	}

	protected void flushAndValidate(Runnable r) {
		commit();
		validate(r);
	}

	private void assertAclError() {
		assertNumberOfErrors(1);
		assertErrors(Acl.T, null);
	}

	private void assertAclEntryError() {
		assertNumberOfErrors(1);
		assertErrors(AclStandardEntry.T, null);
	}

}
