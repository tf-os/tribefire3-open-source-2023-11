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
package com.braintribe.model.manipulation.parser.impl.model.ambig;

import java.math.BigDecimal;
import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * To test handling of types with same simple name, this conflicts with {@link com.braintribe.model.manipulation.parser.impl.model.Joat}.
 */
public interface Joat extends GenericEntity {

	final EntityType<Joat> T = EntityTypes.T(Joat.class);

	String getStringValue();
	void setStringValue(String stringValue);

	boolean getBooleanValue();
	void setBooleanValue(boolean value);

	Date getDateValue();
	void setDateValue(Date value);

	BigDecimal getDecimalValue();
	void setDecimalValue(BigDecimal value);

	int getIntegerValue();
	void setIntegerValue(int value);

	long getLongValue();
	void setLongValue(long value);

	float getFloatValue();
	void setFloatValue(float value);

	double getDoubleValue();
	void setDoubleValue(double value);

}
