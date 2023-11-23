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
package com.braintribe.model.processing.query.test.stringifier;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.processing.query.test.model.Color;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.query.test.stringifier.model.TypeTestModel;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

public class SpecialSelectTests extends AbstractSelectQueryTests {
	@Test
	public void nowCondition() {
		SelectQuery selectQuery = createNowFunctionSelectQuery();

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString).isEqualToIgnoringCase(
				"select * from com.braintribe.model.processing.query.test.model.Person _Person where _Person.birthDate = now()");
	}

	@Test
	public void definedSourceNames() {
		SelectQuery selectQuery = createDefinedSourceNamesSelectQuery("person", "company", "person2");

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString).isEqualToIgnoringCase(
				"select * from com.braintribe.model.processing.query.test.model.Person person join person.company company join company.persons person2 where 'John' = person2.name");
	}

	@Test
	public void definedSourceNames2() {
		SelectQuery selectQuery = createDefinedSourceNamesSelectQuery("where", "from", "where2");

		String actual = stringify(selectQuery);
		String expected = "select * from com.braintribe.model.processing.query.test.model.Person \"where\" join \"where\".company \"from\" join \"from\".persons where2 where 'John' = where2.name";

		Assert.assertTrue(actual.equalsIgnoreCase(expected));
	}

	@Test
	public void mixedSourceConditionOnEntityProperty() {
		TypeTestModel typeTest = TypeTestModel.T.create();

		EnumReference enumReference = EnumReference.T.create();
		enumReference.setTypeSignature(Color.class.getName());
		enumReference.setConstant(Color.GREEN.toString());

		BigDecimal bigDecimal = new BigDecimal(55.2D);
		bigDecimal = bigDecimal.round(MathContext.DECIMAL32);

		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(TypeTestModel.class, "_TypeTestModel")
				.where()
				.disjunction() // D1
					.property("_TypeTestModel", "doubleValue").eq(99.2D)
					.conjunction() // C
						.property("_TypeTestModel", "decimalValue").le(bigDecimal)
						.property("_TypeTestModel", "floatValue").eq(33.2F)
						.negation()
							.property("_TypeTestModel", "longValue").eq(55L)
						.disjunction() // D2
							.negation()
								.property("_TypeTestModel", "intValue").eq(25)
							.property("_TypeTestModel", "boolValue").eq(true)
						.close() // D2
						.property("_TypeTestModel", "enumValue").eq(enumReference)
					.close() // C
					.property("_TypeTestModel", "entityValue").eq().entity(typeTest)
				.close() // D2
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);

		String typeTestModelTypeSignature = TypeTestModel.T.getTypeSignature();
		String compareString = "select * from " + typeTestModelTypeSignature
				+ " _TypeTestModel where (_TypeTestModel.doubleValue = 99.2d or (_TypeTestModel.decimalValue <= 55.20000b and _TypeTestModel.floatValue = 33.2f and not _TypeTestModel.longValue = 55l and (not _TypeTestModel.intValue = 25 or _TypeTestModel.boolValue = true) and _TypeTestModel.enumValue = enum(com.braintribe.model.processing.query.test.model.Color, GREEN)) or _TypeTestModel.entityValue = reference("
				+ typeTestModelTypeSignature + ", "+typeTest.reference().getRefId()+"l, false))";

		Assert.assertTrue(queryString.equalsIgnoreCase(compareString));
	}

	/**
	 * Create a SelectQuery containing the Now function. This function is currently not included to the
	 * SelectQueryBuilder.
	 *
	 * @return SelectQuery
	 */
	private static SelectQuery createNowFunctionSelectQuery() {
		SelectQuery query = SelectQuery.T.create();

		From from = From.T.create();
		from.setEntityTypeSignature(Person.class.getName());
		from.setName("_Person");

		PropertyOperand operand = PropertyOperand.T.create();
		operand.setPropertyName("birthDate");
		operand.setSource(from);

		ValueComparison condition = ValueComparison.T.create();
		condition.setLeftOperand(operand);
		condition.setOperator(Operator.equal);
		condition.setRightOperand(Now.T.create());

		Restriction restriction = Restriction.T.create();
		restriction.setCondition(condition);

		query.setFroms(asList(from));
		query.setRestriction(restriction);

		return query;
	}

	/**
	 * Create a SelectQuery containing a Name for the sources. The source name is currently not included to the
	 * SelectQueryBuilder.
	 *
	 * @return SelectQuery
	 */
	private static SelectQuery createDefinedSourceNamesSelectQuery(String aliasFrom, String aliasJoin1, String aliasJoin2) {
		SelectQuery query = SelectQuery.T.create();

		From from = From.T.create();
		from.setEntityTypeSignature(Person.class.getName());
		from.setName(aliasFrom);

		Join companyJoin = Join.T.create();
		companyJoin.setName(aliasJoin1);
		companyJoin.setSource(from);
		companyJoin.setProperty("company");
		companyJoin.setJoinType(JoinType.inner);

		Join personsJoin = Join.T.create();
		personsJoin.setName(aliasJoin2);
		personsJoin.setSource(companyJoin);
		personsJoin.setProperty("persons");
		personsJoin.setJoinType(JoinType.inner);

		PropertyOperand operand = PropertyOperand.T.create();
		operand.setSource(personsJoin);
		operand.setPropertyName("name");

		ValueComparison condition = ValueComparison.T.create();
		condition.setLeftOperand("John");
		condition.setOperator(Operator.equal);
		condition.setRightOperand(operand);

		Restriction restriction = Restriction.T.create();
		restriction.setCondition(condition);

		companyJoin.setJoins(asSet(personsJoin));
		from.setJoins(asSet(companyJoin));

		query.setFroms(asList(from));
		query.setRestriction(restriction);

		return query;
	}
}
