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

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.Property;

/**
 * Base for an {@link AclEntry} with a custom operation type.
 * <p>
 * The basic ACL entry sub-types are {@link AclStandardEntry}, which has {@link AclOperation} as it's operation type, which defines all it's possible
 * operations. Then there is {@link AclCustomOperationEntry}, which is modeled to be fully flexible, with it's operation property being of type
 * String, but that lacks the convenience of being able to select from a set of known values.
 * <p>
 * This entry is meant as mixture in between, where the sub-type is supposed to have a property called "operation" of any type it wants (most probably
 * a custom enum) and this abstract type ensures that the Acl mechanism uses the {@link Object#toString() toString} value of that operation.
 * <p>
 * NOTE that 
 * 
 * @see AclEntry
 * 
 * @author Dirk Scheffler
 */
@Abstract
public interface AclCustomOperationEntry extends AclEntry {
	EntityType<AclCustomOperationEntry> T = EntityTypes.T(AclCustomOperationEntry.class);

	/**
	 * @see AclEntry#operation()
	 */
	@Override
	default String operation() {
		Property property = entityType().getProperty("operation");
		Object propertyValue = property.get(this);
		return propertyValue == null ? null : propertyValue.toString();
	}
}
