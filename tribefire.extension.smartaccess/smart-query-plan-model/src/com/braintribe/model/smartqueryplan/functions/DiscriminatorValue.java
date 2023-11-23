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
package com.braintribe.model.smartqueryplan.functions;

import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.query.functions.QueryFunction;

/**
 * This is a constant value which is solely dependent on the signature of the entity, just like discriminator in
 * hibernate.
 */
public interface DiscriminatorValue extends QueryFunction {

	EntityType<DiscriminatorValue> T = EntityTypes.T(DiscriminatorValue.class);

	/** This (entitySignature) is here for debugging purposes only */
	String getEntityPropertySignature();
	void setEntityPropertySignature(String entityPropertySignature);

	int getSignaturePosition();
	void setSignaturePosition(int signaturePosition);

	Map<String, Object> getSignatureToStaticValue();
	void setSignatureToStaticValue(Map<String, Object> signatureToStaticValue);

}
