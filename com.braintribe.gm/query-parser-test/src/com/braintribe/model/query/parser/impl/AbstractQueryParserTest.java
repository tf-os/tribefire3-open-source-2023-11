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
package com.braintribe.model.query.parser.impl;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.parser.api.GmqlParsingError;
import com.braintribe.model.processing.query.parser.api.GmqlSemanticParsingError;
import com.braintribe.model.processing.query.parser.api.GmqlSyntacticParsingError;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.utils.junit.assertions.BtAssertions;

public abstract class AbstractQueryParserTest {

	protected SelectQueryBuilder sq() {
		return new SelectQueryBuilder();
	}

	protected void validatedParsedQuery(ParsedQuery parsedQuery) {

		if (!parsedQuery.getIsValidQuery()) {
			System.out.println("invalid query: " + parsedQuery.getErrorList().get(0).getMessage());
		}

		BtAssertions.assertThat(parsedQuery.getIsValidQuery()).as("Query is invalid.").isTrue();
		BtAssertions.assertThat(parsedQuery.getErrorList()).isNotNull();
		BtAssertions.assertThat(parsedQuery.getErrorList()).isEmpty();
		BtAssertions.assertThat(parsedQuery.getQuery()).isNotNull();
		BtAssertions.assertThat(parsedQuery.getSourcesRegistry()).isNotNull();
		BtAssertions.assertThat(parsedQuery.getVariablesMap()).isNotNull();
	}

	protected void validatedInvalidParsedQuery(ParsedQuery parsedQuery, List<GmqlParsingError> expectedErrorList) {
		BtAssertions.assertThat(parsedQuery.getSourcesRegistry()).isNotNull();
		BtAssertions.assertThat(parsedQuery.getIsValidQuery()).isFalse();
		// A query might or might not be available, but there will always be an
		// error
		BtAssertions.assertThat(parsedQuery.getErrorList()).isNotNull();
		BtAssertions.assertThat(parsedQuery.getErrorList()).isNotEmpty();
		validateQueryError(parsedQuery, expectedErrorList);
	}

	private void validateQueryError(ParsedQuery parsedQuery, List<GmqlParsingError> expectedErrorList) {
		List<GmqlParsingError> actualErrorList = parsedQuery.getErrorList();
		BtAssertions.assertThat(actualErrorList.size()).isEqualTo(expectedErrorList.size());

		// GMCoreTools.checkDescription(actualErrorList, expectedErrorList);
		for (int i = 0; i < expectedErrorList.size(); i++) {
			if (actualErrorList.get(i) instanceof GmqlSyntacticParsingError) {
				GmqlSyntacticParsingError actualError = (GmqlSyntacticParsingError) actualErrorList.get(i);
				GmqlSyntacticParsingError expectedError = (GmqlSyntacticParsingError) expectedErrorList.get(i);
				BtAssertions.assertThat(actualError.getCharacterPostionInLine()).isEqualTo(expectedError.getCharacterPostionInLine());
				BtAssertions.assertThat(actualError.getLineNumber()).isEqualTo(expectedError.getLineNumber());
				BtAssertions.assertThat(actualError.getMessage()).isEqualTo(expectedError.getMessage());
				BtAssertions.assertThat(actualError.getOffendingToken()).isEqualTo(expectedError.getOffendingToken());
			} else {
				// semantic error
				GmqlSemanticParsingError actualError = (GmqlSemanticParsingError) actualErrorList.get(i);
				GmqlSemanticParsingError expectedError = (GmqlSemanticParsingError) expectedErrorList.get(i);
				if (actualError.getMessage().contains("@")) {
					// String regex = ".*@\\[[0-9]*\\]";
					String regex = "@(\\d)+";
					String actualMessage = actualError.getMessage().replaceAll(regex, "someId");
					String expectedMessage = expectedError.getMessage().replaceAll(regex, "someId");
					BtAssertions.assertThat(actualMessage).isEqualTo(expectedMessage);
				} else {
					BtAssertions.assertThat(actualError.getMessage()).isEqualTo(expectedError.getMessage());
				}
			}
		}
	}

	protected List<GmqlParsingError> getExpectedError(String message) {
		List<GmqlParsingError> expectedErrorList = new ArrayList<GmqlParsingError>();
		GmqlSemanticParsingError expectedError = GmqlSemanticParsingError.T.create();
		expectedError.setMessage(message);
		expectedErrorList.add(expectedError);
		return expectedErrorList;
	}

	protected List<GmqlParsingError> getExpectedError(int characterPostionInLine, int lineNumber, String message, String offendingToken) {
		List<GmqlParsingError> expectedErrorList = new ArrayList<GmqlParsingError>();
		GmqlSyntacticParsingError expectedError = GmqlSyntacticParsingError.T.create();
		expectedError.setCharacterPostionInLine(characterPostionInLine);
		expectedError.setLineNumber(lineNumber);
		expectedError.setMessage(message);
		expectedError.setOffendingToken(offendingToken);
		expectedErrorList.add(expectedError);
		return expectedErrorList;
	}

	protected List<GmqlSyntacticParsingError> getExpectedError(List<GmqlSyntacticParsingError> expectedErrorList, int characterPostionInLine,
			int lineNumber, String message, String offendingToken) {
		GmqlSyntacticParsingError expectedError = GmqlSyntacticParsingError.T.create();
		expectedError.setCharacterPostionInLine(characterPostionInLine);
		expectedError.setLineNumber(lineNumber);
		expectedError.setMessage(message);
		expectedError.setOffendingToken(offendingToken);
		expectedErrorList.add(expectedError);
		return expectedErrorList;
	}

}
