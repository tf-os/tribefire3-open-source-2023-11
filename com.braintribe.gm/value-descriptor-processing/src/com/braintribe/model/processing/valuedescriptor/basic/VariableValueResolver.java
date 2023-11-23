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
package com.braintribe.model.processing.valuedescriptor.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.valuedescriptor.api.ValueDescriptorResolver;
import com.braintribe.model.processing.valuedescriptor.api.ValueDescriptorResolvingContext;

public class VariableValueResolver implements ValueDescriptorResolver<Variable, Object>{
	
	@Override
	public Map<Variable, Object> resolve(ValueDescriptorResolvingContext valueDescriptorResolvingContext,Set<Variable> valueDescriptors) {
		Map<Variable, Object> resolvedValues = new HashMap<Variable, Object>();
		for(Variable variable : valueDescriptors)
			resolvedValues.put(variable, resolve(valueDescriptorResolvingContext,variable));
		return resolvedValues;
	}
	
	@Override
	public Object resolve(ValueDescriptorResolvingContext valueDescriptorResolvingContext,Variable valueDescriptor) {
		return valueDescriptor.getDefaultValue();
	}

}
