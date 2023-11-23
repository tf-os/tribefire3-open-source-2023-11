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
package com.braintribe.model.processing.query.autocompletion.impl.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.processing.query.autocompletion.QueryAutoCompletionRuntimeException;
import com.braintribe.model.processing.query.autocompletion.api.QueryLexerResult;
import com.braintribe.model.processing.query.autocompletion.impl.ContainerFactory;
import com.braintribe.model.processing.query.autocompletion.impl.GmTypeExpert;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.container.AliasInput;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.container.AliasType;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.container.AliasType.CheckType;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.container.QueryLexerToken;
import com.braintribe.utils.lcd.StringTools;

/**
 * It checks if what is typed 'belong' to a given single token (if it is a valid token).
 * 
 */
public abstract class QueryLexer {
	public static final String emptyString = "";
	private static Integer numberEndIndex;

	/******************************** Public Methods ********************************/

	public static QueryLexerResult getQueryToken(final String queryString, final int cursorPosition, final AliasInput aliasInput) {
		final QueryLexerResult result = ContainerFactory.createQueryLexerResult();

		// Check received data
		if (queryString == null || cursorPosition == 0) {
			return result;
		} else if (cursorPosition < 0 || cursorPosition > queryString.length()) {
			throw new QueryAutoCompletionRuntimeException("Cursor position is out of bounds.");
		}

		// Prepare check result variables, default filter string and closable tokens sorted by index
		final Map<Integer, QueryLexerToken> closableTokenIndices = new TreeMap<Integer, QueryLexerToken>();
		result.setFilterString(queryString.substring(0, cursorPosition));
		int keywordIndex = -1;

		// Check all tokens and its keywords
		for (final QueryLexerToken token : QueryLexerToken.values()) {
			// Get QueryToken and index of token keyword
			for (final String keyword : token.getKeywords()) {
				// Prevent GWT compiled JavaScript bug -> lastIndexOf(x, -1)
				final int index = queryString.lastIndexOf(keyword, cursorPosition - 1);
				if (index > keywordIndex && index < cursorPosition) {
					if (token.isSpaceNeeded() && !keywordHasNeededSpace(queryString, index, keyword.length())) {
						continue;
					}
					
					if (token.isNumberRelated() && !isValidNumber(queryString, index)) {
						continue;
					}
					
					final int endIndex = index + keyword.length();
					if (endIndex <= cursorPosition) {
						// Set filter string and token of keyword; save last index of keyword
						result.setFilterString(queryString.substring(endIndex, cursorPosition));
						result.setQueryToken(token);
						result.setKeywordMatch(keyword);
						if (token.isNumberRelated() && token.equals(QueryLexerToken.Number)) {
							//We set the index to the end of the number
							keywordIndex = numberEndIndex;
						} else
							keywordIndex = index;
					}
					
					if (token.isNumberRelated() && token.equals(QueryLexerToken.Number)) //Ignoring the other numbers that may be found as only one is needed for the check above
						break;
				}
			}

			// Check for closable tokens
			if (isClosableToken(token)) {
				// Get indices (ignore keyword length) of closable tokens
				for (final Integer tokenIndex : getTokenIndices(token, queryString, cursorPosition).keySet()) {
					closableTokenIndices.put(tokenIndex, token);
				}
			}
		}

		// Set unknown token on existing filter string
		if (result.getFilterString().length() > 0 && !result.getQueryToken().isNumberRelated()) {
			result.setQueryToken(QueryLexerToken.Unknown);
			result.setKeywordMatch(null);
		}
		
		//Check special operators (the ones combined of =)
		if (result.getQueryToken().equals(QueryLexerToken.Operator) && "=".equals(result.getKeywordMatch()) && keywordIndex >= 1) {
			char theChar = queryString.charAt(keywordIndex - 1);
			if (theChar == '!' || theChar == '>' || theChar == '<')
				result.setKeywordMatch(theChar + "=");
		}

		// Handle alias and set closable tokens to result
		handleClosableTokens(closableTokenIndices, result);
		handleAliasToken(queryString, cursorPosition, aliasInput, result);

		// Set result values
		return result;
	}

