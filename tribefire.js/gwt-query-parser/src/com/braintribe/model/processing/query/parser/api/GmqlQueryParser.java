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
package com.braintribe.model.processing.query.parser.api;

import com.braintribe.model.query.Query;

/**
 * Gmql query parser interface that should be used by other artifacts.
 * 
 */

public interface GmqlQueryParser {

	/**
	 * Parses a string representation of a query in Gmql format. The result of
	 * the parsing process is returned in a {@link ParsedQuery}. If the query
	 * string is not valid for either syntactic or semantic reasons, a list of
	 * parsing errors is included in the wrapper object.
	 * 
	 * @param queryString
	 *            String representing a {@link Query} in Gmql syntax.
	 * @return {@link ParsedQuery} representing the result of the parsing
	 */
	ParsedQuery parse(String queryString);

}
