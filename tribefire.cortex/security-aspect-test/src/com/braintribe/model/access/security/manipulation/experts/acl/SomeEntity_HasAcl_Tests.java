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
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.security.manipulation.experts.AbstractAclIlsValidatorTest;
import com.braintribe.model.access.security.testdata.query.AclEntity;
import com.braintribe.model.access.security.testdata.query.AclPropsOwner;
import com.braintribe.model.acl.AclOperation;
import com.braintribe.model.generic.GenericEntity;

/**
 * 
 */
public class SomeEntity_HasAcl_Tests extends AbstractAclIlsValidatorTest {

	private AclPropsOwner owner;
	private AclEntity allowedReadOnlyAcl;
	private AclEntity deniedAcl;

	// owner==null && acl==null || isOwner || WRITE/DELETE || HasAcl is Administrable

	@Override
	@Before
	public void setUp() {
		super.setUp();

		setUserRoles(VOLUNTEER_ROLE);

		acls.setAssignedAclOps(asList(AclOperation.READ));
		allowedReadOnlyAcl = acls.create_Volunteer();

		acls.setAssignedAclOps(asList(AclOperation.WRITE, AclOperation.READ));
		deniedAcl = acls.create_AnyAdmin();
		owner = acls.create_PropsOwner();
	}

	@Test
	public void settingAllowed() {
		AclEntity allowedAcl = acls.create_NoAcl();

		flushAndValidate(() -> owner.setSingle(allowedAcl));
		assertOk();
	}

	@Test
	public void settingReadAllowed() {
		flushAndValidate(() -> owner.setSingle(allowedReadOnlyAcl));
		assertOk();
	}

	@Test
	public void settingDenied() {
		validate(() -> owner.setSingle(deniedAcl));
		assertSingleError("single");
	}

	@Test
	public void removingDeniedByOverwriting() {
		owner.setSingle(deniedAcl);

		flushAndValidate(() -> owner.setSingle(allowedReadOnlyAcl));
		assertOk();
	}

	// #####################################
	// ## . . Inserting to collection . . ##
	// #####################################

	@Test
	public void insertingAlloweToSet() {
		flushAndValidate(() -> owner.getSet().add(allowedReadOnlyAcl));
		assertOk();
	}

	@Test
	public void insertingDeniedToSet() {
		flushAndValidate(() -> owner.getSet().add(deniedAcl));
		assertSingleError("set");
	}

	@Test
	public void insertingDeniedToList() {
		flushAndValidate(() -> owner.getList().add(deniedAcl));
		assertSingleError("list");
	}

	@Test
	public void insertingDeniedToMap() {
		flushAndValidate(() -> owner.getMap().put(deniedAcl, deniedAcl));
		assertSingleError("map");
	}

	/** This is more about testing that collection manipulations also work for an Object */
	@Test
	public void insertingDeniedToObject() {
		owner.setObject(newList());

		flushAndValidate(() -> ((List<GenericEntity>) owner.getObject()).add(deniedAcl));
		assertSingleError("object");
	}

	// ######################################
	// ## . . Removing from collection . . ##
	// ######################################

	@Test
	public void removingDeniedFromSet() {
		owner.getSet().add(deniedAcl);

		flushAndValidate(() -> owner.getSet().remove(deniedAcl));
		assertSingleError("set");
	}

	@Test
	public void removingDeniedFromList() {
		owner.getList().add(deniedAcl);

		flushAndValidate(() -> owner.getList().remove(0));
		assertSingleError("list");
	}

	@Test
	public void removingDeniedKeyFromMap() {
		owner.getMap().put(deniedAcl, null);

		flushAndValidate(() -> owner.getMap().remove(deniedAcl));
		assertSingleError("map");
	}

	@Test
	public void removingDeniedValueFromMap() {
		owner.getMap().put(null, deniedAcl);

		flushAndValidate(() -> owner.getMap().remove(null));
		assertSingleError("map");
	}

	@Test
	public void removingDeniedValueFromMapByOverwriting() {
		owner.getMap().put(null, deniedAcl);

		flushAndValidate(() -> owner.getMap().put(null, null));
		assertOk();
	}

	// #####################################
	// ## . . . Clearing collection . . . ##
	// #####################################

	// Jut to test that we don't care about implicitly removing something

	@Test
	public void clearingDeniedFromSet() {
		owner.getSet().add(deniedAcl);

		flushAndValidate(() -> owner.getSet().clear());
		assertOk();
	}

	@Test
	public void clearingDeniedFromList() {
		owner.getList().add(deniedAcl);

		flushAndValidate(() -> owner.getList().clear());
		assertOk();
	}

	@Test
	public void clearingDeniedKeyFromMap() {
		owner.getMap().put(deniedAcl, null);

		flushAndValidate(() -> owner.getMap().clear());
		assertOk();
	}

	// ###################################
	// ## . . . . . Helpers . . . . . . ##
	// ###################################

	protected void flushAndValidate(Runnable r) {
		commit();
		validate(r);
	}

	private void assertSingleError(String propertyName) {
		assertNumberOfErrors(1);
		assertErrors(AclPropsOwner.T, propertyName);
	}

}
