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

import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.Source;

/**
 * Gmql parsing results are represented by this object.
 * 
 * It contains the following items:
 * <ul>
 * <li>Query: which can be a valid query or what the parser was able to create
 * with some erroneous input</li>
 * <li>List of {@link GmqlParsingError} that were identified. The list is empty
 * if a valid query string has been provided. There are two concrete definitions
 * for errors, namely {@link GmqlSyntacticParsingError} and 
 * {@link GmqlSemanticParsingError}</li>
 * <li>Map between the aliases and actual sources {@link Source} in the query</li>
 * <li>A convenience boolean value that indicates the validity of the query</li>
 * </ul>
 * 
 */
public interface ParsedQuery extends GenericEntity {

	EntityType<ParsedQuery> T = EntityTypes.T(ParsedQuery.class);

	Query getQuery();
	void setQuery(Query query);
	
	List<GmqlParsingError> getErrorList(); 
	void setErrorList(List<GmqlParsingError> errorList);
	
	Map<String,Variable> getVariablesMap(); 
	void setVariablesMap(Map<String,Variable> variablesMap);
	
	Map<String,Source> getSourcesRegistry();
	void setSourcesRegistry(Map<String,Source> sourceRegistry);
	
	boolean getIsValidQuery();
	void setIsValidQuery(boolean isValidQuery);
	
}
