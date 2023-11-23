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

import java.math.BigDecimal;

import com.braintribe.model.accessdeployment.smart.meta.conversion.DecimalToString;

/**
 * TODO IMPLEMENT
 */
public class DecimalToStringExpert extends AbstractDecimalToStringExpert<BigDecimal, DecimalToString> {

	public static final DecimalToStringExpert INSTANCE = new DecimalToStringExpert();

	protected DecimalToStringExpert() {
	}

	@Override
	protected BigDecimal parse(String value, DecimalToString conversion) {
		if (conversion.getPattern() == null) {
			return new BigDecimal(value);
		}

		Number number = parseNumber(value, conversion.getPattern());
		return new BigDecimal(number.toString());
	}

	@Override
	protected String toString(BigDecimal value, DecimalToString conversion) {
		if (conversion.getPattern() == null) {
			return value.toString();
		}

		return toString(value, conversion.getPattern());
	}

}
