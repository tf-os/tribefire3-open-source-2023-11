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
package com.braintribe.model.processing.query.parser;

import java.util.List;

import com.braintribe.model.processing.query.parser.api.GmqlQueryParser;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.processing.query.parser.impl.GmqlQueryParserImpl;

/**
 * Entry point for all Gmql query parsing
 * 
 */
public abstract class QueryParser {

	/**
	 * See {@link GmqlQueryParser#parse(String)}
	 */
	public static ParsedQuery parse(String queryString) {
		return new GmqlQueryParserImpl().parse(queryString);
	}

	/**
	 * @return A list of all the Gmql keywords used by the parser. These keywords can
	 *         be escaped using double quotes and utilised as normal identifiers.
	 */
	public static List<String> getKeywords() {
		return GmqlQueryParserImpl.getKeywords();
	}

}
