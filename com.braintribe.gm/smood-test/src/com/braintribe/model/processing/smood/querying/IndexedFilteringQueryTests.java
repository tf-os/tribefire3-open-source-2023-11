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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import org.junit.Test;

import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.test.IndexedFilteringTests;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class IndexedFilteringQueryTests extends AbstractSelectQueryTests {

	/** @see IndexedFilteringTests#singleSourceFindForIndexedEntity() */
	@Test
	public void singleSourceFindForIndexedEntity() {
		Company c1 = b.company("C1").create();
		Company c2 = b.company("C2").create();

		Person p;

		p = b.owner("P1").company(c1).create();
		p = b.owner("P2").company(c2).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.where()
					.property("person", "indexedCompany").eq().entity(c2)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p);
		assertNoMoreResults();
	}

	/** @see IndexedFilteringTests#singleSourceFindByDirectReferecne_In() */
	@Test
	public void singleSourceFindByDirectReferecne_In() {
		@SuppressWarnings("unused")
		Person p0 = b.person("P0").create();

		Person p1 = b.person("P1").create();
		Person p2 = b.person("P2").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.entity("p").inEntities(
						asSet(p1, p2)
					)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p1);
		assertResultContains(p2);
		assertNoMoreResults();
	}

	@Test
	public void singleSourceFindByDirectReferecne_In_NotExistentEntity() {
		PersistentEntityReference ref = b.person("P0").create().reference();
		ref.setRefId("unknown");

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.entity("p").in(asSet(ref))
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}

	/** similar to {@link #singleSourceFindForIndexedEntity()} */
	@Test
	public void singleSourceFindForIndexedEntity_RemovingFromIndex() {
		Company c1 = b.company("C1").create();
		Company c2 = b.company("C2").create();

		Owner p;

		p = b.owner("P1").company(c1).create();
		p = b.owner("P2").company(c1).create();
		p.setIndexedCompany(c2);

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.where()
				.property("person", "indexedCompany").eq().entity(c2)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p);
		assertNoMoreResults();
	}

	/** @see IndexedFilteringTests#singleSourceFindForIndexInt() */
	@Test
	public void singleSourceFindForIndexInt() {
		Person p;

		p = b.person("P1").indexedInteger(5).create();
		p = b.person("P2").indexedInteger(45).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.where()
				.property("person", "indexedInteger").eq(45)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p);
		assertNoMoreResults();
	}

	/** @see IndexedFilteringTests#singleSourceFindForIndexInt() */
	@Test
	public void singleSourceFindForIndexInt_Empty() {
		b.person("P1").indexedInteger(5).create();
		b.person("P2").indexedInteger(45).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.where()
				.property("person", "indexedInteger").eq(90)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}

	/** @see IndexedFilteringTests#singleSourceFindForIndexInt() */
	@Test
	public void singleSourceFindForIndexInt_Empty_Unique() {
		b.person("P1").indexedUniqueName("name1").create();
		b.person("P2").indexedUniqueName("name2").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.where()
				.property("person", "indexedUniqueName").eq("whatever")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}

	/** similar to {@link #singleSourceFindForIndexInt()} */
	@Test
	public void singleSourceFindForIndexInt_WithRemove() {
		Person p;

		p = b.person("P1").indexedInteger(5).create();
		p = b.person("P2").indexedInteger(44).create();
		p.setIndexedInteger(45);

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.where()
				.property("person", "indexedInteger").eq(45)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p);
		assertNoMoreResults();
	}

	/** @see IndexedFilteringTests#singleSourceInOperatorWithIndexedInt() */
	@SuppressWarnings("unused")
	@Test
	public void singleSourceInOperatorWithIndexedInt() {
		Person p1 = b.person("P1").indexedInteger(1).create();
		Person p3 = b.person("P3").indexedInteger(3).create();
		Person p5 = b.person("P5").indexedInteger(5).create();
		Person p7 = b.person("P7").indexedInteger(7).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.where()
				.property("person", "indexedInteger").in(asSet(1, 2, 3, 4))
				.done();
		// @formatter:on

		evaluate(selectQuery);
		assertResultContains(p1);
		assertResultContains(p3);
		assertNoMoreResults();
	}

	/** @see IndexedFilteringTests#singleSourceFindRangeForIndexInt() */
	@Test
	public void singleSourceFindRangeForIndexInt() {
		@SuppressWarnings("unused")
		Person p1 = b.person("P1").indexedInteger(85).create();
		Person p2 = b.person("P2").indexedInteger(86).create();
		Person p3 = b.person("P3").indexedInteger(95).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.where()
				.conjunction()
					.property("person", "indexedInteger").gt(85)
					.property("person", "indexedInteger").le(95)
				.close()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p2);
		assertResultContains(p3);
		assertNoMoreResults();
	}

	/** @see IndexedFilteringTests#singleSourceSimpleIndexChain() */
	@Test
	public void singleSourceSimpleIndexChain() {
		Company c1 = b.company("C1").indexedName("C1").create();
		Company c2 = b.company("C2").indexedName("C2").create();

		@SuppressWarnings("unused")
		Person p1 = b.owner("P1").company(c2).create();
		Person p2 = b.owner("P2").company(c1).create();
		Person p3 = b.owner("P3").company(c1).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.T, "person")
				.where()
					.property("person", "indexedCompany.indexedName").eq("C1")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p2);
		assertResultContains(p3);
		assertNoMoreResults();
	}
}
