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

import java.math.BigDecimal;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.processing.query.autocompletion.api.QueryLexerResult;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.QueryLexer;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.container.AliasInput;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.container.QueryLexerToken;
import com.braintribe.utils.lcd.StringTools;

/**
 * Used for recognizing the combined token (QueryLexer -> single token).
 * It chains tokens (current one plus previous ones, to create something that
 * 'makes sense').
 * Ex.: a whole string, or ignoring extra spaces
 * 
 * Second layer for getting meaningful tokens (first layer in the QueryLexer).
 *
 */
public abstract class AutoCompletionLexer {
	
	private static final String DOT = QueryLexerToken.Dot.getKeywords()[0];

	/******************************** Public Methods ********************************/

	public static QueryLexerResult getQueryToken(final String queryString, final int cursorPosition, final AliasInput aliasInput) {
		// Get and check current QUERY LEXER result
		final QueryLexerResult lexerResult = QueryLexer.getQueryToken(queryString, cursorPosition, aliasInput);
		
		if (lexerResult.getInString()) {
			if (!lexerResult.getInEscapeKeyword()) {
				// Set "Enter string" token
				lexerResult.setQueryToken(QueryLexerToken.StringInput);
				//lexerResult.setFilterString(QueryLexer.emptyString);
			}
			
			return lexerResult;
		}
		
		if (lexerResult.getInEscapeKeyword() == true) {
			// Handle escaped keyword delimiter token - Note: That may change the token result
			handleEscapedKeywordDelimiter(queryString, cursorPosition, aliasInput, lexerResult);
			return lexerResult;
		}
		
		switch (lexerResult.getQueryToken()) {
		case Alias: {
			/* This check is needed for spaced alias queries. */

			// Get previous token result and check previous query token
			final QueryLexerResult previousTokenResult = getQueryToken(queryString, cursorPosition - 1, aliasInput);
			switch (previousTokenResult.getQueryToken()) {
			case AliasPropertyInput:
			case AliasPropertyDelimiter: {
				// Its still an alias property input so set the previous token
				lexerResult.setQueryToken(QueryLexerToken.AliasPropertyInput);
				lexerResult.setAliasType(previousTokenResult.getAliasType());
				lexerResult.setPropertyType(previousTokenResult.getPropertyType());

				break;
			}
			default:
				break;
			}

			break;
		}
		case AliasProperty:
		case AliasPropertyInput:
		case AliasEntityProperty: {
			/* This check is needed for spaced alias queries. */

			// Get previous token result and check previous query token
			final QueryLexerResult previousTokenResult = getQueryToken(queryString, cursorPosition - 1, aliasInput);
			if (previousTokenResult.getQueryToken() == QueryLexerToken.Invalid) {
				// Set previous results to current result
				setPreviousTokenResult(lexerResult, previousTokenResult, true);
			}

			break;
		}
		case Dot: {
			/* This check is needed for spaced alias queries. */

			// Get previous token result and check previous alias type
			final QueryLexerResult previousTokenResult = getQueryToken(queryString, cursorPosition - 1, aliasInput);
			switch (previousTokenResult.getQueryToken()) {
			case Alias:
			case AliasEntityProperty: {
				if (previousTokenResult.getAliasType() != null) {
					// Its an alias property delimiter so set the previous alias type
					lexerResult.setQueryToken(QueryLexerToken.AliasPropertyDelimiter);
					lexerResult.setAliasType(previousTokenResult.getAliasType());
					lexerResult.setPropertyType(previousTokenResult.getPropertyType());
				}

				break;
			}
			case Invalid:
			case AliasProperty:
			case AliasPropertyInput:
			case AliasPropertyDelimiter: {
				// Not allowed to set "AliasPropertyDelimiter" here
				lexerResult.setQueryToken(QueryLexerToken.Invalid);
				lexerResult.setFilterString(QueryLexer.emptyString);
				lexerResult.setAliasType(null);
				lexerResult.setPropertyType(previousTokenResult.getPropertyType());

				break;
			}
			default:
				break;
			}

			break;
		}
		case Space: {
			/* This check is needed for spaced alias queries. */

			// Get previous token result and check previous token
			final QueryLexerResult previousTokenResult = getQueryToken(queryString, cursorPosition - 1, aliasInput);
			switch (previousTokenResult.getQueryToken()) {
			case Alias:
			case AliasPropertyInput:
			case AliasEntityProperty:
			case AliasPropertyDelimiter: {
				// Set previous alias results and ignore space
				setPreviousTokenResult(lexerResult, previousTokenResult, true);
				break;
			}
			default:
				break;
			}

			break;
		}
		case EscapedKeywordDelimiter: {
			/* This check is needed for spaced alias queries. */

			// Get previous token result and check previous token
			final QueryLexerResult previousTokenResult = getQueryToken(queryString, cursorPosition - 1, aliasInput);
			if (previousTokenResult.getQueryToken() == QueryLexerToken.Invalid) {
				// Set previous alias results and ignore space
				setPreviousTokenResult(lexerResult, previousTokenResult, true);
			} else {
				// Handle spaced alias queries - Note: That may change the token result
				handleSpacedAliasQuery(queryString, cursorPosition, aliasInput, lexerResult);
			}

			break;
		}
		case Number : {
			checkNumberToken(queryString, cursorPosition, aliasInput, lexerResult);
			break;
		}
		case LongDelimiter: {
			checkLongToken(queryString, cursorPosition, aliasInput, lexerResult);
			break;
		}
		case FloatDelimiter: {
			checkFloatToken(queryString, cursorPosition, aliasInput, lexerResult);
			break;
		}
		case DoubleDelimiter: {
			checkDoubleToken(queryString, cursorPosition, aliasInput, lexerResult);
			break;
		}
		case BigDecimalDelimiter: {
			checkBigDecimalToken(queryString, cursorPosition, aliasInput, lexerResult);
			break;
		}
		case Unknown: {
			/* This check is needed for spaced alias queries. */

			// Handle spaced alias queries - Note: That may change the token result
			handleSpacedAliasQuery(queryString, cursorPosition, aliasInput, lexerResult);

			// Check token after handling alias query
			switch (lexerResult.getQueryToken()) {
			case AliasPropertyInput: {
				for (int checkPosition = cursorPosition - 1; checkPosition > 1; checkPosition--) {
					// Get and check previous LEXER token result
					final QueryLexerResult previousLexerResult = QueryLexer.getQueryToken(queryString, checkPosition, aliasInput);
					if (previousLexerResult.getQueryToken() != QueryLexerToken.Unknown && previousLexerResult.getQueryToken() != QueryLexerToken.Alias) {
						// Set previous results to current result
						final QueryLexerResult previousTokenResult = getQueryToken(queryString, checkPosition, aliasInput);
						lexerResult.setFilterString(previousTokenResult.getFilterString() + lexerResult.getFilterString());

						break;
					}
				}

				break;
			}
			case Unknown: {
				// Check for known LEXER token
				for (int checkPosition = cursorPosition - 1; checkPosition > 1; checkPosition--) {
					// Get and check previous LEXER token result
					final QueryLexerResult previousLexerResult = QueryLexer.getQueryToken(queryString, checkPosition, aliasInput);
					switch (previousLexerResult.getQueryToken()) {
					case Alias:
					case EscapedKeywordDelimiter: {
						// After escaped keyword, set token to "EscapedKeywordInput"
						lexerResult.setQueryToken(QueryLexerToken.EscapedKeywordInput);
						lexerResult.setAliasType(null);
						lexerResult.setPropertyType(previousLexerResult.getPropertyType());

						// Set previous results to current result
						if (previousLexerResult.getQueryToken() != QueryLexerToken.Alias) {
							final QueryLexerResult previousTokenResult = getQueryToken(queryString, checkPosition, aliasInput);
							lexerResult.setFilterString(previousTokenResult.getFilterString() + lexerResult.getFilterString());
						}

						break;
					}
					case Unknown: {
						continue;
					}
					default:
						// Known token found - get and check previous token result
						final QueryLexerResult previousTokenResult = getQueryToken(queryString, checkPosition, aliasInput);
						if (previousTokenResult.getQueryToken() == QueryLexerToken.Invalid) {
							// Set previous results to current result
							setPreviousTokenResult(lexerResult, previousTokenResult, true);
						}

						break;
					}

					break;
				}

				break;
			}
			default:
				break;
			}

			break;
		}
		case StringDelimiter: {
			// Set token to string (finished string)
			lexerResult.setQueryToken(QueryLexerToken.String);
			lexerResult.setFilterString(QueryLexer.emptyString);

			break;
		}
		default:
			break;
		}

		return lexerResult;
	}

