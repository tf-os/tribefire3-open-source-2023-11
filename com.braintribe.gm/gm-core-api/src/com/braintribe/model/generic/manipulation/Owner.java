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
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.ForwardDeclaration;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;

@Abstract
@ForwardDeclaration("com.braintribe.gm:owner-model")
public interface Owner extends GenericEntity {

	final EntityType<Owner> T = EntityTypes.T(Owner.class);

	// @formatter:off
	String getPropertyName();
	void setPropertyName(String propertyName);
	// @formatter:on

	OwnerType ownerType();

	/**
	 * @return either the {@link EntityReference} (if remote) or the actual entity (if local) that is being manipulated. 
	 */
	GenericEntity ownerEntity();
	
	EntityType<?> ownerEntityType();

	Property property();

}
