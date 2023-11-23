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
package com.braintribe.model.processing.query.autocompletion;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.api.shortening.SignatureExpert;
import com.braintribe.model.processing.query.autocompletion.api.QueryAutoCompletionResult;
import com.braintribe.model.processing.query.autocompletion.api.QueryLexerResult;
import com.braintribe.model.processing.query.autocompletion.impl.AutoCompletionLexer;
import com.braintribe.model.processing.query.autocompletion.impl.ContainerFactory;
import com.braintribe.model.processing.query.autocompletion.impl.GmTypeExpert;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.QueryLexer;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.container.AliasInput;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.container.QueryLexerToken;
import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.query.Operator;

public class QueryAutoCompletion {
	
	private final ModelOracle modelOracle;

	/************************* Method Area ************************/

	public QueryAutoCompletion(final ModelEnvironmentDrivenGmSession gmSession) {

		if (gmSession == null) {
			throw new IllegalArgumentException();
		} else if (gmSession.getModelEnvironment() == null || gmSession.getModelEnvironment().getDataModel() == null) {
			throw new QueryAutoCompletionRuntimeException("ModelEnvironment of gmSession is null.");
		}
		
		modelOracle = gmSession.getModelAccessory().getOracle();
	}

	public QueryAutoCompletionResult getPossibleHints(final String queryString, final int cursorPosition) {
		return getPossibleHints(queryString, cursorPosition, null);
	}

	public QueryAutoCompletionResult getPossibleHints(final String queryString, final int cursorPosition, final SignatureExpert expandMode) {
		final Set<String> possibleHints = new TreeSet<String>();

		// Create and fill input class
		final AliasInput aliasInput = ContainerFactory.createAliasInput();
		aliasInput.setAliasMap(GmTypeExpert.getAliasMap(queryString, modelOracle, expandMode));
		aliasInput.setKeywords(new HashSet<String>(QueryParser.getKeywords()));

		// Get current query lexer result
		QueryLexerResult tokenResult = AutoCompletionLexer.getQueryToken(queryString, cursorPosition, aliasInput);
		
		int amountOfSpaces = 0;
		//If we have just entered a space, we then check the possible hints for the previous token instead
		if (tokenResult.getQueryToken().equals(QueryLexerToken.Space)) {
			do {
				amountOfSpaces++;
				tokenResult = AutoCompletionLexer.getQueryToken(queryString, cursorPosition - amountOfSpaces, aliasInput);
			} while (tokenResult.getQueryToken().equals(QueryLexerToken.Space));
		}

		// Add possible hints and filter them afterwards
		String typeSignature = checkToken(aliasInput, tokenResult, possibleHints, queryString, cursorPosition, amountOfSpaces, expandMode);
		Set<String> possibleHintsFilteredOut = filterPossibleHints(possibleHints, tokenResult.getFilterString());

		// Create result entity and return possible hints
		final QueryAutoCompletionResult result = ContainerFactory.createQueryAutoCompletionResult();
		result.setPossibleHints(possibleHints);
		result.setTypeSignature(typeSignature);
		result.setFilterString(tokenResult.getNumberString() != null ? tokenResult.getNumberString() : tokenResult.getFilterString());
		result.setAliasType(tokenResult.getAliasType());
		result.setPossibleHintsFilteredOut(possibleHintsFilteredOut);
		return result;
	}

