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
package com.braintribe.model.processing.query;

import java.util.Map;

import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.value.Variable;

public class VariableReplacingEvolutionContext extends StandardCloningContext{
	
	private boolean treatUnknownVariablesAsNull = false;
	private Map<String, Object> values;
	
	public void setValues(Map<String, Object> values) {
		this.values = values;
	}
	
	public void setTreatUnknownVariablesAsNull(boolean treatUnknownVariablesAsNull) {
		this.treatUnknownVariablesAsNull = treatUnknownVariablesAsNull;
	}
	
	@Override
	public Object postProcessCloneValue(GenericModelType propertyType,Object clonedValue) {			
		if(clonedValue instanceof Variable){
			Variable variable = (Variable) clonedValue;
			Object value = values.get(variable.getName());
			if (value == null && !treatUnknownVariablesAsNull) {
				throw new TemplateEvaluationException("no value found for variable name '" + variable.getName() + "'");
			}
			return value;
		}
		return clonedValue;
	}
}
