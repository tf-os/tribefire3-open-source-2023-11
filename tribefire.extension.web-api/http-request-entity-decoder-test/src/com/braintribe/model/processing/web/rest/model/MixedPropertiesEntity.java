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

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * This entity contains standard headers and custom properties.
 * 
 *
 */
public interface MixedPropertiesEntity extends MixedPropertiesParent {
	
	static EntityType<MixedPropertiesEntity> T = EntityTypes.T(MixedPropertiesEntity.class);
	
	// shared with StandardHeadersEntity

	Set<String> getAcceptEncoding();
	void setAcceptEncoding(Set<String> acceptEncoding);

	String getAuthorization();
	void setAuthorization(String authorization);
	
	// shared with CustomPropertiesEntities
	
	String getStringProperty();
	void setStringProperty(String stringProperty);

	boolean getBooleanProperty();
	void setBooleanProperty(boolean booleanProperty);

	int getIntProperty();
	void setIntProperty(int intProperty);

	// Own properties
	
	float getFloatProperty();
	void setFloatProperty(float floatProperty);
	
	long getLongProperty();
	void setLongProperty(long longProperty);
	
	// Unused property
	
	String getUnusedStringProperty();
	void setUnusedStringProperty(String unusedStringProperty);
}