	public static QueryLexerResult getPreviousNonSpaceQueryToken(final String queryString, final int cursorPosition, final AliasInput aliasInput) {
		QueryLexerResult result = getQueryToken(queryString, cursorPosition, aliasInput);
		if (!result.getQueryToken().equals(QueryLexerToken.Space))
			return result;
		else
			return getPreviousNonSpaceQueryToken(queryString, cursorPosition - 1, aliasInput);
	}

	public static String getFirstTokenKeyword(final QueryLexerToken token) {
		final String[] keywords = token.getKeywords();
		if (keywords.length > 0) {
			return keywords[0];
		}

		return QueryLexer.emptyString;
	}

	/******************************** Helper Methods ********************************/

	private static String getAliasQuery(final String queryString, final int cursorPosition, final AliasInput aliasInput) {

		// Get start index of alias query
		final int aliasQueryIndex = getAliasQueryStart(queryString, cursorPosition, aliasInput);
		if (aliasQueryIndex < 0)
			return null;
		
		// Get aliasQuery from queryString and get keywords of space
		final StringBuilder aliasQuery = new StringBuilder(queryString.substring(aliasQueryIndex, cursorPosition));
		for (final String keyword : QueryLexerToken.Space.getKeywords()) {
			// Get keyword length & check for keyword
			final int keywordLen = keyword.length();
			for (int i = 0; i < aliasQuery.length(); i++) {
				// Check alias query with space keyword & remove keyword
				if (aliasQuery.substring(i, keywordLen + i).equals(keyword)) {
					aliasQuery.delete(i, keywordLen + i);
					i--;
				}
			}
		}

		// No space alias query
		return aliasQuery.toString();
	}

