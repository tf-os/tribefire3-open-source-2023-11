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
package com.braintribe.model.processing.smart.query.planner;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartAddress;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;

/**
 * 
 */
public class SingleSource_DelegateCondition_PlannerTests extends AbstractSmartQueryPlannerTests {

	@Test
	public void simpleCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartAddress.T, "a")
					.select("a", "street")
					.where()
						.property("a", "number").eq().value(10)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessA").close()
				.whereProperty("scalarMappings").isListWithSize(1).close()
				.whereDelegateQuery()
					.whereProperty("selections")
						.whereElementAt(0).isPropertyOperand("street")
					.whereCondition()
						.hasType(Conjunction.T)
						.whereProperty("operands").isListWithSize(1)
							.whereElementAt(0).hasType(ValueComparison.T)
								.whereProperty("leftOperand").isPropertyOperand("number")
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isTupleComponent_(0)
		;
		// @formatter:on
	}

	@Test
	public void simplePropertyPathCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.join("p", "companyA", "c")
				.join("c", "ownerA", "o")
				.select("p", "nameA")
					.where()
						.property("o", "nameA").eq("Bill")
				.done();
		// @formatter:on

		runTest(selectQuery);

		/* Expecting simple delegate query: select PersonA.nameA from PersonA as PersonA join PersonA.companyA as
		 * CompanyA join CompanyA.ownerA as PersonA1 where (PersonA1.nameA = 'Bill') */

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessA").close()
				.whereProperty("scalarMappings").isListWithSize(1).close()
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("nameA")
					.whereCondition()
						.isSingleOperandConjunctionAndOperand()
							.isValueComparison(Operator.equal).whereProperty("leftOperand")
								.isPropertyOperand("nameA")
								.whereSource().isJoin("ownerA")
								.whereSource().isJoin("companyA")
								.whereSource().isFrom(PersonA.T)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isTupleComponent_(0)
		;
		// @formatter:on
	}

	@Test
	public void simpleFulltextCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.select("p", "nameA")
					.where()
						.fullText("p", "foobar")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessA").close()
				.whereProperty("scalarMappings").isListWithSize(1).close()
				.whereDelegateQuery()
					.whereProperty("selections")
						.whereElementAt(0).isPropertyOperand("nameA")
					.whereCondition()
						.hasType(Conjunction.T)
						.whereProperty("operands").isListWithSize(1)
							.whereElementAt(0).hasType(FulltextComparison.T)
							.whereProperty("text").is_("foobar")
							.whereProperty("source")
								.isFrom(PersonA.T)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isTupleComponent_(0)
		;
		// @formatter:on
	}

	// TODO SmoodAccess tests
	// also check how I pick which columns will be scanned for FulltextSearch
	@Test
	public void disjunctionFulltextCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "pA")
				.join("pA", "smartParentB", "pB")
					.select("pA", "nameA")
					.where()
						.disjunction()
							.fullText("pA", "foobar")
							.fullText("pB", "foobar")
						.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// TODO add asserts
	}

	@Test
	public void simpleConjunction() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartAddress.T, "a")
				.select("a", "street")
				.where()
					.conjunction()
						.property("a", "number").ge().value(10)
						.property("a", "number").lt().value(20)
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessA").close()
				.whereProperty("scalarMappings").isListWithSize(1).close()
				.whereDelegateQuery()
					.whereProperty("selections")
						.whereElementAt(0).isPropertyOperand("street")
					.whereCondition()
						.isConjunction(2)
							.whereElementAt(0).hasType(ValueComparison.T).whereProperty("leftOperand").isPropertyOperand("number").close(2)
							.whereElementAt(1).hasType(ValueComparison.T).whereProperty("leftOperand").isPropertyOperand("number").close(2)
				.endQuery()

			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isTupleComponent_(0)
		;
		// @formatter:on
	}

	@Test
	public void simpleDisjunction() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartAddress.T, "a")
				.select("a", "street")
				.where()
					.disjunction()
						.property("a", "number").lt().value(10)
						.property("a", "number").ge().value(20)
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessA").close()
				.whereProperty("scalarMappings").isListWithSize(1).close()
				.whereDelegateQuery()
					.whereProperty("selections")
						.whereElementAt(0).isPropertyOperand("street")
					.whereCondition()
						.isConjunction(1).whereElementAt(0).isDisjunction(2)
								.whereElementAt(0).hasType(ValueComparison.T).whereProperty("leftOperand").isPropertyOperand("number").close(2)
								.whereElementAt(1).hasType(ValueComparison.T).whereProperty("leftOperand").isPropertyOperand("number").close(2)
				.endQuery()

			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isTupleComponent_(0)
		;
		// @formatter:on
	}

	@Test
	public void simpleNegation() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartAddress.T, "a")
				.select("a", "street")
				.where()
					.negation()
						.property("a", "zipCode").like("*555*")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQuerySet.T)
				.whereProperty("delegateAccess").isDelegateAccess("accessA").close()
				.whereProperty("scalarMappings").isListWithSize(1).close()
				.whereDelegateQuery()
					.whereProperty("selections")
						.whereElementAt(0).isPropertyOperand("street")
					.whereCondition()
						.isConjunction(1)
							.whereElementAt(0)
							.hasType(Negation.T)
								.whereProperty("operand")
								.hasType(ValueComparison.T).whereProperty("leftOperand").isPropertyOperand("zipCode").close().close()
				.endQuery()

			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isTupleComponent_(0)
		;
		// @formatter:on
	}
}
