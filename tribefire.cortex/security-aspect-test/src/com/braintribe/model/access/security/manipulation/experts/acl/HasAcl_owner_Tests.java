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

import static com.braintribe.model.access.security.common.AbstractSecurityAspectTest.VOLUNTEER_ROLE;
import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.security.common.AbstractSecurityAspectTest;
import com.braintribe.model.access.security.manipulation.experts.AbstractAclIlsValidatorTest;
import com.braintribe.model.access.security.testdata.acl.AclFactory;
import com.braintribe.model.access.security.testdata.query.AclEntity;
import com.braintribe.model.acl.AclOperation;
import com.braintribe.model.acl.HasAcl;
import com.braintribe.model.processing.security.manipulation.SecurityViolationEntry;

/**
 * 
 */
public class HasAcl_owner_Tests extends AbstractAclIlsValidatorTest {

	private AclEntity volunteerModify;
	private AclEntity onlyOwner;

	@Override
	@Before
	public void setUp() {
		super.setUp();

		setUserRoles(VOLUNTEER_ROLE);

		acls.setAssignedAclOps(asList(AclOperation.MODIFY_ACL, AclOperation.WRITE, AclOperation.READ));
		onlyOwner = acls.create_JustOwner();
		volunteerModify = acls.create_Volunteer();
	}

	// ###################################
	// ## . . . . . . AddAcl . . . . . .##
	// ###################################

	@Test
	public void set_NoAclOrOwner_Deny() {
		AclEntity hasNoAcl = acls.create_NoAcl();
		flushAndValidate(() -> hasNoAcl.setOwner("Random"));
		assertOwnerError();
	}

	@Test
	public void set_NoAclOrOwner_CanAdministerHasAcl_Allyw() {
		setUserRoles(AbstractSecurityAspectTest.ADMINISTERING_HAS_ACL_ROLE);

		AclEntity hasNoAcl = acls.create_NoAcl();
		flushAndValidate(() -> hasNoAcl.setOwner("Random"));
		assertOk();
	}

	// ###################################
	// ## . . . . . RemoveAcl . . . . . ##
	// ###################################

	// isOwner || HasAcl is Administrable

	@Test
	public void remove_OnlyOwner_IsOwner_Allowed() {
		AclEntity onlyOwner = acls.create_JustOwner();
		setUserName(AclFactory.ALLOWED_ACL_USER);

		flushAndValidate(() -> onlyOwner.setOwner(null));
		assertOk();
	}

	@Test
	public void remove_OnlyOwner_NotOwner_Deny() {
		AclEntity onlyOwner = acls.create_JustOwner();
		setUserName(AclFactory.UNKNOWN_ACL_USER);

		flushAndValidate(() -> onlyOwner.setOwner(null));

		assertNumberOfErrors(2); // touching admin

		Iterator<SecurityViolationEntry> it = validationResult.getViolationEntries().iterator();
		assertError(it.next(), AclEntity.T, null);
		assertError(it.next(), AclEntity.T, HasAcl.owner);
	}

	@Test
	public void remove_OnlyOwner_CanWriteAndAdministerAcl_Allow() {
		setUserRoles(VOLUNTEER_ROLE, AbstractSecurityAspectTest.ADMINISTERING_HAS_ACL_ROLE);
		flushAndValidate(() -> onlyOwner.setAcl(null));
		assertOk();
	}

	@Test
	public void remove_Acl_CanModify_Deny() {
		flushAndValidate(() -> volunteerModify.setOwner(null));
		assertOwnerError();
	}

	// ###################################
	// ## . . . . . Helpers . . . . . . ##
	// ###################################

	protected void flushAndValidate(Runnable r) {
		commit();
		validate(r);
	}

	private void assertOwnerError() {
		assertNumberOfErrors(1);
		assertErrors(AclEntity.T, "owner");
	}

}
