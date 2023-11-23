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
package com.braintribe.model.processing.query.stringifier.experts.basic;

import java.util.Date;

import com.braintribe.model.processing.query.api.stringifier.QueryStringifierRuntimeException;
import com.braintribe.model.processing.query.api.stringifier.experts.Stringifier;
import com.braintribe.model.processing.query.api.stringifier.experts.StringifierContext;
import com.braintribe.utils.format.api.CustomDateFormat;
import com.braintribe.utils.format.lcd.FormatTool;

public class DateStringifier implements Stringifier<Date, StringifierContext> {
	@Override
	public String stringify(Date date, StringifierContext context) throws QueryStringifierRuntimeException {
		StringBuilder queryString = new StringBuilder();
		// Get CustomDateFormat from FormatTool expert
		final CustomDateFormat dateFormat = FormatTool.getExpert().getDateFormat();
		if (dateFormat != null) {
			queryString.append("date(");

			// Format string to the parser date-string
			queryString.append(dateFormat.formatDate(date, "yyyy'Y,' MM'M,' dd'D,' HH'H,' mm'm,' ss'S,' SSS's,' Z'Z'"));

			// Return parsed date-string
			queryString.append(")");
			return queryString.toString();
		} else {
			// Expert not found, throw Exception
			throw new QueryStringifierRuntimeException("Could not find format expert: " + CustomDateFormat.class.getName());
		}
	}
}
