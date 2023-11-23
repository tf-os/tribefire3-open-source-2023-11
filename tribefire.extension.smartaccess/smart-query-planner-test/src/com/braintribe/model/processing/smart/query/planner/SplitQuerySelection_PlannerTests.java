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

import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdB;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.special.BookA;
import com.braintribe.model.processing.query.smart.test.model.accessA.special.ManualA;
import com.braintribe.model.processing.query.smart.test.model.accessB.special.BookB;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartBookA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartBookB;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartPublication;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartReaderA;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.Concatenation;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.smartqueryplan.set.OrderedConcatenation;

/**
 * Tests for such queries which need to be split into multiple smart queries, cause the hierarchy rooted at given source entity is not
 * mapped to exactly one delegate hierarchy. The most simple example of such query is <tt>select ge from GenericEntity ge</tt>.
 */
public class SplitQuerySelection_PlannerTests extends AbstractSmartQueryPlannerTests {

	@Test
	public void selectSplitPropertyWithEntityCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "favoritePublication")
				.from(SmartReaderA.T, "r")
				.where()
					.property("r", "favoritePublication").eq().entityReference(super.reference(SmartBookB.class, accessIdB, 99L))
				.done();
		// @formatter:on

		runTest(selectQuery);

		System.out.println("END");
	}

	@Test
	public void simpleEntitySelect() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPublication.T, "p")
				.select("p")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Concatenation.T)
			.whereProperty("firstOperand")
				.hasType(Projection.T)
				.whereProperty("operand")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(BookA.T)
					.endQuery()
				.close()
			.close()
			.whereProperty("secondOperand")
				.hasType(Concatenation.T)
				.whereProperty("firstOperand")
					.hasType(Projection.T)
					.whereProperty("operand")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(ManualA.T)
						.endQuery()
					.close()
				.close()
				.whereProperty("secondOperand")
					.hasType(Projection.T)
					.whereProperty("operand")
						.isDelegateQuerySet("accessB")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(BookB.T)
						.endQuery()
					.close()
				.close()
			.close()
		;
		// @formatter:on
	}

	@Test
	public void selectSignature() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPublication.T, "p")
				.select("p", "title")
				.select().entitySignature().entity("p")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// TODO signature should be static in first case too (it is not)
		// see SmartOperandConverter.convertQueryFunction(QueryFunction operand)
		/* SmartBookA has sub-type SmartManulaA, but BookA is not super-type of ManualA, so that's why it's three queries (with the one for
		 * BookB), and in all cases the signature should be static */

		// @formatter:off
		assertQueryPlan()
			.hasType(Concatenation.T)
			.whereProperty("firstOperand")
				.hasType(Projection.T)
				.whereProperty("operand")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(BookA.T)
					.endQuery()
				.close()
			.close()
			.whereProperty("secondOperand")
				.hasType(Concatenation.T)
				.whereProperty("firstOperand")
					.hasType(Projection.T)
					.whereProperty("operand")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(ManualA.T)
						.endQuery()
					.close()
				.close()
				.whereProperty("secondOperand")
					.hasType(Projection.T)
					.whereProperty("operand")
						.isDelegateQuerySet("accessB")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(BookB.T)
						.endQuery()
					.close()
				.close()
			.close()
		;
		// @formatter:on
	}

	@Test
	public void selectPaginatedEntity() {
		// @formatter:off
			SelectQuery selectQuery = query()		
					.from(SmartBookA.T, "b")
					.select("b")
					.paging(10, 5)
					.done();
			// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.isPaginatedSetWithOperand(10, 5)
				.hasType(Concatenation.T)
				.whereProperty("firstOperand")
					.isPaginatedSetWithOperand(15, 0) // we only know we want at most 10+5=15 results
						.hasType(Projection.T)
						.whereProperty("operand")
							.isDelegateQuerySet("accessA")
							.whereDelegateQuery()
								.whereFroms(1).whereElementAt(0).isFrom(BookA.T)
							.endQuery()
						.close()
					.close()
				.close()
				.whereProperty("secondOperand")
					.isPaginatedSetWithOperand(15, 0)
						.hasType(Projection.T)
						.whereProperty("operand")
							.isDelegateQuerySet("accessA")
							.whereDelegateQuery()
								.whereFroms(1).whereElementAt(0).isFrom(ManualA.T)
							.endQuery()
						.close()
					.close()
				.close()
		;
		// @formatter:on
	}

	@Test
	public void selectSimplySortedEntity() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartBookA.T, "b")
				.select("b")
				.orderBy().property("b", "author")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(8)
			.hasType(Projection.T)
			.whereProperty("values").isListWithSize(1).close()
			.whereProperty("operand")
				.hasType(OrderedConcatenation.T)
				.whereProperty("tupleSize").is_(2)
				.whereProperty("firstOperand")
					.hasType(Projection.T)
					.whereProperty("operand")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(BookA.T)
						.endQuery()
					.close()
				.close()
				.whereProperty("secondOperand")
					.hasType(Projection.T)
					.whereProperty("operand")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(ManualA.T)
						.endQuery()
					.close()
				.close()
		;
		// @formatter:on
	}

	@Test
	public void selectSimplySortedEntityProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartBookA.T, "b")
				.select("b", "title")
				.orderBy().property("b", "author")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("values").isListWithSize(1).close()
			.whereProperty("operand")
				.hasType(OrderedConcatenation.T)
				.whereProperty("firstOperand")
					.hasType(Projection.T)
					.whereProperty("operand")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(BookA.T)
						.endQuery()
					.close()
				.close()
				.whereProperty("secondOperand")
					.hasType(Projection.T)
					.whereProperty("operand")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(ManualA.T)
						.endQuery()
					.close()
				.close()
		;
		// @formatter:on
	}

	@Test
	public void selectMultiSortedEntity() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartBookA.T, "b")
				.select("b", "title")
				.select("b", "author")
				.orderByCascade()
					.property("b", "author")
					.dir(OrderingDirection.descending).property("b", "isbn")
				.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("values").isListWithSize(2).close()
			.whereProperty("operand")
				.hasType(OrderedConcatenation.T)
				.whereProperty("firstOperand")
					.hasType(Projection.T)
					.whereProperty("operand")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(BookA.T)
						.endQuery()
					.close()
				.close()
				.whereProperty("secondOperand")
					.hasType(Projection.T)
					.whereProperty("operand")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(ManualA.T)
						.endQuery()
					.close()
				.close()
				.whereProperty("sortCriteria").isListWithSize(2)
					.whereElementAt(0).isSortCriteriaAndValue(false).isTupleComponent_(2).close()
					.whereElementAt(1).isSortCriteriaAndValue(true).isTupleComponent_(3).close()
		;
		// @formatter:on
	}

	/**
	 * We are doing a Cartesian product of two split-inducing sources, so this results in four queries, with sources:
	 * 
	 * <ul>
	 * <li>BookA, BookA</li>
	 * <li>ManualA, BookA</li>
	 * <li>BookA, ManualA</li>
	 * <li>ManualA, ManualA</li>
	 * </ul>
	 * 
	 * BookA, BookA
	 */
	@Test
	public void selectCartesianProduct() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("b1")
				.select("b2")
				.from(SmartBookA.T, "b1")
				.from(SmartBookA.T, "b2")
				.where()
					.property("b1", "isbn").eq().property("b2", "isbn")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Concatenation.T)
			.whereProperty("tupleSize").is_(2)
			.whereProperty("firstOperand")
				.hasType(Projection.T)
				.whereProperty("operand")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(2)
							.whereElementAt(0).isFrom(BookA.T).close()
							.whereElementAt(1).isFrom(BookA.T).close()
					.endQuery()
				.close()
			.close()
			.whereProperty("secondOperand")
				.hasType(Concatenation.T)
				.whereProperty("tupleSize").is_(2)
				.whereProperty("firstOperand")
					.hasType(Projection.T)
					.whereProperty("operand")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(2)
								.whereElementAt(0).isFrom(BookA.T).close()
								.whereElementAt(1).isFrom(ManualA.T).close()
						.endQuery()
					.close()
				.close()
				.whereProperty("secondOperand")
					.hasType(Concatenation.T)
					.whereProperty("tupleSize").is_(2)
					.whereProperty("firstOperand")
						.hasType(Projection.T)
						.whereProperty("operand")
							.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(2)
								.whereElementAt(0).isFrom(BookA.T).close()
								.whereElementAt(1).isFrom(ManualA.T).close()
						.endQuery()
						.close()
					.close()
					.whereProperty("secondOperand")
						.hasType(Projection.T)
						.whereProperty("operand")
							.isDelegateQuerySet("accessA")
							.whereDelegateQuery()
								.whereFroms(2)
									.whereElementAt(0).isFrom(ManualA.T).close()
									.whereElementAt(1).isFrom(ManualA.T).close()
							.endQuery()
						.close()
					.close()
			;
		// @formatter:on
	}

}
