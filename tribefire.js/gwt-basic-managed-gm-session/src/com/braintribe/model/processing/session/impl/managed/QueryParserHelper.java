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
package com.braintribe.model.processing.session.impl.managed;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.query.parser.api.GmqlParsingError;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;

public class QueryParserHelper {

	protected static Logger logger = Logger.getLogger(QueryParserHelper.class);
	
	
	public static EntityQuery parseEntityQuery (String queryString) {
		Query entityQuery = parseQuery(queryString);
		if (!(entityQuery instanceof EntityQuery))  {
			throw new GmSessionRuntimeException("Query: "+queryString+" is not an EntityQuery.");
		}
		return (EntityQuery) entityQuery;

	}
	
	public static PropertyQuery parsePropertyQuery (String queryString) {
		Query entityQuery = parseQuery(queryString);
		if (!(entityQuery instanceof PropertyQuery))  {
			throw new GmSessionRuntimeException("Query: "+queryString+" is not a PropertyQuery.");
		}
		return (PropertyQuery) entityQuery;

	}

	public static SelectQuery parseSelectQuery (String queryString) {
		Query entityQuery = parseQuery(queryString);
		if (!(entityQuery instanceof SelectQuery))  {
			throw new GmSessionRuntimeException("Query: "+queryString+" is not a SelectQuery.");
		}
		return (SelectQuery) entityQuery;

	}

	
	public static Query parseQuery (String queryString) {
		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		if (parsedQuery.getErrorList().isEmpty()) {
			Query query = parsedQuery.getQuery();
			return query;
		} else {
			StringBuilder msg = new StringBuilder();
			msg.append("The query: "+queryString+" could not be parsed to a valid query.");
			for (GmqlParsingError error : parsedQuery.getErrorList()) {
				msg.append("\n").append(error.getMessage());
			}
			logger.error(msg.toString());
			throw new GmSessionRuntimeException(msg.toString());
		}

	}
	
}
