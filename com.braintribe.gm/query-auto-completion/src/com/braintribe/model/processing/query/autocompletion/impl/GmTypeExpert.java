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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.api.shortening.QueryShorteningRuntimeException;
import com.braintribe.model.processing.query.api.shortening.SignatureExpert;
import com.braintribe.model.processing.query.autocompletion.QueryAutoCompletionRuntimeException;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.QueryLexer;
import com.braintribe.model.processing.query.autocompletion.impl.lexer.container.QueryLexerToken;
import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Source;
import com.braintribe.utils.lcd.StringTools;

/**
 * It will recognize every children of an GmEntityType (properties and so on).
 * Also aliases.
 *
 */
public class GmTypeExpert {
	public static Map<String, GmType> getAliasMap(final String queryString, final ModelOracle modelOracle) {
		return getAliasMap(queryString, modelOracle, null);
	}

	public static Map<String, GmType> getAliasMap(final String queryString, final ModelOracle modelOracle, final SignatureExpert expandMode) {
		final Map<String, GmType> result = new HashMap<>();

		// Get check query -> [Select * ("from" to "where")]
		final String checkQuery = getAliasCheckQuery(queryString);
		if (StringTools.isEmpty(checkQuery) == false) {
			// Get Alias, Source Map from QueryParser
			final ParsedQuery parsedQuery = QueryParser.parse(checkQuery);
			final Map<String, Source> sourceRegistry = parsedQuery.getSourcesRegistry();

			// Get GmTypes from Source
			for (Map.Entry<String, Source> entry : sourceRegistry.entrySet()) {
				try {
					String aliasName = entry.getKey();
					Source source = entry.getValue();
					// Get GmType of Source
					final GmType gmType = getGmType(source, modelOracle, expandMode);
					if (gmType != null) {
						result.put(aliasName, gmType);
					}
				} catch (final QueryAutoCompletionRuntimeException e) {
					// Next alias
					continue;
				}
			}
			for (final String aliasName : sourceRegistry.keySet()) {
				try {
					// Get GmType of Source
					final GmType gmType = getGmType(sourceRegistry.get(aliasName), modelOracle, expandMode);
					if (gmType != null) {
						result.put(aliasName, gmType);
					}
				} catch (final QueryAutoCompletionRuntimeException e) {
					// Next alias
					continue;
				}
			}
		}

		return result;
	}

	public static GmType getGmType(final Source aliasSource, final ModelOracle modelOracle) {
		return getGmType(aliasSource, modelOracle, null);
	}

	public static GmType getGmType(final Source aliasSource, final ModelOracle modelOracle, final SignatureExpert expandMode) {
		if (aliasSource instanceof From) {
			final From fromSource = (From) aliasSource;

			// Get TypeSignature of From
			String typeSignature = fromSource.getEntityTypeSignature();
			if (expandMode != null) {
				try {
					// Expand TypeSignature of From
					typeSignature = expandMode.expand(typeSignature);
				} catch (final QueryShorteningRuntimeException e) {
					// Re-Throw as QueryAutoCompletion-Exception
					throw new QueryAutoCompletionRuntimeException(e);
				}
			}

			// Get GmType of TypeSignature
			return modelOracle.findGmType(typeSignature);
		} else if (aliasSource instanceof Join) {
			final Join joinSource = (Join) aliasSource;

			// Get GmType from TypeSignature of Source inside Join
			GmType gmType = getGmType(joinSource.getSource(), modelOracle, expandMode);
			if (StringTools.isEmpty(joinSource.getProperty()) == false && gmType.typeKind() == GmTypeKind.ENTITY) {
				final GmEntityType entityType = (GmEntityType) gmType;

				// Get and check GmProperty of GmEntityType
				final GmProperty entityProperty = getEntityPropertyByName(entityType, joinSource.getProperty());
				if (entityProperty == null) {
					// Property is defined but was not found in GmEntityType
					throw new QueryAutoCompletionRuntimeException("Invalid Source property found!\n\r" + joinSource.getProperty());
				}

				// Return GmType of GmProperty
				gmType = entityProperty.getType();
			}

			// GmType found
			return gmType;
		} else {
			// Should never happen, but just in case
			throw new QueryAutoCompletionRuntimeException("Unknown Source type found!\n\r" + aliasSource);
		}
	}

	public static GmProperty getEntityPropertyByName(final GmType gmType, final String propertyName) {
		if (propertyName.isEmpty() || !gmType.typeKind().equals(GmTypeKind.ENTITY))
			return null;

		final GmEntityType entityType = (GmEntityType) gmType;

		// Get properties from entity type
		for (final GmProperty entityProperty : entityType.getProperties()) {
			// Is entity property name the searched one?
			if (entityProperty.getName().equals(propertyName)) {
				return entityProperty;
			}
		}

		for (GmEntityType superType : entityType.getSuperTypes()) {
			GmProperty property = getEntityPropertyByName(superType, propertyName);
			if (property != null)
				return property;
		}

		return null;
	}
	
	public static List<String> getGmEntityTypes(ModelOracle modelOracle, SignatureExpert expandMode, String filter) {
		// @formatter:off
		return modelOracle.getTypes().onlyEntities().asGmTypes()
				.map(GmType::getTypeSignature)
				.filter(signature -> signature.startsWith(filter) || expandMode.shorten(signature).startsWith(filter))
				.map(signature -> {
					String shortened = expandMode.shorten(signature);
					return shortened.startsWith(filter) ? shortened : signature;
				})
				.collect(Collectors.toList());
		// @formatter:on
	}

	/******************************** Helper Methods ********************************/

	private static String getAliasCheckQueryPrefix() {
		final StringBuilder prefix = new StringBuilder();

		prefix.append(AutoCompletionLexer.getFirstTokenKeyword(QueryLexerToken.Select));
		prefix.append(AutoCompletionLexer.getFirstTokenKeyword(QueryLexerToken.Space));
		prefix.append(AutoCompletionLexer.getFirstTokenKeyword(QueryLexerToken.WildChar));
		prefix.append(AutoCompletionLexer.getFirstTokenKeyword(QueryLexerToken.Space));

		return prefix.toString();
	}

	private static String getAliasCheckQuery(final String queryString) {
		String checkQuery = null;

		// Remove everything before from from QueryString
		final Map<Integer, Integer> fromIndices = QueryLexer.getTokenIndices(QueryLexerToken.From, queryString, queryString.length());
		for (final int fromIndex : fromIndices.keySet()) {
			checkQuery = getAliasCheckQueryPrefix() + queryString.substring(fromIndex);
			break;
		}

		if (checkQuery != null) {
			// Remove everything behind where from CheckQuery
			final Map<Integer, Integer> whereIndices = QueryLexer.getTokenIndices(QueryLexerToken.Where, checkQuery, checkQuery.length());
			for (final int whereIndex : whereIndices.keySet()) {
				checkQuery = checkQuery.substring(0, whereIndex);
				break;
			}
		}

		return checkQuery;
	}
}
