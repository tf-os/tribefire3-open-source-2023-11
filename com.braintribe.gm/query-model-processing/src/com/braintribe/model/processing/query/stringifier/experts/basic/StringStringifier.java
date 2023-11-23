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

import com.braintribe.model.processing.query.api.stringifier.QueryStringifierRuntimeException;
import com.braintribe.model.processing.query.api.stringifier.experts.Stringifier;
import com.braintribe.model.processing.query.api.stringifier.experts.StringifierContext;

public class StringStringifier implements Stringifier<String, StringifierContext> {
	@Override
	public String stringify(String operand, StringifierContext context) throws QueryStringifierRuntimeException {
		StringBuilder queryString = new StringBuilder();
		queryString.append("'");

		// Prepare string operand by escaping
		for (final char character : operand.toCharArray()) {
			switch (character) {
			case '\\': {
				queryString.append("\\");
				break;
			}
			case '\'': {
				queryString.append("\'");
				break;
			}
			case '\b': {
				queryString.append("\b");
				break;
			}
			case '\t': {
				queryString.append("\t");
				break;
			}
			case '\n': {
				queryString.append("\n");
				break;
			}
			case '\f': {
				queryString.append("\f");
				break;
			}
			case '\r': {
				queryString.append("\r");
				break;
			}
			default: {
				queryString.append(character);
				break;
			}
			}
		}

		queryString.append("'");
		return queryString.toString();
	}
}
