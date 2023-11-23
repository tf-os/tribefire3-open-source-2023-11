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
package com.braintribe.model.meta.data.security;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.ExplicitPredicate;
import com.braintribe.model.meta.data.UniversalMetaData;
import com.braintribe.model.meta.selector.RoleSelector;

/**
 * MD to mark that a given type is manageable by an administrator, i.e. it can be accessed bypassing whatever security
 * checks are in place. The actual security - determining whether a given client (doing the MD resolution) is and admin
 * - is typically done via {@link RoleSelector}, possibly in combination with other selectors.
 */
public interface Administrable extends UniversalMetaData, ExplicitPredicate {

	EntityType<Administrable> T = EntityTypes.T(Administrable.class);

}