	private static int getAliasQueryStart(final String queryString, final int cursorPosition, final AliasInput aliasInput) {
		int aliasStartIndex = -1;

		// Check flags (space handling)
		boolean spaceToAliasAllowed = true;
		boolean spaceToDotAllowed = false;
		boolean spaceUsed = false;

		// Check flags (alias found)
		boolean aliasEndFound = false;
		boolean aliasPossible = true;

		// Get previous QUERY LEXER result
		for (int aliasCheckPosition = cursorPosition; aliasCheckPosition > 0; aliasCheckPosition--) {
			final QueryLexerResult aliasCheckLexerResult = QueryLexer.getQueryToken(queryString, aliasCheckPosition, aliasInput);

			// Check token of QUERY LEXER result
			switch (aliasCheckLexerResult.getQueryToken()) {
			case Alias: {
				// End of alias found?
				if (aliasEndFound == false && aliasPossible == true) {
					// End of alias found - save index!
					aliasStartIndex = aliasCheckPosition - 1;
					aliasEndFound = true;
				}

				continue;
			}
			case Space: {
				// End of alias found?
				if (aliasEndFound == true) {
					// Start of alias found!
					return aliasStartIndex;
				}

				// Set tokens
				aliasPossible = true;
				spaceUsed = true;
				continue;
			}
			case Dot: {
				// End of alias found? 2 following Dots seperated with space?
				if (aliasEndFound == true || (spaceUsed == true && spaceToDotAllowed == false)) {
					// Start of alias found!
					return aliasStartIndex;
				}

				// Set space flags (to alias)
				spaceToAliasAllowed = true;
				spaceToDotAllowed = false;
				spaceUsed = false;

				// Set alias is valid
				aliasPossible = true;
				continue;
			}
			case Unknown:
			case AliasPropertyInput:
			case AliasEntityProperty:
			case EscapedKeywordDelimiter: {
				// No space between input of "alias property"?
				if (spaceUsed == true && spaceToAliasAllowed == false) {
					// Invalid space
					return aliasStartIndex;
				} else if (aliasEndFound == true) {
					// Still in alias!
					aliasStartIndex--;
					continue;
				}

				// Set space flags (to dot)
				spaceToAliasAllowed = false;
				spaceToDotAllowed = true;
				spaceUsed = false;

				// Set alias is not valid
				aliasPossible = false;
				continue;
			}
			default:
				// Invalid token
				return aliasStartIndex;
			}
		}

		return aliasStartIndex;
	}

