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
package com.braintribe.model.processing.smood.querying;

import static com.braintribe.model.query.OrderingDirection.ascending;
import static com.braintribe.model.query.OrderingDirection.descending;

import org.junit.Test;

import com.braintribe.model.processing.query.test.OrderByIndexedTests;
import com.braintribe.model.processing.query.test.model.Address;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

/**
 * Query tests for {@link OrderByIndexedTests}
 */
public class OrderByIndexedQueryTests extends AbstractSelectQueryTests {

	private Person p1, p2, p3, p4, p5;

	/** @see OrderByIndexedTests#orderById */
	@Test
	public void orderById() {
		p1 = b.person("p1").id(3L).create();
		p2 = b.person("p2").id(2L).create();
		p3 = b.person("p3").id(1L).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.orderBy(ascending).property("p", "id")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p3);
		assertNextResult(p2);
		assertNextResult(p1);
		assertNoMoreResults();
	}

	/** @see OrderByIndexedTests#orderById_Desc */
	@Test
	public void orderById_Desc() {
		p1 = b.person("p1").id(1L).create();
		p2 = b.person("p2").id(2L).create();
		p3 = b.person("p3").id(3L).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.orderBy(descending).property("p", "id")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p3);
		assertNextResult(p2);
		assertNextResult(p1);
		assertNoMoreResults();
	}

	/** @see OrderByIndexedTests#orderByIndexedAndOther */
	@Test
	public void orderByIndexedAndOther() {
		p1 = b.person("p1").indexedInteger(2).create();
		p2 = b.person("p2").indexedInteger(2).create();
		p3 = b.person("p3").indexedInteger(1).create();
		p4 = b.person("p4").indexedInteger(1).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.orderByCascade()
					.property("p", "indexedInteger")
					.property("p", "name")
				.close()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p3);
		assertNextResult(p4);
		assertNextResult(p1);
		assertNextResult(p2);
		assertNoMoreResults();
	}

	/** @see OrderByIndexedTests#orderByIndexed_WithNonIndexedCondition */
	@Test
	public void orderByIndexed_WithNonIndexedCondition() {
		p1 = b.person("p1").id(5L).companyName("Tesla").create();
		p2 = b.person("p2").id(4L).companyName("GM").create();
		p3 = b.person("p3").id(3L).companyName("Tesla").create();
		p4 = b.person("p4").id(2L).companyName("Ford").create();
		p5 = b.person("p5").id(1L).companyName("Tesla").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "companyName").eq("Tesla")
				.orderBy(ascending).property("p", "id")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p5);
		assertNextResult(p3);
		assertNextResult(p1);
		assertNoMoreResults();
	}

	/** @see OrderByIndexedTests#orderByIndexed_WithNonIndexedCondition_Desc */
	@Test
	public void orderByIndexed_WithNonIndexedCondition_Desc() {
		p1 = b.person("p1").id(5L).companyName("Tesla").create();
		p2 = b.person("p2").id(4L).companyName("GM").create();
		p3 = b.person("p3").id(3L).companyName("Tesla").create();
		p4 = b.person("p4").id(2L).companyName("Ford").create();
		p5 = b.person("p5").id(1L).companyName("Tesla").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "companyName").eq("Tesla")
				.orderBy(descending).property("p", "id")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p1);
		assertNextResult(p3);
		assertNextResult(p5);
		assertNoMoreResults();
	}

	/** @see OrderByIndexedTests#orderByIndexed_WithIndexedCondition */
	@Test
	public void orderByIndexed_WithIndexedCondition() {
		p1 = b.person("p1").id(1L).indexedInteger(666).create();
		p2 = b.person("p2").id(2L).indexedInteger(88).create();
		p3 = b.person("p3").id(3L).indexedInteger(666).create();
		p4 = b.person("p4").id(4L).indexedInteger(77).create();
		p5 = b.person("p5").id(5L).indexedInteger(666).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "indexedInteger").eq(666)
				.orderBy(descending).property("p", "id")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p5);
		assertNextResult(p3);
		assertNextResult(p1);
		assertNoMoreResults();
	}

	/** @see OrderByIndexedTests#orderByIndexed_CartesianProduct */
	@Test
	public void orderByIndexed_CartesianProduct() {
		p1 = b.person("p1").id(2L).create();
		p2 = b.person("p2").id(1L).create();

		Company c1 = b.company("c1").id(2L).create();
		Company c2 = b.company("c2").id(1L).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.orderByCascade()
					.property("p", "id")
					.property("c", "id")
				.close()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p2, c2);
		assertNextResult(p2, c1);
		assertNextResult(p1, c2);
		assertNextResult(p1, c1);
		assertNoMoreResults();
	}

	// ################################################
	// ## . . . . . . . . IndexJoins . . . . . . . . ##
	// ################################################

	/** @see OrderByIndexedTests#orderByIndexed_WithIndexJoin_OrderByJoinedSide_NoIos */
	@Test
	public void orderByIndexed_WithIndexJoin_OrderByJoinedSide_NoIos() {
		p1 = b.person("p1").id(1L).indexedName("c1").create();
		p2 = b.person("p2").id(2L).indexedName("c2").create();

		Company c1 = b.company("c1").id(1L).create();
		Company c2 = b.company("c2").id(2L).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Company.T, "c")
				.from(Person.T, "p")
				.where()
					.property("p", "indexedName").eq().property("c", "name") // this makes no semantic sense of course..
				.orderBy(ascending).property("p", "id")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(c1, p1);
		assertNextResult(c2, p2);
		assertNoMoreResults();
	}

	/** @see OrderByIndexedTests#orderByIndexed_WithIndexJoin_OrderBySourceSide_YesIos */
	@Test
	public void orderByIndexed_WithIndexJoin_OrderBySourceSide_YesIos() {
		p1 = b.person("p1").id(1L).indexedName("c1").create();
		p2 = b.person("p2").id(2L).indexedName("c2").create();

		Company c1 = b.company("c1").id(2L).create();
		Company c2 = b.company("c2").id(1L).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Company.T, "c")
				.from(Person.T, "p")
				.where()
					.property("p", "indexedName").eq().property("c", "name") // this makes no semantic sense of course..
				.orderBy(ascending).property("c", "id")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(c2, p2);
		assertNextResult(c1, p1);
		assertNoMoreResults();
	}

	// ################################################
	// ## . . . . . . . . MergeJoins . . . . . . . . ##
	// ################################################

	/** @see OrderByIndexedTests#orderByIndexed_WithMergeJoin_OrderByOneSide_YesIos */
	@Test
	public void orderByIndexed_WithMergeJoin_OrderByOneSide_YesIos() {
		p1 = b.person("p1").id(1L).companyName("c1").create();
		p2 = b.person("p2").id(2L).companyName("c2").create();

		Company c1 = b.company("c1").id(2L).create();
		Company c2 = b.company("c2").id(1L).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Company.T, "c")
				.from(Person.T, "p")
				.where()
					.property("p", "companyName").eq().property("c", "name")
				.orderBy(ascending).property("c", "id")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(c2, p2);
		assertNextResult(c1, p1);
		assertNoMoreResults();
	}

	/** @see OrderByIndexedTests#orderByIndexed_WithMergeJoin_OrderByOtherSide_YesIos */
	@Test
	public void orderByIndexed_WithMergeJoin_OrderByOtherSide_YesIos() {
		p1 = b.person("p1").id(1L).companyName("c1").create();
		p2 = b.person("p2").id(2L).companyName("c2").create();

		Company c1 = b.company("c1").id(2L).create();
		Company c2 = b.company("c2").id(1L).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Company.T, "c")
				.from(Person.T, "p")
				.where()
					.property("p", "companyName").eq().property("c", "name")
				.orderBy(ascending).property("p", "id")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(c1, p1);
		assertNextResult(c2, p2);
		assertNoMoreResults();
	}

	/** @see OrderByIndexedTests#orderByIndexed_WithMergeLookupJoin_OrderByBothSides_SomeIos */
	@Test
	public void orderByIndexed_WithMergeLookupJoin_OrderByBothSides_SomeIos() {
		p1 = b.person("p1").id(2L).companyName("c1").create();
		p2 = b.person("p2").id(1L).companyName("c2").create();

		Company c1 = b.company("c1").id(2L).create();
		Company c2 = b.company("c2").id(1L).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.property("p", "companyName").eq().property("c", "name")
				.orderByCascade()
					.property("p", "id")
					.property("c", "id")
				.close()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p2, c2);
		assertNextResult(p1, c1);
		assertNoMoreResults();
	}

	/** @see OrderByIndexedTests#orderByIndexed_WithMergeLookupJoinAndCartesianProduct_OrderByBothSides_SomeIos */
	@Test
	public void orderByIndexed_WithMergeLookupJoinAndCartesianProduct_OrderByBothSides_SomeIos() {
		p1 = b.person("p1").id(2L).companyName("c1").create();
		p2 = b.person("p2").id(1L).companyName("c2").create();

		Company c1 = b.company("c1").id(2L).create();
		Company c2 = b.company("c2").id(1L).create();

		Address a1 = b.address("a1").id(1L).create();
		Address a2 = b.address("a2").id(2L).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.from(Address.T, "a")
				.where()
					.property("p", "companyName").eq().property("c", "name")
				.orderByCascade()
					.dir(ascending).property("p", "indexedName")
					.dir(ascending).property("c", "id")
					.dir(descending).property("a", "id")
				.close()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p2, c2, a2);
		assertNextResult(p2, c2, a1);
		assertNextResult(p1, c1, a2);
		assertNextResult(p1, c1, a1);
		assertNoMoreResults();
	}

	/** @see OrderByIndexedTests#orderByIndexed_WithMergeRangeJoin_OrderByBothSides_SomeIos */
	@Test
	public void orderByIndexed_WithMergeRangeJoin_OrderByBothSides_SomeIos() {
		p1 = b.person("p1").id(2L).companyName("c1").create();
		p2 = b.person("p2").id(1L).companyName("c2").create();

		@SuppressWarnings("unused")
		Company c2 = b.company("c2").id(1L).create();
		Company c1 = b.company("c1").id(2L).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.property("p", "companyName").gt().property("c", "name")
				.orderByCascade()
					.dir(ascending).property("p", "id")
					.dir(ascending).property("c", "id")
				.close()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p2, c1);
		assertNoMoreResults();
	}

}
