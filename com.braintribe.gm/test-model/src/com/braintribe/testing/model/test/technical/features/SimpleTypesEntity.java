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
package com.braintribe.testing.model.test.technical.features;

import java.math.BigDecimal;
import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.SimpleType;

/**
 * An entity with properties for all {@link SimpleType}s, but no collections or relations to other entities.
 *
 * @author michael.lafite
 *
 * @see PrimitiveTypesEntity
 * @see ComplexEntity
 */

public interface SimpleTypesEntity extends GenericEntity {

	EntityType<SimpleTypesEntity> T = EntityTypes.T(SimpleTypesEntity.class);

	String getStringProperty();
	void setStringProperty(String stringProperty);

	Boolean getBooleanProperty();
	void setBooleanProperty(Boolean booleanProperty);

	Integer getIntegerProperty();
	void setIntegerProperty(Integer integerProperty);

	Long getLongProperty();
	void setLongProperty(Long longProperty);

	Float getFloatProperty();
	void setFloatProperty(Float floatProperty);

	Double getDoubleProperty();
	void setDoubleProperty(Double doubleProperty);

	Date getDateProperty();
	void setDateProperty(Date dateProperty);

	BigDecimal getDecimalProperty();
	void setDecimalProperty(BigDecimal decimalProperty);

}
