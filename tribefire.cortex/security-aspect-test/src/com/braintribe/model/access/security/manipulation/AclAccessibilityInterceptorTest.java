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
package com.braintribe.model.access.security.manipulation;

import static com.braintribe.model.acl.AclPermission.DENY;
import static com.braintribe.model.acl.AclPermission.GRANT;

import org.junit.Test;

import com.braintribe.model.access.security.manipulation.acl.AclAccessibilityInterceptorTestBase;
import com.braintribe.model.acl.AclEntry;
import com.braintribe.model.acl.AclOperation;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class AclAccessibilityInterceptorTest extends AclAccessibilityInterceptorTestBase {

	private final AclAccessibilityInterceptorTest i = this;

	@Test
	public void singleGrantCreated() throws Exception {
		acl(aopSession, "defaultAcl", //
				i::grantAdminRead //
		);
		aopSession.commit();

		assertAcl("defaultAcl", //
				ADMIN_ROLE //
		);
	}

	@Test
	public void singleDenyCreated() throws Exception {
		acl(aopSession, "defaultAcl", //
				i::denyVolunteerRead //
		);
		aopSession.commit();

		assertAcl("defaultAcl", //
				not(VOLUNTEER_ROLE) //
		);
	}

	@Test
	public void ignoresNonReadOperations() throws Exception {
		acl(aopSession, "defaultAcl", //
				i::grantAdminRead, //
				i::grantVolunteerWrite //
		);
		aopSession.commit();

		assertAcl("defaultAcl", //
				ADMIN_ROLE //
		);
	}

	@Test
	public void singleDenyAdded() throws Exception {
		acl(delegateSession, "defaultAcl", //
				i::grantAdminRead //
		);
		delegateSession.commit();
		
		acl(aopSession, "defaultAcl", //
				i::denyVolunteerRead //
		);
		aopSession.commit();

		assertAcl("defaultAcl", //
				ADMIN_ROLE, //
				not(VOLUNTEER_ROLE) //
		);
	}

	private AclEntry grantAdminRead(PersistenceGmSession entityFactory) {
		return aclEntry(entityFactory, GRANT, ADMIN_ROLE, AclOperation.READ);
	}

	
	private AclEntry denyVolunteerRead(PersistenceGmSession entityFactory) {
		return aclEntry(entityFactory, DENY, VOLUNTEER_ROLE, AclOperation.READ);
	}

	private AclEntry grantVolunteerWrite(PersistenceGmSession entityFactory) {
		return aclEntry(entityFactory, DENY, VOLUNTEER_ROLE, AclOperation.WRITE);
	}
	
}
