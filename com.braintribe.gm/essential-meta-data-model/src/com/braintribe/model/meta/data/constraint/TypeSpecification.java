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
package com.braintribe.model.meta.data.constraint;

import java.util.Objects;


import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.ModelSkeletonCompatible;


public interface TypeSpecification extends TypeRestriction, ModelSkeletonCompatible {

	EntityType<TypeSpecification> T = EntityTypes.T(TypeSpecification.class);

	// NOTE this might not exist, so if needed, create it 
	String STRING_TYPE_SPECIFICATION_GLOBAL_ID = "typeSpecification:string";
	
	GmType getType();
	void setType(GmType type);
	
	@Override
	default boolean isInstance(Object instance) {
		return Objects.requireNonNull(getType(), "type property may not be null").reflectionType().isInstance(instance);
	}
}