	private static void setPreviousTokenResult(final QueryLexerResult lexerResult, final QueryLexerResult previousTokenResult, final boolean setFilterString) {
		// Set previous results to current result
		lexerResult.setQueryToken(previousTokenResult.getQueryToken());
		lexerResult.setAliasType(previousTokenResult.getAliasType());
		lexerResult.setPropertyType(previousTokenResult.getPropertyType());

		if (setFilterString == true) {
			lexerResult.setFilterString(previousTokenResult.getFilterString());
		}
	}

	private static void handleSpacedAliasQuery(final String queryString, final int cursorPosition, final AliasInput aliasInput, final QueryLexerResult lexerResult) {

		// Get and check alias query from query string
		final String aliasQuery = getAliasQuery(queryString, cursorPosition, aliasInput);
		if (StringTools.isEmpty(aliasQuery) == false) {
			// Get and check alias QUERY LEXER result
			final QueryLexerResult aliasLexerResult = QueryLexer.getQueryToken(aliasQuery, aliasQuery.length(), aliasInput);
			switch (aliasLexerResult.getQueryToken()) {
			case Invalid:
			case AliasProperty:
			case AliasPropertyInput:
			case AliasEntityProperty: {
				// Set previous alias results and ignore space
				setPreviousTokenResult(lexerResult, aliasLexerResult, true);
				break;
			}
			case EscapedKeywordDelimiter: {
				// Handle escaped keyword delimiter token and set "AliasPropertyInput" token
				handleEscapedKeywordDelimiter(queryString, cursorPosition, aliasInput, lexerResult);
				lexerResult.setQueryToken(QueryLexerToken.AliasPropertyInput);

				break;
			}
			default:
				break;
			}
		}

		// Check after handling spaced alias
		switch (lexerResult.getQueryToken()) {
		case AliasProperty:
		case AliasPropertyInput:
		case AliasEntityProperty: {
			/* This check is needed to handle the filter string after handling spaced alias queries. */

			// Check cursor position
			if (cursorPosition >= 1) {
				// Get previous token result and check token result is invalid, else check length of current filter string
				final QueryLexerResult previousTokenResult = getQueryToken(queryString, cursorPosition - 1, aliasInput);
				switch (previousTokenResult.getQueryToken()) {
				case Invalid: {
					// Set previous results to current result
					setPreviousTokenResult(lexerResult, previousTokenResult, true);
					break;
				}
				default:
					break;
				}
			}

			break;
		}
		default:
			break;
		}
	}

