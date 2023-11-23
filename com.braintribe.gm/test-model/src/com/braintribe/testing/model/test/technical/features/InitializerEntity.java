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
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * An entity with various properties with {@link Initializer}s, i.e. default values.
 *
 * @author michael.lafite
 */

public interface InitializerEntity extends GenericEntity {

	EntityType<InitializerEntity> T = EntityTypes.T(InitializerEntity.class);

	@Initializer("enum(com.braintribe.testing.model.test.technical.features.SimpleEnum,TWO)")
	SimpleEnum getEnumProperty();
	void setEnumProperty(SimpleEnum enumProperty);

	@Initializer("'abc'")
	String getStringProperty();
	void setStringProperty(String stringProperty);

	@Initializer("true")
	Boolean getBooleanProperty();
	void setBooleanProperty(Boolean booleanProperty);

	@Initializer("123")
	Integer getIntegerProperty();
	void setIntegerProperty(Integer integerProperty);

	@Initializer("123l")
	Long getLongProperty();
	void setLongProperty(Long longProperty);

	@Initializer("123.45f")
	Float getFloatProperty();
	void setFloatProperty(Float floatProperty);

	@Initializer("123.45d")
	Double getDoubleProperty();
	void setDoubleProperty(Double doubleProperty);

	@Initializer("123.45b")
	BigDecimal getDecimalProperty();
	void setDecimalProperty(BigDecimal decimalProperty);

	@Initializer("now()")
	Date getDateProperty();
	void setDateProperty(Date dateProperty);

	@Initializer("true")
	boolean getPrimitiveBooleanProperty();
	void setPrimitiveBooleanProperty(boolean primitiveBooleanProperty);

	@Initializer("123")
	int getPrimitiveIntegerProperty();
	void setPrimitiveIntegerProperty(int primitiveIntegerProperty);

	@Initializer("123l")
	long getPrimitiveLongProperty();
	void setPrimitiveLongProperty(long primitiveLongProperty);

	@Initializer("123.45f")
	float getPrimitiveFloatProperty();
	void setPrimitiveFloatProperty(float primitiveFloatProperty);

	@Initializer("123.45d")
	double getPrimitiveDoubleProperty();
	void setPrimitiveDoubleProperty(double primitiveDoubleProperty);
}
