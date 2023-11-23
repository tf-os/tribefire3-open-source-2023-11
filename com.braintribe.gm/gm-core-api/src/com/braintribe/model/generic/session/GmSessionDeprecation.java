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
package com.braintribe.model.generic.session;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;

interface GmSessionDeprecation {

	/**
	 * @deprecated use {@link GmSession#create(EntityType)}
	 */
	@Deprecated
	<T extends GenericEntity> T createEntity(EntityType<T> entityType);

	/**
	 * @deprecated use {@link GmSession#create(EntityType)}, by either using the type Literal or resolving the {@link EntityType} via
	 *             {@link GenericModelTypeReflection}.
	 */
	@Deprecated
	<T extends GenericEntity> T createEntity(Class<T> entityClass);

	/**
	 * @deprecated use {@link GmSession#create(EntityType)}, after resolving the {@link EntityType} via
	 *             {@link GenericModelTypeReflection#getType(String)}.
	 */
	@Deprecated
	<T extends GenericEntity> T createEntity(String typeSignature);

}
