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
package com.braintribe.model.processing.query.autocompletion.impl;

import com.braintribe.model.processing.query.autocompletion.api.QueryAutoCompletionResult;
import com.braintribe.model.processing.query.autocompletion.api.QueryLexerResult;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.QueryLexer;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.container.AliasInput;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.container.AliasType;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.container.AliasType.CheckType;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.container.QueryLexerToken;

/**
 * Helper to create default results.
 *
 */
public abstract class ContainerFactory {

	public static QueryAutoCompletionResult createQueryAutoCompletionResult() {
		final QueryAutoCompletionResult queryAutoCompletionResult = QueryAutoCompletionResult.T.create();

		// Set default values
		queryAutoCompletionResult.setPossibleHints(null);

		// Done
		return queryAutoCompletionResult;
	}

	public static QueryLexerResult createQueryLexerResult() {
		final QueryLexerResult queryLexerResult = QueryLexerResult.T.create();

		// Set default values (component, filter & alias)
		queryLexerResult.setQueryToken(QueryLexerToken.Unknown);
		queryLexerResult.setFilterString(QueryLexer.emptyString);
		queryLexerResult.setAliasType(null);

		// Set default values (closable)
		queryLexerResult.setInEscapeKeyword(false);
		queryLexerResult.setInString(false);
		queryLexerResult.setBracketScope(0);

		// Done
		return queryLexerResult;
	}

	public static AliasInput createAliasInput() {
		final AliasInput aliasInput = AliasInput.T.create();

		// Set default values
		aliasInput.setAliasMap(null);
		aliasInput.setKeywords(null);

		// Done
		return aliasInput;
	}

	public static AliasType createAliasType() {
		final AliasType aliasType = AliasType.T.create();

		// Set default values
		aliasType.setEntityType(null);
		aliasType.setHasProperties(false);
		aliasType.setPropertyFound(false);
		aliasType.setCheckType(CheckType.None);
		aliasType.setLastAliasPart(QueryLexer.emptyString);

		// Done
		return aliasType;
	}
}
