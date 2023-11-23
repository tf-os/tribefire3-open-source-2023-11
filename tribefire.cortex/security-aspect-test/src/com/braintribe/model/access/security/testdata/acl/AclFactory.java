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
package com.braintribe.model.access.security.testdata.acl;

import static com.braintribe.model.access.security.common.AbstractSecurityAspectTest.ADMIN_ROLE;
import static com.braintribe.model.access.security.common.AbstractSecurityAspectTest.VOLUNTEER_ROLE;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.access.security.testdata.query.AclEntity;
import com.braintribe.model.access.security.testdata.query.AclPropsOwner;
import com.braintribe.model.acl.Acl;
import com.braintribe.model.acl.AclEntry;
import com.braintribe.model.acl.AclOperation;
import com.braintribe.model.acl.AclPermission;
import com.braintribe.model.acl.AclStandardEntry;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * @author peter.gazdik
 */
public class AclFactory {

	public static final String ALLOWED_ACL_USER = "AllowedUser";
	public static final String UNKNOWN_ACL_USER = "DeniedUser";

	private final PersistenceGmSession session;

	private List<AclOperation> assignedAclOps = asList(AclOperation.READ);

	public AclFactory(PersistenceGmSession session) {
		this.session = session;
	}

	public void setAssignedAclOps(List<AclOperation> assignedAclOps) {
		this.assignedAclOps = assignedAclOps;
	}

	public AclEntity create_NoAcl() {
		AclEntity result = newAclEntity();
		commit();

		return result;
	}

	public AclEntity create_JustOwner() {
		AclEntity entity = newAclEntity();
		entity.setOwner(ALLOWED_ACL_USER);
		commit();

		return entity;
	}

	public AclEntity create_JustOwner(String id) {
		AclEntity entity = newAclEntity();
		entity.setId(id);
		entity.setOwner(ALLOWED_ACL_USER);
		commit();

		return entity;
	}

	public AclEntity create_AnyAdmin() {
		AclEntity entity = newAclEntity();
		entity.setAcl(createAcl(ADMIN_ROLE));
		commit();

		return entity;
	}

	public AclEntity create_AdminNotVolunteer() {
		AclEntity entity = newAclEntity();
		entity.setAcl(createAcl(ADMIN_ROLE, "!" + VOLUNTEER_ROLE));
		commit();

		return entity;
	}

	public AclEntity create_AdminNotVolunteer(String id) {
		AclEntity entity = newAclEntity();
		entity.setId(id);
		entity.setAcl(createAcl(ADMIN_ROLE, "!" + VOLUNTEER_ROLE));
		commit();

		return entity;
	}

	public AclEntity create_ownerOrAdmin() {
		AclEntity entity = newAclEntity();
		entity.setOwner(ALLOWED_ACL_USER);
		entity.setAcl(createAcl(ADMIN_ROLE));

		commit();

		return entity;
	}

	public AclEntity create_ownerOrAdmin(String id) {
		AclEntity entity = newAclEntity();
		entity.setId(id);
		entity.setOwner(ALLOWED_ACL_USER);
		entity.setAcl(createAcl(ADMIN_ROLE));

		commit();

		return entity;
	}

	public AclEntity create_Volunteer() {
		AclEntity entity = newAclEntity();
		entity.setAcl(createAcl(VOLUNTEER_ROLE));
		commit();

		return entity;
	}

	
	private AclEntity newAclEntity() {
		return session.create(AclEntity.T);
	}

	public Acl createAcl(String... roles) {
		Acl result = session.create(Acl.T);
		result.setAccessibility(asSet(roles));
		result.setEntries(entries(roles));

		return result;
	}

	private List<AclEntry> entries(String... roles) {
		return Stream.of(roles) //
				.flatMap(role -> toAclEntries(role)) //
				.collect(Collectors.toList());
	}

	private Stream<AclEntry> toAclEntries(String role) {
		String _role = role.startsWith("!") ? role.substring(1) : role;
		AclPermission permission = role.startsWith("!") ? AclPermission.DENY : AclPermission.GRANT;

		return assignedAclOps.stream() //
				.map(aclOp -> createAclEntry(_role, permission, aclOp));
	}

	public AclStandardEntry createAclEntry(String _role, AclPermission permission, AclOperation operation) {
		AclStandardEntry result = session.create(AclStandardEntry.T);
		result.setOperation(operation);
		result.setRole(_role);
		result.setPermission(permission);
		return result;
	}

	public AclPropsOwner create_PropsOwner() {
		AclPropsOwner entity = session.create(AclPropsOwner.T);
		commit();

		return entity;
	}

	protected void commit() {
		session.commit();
	}

}
