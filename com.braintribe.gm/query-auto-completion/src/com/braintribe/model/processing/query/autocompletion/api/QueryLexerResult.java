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
package com.braintribe.model.processing.query.autocompletion.api;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.container.QueryLexerToken;

/**
 * The actual result information of the current token -> from the QueryAutocompletionLexer
 */
public interface QueryLexerResult extends StandardIdentifiable {
	
	final EntityType<QueryLexerResult> T = EntityTypes.T(QueryLexerResult.class);

	boolean getInString();

	void setInString(boolean value);

	boolean getInEscapeKeyword();

	void setInEscapeKeyword(boolean value);

	int getBracketScope();

	void setBracketScope(int value);

	QueryLexerToken getQueryToken();

	void setQueryToken(QueryLexerToken value);

	GmType getAliasType();

	void setAliasType(GmType value);

	String getFilterString();

	void setFilterString(String value);
	
	GmType getPropertyType();
	
	void setPropertyType(GmType gmType);
	
	String getKeywordMatch();
	
	void setKeywordMatch(String keywordMatch);
	
	String getNumberString();
	
	void setNumberString(String numberString);
}
