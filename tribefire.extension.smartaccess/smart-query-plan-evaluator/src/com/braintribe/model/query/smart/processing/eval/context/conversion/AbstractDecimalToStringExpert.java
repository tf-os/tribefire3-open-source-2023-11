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

import java.text.DecimalFormat;
import java.text.ParseException;

import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.query.smart.processing.SmartQueryEvaluatorRuntimeException;

/**
 * 
 */
public abstract class AbstractDecimalToStringExpert<T extends Number, C extends SmartConversion> extends AbstractToStringExpert<T, C> {

	protected Number parseNumber(String value, String pattern) {
		try {
			return formatFor(pattern).parse(value);

		} catch (ParseException e) {
			throw new SmartQueryEvaluatorRuntimeException("Error while parsing String: " + value, e);
		}
	}

	protected String toString(Number value, String pattern) {
		return formatFor(pattern).format(value);
	}

	/**
	 * This is not nice, but let's have this protected so that we can also set the decimal separator in unit tests. Maybe later we'll also
	 * want to configure it per conversion...
	 */
	protected DecimalFormat formatFor(String pattern) {
		return pattern == null ? new DecimalFormat() : new DecimalFormat(pattern);
	}

}
