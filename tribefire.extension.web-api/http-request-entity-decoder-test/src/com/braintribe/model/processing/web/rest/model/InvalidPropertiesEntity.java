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
package com.braintribe.model.processing.web.rest.model;

import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * This entity contains invalid properties: Map, Entity, Object and a property that cannot be parsed to its type
 * 
 *
 */
public interface InvalidPropertiesEntity extends InvalidPropertiesParent {

	static EntityType<InvalidPropertiesEntity> T = EntityTypes.T(InvalidPropertiesEntity.class);
	
	Map<String, String> getMapProperty();
	void setMapProperty(Map<String, String> mapProperty);

	CustomPropertiesEntity getEntityProperty();
	void setEntityProperty(CustomPropertiesEntity entityProperty);
	
	Object getObjectProperty();
	void setObjectProperty(Object objectProperty);

	int getInvalidIntProperty();
	void setInvalidIntProperty(int invalidIntProperty);
}