	public static Map<Integer, Integer> getTokenIndices(final QueryLexerToken token, final String queryString, final int cursorPosition) {
		final Map<Integer, Integer> tokenIndices = new TreeMap<Integer, Integer>();

		// Check all keywords of token
		for (final String keyword : token.getKeywords()) {
			// Prevent GWT compiled JavaScript bug -> lastIndexOf(x, -1)
			int keywordIndex = queryString.lastIndexOf(keyword, cursorPosition - 1);
			while (keywordIndex >= 0 && keywordIndex < cursorPosition) {
				// Store index and length of token keyword
				tokenIndices.put(keywordIndex, keyword.length());
				keywordIndex--;

				// Prevent GWT compiled JavaScript bug -> lastIndexOf(x, -1) was like lastIndexOf(x, 0)
				keywordIndex = (keywordIndex >= 0 ? queryString.lastIndexOf(keyword, keywordIndex) : keywordIndex);
			}
		}

		return tokenIndices;
	}

	public static Pair<Integer, Integer> getLastIndexOfToken(final String queryString, final int cursorPosition, final QueryLexerToken... tokens) {
		int lastIndex = -1;
		int keywordLength = 0;

		// Check all keywords of all tokens
		for (QueryLexerToken token : tokens) {
			for (final String keyword : token.getKeywords()) {
				// Prevent GWT compiled JavaScript bug -> lastIndexOf(x, -1)
				int keywordIndex = queryString.lastIndexOf(keyword, cursorPosition - 1);
				while (keywordIndex >= 0 && keywordIndex < cursorPosition) {
					if (token.isSpaceNeeded() && !keywordHasNeededSpace(queryString, keywordIndex, keyword.length()))
						break;
					
					// Check last index of token
					if (keywordIndex > lastIndex) {
						// Set last index of token
						keywordLength = keyword.length();
						lastIndex = keywordIndex;
					}
	
					// search index
					keywordIndex--;
	
					// Prevent GWT compiled JavaScript bug -> lastIndexOf(x, -1) was like lastIndexOf(x, 0)
					keywordIndex = (keywordIndex >= 0 ? queryString.lastIndexOf(keyword, keywordIndex) : keywordIndex);
				}
			}
		}

		return new Pair<Integer, Integer>(lastIndex, keywordLength);
	}

	/**************************** Generic Helper Methods ****************************/

	private static boolean isClosableToken(final QueryLexerToken token) {

		if (token != null) {
			switch (token) {
			case StringEscape:
			case OpenBracket:
			case CloseBracket:
			case StringDelimiter:
			case EscapedKeywordDelimiter: {
				return true;
			}
			default:
				break;
			}
		}

		return false;
	}

	private static boolean keywordHasNeededSpace(final String queryString, final int keywordIndex, final int keywordLength) {
		boolean previousSpaceKeywordFound = false;
		boolean nextSpaceKeywordFound = false;

		if (keywordIndex > 0) {
			// Check all space keywords (HTML space "&nbsp;" and " ")
			for (final String spaceKeyword : QueryLexerToken.Space.getKeywords()) {
				// Get string before current keyword and check if it is the space keyword
				final String previousSpaceCheck = queryString.substring(keywordIndex - spaceKeyword.length(), keywordIndex);
				if (previousSpaceCheck.equals(spaceKeyword)) {
					previousSpaceKeywordFound = true;
					break;
				}
			}
		} else {
			// No index before keyword
			previousSpaceKeywordFound = true;
		}

		final int nextSpaceIndex = keywordIndex + keywordLength;
		if (nextSpaceIndex < queryString.length()) {
			// Check all space keywords (HTML space "&nbsp;" and " ")
			for (final String spaceKeyword : QueryLexerToken.Space.getKeywords()) {
				// Get end of keyword and check if string is long enougth
				final int endSpaceIndex = nextSpaceIndex + spaceKeyword.length();
				if (queryString.length() >= endSpaceIndex) {
					// Get string after current keyword and check if it is the space keyword
					final String nextSpaceCheck = queryString.substring(nextSpaceIndex, nextSpaceIndex + spaceKeyword.length());
					if (nextSpaceCheck.equals(spaceKeyword)) {
						nextSpaceKeywordFound = true;
						break;
					}
				}
			}
		} else {
			// No index before keyword
			nextSpaceKeywordFound = true;
		}

		return previousSpaceKeywordFound && nextSpaceKeywordFound;
	}
	
