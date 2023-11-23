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
package com.braintribe.model.smartqueryplan.value;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;

import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.queryplan.value.ValueType;

/**
 * 
 */
public interface ConvertedValue extends SmartValue {

	EntityType<ConvertedValue> T = EntityTypes.T(ConvertedValue.class);

	Value getOperand();
	void setOperand(Value operand);

	SmartConversion getConversion();
	void setConversion(SmartConversion conversion);

	boolean getInverse();
	void setInverse(boolean inverse);

	@Override
	default ValueType valueType() {
		return ValueType.extension;
	}

	@Override
	default SmartValueType smartValueType() {
		return SmartValueType.convertedValue;
	}

}
