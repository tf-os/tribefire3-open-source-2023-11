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

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.test.OrderByTests;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.HasStringId;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class OrderByQueryTests extends AbstractSelectQueryTests {

	/** @see OrderByTests#simpleAscendingSort() */
	@Test
	public void simpleAscendingSort() {
		Person p1 = b.person("p1").create();
		Person p2 = b.person("p2").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.orderBy(OrderingDirection.ascending).property("person", "name")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p1);
		assertNextResult(p2);
		assertNoMoreResults();
	}

	/** @see OrderByTests#simpleAscendingSort() */
	@Test
	public void simpleAscendingSort_ById() {
		Person p1 = b.person("p1").create();
		Person p2 = b.person("p2").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.orderBy(OrderingDirection.ascending).property("person", "id")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p1);
		assertNextResult(p2);
		assertNoMoreResults();
	}

	/** @see OrderByTests#simpleAscendingSort() */
	@Test
	public void simpleAscendingSort_ByObjectProperty_DifferentTypeValues() {
		Person p1 = b.person("p1").id(1L).create();

		HasStringId stringId = HasStringId.T.create();
		stringId.setId("one");
		smood.registerEntity(stringId, true);

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(GenericEntity.T, "ge")
				.orderBy(OrderingDirection.ascending).property("ge", "id")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p1);
		assertResultContains(stringId);
		assertNoMoreResults();
	}

	/** @see OrderByTests#simpleDescendingSort() */
	@Test
	public void simpleDescendingSort() {
		Person p1 = b.person("p1").create();
		Person p2 = b.person("p2").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.orderBy(OrderingDirection.descending).property("person", "name")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p2);
		assertNextResult(p1);
		assertNoMoreResults();
	}

	/** @see OrderByTests#compoundProperty() */
	@Test
	public void compoundProperty() {
		Company c1 = b.company("C1").create();
		Company c2 = b.company("C2").create();

		Person p1 = b.owner("p1").company(c2).create();
		Person p2 = b.owner("p2").company(c1).create();
		Person p3 = b.owner("p3").company(null).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.T, "person")
				.orderBy().property("person", "company.name")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p3);
		assertNextResult(p2);
		assertNextResult(p1);
		assertNoMoreResults();
	}

	/** @see OrderByTests#multipleOrderBys() */
	@Test
	public void multipleOrderBys() {
		Company c1 = b.company("AA").create();
		Company c2 = b.company("BB").create();
		Company c3 = b.company("AA").create();

		Person p1 = b.owner("p1").company(c1).create();
		Person p2 = b.owner("p2").company(c2).create();
		Person p3 = b.owner("p2").company(c3).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.T, "person")
				.orderByCascade()
					.dir(OrderingDirection.descending).property("person", "company.name")
					.dir(OrderingDirection.ascending).value(45) // this must be ignored 
					.dir(OrderingDirection.ascending).property("person", "name")
				.close()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p2); // company name is BB (companyName is sorted in desc order)
		assertNextResult(p1); // company name is AA, name is p1
		assertNextResult(p3); // company name is AA, name is p2
		assertNoMoreResults();
	}

	/** @see OrderByTests#multipleOrderBys() */
	@Test
	public void multipleOrderBysWithProjection() {
		Company c1 = b.company("AA").create();
		Company c2 = b.company("BB").create();
		Company c3 = b.company("AA").create();

		Person p1 = b.owner("p1").company(c1).create();
		Person p2 = b.owner("p2").company(c2).create();
		Person p3 = b.owner("p3").company(c3).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.T, "person")
				.select("person", "company.name")
				.select("person", "name")
				.select("person")
				.orderByCascade()
					.dir(OrderingDirection.descending).property("person", "company.name")
					.dir(OrderingDirection.ascending).value(45) // this must be ignored 
					.dir(OrderingDirection.ascending).property("person", "name")
				.close()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult("BB", "p2", p2);
		assertNextResult("AA", "p1", p1);
		assertNextResult("AA", "p3", p3);
		assertNoMoreResults();
	}

	/** @see OrderByTests#orderByWithPagination() */
	@Test
	public void orderByWithPagination() {
		@SuppressWarnings("unused")
		Person p1 = b.person("p1").create();
		Person p2 = b.person("p2").create();
		Person p3 = b.person("p3").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.orderBy().property("person", "name")
				.paging(2, 1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p2);
		assertResultContains(p3);
		assertPaginationHasMore(false);
		assertNoMoreResults();
	}

	@Test
	public void orderByWithPagination_NonPositiveLimitMeansInfinity() {
		@SuppressWarnings("unused")
		Person p1 = b.person("p1").create();
		Person p2 = b.person("p2").create();
		Person p3 = b.person("p3").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.orderBy().property("person", "name")
				.paging(0, 1)
				.done();
		// @formatter:on

		// Limit is 0

		evaluate(selectQuery);

		assertResultContains(p2);
		assertResultContains(p3);
		assertPaginationHasMore(false);
		assertNoMoreResults();

		// Limit is negative
		
		selectQuery.getRestriction().getPaging().setPageSize(-10);
	
		evaluate(selectQuery);

		assertResultContains(p2);
		assertResultContains(p3);
		assertPaginationHasMore(false);
		assertNoMoreResults();

	}

	/** @see OrderByTests#orderByWithPagination() */
	@Test
	public void orderByWithPaginationAndMoreExistingResults() {
		b.person("p1").create();
		b.person("p2").create();
		b.person("p3").create();
		b.person("p4").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.orderBy().property("person", "name")
				.paging(2, 1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertPaginationHasMore(true);
	}

	/**
	 * Same as {@link #orderByWithPaginationAndMoreExistingResults() }, but adds a join and select clause. There was a
	 * bug.
	 */
	@Test
	public void orderByWithPaginationAndJoinAndMoreExistingResults() {
		b.person("p1").create();
		b.person("p2").create();
		b.person("p3").create();
		b.person("p4").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.join("person", "company", "c", JoinType.left)
				.select("person")
				.where()
					.entity("c").eq(null)
				.orderBy().property("person", "name")
				.paging(2, 1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertPaginationHasMore(true);
	}

}
