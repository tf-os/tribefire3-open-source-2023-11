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

import com.braintribe.model.accessdeployment.smart.meta.conversion.BooleanToString;

/**
 * 
 */
public class BooleanToStringExpert extends AbstractToStringExpert<Boolean, BooleanToString> {

	public static final BooleanToStringExpert INSTANCE = new BooleanToStringExpert();

	private static final String DEFAULT_TRUE = Boolean.TRUE.toString();
	private static final String DEFAULT_FALSE = Boolean.FALSE.toString();

	private BooleanToStringExpert() {
	}

	@Override
	protected Boolean parse(String value, BooleanToString conversion) {
		if (matches(value, conversion.getTrueValue())) {
			return Boolean.TRUE;
		}

		if (matches(value, conversion.getFalseValue())) {
			return Boolean.TRUE;
		}

		return Boolean.parseBoolean(value);
	}

	private boolean matches(String stringValue, String referenceValue) {
		return referenceValue != null && stringValue.trim().equals(referenceValue);
	}

	@Override
	protected String toString(Boolean value, BooleanToString conversion) {
		if (value) {
			return valueOrDefault(conversion.getTrueValue(), DEFAULT_TRUE);
		} else {
			return valueOrDefault(conversion.getFalseValue(), DEFAULT_FALSE);
		}
	}

	private String valueOrDefault(String value, String defaultValue) {
		return value != null ? value : defaultValue;
	}

}
