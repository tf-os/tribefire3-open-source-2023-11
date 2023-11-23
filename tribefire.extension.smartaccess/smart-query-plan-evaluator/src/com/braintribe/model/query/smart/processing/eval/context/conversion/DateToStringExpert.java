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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.braintribe.model.accessdeployment.smart.meta.conversion.DateToString;
import com.braintribe.model.processing.smartquery.eval.api.RuntimeSmartQueryEvaluationException;

/**
 * 
 */
public class DateToStringExpert extends AbstractToStringExpert<Date, DateToString> {

	public static final DateToStringExpert INSTANCE = new DateToStringExpert();

	private DateToStringExpert() {
	}

	@Override
	protected Date parse(String value, DateToString conversion) {
		SimpleDateFormat sdf = new SimpleDateFormat(conversion.getPattern());

		try {
			return sdf.parse(value);

		} catch (ParseException e) {
			throw new RuntimeSmartQueryEvaluationException("Cannot convert '" + value + "' to a Date.", e);
		}
	}

	@Override
	protected String toString(Date value, DateToString conversion) {
		SimpleDateFormat sdf = new SimpleDateFormat(conversion.getPattern());

		return sdf.format(value);
	}

}