	private String checkToken(AliasInput aliasInput, QueryLexerResult tokenResult, Set<String> possibleHints,
			String queryString, int cursorPosition, int amountOfSpaces, SignatureExpert expandMode) {
		
		if (tokenResult.getInString())
			return SimpleType.TYPE_STRING.getTypeSignature();
		
		if (tokenResult.getInEscapeKeyword()) {
			switch (tokenResult.getQueryToken()) {
				case AliasPropertyInput:
				case EscapedKeywordInput:
				case EscapedKeywordDelimiter: {
					if (tokenResult.getAliasType() != null) {
						addProperties(possibleHints, tokenResult.getAliasType(), aliasInput);
					} else {
						addAlias(possibleHints, aliasInput, true);
					}

					break;
				}
				default:
					break;
			}
			
			return null;
		}

		switch (tokenResult.getQueryToken()) {
		case Operator:
		case OperatorWithSpace:
			int position = cursorPosition - amountOfSpaces;
			String typeSignature = addPossibleHintsBasedOnLeftOperand(queryString, position - tokenResult.getKeywordMatch().length(), aliasInput, possibleHints);
			//addSimpleTypes(possibleHints);
			addAlias(possibleHints, aliasInput);
			return typeSignature;
		case Alias:
			handleAlias(aliasInput, tokenResult, possibleHints, queryString, cursorPosition, "");
			break;
		case AliasProperty:
			tokenResult.setFilterString(QueryLexer.emptyString);
			addOperators(possibleHints);
			break;
		case AliasEntityProperty: {
			addTokenKeywords(possibleHints, QueryLexerToken.Dot);
			addOperators(possibleHints);
			tokenResult.setFilterString(QueryLexer.emptyString);
			break;
		}
		case AliasPropertyInput:
		case AliasPropertyDelimiter:
			if (tokenResult.getAliasType() != null) {
				addProperties(possibleHints, tokenResult.getAliasType(), aliasInput);
			}

			break;
		case BigDecimal:
			return SimpleType.TYPE_DECIMAL.getTypeSignature();
		case Boolean:
			return SimpleType.TYPE_BOOLEAN.getTypeSignature();
		case Double:
			return SimpleType.TYPE_DOUBLE.getTypeSignature();
		case Float:
			return SimpleType.TYPE_FLOAT.getTypeSignature();
		case Integer:
			QueryLexerResult previousToken; //If I am typing a number, I have to check if it is a right operand. If it is, I check what was the left operand type
			QueryLexerToken previousQueryToken;
			int counter = 0;
			int numberLength = tokenResult.getNumberString().length();
			do {
				previousToken = AutoCompletionLexer.getQueryToken(queryString, cursorPosition - numberLength - counter++, aliasInput);
				previousQueryToken = previousToken.getQueryToken();
			} while (previousQueryToken.equals(QueryLexerToken.Integer));
			
			QueryLexerToken queryToken = previousQueryToken;
			if (queryToken.equals(QueryLexerToken.Space)) {
				previousToken = AutoCompletionLexer.getQueryToken(queryString, cursorPosition - numberLength - counter, aliasInput);
				previousQueryToken = previousToken.getQueryToken();
			}
			
			if (previousQueryToken.equals(QueryLexerToken.Operator) || previousQueryToken.equals(QueryLexerToken.OperatorWithSpace)) {
				typeSignature = addPossibleHintsBasedOnLeftOperand(queryString, cursorPosition - counter - numberLength - previousToken.getKeywordMatch().length(), aliasInput, possibleHints);
				return typeSignature;
			}
			
			return SimpleType.TYPE_INTEGER.getTypeSignature();
		case Long:
			return SimpleType.TYPE_LONG.getTypeSignature();
		case From:
			addTypes(possibleHints, modelOracle, expandMode, tokenResult.getFilterString());
			break;
		case Where:
			addAlias(possibleHints, aliasInput);
			break;
		case Unknown:
			if (queryString.isEmpty() || isTypingStartQueryKeywords(queryString)) {
				addStartQueryKeywords(possibleHints, queryString);
				return null;
			}
			
			//I have to check if I am currently typing a right operand. If I am, then check and return the typeSignature.
			QueryLexerResult previous; //If I am typing a number, I have to check if it is a right operand. If it is, I check what was the left operand type
			QueryLexerToken prevQueryToken;
			String filterString = tokenResult.getFilterString();
			int filterLength = filterString.length();
			int i = 0;
			do {
				previous = AutoCompletionLexer.getQueryToken(queryString, cursorPosition - filterLength - i++, aliasInput);
				prevQueryToken = previous.getQueryToken();
			} while (prevQueryToken.equals(QueryLexerToken.Unknown) && cursorPosition - filterLength - i >= 0);
			
			if (prevQueryToken.equals(QueryLexerToken.Space)) {
				previous = AutoCompletionLexer.getQueryToken(queryString, cursorPosition - filterLength - i, aliasInput);
				prevQueryToken = previous.getQueryToken();
			}
			
			return checkToken(aliasInput, previous, possibleHints, queryString, cursorPosition - filterLength - i, amountOfSpaces, expandMode);
		default:
			break;
		}
		
		return null;
	}