	private static void handleEscapedKeywordDelimiter(final String queryString, final int cursorPosition, final AliasInput aliasInput, final QueryLexerResult lexerResult) {
		if (lexerResult.getInString())
			return;
		
		if (!lexerResult.getInEscapeKeyword()) {
			// Get previous token and check previous token result
			final QueryLexerResult previousTokenResult = getQueryToken(queryString, cursorPosition - 1, aliasInput);
			if (previousTokenResult.getQueryToken() == QueryLexerToken.AliasPropertyInput) {
				// Set previous token "AliasPropertyInput"
				setPreviousTokenResult(lexerResult, previousTokenResult, false);
			}

			// Set filter string of previous token and add escaped keyword delimiter
			lexerResult.setFilterString(previousTokenResult.getFilterString() + getFirstTokenKeyword(QueryLexerToken.EscapedKeywordDelimiter));
			
			return;
		}

		// Is query token start of escaped keyword?
		switch (lexerResult.getQueryToken()) {
		case EscapedKeywordDelimiter: {
			// Get previous token result and check previous query token
			final QueryLexerResult previousTokenResult = getQueryToken(queryString, cursorPosition - 1, aliasInput);
			switch (previousTokenResult.getQueryToken()) {
			case AliasPropertyDelimiter: {
				// Its an alias property so set the "AliasPropertyInput" token
				lexerResult.setQueryToken(QueryLexerToken.AliasPropertyInput);
				lexerResult.setAliasType(previousTokenResult.getAliasType());
				lexerResult.setPropertyType(previousTokenResult.getPropertyType());

				// Set previous filter string to current filter string
				lexerResult.setFilterString(previousTokenResult.getFilterString() + getFirstTokenKeyword(QueryLexerToken.EscapedKeywordDelimiter));
				break;
			}
			case Invalid: {
				// Set previous results to current result
				setPreviousTokenResult(lexerResult, previousTokenResult, true);
				break;
			}
			default:
				// Set previous filter string to current filter string
				lexerResult.setFilterString(previousTokenResult.getFilterString() + getFirstTokenKeyword(QueryLexerToken.EscapedKeywordDelimiter));
				break;
			}

			break;
		}
		case Invalid: {
			break;
		}
		default:
			// Search and get "EscapedKeywordDelimiter" token.
			final Pair<Integer, Integer> escapeDelimiterIndex = QueryLexer.getLastIndexOfToken(queryString, cursorPosition, QueryLexerToken.EscapedKeywordDelimiter);
			if (escapeDelimiterIndex.first() < 0 || escapeDelimiterIndex.first() + escapeDelimiterIndex.second() >= cursorPosition)
				break;
			
			// Get last "EscapedKeywordDelimiter" token
			final int escapeDelimiterTokenEndIndex = escapeDelimiterIndex.first() + escapeDelimiterIndex.second();
			final QueryLexerResult escapeDelimiterTokenResult = getQueryToken(queryString, escapeDelimiterTokenEndIndex, aliasInput);

			// Check if "EscapedKeywordDelimiter" result is Invalid
			if (escapeDelimiterTokenResult.getQueryToken() == QueryLexerToken.Invalid) {
				// Set "EscapedKeywordDelimiter" results to current result
				setPreviousTokenResult(lexerResult, escapeDelimiterTokenResult, true);
				break;
			}
			
			// Set: Alias token not set
			boolean aliasTokenSet = false;

			// Set whole text of escaped keyword by splitting filter text from query string
			final String escapedDelimiterFilterString = queryString.substring(escapeDelimiterTokenEndIndex, cursorPosition);
			lexerResult.setFilterString(escapeDelimiterTokenResult.getFilterString() + escapedDelimiterFilterString);

			// Check current token
			switch (lexerResult.getQueryToken()) {
			case Unknown:
			case AliasPropertyInput: {
				// Check if alias type was found
				if (escapeDelimiterTokenResult.getAliasType() != null) {
					// Set token "AliasPropertyInput"
					lexerResult.setQueryToken(QueryLexerToken.AliasPropertyInput);
					lexerResult.setAliasType(escapeDelimiterTokenResult.getAliasType());
					lexerResult.setPropertyType(escapeDelimiterTokenResult.getPropertyType());

					// Set: Alias token set
					aliasTokenSet = true;
				}

				break;
			}
			default:
				break;
			}

			// Check: Alias token set?
			if (!aliasTokenSet) {
				// Set default input token "EscapedKeywordInput".
				lexerResult.setQueryToken(QueryLexerToken.EscapedKeywordInput);
			}

			break;
		}
	}
	
	private static String getNumberFromNumberRelatedToken(final String queryString, final int cursorPosition, final AliasInput aliasInput, final QueryLexerResult lexerResult, boolean useDelimiter) {
		int position = cursorPosition;
		String theNumber = useDelimiter ? Character.toString(queryString.charAt(cursorPosition - 1)) : "";
		QueryLexerResult previousTokenResult = null;
		QueryLexerToken queryToken = null;
		boolean valid = true;
		
		if (position <= 1)
			return theNumber;
		
		do {
			previousTokenResult = getQueryToken(queryString, --position, aliasInput);
			queryToken = previousTokenResult.getQueryToken();
			if (queryToken.equals(QueryLexerToken.Integer) || queryToken.equals(QueryLexerToken.Minus) || queryToken.equals(QueryLexerToken.Double) || queryToken.equals(QueryLexerToken.Dot)
					|| queryToken.equals(QueryLexerToken.Invalid)) {
				if (queryToken.equals(QueryLexerToken.Invalid)) {
					String token = previousTokenResult.getKeywordMatch();
					if (token != null && Character.isDigit(token.charAt(0)) || DOT.equals(token))
						theNumber = queryString.charAt(position - 1) + theNumber;
					else {
						valid = false;
						lexerResult.setQueryToken(QueryLexerToken.Invalid);
						break;
					}
				} else {
					theNumber = queryString.charAt(position - 1) + theNumber;
				}
			} else if (!queryToken.equals(QueryLexerToken.Space) && !queryToken.equals(QueryLexerToken.Operator) && !queryToken.equals(QueryLexerToken.OperatorWithSpace)) {
				valid = false;
				lexerResult.setQueryToken(QueryLexerToken.Invalid);
				break;
			}
		} while (position > 1 && (!queryToken.equals(QueryLexerToken.Space) && !queryToken.equals(QueryLexerToken.Operator) && !queryToken.equals(QueryLexerToken.OperatorWithSpace)));
		
		return !valid ? null : theNumber;
	}
	
