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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A single piece of configuration which specifies a grant or deny {@link #getPermission() permission} for a {@link #getRole() role} to perform given
 * {@link #operation() operation}.
 * <p>
 * Note that for the ACL to work properly, user should not create it's own custom sub-types, but extend either {@link AclCustomEntry} or
 * {@link AclCustomOperationEntry}!
 * 
 * @see Acl
 * @see AclCustomEntry
 * @see AclStandardEntry
 * @see AclCustomOperationEntry
 * 
 * @author Dirk Scheffler
 */
@Abstract
public interface AclEntry extends GenericEntity {

	EntityType<AclEntry> T = EntityTypes.T(AclEntry.class);

	/**
	 * Specifies the permission of this entry.
	 * <p>
	 * Note that {@link AclPermission#DENY DENY} has higher priority than {@link AclPermission#GRANT GRANT}, i.e. if two entries with same values but
	 * opposite permissions are defined, DENY is resolved.
	 */
	@Initializer("enum(com.braintribe.model.acl.AclPermission,GRANT)")
	@Mandatory
	AclPermission getPermission();
	void setPermission(AclPermission permission);

	/** The role who's permissions are configured by this entry. */
	@Mandatory
	String getRole();
	void setRole(String role);

	/**
	 * The operation in form of a functional method that is implemented with the default methods {@link AclStandardEntry#operation()},
	 * {@link AclCustomEntry#operation()} and {@link AclCustomOperationEntry#operation()}.
	 */
	default String operation() {
		throw new UnsupportedOperationException(
				"This code should not have been reached. This method is implemented in " + AclStandardEntry.class.getSimpleName() + ", "
						+ AclCustomEntry.class.getSimpleName() + " and " + AclCustomOperationEntry.class.getSimpleName()
						+ ". Every ACL entry must be a sub-type of one of those, and must never implement this method itself.");
	}

}
