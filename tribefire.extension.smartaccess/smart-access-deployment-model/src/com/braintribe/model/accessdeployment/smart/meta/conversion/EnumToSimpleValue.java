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
package com.braintribe.model.accessdeployment.smart.meta.conversion;

import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmEnumConstant;

/**
 * @see SmartConversion
 */
public interface EnumToSimpleValue extends SmartConversion {

	EntityType<EnumToSimpleValue> T = EntityTypes.T(EnumToSimpleValue.class);

	/**
	 * explicit mappings. If no mapping for a constant is given the name of the enum constant is assumed to be the
	 * value.
	 */
	void setValueMappings(Map<GmEnumConstant, Object> valueMappings);
	Map<GmEnumConstant, Object> getValueMappings();

}