	private static void checkNumberToken(final String queryString, final int cursorPosition, final AliasInput aliasInput, final QueryLexerResult lexerResult) {
		String theNumber = getNumberFromNumberRelatedToken(queryString, cursorPosition, aliasInput, lexerResult, true);
		
		if (theNumber == null)
			return;
		
		boolean checkDouble = theNumber.contains(DOT) || numberContainsDoubleDelimiter(theNumber);
		if (checkDouble) {
			try {
				Double.parseDouble(theNumber);
				lexerResult.setQueryToken(QueryLexerToken.Double);
				lexerResult.setFilterString(QueryLexer.emptyString);
				lexerResult.setNumberString(theNumber);
			} catch (NumberFormatException e) {
				lexerResult.setQueryToken(QueryLexerToken.Invalid);
			}
		} else {
			try {
				Integer.parseInt(theNumber);
				lexerResult.setQueryToken(QueryLexerToken.Integer);
				lexerResult.setFilterString(QueryLexer.emptyString);
				lexerResult.setNumberString(theNumber);
			} catch (NumberFormatException ex) {
				lexerResult.setQueryToken(QueryLexerToken.Invalid);
			}
		}
	}
	
	private static boolean numberContainsDoubleDelimiter(String number) {
		boolean contains = false;
		for (String doubleDelimiter : QueryLexerToken.DoubleDelimiter.getKeywords()) {
			contains = number.contains(doubleDelimiter);
			if (contains)
				break;
		}
		
		return contains;
	}
	
	private static void checkLongToken(final String queryString, final int cursorPosition, final AliasInput aliasInput, final QueryLexerResult lexerResult) {
		String theNumber = getNumberFromNumberRelatedToken(queryString, cursorPosition, aliasInput, lexerResult, false);
		
		if (theNumber == null)
			return;
		
		try {
			Long.parseLong(theNumber);
			lexerResult.setQueryToken(QueryLexerToken.Long);
			lexerResult.setFilterString(QueryLexer.emptyString);
			lexerResult.setNumberString(theNumber);
		} catch (NumberFormatException ex) {
			lexerResult.setQueryToken(QueryLexerToken.Invalid);
		}
	}
	
	private static void checkFloatToken(final String queryString, final int cursorPosition, final AliasInput aliasInput, final QueryLexerResult lexerResult) {
		String theNumber = getNumberFromNumberRelatedToken(queryString, cursorPosition, aliasInput, lexerResult, true);
		
		if (theNumber == null)
			return;
		
		try {
			Float.parseFloat(theNumber);
			lexerResult.setQueryToken(QueryLexerToken.Float);
			lexerResult.setFilterString(QueryLexer.emptyString);
			lexerResult.setNumberString(theNumber);
		} catch (NumberFormatException ex) {
			lexerResult.setQueryToken(QueryLexerToken.Invalid);
		}
	}
	
	private static void checkDoubleToken(final String queryString, final int cursorPosition, final AliasInput aliasInput, final QueryLexerResult lexerResult) {
		checkNumberToken(queryString, cursorPosition, aliasInput, lexerResult);
	}
	
	@SuppressWarnings("unused")
	private static void checkBigDecimalToken(final String queryString, final int cursorPosition, final AliasInput aliasInput, final QueryLexerResult lexerResult) {
		String theNumber = getNumberFromNumberRelatedToken(queryString, cursorPosition, aliasInput, lexerResult, false);
		
		if (theNumber == null)
			return;
		
		try {
			new BigDecimal(theNumber);
			lexerResult.setQueryToken(QueryLexerToken.BigDecimal);
			lexerResult.setFilterString(QueryLexer.emptyString);
			lexerResult.setNumberString(theNumber);
		} catch (NumberFormatException ex) {
			lexerResult.setQueryToken(QueryLexerToken.Invalid);
		}
	}
	
}