	private static boolean isValidNumber(String queryString, int keywordIndex) {
		Integer numberStartIndex = null; //finds the start index of the number by going left and stopping at a space or last char of an operator
		
		for (int i = keywordIndex; i > 0; i--) {
			char theChar = queryString.charAt(i);
			String theCharString = Character.toString(theChar);
			
			for (String spaceKeyword : QueryLexerToken.Space.getKeywords()) {
				if (spaceKeyword.equals(theCharString)) {
					numberStartIndex = i + 1;
					break;
				}
			}
			
			if (numberStartIndex != null)
				break;
			
			for (String operatorKeyword : QueryLexerToken.Operator.getKeywords()) {
				if (operatorKeyword.charAt(operatorKeyword.length() - 1) == theChar) {
					numberStartIndex = i + 1;
					break;
				}
			}
			
			for (String operatorKeyword : QueryLexerToken.OperatorWithSpace.getKeywords()) {
				if (operatorKeyword.charAt(operatorKeyword.length() - 1) == theChar) {
					numberStartIndex = i + 1;
					break;
				}
			}
			
			if (numberStartIndex != null)
				break;
		}
		
		if (numberStartIndex == null)
			numberStartIndex = 0;
		
		char numberStartChar = queryString.charAt(numberStartIndex);
		
		if (!Character.isDigit(numberStartChar) && !isStringMinus(Character.toString(numberStartChar)))
			return false;
		
		numberEndIndex = getLastValuePosition(keywordIndex, queryString);
		
		for (int i = numberStartIndex + 1; i <= numberEndIndex; i++) {
			char theChar = queryString.charAt(i);
			String theCharString = Character.toString(theChar);
			
			if (i == numberEndIndex) {
				if (!Character.isDigit(theChar) && !isStringDot(theCharString) && !isStringNumberDelimiter(theCharString))
					return false;
			} else {
				if (!Character.isDigit(theChar) && !isStringDot(theCharString))
					return false;
			}
		}
		
		return true;
	}
	
	public static int getLastValuePosition(int startIndex, String queryString) {
		Integer valueEndIndex = null; //finds the end index of the value by going right and stopping at a space or first char of an operator
		
		if (startIndex >= queryString.length() - 1)
			return queryString.length() - 1;
		
		for (int i = startIndex + 1; i <= queryString.length() - 1; i++) {
			char theChar = queryString.charAt(i);
			String theCharString = Character.toString(theChar);
			
			for (String spaceKeyword : QueryLexerToken.Space.getKeywords()) {
				if (spaceKeyword.equals(theCharString)) {
					valueEndIndex = i - 1;
					break;
				}
			}
			
			if (valueEndIndex != null)
				break;
			
			for (String operatorKeyword : QueryLexerToken.Operator.getKeywords()) {
				if (operatorKeyword.charAt(0) == theChar) {
					valueEndIndex = i - 1;
					break;
				}
			}
			
			for (String operatorKeyword : QueryLexerToken.OperatorWithSpace.getKeywords()) {
				if (operatorKeyword.charAt(0) == theChar) {
					valueEndIndex = i - 1;
					break;
				}
			}
			
			if (valueEndIndex != null)
				break;
		}
		
		if (valueEndIndex == null)
			valueEndIndex = queryString.length() - 1;
		
		return valueEndIndex;
	}

