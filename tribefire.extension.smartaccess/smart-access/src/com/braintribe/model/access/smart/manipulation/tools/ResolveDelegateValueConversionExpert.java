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
package com.braintribe.model.access.smart.manipulation.tools;

import com.braintribe.model.access.smart.manipulation.SmartManipulationProcessor;
import com.braintribe.model.access.smart.manipulation.conversion.ResolveDelegateValueConversion;
import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.smart.SmartAccessException;
import com.braintribe.model.processing.smartquery.eval.api.ConversionDirection;
import com.braintribe.model.processing.smartquery.eval.api.SmartConversionExpert;

/**
 * 
 */
class ResolveDelegateValueConversionExpert implements SmartConversionExpert<ResolveDelegateValueConversion> {

	private final SmartManipulationProcessor smp;
	private final PropertyValueResolver propertyValueResolver;
	private final ValueConverter valueConverter;

	public ResolveDelegateValueConversionExpert(SmartManipulationProcessor smp, ValueConverter valueConverter) {
		this.smp = smp;
		this.propertyValueResolver = smp.propertyValueResolver();
		this.valueConverter = valueConverter;
	}

	@Override
	public Object convertValue(ResolveDelegateValueConversion conversion, Object value, ConversionDirection direction) {
		if (direction != ConversionDirection.smart2Delegate) {
			throw new SmartAccessException("This direction of conversion is not supported by this expert!");
		}

		EntityReference ref = (EntityReference) value;

		String propertyName = conversion.getPropertyName();
		propertyName = smp.findDelegatePropertyForKeyPropertyOfCurrentSmartType(propertyName, ref);

		SmartConversion nestedConversion = conversion.getNestedConversion();

		Object simpleValue = propertyValueResolver.acquireDelegatePropertyValue(ref, propertyName);

		if (nestedConversion != null) {
			simpleValue = convert(nestedConversion, simpleValue);
		}

		return simpleValue;
	}

	private Object convert(SmartConversion nestedConversion, Object simpleValue) {
		return valueConverter.convertToDelegate(simpleValue, nestedConversion, false);
	}
}
