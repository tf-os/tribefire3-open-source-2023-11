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
package com.braintribe.model.processing.query.test.stringifier.model;

import java.math.BigDecimal;

import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.test.model.Color;

public interface TypeTestModel extends StandardStringIdentifiable {

	EntityType<TypeTestModel> T = EntityTypes.T(TypeTestModel.class);

	Double getDoubleValue();
	void setDoubleValue(Double doubleValue);

	BigDecimal getDecimalValue();
	void setDecimalValue(BigDecimal decimalValue);

	Float getFloatValue();
	void setFloatValue(Float floatValue);

	Long getLongValue();
	void setLongValue(Long longValue);

	Integer getIntValue();
	void setIntValue(Integer intValue);

	Boolean getBoolValue();
	void setBoolValue(Boolean boolValue);

	Color getEnumValue();
	void setEnumValue(Color enumValue);

	TypeTestModel getEntityValue();
	void setEntityValue(TypeTestModel entityValue);

}
