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
package com.braintribe.model.processing.query.smart.test.model.smart;

import com.braintribe.model.generic.annotation.TypeRestriction;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.smart.test.model.accessA.Id2UniqueEntityA;

/**
 * This is an entity which has an id, which not mapped to the id of the delegate, but to a unique property on that delegate.
 * 
 * Mapped to {@link Id2UniqueEntityA}
 */
public interface Id2UniqueEntity extends SmartGenericEntity {
	
	EntityType<Id2UniqueEntity> T = EntityTypes.T(Id2UniqueEntity.class);

	@Override
	@TypeRestriction(String.class)
	<T> T getId();
	@Override
	void setId(Object unique);

	String getDescription();
	void setDescription(String description);

}
