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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeIkpaEntityA;

/**
 * Mapped to {@link CompositeIkpaEntityA}:
 * 
 * Property {@link SmartPersonA#getCompositeIkpaEntity()} is mapped via:
 * <ul>
 * <li>delegate*.personId = PersonA.id</li>
 * <li>delegate*.personName = PersonA.name</li>
 * </ul>
 * 
 * Property {@link SmartPersonA#getCompositeIkpaEntity()} is mapped via:
 * <ul>
 * <li>delegate*.personId_Set = PersonA.id</li>
 * <li>delegate*.personName_Set = PersonA.name</li>
 * </ul>
 * 
 * (*) - delegate means {@link CompositeIkpaEntityA}
 */
public interface CompositeIkpaEntity extends StandardSmartIdentifiable {
	
	EntityType<CompositeIkpaEntity> T = EntityTypes.T(CompositeIkpaEntity.class);

	String getDescription();
	void setDescription(String description);

}
