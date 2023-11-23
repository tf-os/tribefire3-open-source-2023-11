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
package com.braintribe.model.acl;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * <p>
 * HasAcl is the super type for access controlled entity types. It optionally holds an {@link #getOwner() owner} that
 * has full access on the instance. All other permissions can be optionally by assigning a sharable {@link Acl} to the
 * {@link #getAcl() acl} property.
 * 
 * <p>
 * If an instance has an owner the absence of an {@link Acl} means that no one else has any access on the instance. If
 * an instance has no owner the absence of an {@link Acl} means that everybody has full access on the instance.
 * 
 * <p>
 * {@link #isOperationGranted(AclOperation, String, Set)} and {@link #isOperationGranted(String, String, Set)} can be
 * used to conveniently check access for a given user and its roles
 * 
 * <p>
 * A security aspect configured to a persistence layer should enrich queries on HasAcl sub types to filter by access
 * rights within the persistence layer to make it efficient and to properly support paging. Also the updates on the
 * persistence layer should be checked to happen within the limits of the access rights. Furthermore the return deep
 * structure from the filtered top level entities should be trimmed where every the access control demands. This can
 * only happen after the initial query already filtered the top level.
 * 
 * @author Dirk Scheffler
 */
@Abstract
public interface HasAcl extends GenericEntity {

	EntityType<HasAcl> T = EntityTypes.T(HasAcl.class);

	String owner = "owner";
	String acl = "acl";

	/**
	 * The optional owner (user name) that has full access. If given and the {@link #getAcl() acl} property is null no
	 * one else has any access. If not given and the {@link #getAcl() acl} property is null everyone has full access.
	 */
	String getOwner();
	void setOwner(String owner);

	/**
	 * The detailed configuration of access control which are permissions on operations for roles
	 */
	Acl getAcl();
	void setAcl(Acl acl);

	/**
	 * Checks access rights for operations for a given user and its roles. The algorithm makes use of the
	 * {@link #getOwner() owner} and {@link #getAcl()} properties.
	 * 
	 * @see #getOwner()
	 * @see Acl
	 * 
	 * @param operation
	 *            the operation expressed as a string (to conveniently check for standard operations use
	 *            {@link #isOperationGranted(AclOperation, String, Set)})
	 * @param user
	 *            the user name of the user for which the access is checked. The user name will be used to check
	 *            ownership which grants full access.
	 * @param givenRoles
	 *            the user roles that are used to check access rights via the access control details found in
	 *            {@link #getAcl() acl}.
	 * @return true if the access is granted
	 */
	default boolean isOperationGranted(String operation, String user, Set<String> givenRoles) {
		String owner = getOwner();

		if (owner != null && owner.equals(user))
			return true;

		Acl accessControl = getAcl();
		return accessControl != null ? accessControl.isOperationGranted(operation, givenRoles) : owner == null;
	}

	/**
	 * Convenience access check for for {@link AclOperation standard operations} which will use the name of the given
	 * enum constant to delegate the check to the string based {@link #isOperationGranted(String, String, Set)}
	 * 
	 * @return true if the access is granted.
	 */
	default boolean isOperationGranted(AclOperation operation, String user, Set<String> givenRoles) {
		return isOperationGranted(operation.name(), user, givenRoles);
	}
}
