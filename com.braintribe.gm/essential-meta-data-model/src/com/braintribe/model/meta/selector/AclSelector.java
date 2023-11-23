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
package com.braintribe.model.meta.selector;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Matches if the {@link #getOperation() operation specified here} is granted for the context entity (one for which we are resolving the MD). The
 * operation is not granted only if given entity is an instance of com.braintribe.model.acl.HasAcl, and it's isOperationGranted method returns
 * <tt>false</tt>.
 * <p>
 * Roles for this check check are taken from the RoleAspect, if not available, it falls back to UserInfoAttribute in the {@link AttributeContext}.
 */
public interface AclSelector extends MetaDataSelector {

	EntityType<AclSelector> T = EntityTypes.T(AclSelector.class);

	String getOperation();
	void setOperation(String operation);

}