	private static boolean isClosableTokenEscaped(final Map<Integer, QueryLexerToken> closableTokenIndices, final int tokenIndex) {

		// Prepare check variables
		boolean isEscapedToken = false;
		int checkIndex = tokenIndex - 1;

		// Check for escape character directly before token
		QueryLexerToken checkIndexToken = closableTokenIndices.get(checkIndex);
		while (checkIndexToken != null && checkIndexToken == QueryLexerToken.StringEscape) {
			isEscapedToken = !isEscapedToken;
			checkIndex--;

			// Re-Check for escape character directly before escape character
			checkIndexToken = closableTokenIndices.get(checkIndex);
		}

		return isEscapedToken;
	}

	private static void handleClosableTokens(final Map<Integer, QueryLexerToken> closableTokenIndices, final QueryLexerResult result) {

		// Get values of closable tokens (sorted by the index)
		for (final int tokenIndex : closableTokenIndices.keySet()) {
			final QueryLexerToken token = closableTokenIndices.get(tokenIndex);

			// Check closable token state
			if (result.getInEscapeKeyword()) {
				// Handle escape quotes
				if (token == QueryLexerToken.EscapedKeywordDelimiter) {
					result.setInEscapeKeyword(false);
				}
			} else if (result.getInString()) {
				// Check for escaped string quotes
				if (isClosableTokenEscaped(closableTokenIndices, tokenIndex)) {
					continue;
				}

				// Handle string quotes
				if (token == QueryLexerToken.StringDelimiter) {
					result.setInString(false);
				}
			} else {
				// Handle token
				switch (token) {
				case EscapedKeywordDelimiter: {
					result.setInEscapeKeyword(true);
					break;
				}
				case StringDelimiter: {
					result.setInString(true);
					break;
				}
				case OpenBracket:
				case CloseBracket: {
					// Handle bracket scope
					final int bracketScope = result.getBracketScope();
					if (token == QueryLexerToken.OpenBracket) {
						result.setBracketScope(bracketScope + 1);
					} else if (token == QueryLexerToken.CloseBracket && bracketScope > 0) {
						result.setBracketScope(bracketScope - 1);
					}

					break;
				}
				default:
					break;
				}
			}
		}
	}

	private static List<String> splitAtTokenKeywords(final QueryLexerToken token, final String queryString, final int cursorPosition) {
		final List<String> tokenSplit = new ArrayList<>();
		int splitStartIndex = 0;

		// Check all keywords of token
		final Map<Integer, Integer> tokenSplitInfo = getTokenIndices(token, queryString, cursorPosition);
		for (final int keywordIndex : tokenSplitInfo.keySet()) {
			// In case two keyword index are colliding
			if (keywordIndex < splitStartIndex) {
				continue;
			}

			// Add value between token keywords to the result
			tokenSplit.add(queryString.substring(splitStartIndex, keywordIndex));

			// Get length of token keywords and set split index
			final int keywordLength = tokenSplitInfo.get(keywordIndex);
			splitStartIndex = keywordIndex + keywordLength;
		}

		// Add the rest of the queryString to the result
		tokenSplit.add(queryString.substring(splitStartIndex));
		return tokenSplit;
	}
	
	private static boolean isStringMinus(String theString) {
		for (String minusKeyword : QueryLexerToken.Minus.getKeywords()) {
			if (minusKeyword.equals(theString))
				return true;
		}
		
		return false;
	}
	
	private static boolean isStringDot(String theString) {
		for (String dotKeyword : QueryLexerToken.Dot.getKeywords()) {
			if (dotKeyword.equals(theString))
				return true;
		}
		
		return false;
	}
	
