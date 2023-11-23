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
package com.braintribe.model.processing.test.itw.entity;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Used for test in efficient mode, whether the primitive values are initialized correctly
 */
public interface PrimitivePropsEntity extends GenericEntity {

	EntityType<PrimitivePropsEntity> T = EntityTypes.T(PrimitivePropsEntity.class);

	int getIntValue();
	void setIntValue(int intValue);

	long getLongValue();
	void setLongValue(long longValue);

	float getFloatValue();
	void setFloatValue(float flaotValue);

	double getDoubleValue();
	void setDoubleValue(double doubleValue);

	boolean getBooleanValue();
	void setBooleanValue(boolean doubleValue);

}
