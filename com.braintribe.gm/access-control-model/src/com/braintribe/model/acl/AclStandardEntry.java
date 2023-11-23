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

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * {@link AclEntry} that allows configuration of one of the {@link AclOperation standard} {@link #getOperation() operations}.
 *
 * @see AclEntry
 * @see AclCustomOperationEntry
 * 
 * @author Dirk Scheffler
 */
public interface AclStandardEntry extends AclEntry {
	EntityType<AclStandardEntry> T = EntityTypes.T(AclStandardEntry.class);

	/** Mandatory enum constant property to satisfy the {@link AclEntry#operation()} method */
	@Mandatory
	AclOperation getOperation();
	void setOperation(AclOperation access);

	/** @see AclEntry#operation() */
	@Override
	default String operation() {
		return getOperation().name();
	}
}
