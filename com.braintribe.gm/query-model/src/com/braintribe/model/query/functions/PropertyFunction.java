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
package com.braintribe.model.query.functions;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.query.PropertyOperand;

/**
 * {@link PropertyFunction}s are functions that operate on properties and have a configurable {@link PropertyOperand}
 * 
 * @deprecated this (function for just a property) makes no sense, functions (in general) must work on operands of many different types
 */
@Deprecated
@Abstract
public interface PropertyFunction extends QueryFunction {

	EntityType<PropertyFunction> T = EntityTypes.T(PropertyFunction.class);

	void setPropertyOperand(PropertyOperand propertyOperand);
	PropertyOperand getPropertyOperand();
}