	private static boolean isStringNumberDelimiter(String theString) {
		for (String delimiterKeyword : QueryLexerToken.FloatDelimiter.getKeywords()) {
			if (delimiterKeyword.equals(theString))
				return true;
		}
		
		for (String delimiterKeyword : QueryLexerToken.LongDelimiter.getKeywords()) {
			if (delimiterKeyword.equals(theString))
				return true;
		}
		
		for (String delimiterKeyword : QueryLexerToken.BigDecimalDelimiter.getKeywords()) {
			if (delimiterKeyword.equals(theString))
				return true;
		}
		
		for (String delimiterKeyword : QueryLexerToken.DoubleDelimiter.getKeywords()) {
			if (delimiterKeyword.equals(theString))
				return true;
		}
		
		return false;
	}

	/***************************** Alias Helper Methods *****************************/

	private static String getUnescapedAliasPart(final String queryPart, final Set<String> keywords) {

		// Check the keywords
		if (keywords.contains(queryPart)) {
			// Should be escaped!
			return emptyString;
		} else {
			// Get and check escaped token indices (ignore keyword length)
			final Set<Integer> escapeTokenIndices = getTokenIndices(QueryLexerToken.EscapedKeywordDelimiter, queryPart, queryPart.length()).keySet();
			if (escapeTokenIndices.size() > 0 && escapeTokenIndices.contains(0)) {
				// Get query part variables
				String checkQueryPart = queryPart;
				int endIndex = checkQueryPart.length() - 1;

				// Check if escape end does exist
				if (escapeTokenIndices.contains(endIndex)) {
					// No escape end?
					if (endIndex < 1) {
						endIndex = 1;
					}

					// Split and check the keywords
					checkQueryPart = checkQueryPart.substring(1, endIndex);
					if (keywords.contains(checkQueryPart)) {
						return checkQueryPart;
					}
				}
			}
		}

		// No keyword!
		return queryPart;
	}

	private static AliasType getAliasType(final String queryString, final int cursorPosition, final AliasInput aliasInput) {
		final AliasType aliasType = ContainerFactory.createAliasType();

		// Get and check received Alias-Map
		final Map<String, GmType> aliasMap = aliasInput.getAliasMap();
		if (aliasMap == null || aliasMap.isEmpty())
			return aliasType;
		
		// Get index of alias before token "Dot" before the cursor position
		final Pair<Integer, Integer> operatorIndex = getLastIndexOfToken(queryString, cursorPosition - 1, QueryLexerToken.Operator, QueryLexerToken.OperatorWithSpace);
		final Pair<Integer, Integer> spaceIndex = getLastIndexOfToken(queryString, cursorPosition - 1, QueryLexerToken.Space);

		// Get alias index before token "Dot" and check alias index with cursor position
		final int aliasStringIndex = (operatorIndex.getFirst() > spaceIndex.getFirst() ? operatorIndex.getFirst() + operatorIndex.getSecond() : spaceIndex.getFirst() + spaceIndex.getSecond());
		if (aliasStringIndex > cursorPosition)
			return aliasType;
		
		// Get alias before token "Dot" to the cursor position
		final String aliasString = queryString.substring((aliasStringIndex == -1 ? 0 : aliasStringIndex), cursorPosition);

		// Split alias string into parts at "Dot" keywords
		final List<String> aliasParts = splitAtTokenKeywords(QueryLexerToken.Dot, aliasString, aliasString.length());
		for (int i = 0, l = aliasParts.size() - 1; i <= l; i++) {
			final String aliasPart = getUnescapedAliasPart(aliasParts.get(i), aliasInput.getKeywords());
			aliasType.setLastAliasPart(aliasParts.get(i));

			// Is entity type set?
			if (aliasType.getEntityType() == null) {
				aliasType.setCheckType(CheckType.Alias);

				// Check if there is a alias set before first "Dot" and alias is in mapping
				if (StringTools.isEmpty(aliasPart) || !aliasMap.containsKey(aliasPart)) {
					return aliasType;
				}

				// Set entity type of mapped alias
				aliasType.setEntityType(aliasMap.get(aliasPart));
				aliasType.setHasProperties(true);
				continue;
			}
			
			aliasType.setCheckType(CheckType.Property);

			// Check if entity property exists
			final GmProperty entityProperty = GmTypeExpert.getEntityPropertyByName(aliasType.getEntityType(), aliasPart);
			if (entityProperty == null) {
				if (i < l) {
					// Disable properties
					aliasType.setHasProperties(false);
				}

				// Set property not found
				aliasType.setPropertyFound(false);
				return aliasType;
			} else {
				// Set property found
				aliasType.setPropertyFound(true);
			}

			// Check type of property
			if (entityProperty.getType().typeKind() == GmTypeKind.ENTITY) {
				final GmEntityType gmEntityType = (GmEntityType) entityProperty.getType();
				EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(entityProperty.getType().getTypeSignature());

				// Set entity type of entity property
				aliasType.setEntityType(gmEntityType);
				aliasType.setHasProperties(!entityType.getProperties().isEmpty());
			} else {
				// Remove set entity type
				aliasType.setHasProperties(false);
				aliasType.setPropertyType(entityProperty.getType());
			}
		}

		return aliasType;
	}

