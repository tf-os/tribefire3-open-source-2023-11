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
package com.braintribe.model.processing.query.test;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.processing.query.planner.QueryPlanner;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.query.test.repository.DelegatingRepositoryMock;
import com.braintribe.model.processing.query.test.repository.IndexConfiguration;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.queryplan.filter.Equality;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.MergeLookupJoin;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.QuerySourceSet;
import com.braintribe.model.queryplan.set.join.EntityJoin;
import com.braintribe.testing.category.KnownIssue;

/**
 * 
 */
public class DelegatingRepositoryTests extends AbstractQueryPlannerTests {

	private static final QueryPlanner standardQueryPlanner = new QueryPlanner(new DelegatingRepositoryMock(new IndexConfiguration()));

	@Override
	protected QueryPlanner getQueryPlanner() {
		return standardQueryPlanner;
	}

	@Test
	public void simpleConditionQuery() throws Exception {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "name").eq("John")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(FilteredSet.T)
				.whereProperty("filter").hasType(Equality.T).close()
				.whereOperand()
					.hasType(QuerySourceSet.T)
					.whereProperty("entityTypeSignature").is_(Person.class.getName())
					.whereProperty("condition")
						.hasType(ValueComparison.T)
						.whereProperty("leftOperand")
							.isPropertyOperandAndSource("name").isNull().close()
						.close()
						.whereProperty("rightOperand").is_("John")
		;
		// @formatter:on
	}

	/**
	 * There once was a bug (DEVCX-988) that a disjunction was handled incorrectly and only a simple {@link FilteredSet}
	 * was created. Special handling for Disjunctions was added, but its's till not perfect.
	 * 
	 * @see #EXPECTED_TO_FAIL_complexDisjunction()
	 */
	@Test
	public void simpleDisjunction() throws Exception {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.disjunction()
						.property("p", "name").eq("Peter")
						.property("p", "name").eq("Elon")
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(FilteredSet.T)
				.whereProperty("filter").hasType(com.braintribe.model.queryplan.filter.Disjunction.T).close()
				.whereOperand()
					.hasType(QuerySourceSet.T)
					.whereProperty("entityTypeSignature").is_(Person.class.getName())
					.whereProperty("condition")
						.hasType(Disjunction.T)
						.whereProperty("operands").isListWithSize(2)
							.whereElementAt(0)
								.hasType(ValueComparison.T)
								.whereProperty("leftOperand")
									.isPropertyOperandAndSource("name").close()
								.close()
								.whereProperty("rightOperand").is_("Peter")
							.close()
							.whereElementAt(1)
								.hasType(ValueComparison.T)
								.whereProperty("leftOperand")
									.isPropertyOperandAndSource("name").close()
								.close()
								.whereProperty("rightOperand").is_("Elon")
							.close()
						.close() // operands
					.close() // condition
				.close() // operand
		;
		// @formatter:on
	}

	/**
	 * Let's make sure that we create a {@link QuerySourceSet} even if we have more than just a disjunction.
	 * 
	 * @see #simpleDisjunction()
	 */
	@Test
	public void simpleDisjunctionAndOther() throws Exception {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.conjunction()
						.disjunction()
							.property("p", "name").eq("Peter")
							.property("p", "name").eq("Elon")
						.close()
						.property("p", "companyName").eq().property("c", "name")
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(MergeLookupJoin.T)
				.whereOperand()
					.hasType(FilteredSet.T)
					.whereProperty("filter").hasType(com.braintribe.model.queryplan.filter.Disjunction.T).close()
					.whereOperand()
						.hasType(QuerySourceSet.T)
						.whereProperty("entityTypeSignature").is_(Person.class.getName())
						.whereProperty("condition")
							.hasType(Disjunction.T)
							.whereProperty("operands").isListWithSize(2)
		;
		// @formatter:on
	}

	/**
	 * The problem here is, that the planner doesn't try to find a part of the disjunction it could delegate (it would
	 * only do that if Person.name was indexed). So it ends up seeing the whole disjunction affects two different Froms,
	 * and therefore doesn't try to delegate an EntityQuery (i.e. doesn't build a {@link QuerySourceSet})
	 * 
	 * @see #simpleDisjunction()
	 */
	@Category(KnownIssue.class)
	@Test
	public void EXPECTED_TO_FAIL_complexDisjunction() throws Exception {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.disjunction()
						.property("p", "name").eq("Peter")
						.conjunction()
							.property("p", "name").eq("Elon")
							.property("c", "name").eq("Tesla")
						.close()
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(FilteredSet.T)
				.whereProperty("filter").hasType(com.braintribe.model.queryplan.filter.Disjunction.T).close()
				.whereOperand()
					.hasType(QuerySourceSet.T)
		;
		// @formatter:on
	}

	@Test
	public void entityConditionQuery() throws Exception {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "company").eq().entity(instance(Company.T, 55))
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(FilteredSet.T)
				.whereProperty("filter").hasType(Equality.T).close()
				.whereOperand()
					.hasType(QuerySourceSet.T)
					.whereProperty("entityTypeSignature").is_(Person.class.getName())
					.whereProperty("condition")
						.hasType(ValueComparison.T)
						.whereProperty("leftOperand")
							.isPropertyOperandAndSource("company").isNull().close()
						.close()
						.whereProperty("rightOperand")
							.isReference_(Company.T, 55L, null)
		;
		// @formatter:on
	}

	@Test
	public void implicitJoinConditionQuery() throws Exception {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "company.name").eq("Braintribe")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(FilteredSet.T)
				.whereProperty("filter").hasType(Equality.T).close()
				.whereOperand()
					.hasType(EntityJoin.T)
					.whereOperand()
						.hasType(QuerySourceSet.T)
						.whereProperty("entityTypeSignature").is_(Person.class.getName())
						.whereProperty("condition")
							.hasType(ValueComparison.T)
							.whereProperty("leftOperand")
								.isPropertyOperandAndSource("company.name").isNull().close()
							.close()
							.whereProperty("rightOperand").is_("Braintribe")
		;
		// @formatter:on
	}

	@Test
	public void explicitJoinConditionQuery() throws Exception {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
					.join("p", "company", "c")
				.where()
					.property("c", "name").eq("Braintribe")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(FilteredSet.T)
				.whereProperty("filter").hasType(Equality.T).close()
				.whereOperand()
					.hasType(EntityJoin.T)
					.whereOperand()
						.hasType(QuerySourceSet.T)
						.whereProperty("entityTypeSignature").is_(Person.class.getName())
						.whereProperty("condition")
							.hasType(ValueComparison.T)
							.whereProperty("leftOperand")
								.isPropertyOperandAndSource("company.name").isNull().close()
							.close()
							.whereProperty("rightOperand").is_("Braintribe")
		;
		// @formatter:on
	}

}
