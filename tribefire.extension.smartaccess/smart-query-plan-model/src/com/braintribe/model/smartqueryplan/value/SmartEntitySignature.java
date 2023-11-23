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

import java.util.Map;

import com.braintribe.model.queryplan.value.ValueType;

public interface SmartEntitySignature extends SmartValue {

	EntityType<SmartEntitySignature> T = EntityTypes.T(SmartEntitySignature.class);

	String getSignature();
	void setSignature(String signature);

	int getTuplePosition();
	void setTuplePosition(int tuplePosition);

	Map<String, String> getSignatureMapping();
	void setSignatureMapping(Map<String, String> signatureMapping);

	@Override
	default ValueType valueType() {
		return ValueType.extension;
	}

	@Override
	default SmartValueType smartValueType() {
		return SmartValueType.smartEntitySignature;
	}

}
