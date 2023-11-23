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
package com.braintribe.gwt.customization.client.tests.model.initializer;

import java.math.BigDecimal;
import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface InitializedEntity extends GenericEntity {

	EntityType<InitializedEntity> T = EntityTypes.T(InitializedEntity.class);

	// @formatter:off
	@Initializer("99")
	int getIntValue();
	void setIntValue(int value);

	@Initializer("11L")
	long getLongValue();
	void setLongValue(long value);

	@Initializer("+123f")
	float getFloatValue();
	void setFloatValue(float value);

	@Initializer("-123D")
	double getDoubleValue();
	void setDoubleValue(double value);

	@Initializer("+1.0e30f")
	float getBigFloatValue();
	void setBigFloatValue(float value);

	@Initializer("-1.0e30d")
	double getBigDoubleValue();
	void setBigDoubleValue(double value);

	@Initializer("true")
	boolean getBooleanValue();
	void setBooleanValue(boolean value);

	@Initializer("99889988.00b")
	BigDecimal getDecimalValue();
	void setDecimalValue(BigDecimal value);

	@Initializer("now()")
	Date getDateValue();
	void setDateValue(Date value);

	@Initializer("enum(com.braintribe.gwt.customization.client.tests.model.initializer.Color,green)")
	Color getEnumValue();
	void setEnumValue(Color value);

	@Initializer("green")
	Color getEnumShort();
	void setEnumShort(Color value);
	
	Date getUninitializedDateValue();
	void setUninitializedDateValue(Date value);
	
	long getUninitializedLongValue();
	void setUninitializedLongValue(long value);
	// @formatter:on

	boolean getUninitializedBooleanValue();
	void setUninitializedBooleanValue(boolean value);

}
