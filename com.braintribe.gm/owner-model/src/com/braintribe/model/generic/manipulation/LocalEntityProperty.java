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

import com.braintribe.model.generic.GenericEntity;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.Property;

/* NOTE that other entities from this model have forward declarations in the RootModel. */


public interface LocalEntityProperty extends Owner {

	final EntityType<LocalEntityProperty> T = EntityTypes.T(LocalEntityProperty.class);

	GenericEntity getEntity();
	void setEntity(GenericEntity entity);

	// @formatter:off
//	 @Transient
//	 void setProperty(Property property);
//	  
//	  @Transient
//	  Property getProperty();
	// @formatter:on

	@Override
	default OwnerType ownerType() {
		return OwnerType.LOCAL_ENTITY_PROPERTY;
	}

	@Override
	default GenericEntity ownerEntity() {
		return getEntity();
	}
	
	@Override
	default EntityType<?> ownerEntityType() {
		GenericEntity entity = getEntity();
		if (entity == null)
			return null;

		return entity.entityType();
	}

	@Override
	// TODO OPTIMIZE
	default Property property() {
		Property property = null; // localEntityProperty.getProperty();

		// if (property != null)
		// return property;

		GenericEntity entity = getEntity();
		if (entity == null)
			return null;

		property = entity.entityType().findProperty(getPropertyName());

		// localEntityProperty.setProperty(property);

		return property;
	}

}
