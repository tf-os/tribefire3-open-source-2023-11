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
package com.braintribe.model.query.smart.processing.eval.context.conversion;

import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.processing.smartquery.eval.api.ConversionDirection;
import com.braintribe.model.processing.smartquery.eval.api.SmartConversionExpert;

/**
 * Basic class for simple "to-string" {@link SmartConversionExpert}s.
 */
public abstract class AbstractToSimpleTypeExpert<T, C extends SmartConversion, S> implements SmartConversionExpert<C> {

	@Override
	public Object convertValue(C conversion, Object value, ConversionDirection direction) {
		if (value == null) {
			return null;
		}

		boolean valueIsDelegate = (direction == ConversionDirection.delegate2Smart);

		return convert(value, conversion, conversion.getInverse() == valueIsDelegate);
	}

	@SuppressWarnings("unchecked")
	protected Object convert(Object value, C conversion, boolean valueIsSimple) {
		if (valueIsSimple) {
			return parse((S) value, conversion);
		} else {
			return toSimpleValue((T) value, conversion);
		}
	}

	protected abstract T parse(S value, C conversion);

	/**
	 * @param conversion
	 *            actual instance of {@link SmartConversion} with possible parameters
	 */
	protected abstract S toSimpleValue(T value, C conversion);

}