	private void handleAlias(AliasInput aliasInput, QueryLexerResult tokenResult, Set<String> possibleHints, String queryString, int cursorPosition, String filterString) {
		QueryLexerResult previous;
		QueryLexerToken prevQueryToken;
		int filterLength = tokenResult.getFilterString().length();
		int counter = 0;
		do {
			previous = AutoCompletionLexer.getQueryToken(queryString, cursorPosition - filterLength - counter++, aliasInput);
			prevQueryToken = previous.getQueryToken();
		} while ((prevQueryToken.equals(QueryLexerToken.Unknown) || prevQueryToken.equals(QueryLexerToken.Alias) || prevQueryToken.equals(QueryLexerToken.Space))
				&& cursorPosition - filterLength - counter >= 0);
		
		QueryLexerToken queryToken1 = prevQueryToken;
		if (queryToken1.equals(QueryLexerToken.Space)) {
			previous = AutoCompletionLexer.getQueryToken(queryString, cursorPosition - filterLength - counter, aliasInput);
			prevQueryToken = previous.getQueryToken();
		}
		
		if (QueryLexerToken.From.equals(prevQueryToken)) {
			if (filterString.isEmpty()) {
				addTokenKeywords(possibleHints, QueryLexerToken.Where);
				addTokenKeywords(possibleHints, QueryLexerToken.OrderBy);
				tokenResult.setFilterString(QueryLexer.emptyString);
			} else {
				if (isTypingToken(filterString, QueryLexerToken.Where))
					addTokenKeywords(possibleHints, QueryLexerToken.Where);
				else if (isTypingToken(filterString, QueryLexerToken.OrderBy))
					addTokenKeywords(possibleHints, QueryLexerToken.OrderBy);
			}
		} else {
			addTokenKeywords(possibleHints, QueryLexerToken.Dot);
			tokenResult.setFilterString(QueryLexer.emptyString);
		}
	}

	private void addStartQueryKeywords(Set<String> possibleHints, String queryString) {
		if (!queryString.isEmpty()) {
			possibleHints.add(getTypedStartQueryKeyword(queryString));
			return;
		}
		
		addTokenKeywords(possibleHints, QueryLexerToken.Select);
		addTokenKeywords(possibleHints, QueryLexerToken.From);
		addTokenKeywords(possibleHints, QueryLexerToken.Property);
	}
	
	private String getTypedStartQueryKeyword(String queryString) {
		List<String> keywords = newList();
		keywords.addAll(Arrays.asList(QueryLexerToken.Select.getKeywords()));
		keywords.addAll(Arrays.asList(QueryLexerToken.From.getKeywords()));
		keywords.addAll(Arrays.asList(QueryLexerToken.Property.getKeywords()));
		
		Optional<String> keywordFound = keywords.stream().filter(keyword -> keyword.startsWith(queryString)).findFirst();
		return keywordFound.orElse(null);
	}

	private boolean isTypingStartQueryKeywords(String queryString) {
		List<String> keywords = newList();
		keywords.addAll(Arrays.asList(QueryLexerToken.Select.getKeywords()));
		keywords.addAll(Arrays.asList(QueryLexerToken.From.getKeywords()));
		keywords.addAll(Arrays.asList(QueryLexerToken.Property.getKeywords()));
		
		return keywords.stream().anyMatch(keyword -> keyword.startsWith(queryString));
	}
	
	private boolean isTypingToken(String queryString, QueryLexerToken token) {
		return Arrays.asList(token.getKeywords()).stream().anyMatch(keyword -> keyword.startsWith(queryString));
	}

	private static Set<String> filterPossibleHints(final Set<String> possibleHints, final String filterString) {

		Set<String> possibleHintsFilteredOut = new HashSet<>();
		// Check all hints and remove hints not starting with filter
		for (final Iterator<String> iterator = possibleHints.iterator(); iterator.hasNext(); /* NOP */) {
			final String possibleHint = iterator.next();

			// Check hint is starting with filter
			if (possibleHint.startsWith(filterString) == false) {
				// Remove hint
				iterator.remove();
				possibleHintsFilteredOut.add(possibleHint);
			}
		}
		
		return possibleHintsFilteredOut;
	}

	/*********************** Helper Methods ***********************/

	private static void addAlias(final Set<String> possibleHints, final AliasInput aliasInput) {
		addAlias(possibleHints, aliasInput, false);
	}

