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
package com.braintribe.model.generic.manipulation;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.ForwardDeclaration;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;

@ForwardDeclaration("com.braintribe.gm:owner-model")
public interface EntityProperty extends Owner {

	final EntityType<EntityProperty> T = EntityTypes.T(EntityProperty.class);

	// @formatter:off
	EntityReference getReference();
	void setReference(EntityReference reference);
	// @formatter:on

	@Override
	default OwnerType ownerType() {
		return OwnerType.ENTITY_PROPERTY;
	}

	@Override
	default GenericEntity ownerEntity() {
		return getReference();
	}
	
	@Override
	default EntityType<?> ownerEntityType() {
		EntityReference reference = getReference();
		if (reference == null)
			return null;

		return GMF.getTypeReflection().getEntityType(reference.getTypeSignature());
	}

	@Override
	// PGA TODO OPTIMIZE
	default Property property() {
		Property property = null; // localEntityProperty.getProperty();

		// if (property != null)
		// return property;

		EntityType<?> entityType = ownerEntityType();

		if (entityType == null)
			return null;

		property = entityType.findProperty(getPropertyName());

		// localEntityProperty.setProperty(property);

		return property;
	}

}
