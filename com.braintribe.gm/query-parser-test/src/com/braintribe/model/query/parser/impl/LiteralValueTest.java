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

import java.math.BigDecimal;

import org.junit.Test;

import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.utils.genericmodel.GMCoreTools;
import com.braintribe.utils.junit.assertions.BtAssertions;

public class LiteralValueTest extends AbstractQueryParserTest {

	@Test
	public void testDecimalInteger() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Integer.valueOf(1)).eq().value(Integer.valueOf(-145))
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 1 = -145";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testHexadecimalInteger() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Integer.valueOf(40)).eq().value(Integer.valueOf(-12431))
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 0x28 = -0x308F";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testDecimalLong() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Long.valueOf(1)).eq().value(Long.valueOf(-145))
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 1l = -145l";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testHexadecimalLong() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Long.valueOf(40)).eq().value(Long.valueOf(-12431))
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 0x28l = -0x308Fl";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testDoubleDigitPriorToPointWithSuffix() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Double.valueOf(0.1234)).eq().value(Double.valueOf(-23.4567))
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 0.1234d = -234.567e-1d";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testDoubleNoDigitPointWithSuffix() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Double.valueOf(0.1234)).eq().value(Double.valueOf(-.04567))
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 0.01234e1d = -.4567e-1d";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testDoubleNoPointWithExponentWithSuffix() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Double.valueOf(230)).eq().value(Double.valueOf(-.0023))
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 23e1d = -23e-4d";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testDoubleDigitPriorToPoint() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Double.valueOf(0.1234)).eq().value(Double.valueOf(-23.4567))
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 0.1234 = -234.567e-1";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testDoubleNoDigitPoint() throws Exception {
		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Double.valueOf(0.1234)).eq().value(Double.valueOf(-.04567))
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 0.01234e1 = -.4567e-1";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testDoubleNoPointWithExponent() throws Exception {
		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Double.valueOf(230)).eq().value(Double.valueOf(-.0023))
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 23e1 = -23e-4";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testDoubleNoPointWithoutExponent() throws Exception {
		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Double.valueOf(23)).eq().value(Double.valueOf(-34))
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 23d = -34d";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testDoubleSpecial() throws Exception {
		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Double.NaN).eq().value(Double.POSITIVE_INFINITY)
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where +NaND = +InfinityD";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testFloatNoPoint() throws Exception {
		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Float.valueOf(230)).eq().value(Float.valueOf(-0.0023f))
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 23e1f = -23e-4f";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testFloatNoPointRationalNumber() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Float.valueOf(230)).eq().value(Float.valueOf(-23))
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 230f = -23f";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testFloatSpecial() throws Exception {
		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Float.NaN).eq().value(Float.NEGATIVE_INFINITY)
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where +NaNF = -InfinityF";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testDecimalNoPoint() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(new BigDecimal("23")).eq().value(new BigDecimal(-45))
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 23e0b = -45e0b";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testDecimalNoPointWithSuffix() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(new BigDecimal("23")).eq().value(new BigDecimal(-45))
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 23b = -45b";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testBoolean() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Boolean.TRUE).ne().value(Boolean.FALSE)
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where true != false";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		BtAssertions.assertThat(parsedQuery.getVariablesMap()).isEmpty();
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testVariable() throws Exception {

		Variable var1 = Variable.T.create();
		var1.setName("var1");
		Variable var2 = Variable.T.create();
		var2.setName("var2");
		Variable var3 = Variable.T.create();
		var3.setName("var3");

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.select("p","name")
				.where()
					.conjunction()
						.value(Boolean.TRUE).eq().value(Boolean.TRUE)
						.value(var1).ne().value(var2)
						.value(var3).eq().value(5)
						.close()
				.done();
		// @formatter:on

		expectedQuery.getSelections().add(var1);

		String queryString = "select  p.name, :var1 from " + Person.class.getName() + " p where true = true and :var1 != :var2 and :var3 = 5";
		ParsedQuery parsedQuery = QueryParser.parse(queryString);

		validatedParsedQuery(parsedQuery);
		BtAssertions.assertThat(parsedQuery.getVariablesMap()).isNotEmpty();
		BtAssertions.assertThat(parsedQuery.getVariablesMap().size()).isEqualTo(3);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);

	}

	@Test
	public void testVariableWithSpecifications() throws Exception {

		Variable var1 = Variable.T.create();
		var1.setName("var1");
		Variable var2 = Variable.T.create();
		var2.setName("var2");
		var2.setDefaultValue("hello");
		Variable var3 = Variable.T.create();
		var3.setName("var3");
		var3.setTypeSignature("integer");
		var3.setDefaultValue(1);
		Variable var4 = Variable.T.create();
		var4.setName("var4");
		var4.setTypeSignature("string");
		var4.setDefaultValue(null);
		Variable var5 = Variable.T.create();
		var5.setName("var5");
		var5.setTypeSignature("integer");

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.select("p","name")
				.where()
					.conjunction()
						.value(Boolean.TRUE).eq().value(Boolean.TRUE)
						.value(var1).ne().value(var2)
						.value(var3).eq().value(5)
						.value(var4).eq().value(5)
						.value(var5).eq().value(5)
						.close()
				.done();
		// @formatter:on

		expectedQuery.getSelections().add(var1);

		String queryString = "select  p.name, :var1 from " + Person.class.getName()
				+ " p where true = true and :var1 != :var2(string,'hello') and :var3(integer,1) = 5 and :var4(string,null) = 5 and :var5(integer) = 5";
		ParsedQuery parsedQuery = QueryParser.parse(queryString);

		validatedParsedQuery(parsedQuery);
		BtAssertions.assertThat(parsedQuery.getVariablesMap()).isNotEmpty();
		BtAssertions.assertThat(parsedQuery.getVariablesMap().size()).isEqualTo(5);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);

	}

	@Test
	public void testVariableWithTypeSignatureAndMultipleDeclarations() throws Exception {

		String queryString = "select  p.name from " + Person.class.getName() + " p where true = true and :var1(integer,1) != :var1(string,'hello')";
		ParsedQuery parsedQuery = QueryParser.parse(queryString);

		BtAssertions.assertThat(parsedQuery.getIsValidQuery()).isEqualTo(false);
		BtAssertions.assertThat(parsedQuery.getErrorList().get(0).getMessage()).contains("must not be declared multiple times");

	}

	@Test
	public void testNull() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Boolean.TRUE).ne().value(null)
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where true != null";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		BtAssertions.assertThat(parsedQuery.getVariablesMap()).isEmpty();
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testString() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.value(Boolean.TRUE).ne().value("\\u0100 \u0100 ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMONPRSTUVWXZY1234567890!@#$%^&*()_`\\n \\b \\t \\f \\' \\r \\\\")
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName()
				+ " p where true != '\\u0100 \u0100 ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMONPRSTUVWXZY1234567890!@#$%^&*()_`\\n \\b \\t \\f \\' \\r \\\\'";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}
}