	private static void addAlias(final Set<String> possibleHints, final AliasInput aliasInput, final boolean escapedOnly) {
		if (possibleHints != null && aliasInput != null) {
			if (aliasInput.getAliasMap() != null) {
				final Set<String> keywords = aliasInput.getKeywords();
				final String escapeKeyword = AutoCompletionLexer.getFirstTokenKeyword(QueryLexerToken.EscapedKeywordDelimiter);

				for (String alias : aliasInput.getAliasMap().keySet()) {
					if (keywords != null && keywords.contains(alias)) {
						alias = escapeKeyword + alias + escapeKeyword;
					} else if (escapedOnly == true) {
						continue;
					}

					possibleHints.add(alias);
				}
			}
		}
	}

	private static void addProperties(Set<String> possibleHints, GmType gmType, AliasInput aliasInput) {
		addProperties(possibleHints, gmType, aliasInput, false);
	}

	private static void addProperties(Set<String> possibleHints, GmType gmType, AliasInput aliasInput, boolean escapedOnly) {
		if (possibleHints != null && gmType != null && gmType.typeKind() == GmTypeKind.ENTITY)
			addProperties(GMF.getTypeReflection().getEntityType(gmType.getTypeSignature()), aliasInput, escapedOnly, possibleHints);
	}
	
	private static void addProperties(EntityType<?> entityType, AliasInput aliasInput, boolean escapedOnly, Set<String> possibleHints) {
		Set<String> keywords = aliasInput.getKeywords();
		String escapeKeyword = AutoCompletionLexer.getFirstTokenKeyword(QueryLexerToken.EscapedKeywordDelimiter);
		
		for (Property property : entityType.getProperties()) {
			String entityPropertyName = property.getName();
			if (keywords != null && keywords.contains(entityPropertyName)) {
				entityPropertyName = escapeKeyword + entityPropertyName + escapeKeyword;
			} else if (escapedOnly == true) {
				continue;
			}

			possibleHints.add(entityPropertyName);
		}
	}

	private static void addTokenKeywords(final Set<String> possibleHints, final QueryLexerToken token) {
		if (possibleHints != null && token != null) {
			for (final String keyword : token.getKeywords()) {
				possibleHints.add(keyword);
			}
		}
	}
	
	private static void addOperators(Set<String> possibleHints) {
		Arrays.asList(Operator.getOperatorSigns()).forEach(operator -> {
			possibleHints.add(operator);
		});
	}
	
	private String addPossibleHintsBasedOnLeftOperand(String queryString, int cursorPosition, AliasInput aliasInput, Set<String> possibleHints) {
		QueryLexerResult leftOperand = AutoCompletionLexer.getPreviousNonSpaceQueryToken(queryString, cursorPosition, aliasInput);
			
		switch (leftOperand.getQueryToken()) {
			case AliasProperty:
			case AliasEntityProperty:
				GmType propertyType = leftOperand.getPropertyType() != null ? leftOperand.getPropertyType() : leftOperand.getAliasType();
				String typeSignature = propertyType.getTypeSignature();
				possibleHints.add(typeSignature);
				return typeSignature;
			case String:
				possibleHints.add(SimpleTypes.TYPE_STRING.getTypeName());
				return SimpleTypes.TYPE_STRING.getTypeSignature();
			case Boolean:
				possibleHints.add(SimpleTypes.TYPE_BOOLEAN.getTypeName());
				return SimpleTypes.TYPE_BOOLEAN.getTypeSignature();
			case Integer:
				possibleHints.add(SimpleTypes.TYPE_INTEGER.getTypeName());
				return SimpleTypes.TYPE_INTEGER.getTypeSignature();
			case Long:
				possibleHints.add(SimpleTypes.TYPE_LONG.getTypeName());
				return SimpleTypes.TYPE_LONG.getTypeSignature();
			case Float:
				possibleHints.add(SimpleTypes.TYPE_FLOAT.getTypeName());
				return SimpleTypes.TYPE_FLOAT.getTypeSignature();
			case Double:
				possibleHints.add(SimpleTypes.TYPE_DOUBLE.getTypeName());
				return SimpleTypes.TYPE_DOUBLE.getTypeSignature();
			case BigDecimal:
				possibleHints.add(SimpleTypes.TYPE_DECIMAL.getTypeName());
				return SimpleTypes.TYPE_DECIMAL.getTypeSignature();
		default:
			return null;
		}
	}
	
	private static void addTypes(Set<String> possibleHints, ModelOracle modelOracle, SignatureExpert expandMode, String filterString) {
		possibleHints.addAll(GmTypeExpert.getGmEntityTypes(modelOracle, expandMode, filterString));
	}

}