	private static void handleAliasToken(final String queryString, final int cursorPosition, final AliasInput aliasInput, final QueryLexerResult result) {

		// No alias variables!
		if (aliasInput == null) {
			return;
		}

		// Get alias type and check received alias entity
		final AliasType aliasType = getAliasType(queryString, cursorPosition, aliasInput);
		if (aliasType.getEntityType() == null)
			return;
		
		// Check if property was found
		if (aliasType.getPropertyFound()) {
			// Check current token
			switch (result.getQueryToken()) {
			case Unknown:
			case EscapedKeywordDelimiter: {
				// Check properties of parent entity
				if (aliasType.getHasProperties()) {
					result.setQueryToken(QueryLexerToken.AliasEntityProperty);
					result.setFilterString(aliasType.getLastAliasPart());
					result.setAliasType(aliasType.getEntityType());
					result.setPropertyType(aliasType.getPropertyType());
				} else {
					result.setQueryToken(QueryLexerToken.AliasProperty);
					result.setFilterString(aliasType.getLastAliasPart());
					result.setAliasType(null);
					result.setPropertyType(aliasType.getPropertyType());
				}

				break;
			}
			default:
				break;
			}
			
			return;
		}
			
		// Check current token
		switch (result.getQueryToken()) {
		case Unknown:
		case EscapedKeywordDelimiter: {
			// Check alias check type
			switch (aliasType.getCheckType()) {
			case Alias: {
				result.setQueryToken(QueryLexerToken.Alias);
				result.setFilterString(aliasType.getLastAliasPart());
				result.setAliasType(aliasType.getEntityType());
				result.setPropertyType(aliasType.getPropertyType());

				break;
			}
			case Property: {
				// Check properties of parent entity
				if (aliasType.getHasProperties()) {
					// Set "AliasPropertyInput" token, if current token is "Unknown"
					if (result.getQueryToken() != QueryLexerToken.EscapedKeywordDelimiter) {
						result.setQueryToken(QueryLexerToken.AliasPropertyInput);
					}

					// Set alias type of entity type
					result.setAliasType(aliasType.getEntityType());
					result.setPropertyType(aliasType.getPropertyType());
				} else {
					// No property so set no alias type & invalid
					result.setQueryToken(QueryLexerToken.Invalid);
					result.setFilterString(emptyString);
					result.setAliasType(null);
					result.setPropertyType(aliasType.getPropertyType());
				}

				break;
			}
			default:
				break;
			}

			break;
		}
		default:
			break;
		}
	}
}
