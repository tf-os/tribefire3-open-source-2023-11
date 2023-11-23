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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

/**
 *
 */
public class IndexedFilteringQueryTests extends AbstractSelectQueryTests {

	@Test
	public void singleSourceFindForIndexedEntity() {
		Company c = getCompany();

		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.property("_Person", "indexedCompany").eq().entity(c)
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select * from com.braintribe.model.processing.query.test.model.Person _Person where _Person.indexedCompany = reference(com.braintribe.model.processing.query.test.model.Company, 1l)"));
	}

	@Test
	public void singleSourceFindForIndexedEntity_RemovingFromIndex() {
		Company c = getCompany();

		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.property("_Person", "indexedCompany").eq().entity(c)
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select * from com.braintribe.model.processing.query.test.model.Person _Person where _Person.indexedCompany = reference(com.braintribe.model.processing.query.test.model.Company, 1l)"));
	}

	@Test
	public void singleSourceFindForIndexInt() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.property("_Person", "indexedInteger").eq(45)
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString
				.equalsIgnoreCase("select * from com.braintribe.model.processing.query.test.model.Person _Person where _Person.indexedInteger = 45"));
	}

	@Test
	public void singleSourceFindForIndexInt_Empty() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.property("_Person", "indexedInteger").eq(90)
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString
				.equalsIgnoreCase("select * from com.braintribe.model.processing.query.test.model.Person _Person where _Person.indexedInteger = 90"));
	}

	@Test
	public void singleSourceFindForIndexInt_Empty_Unique() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.property("_Person", "indexedUniqueName").eq("whatever")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select * from com.braintribe.model.processing.query.test.model.Person _Person where _Person.indexedUniqueName = 'whatever'"));
	}

	@Test
	public void singleSourceFindForIndexInt_WithRemove() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.property("_Person", "indexedInteger").eq(45)
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString
				.equalsIgnoreCase("select * from com.braintribe.model.processing.query.test.model.Person _Person where _Person.indexedInteger = 45"));
	}

	@Test
	public void singleSourceInOperatorWithIndexedInt() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.property("_Person", "indexedInteger").in(asSet(1, 2, 3, 4))
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select * from com.braintribe.model.processing.query.test.model.Person _Person where _Person.indexedInteger in (1, 2, 3, 4)"));
	}

	@Test
	public void singleSourceFindRangeForIndexInt() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.conjunction()
					.property("_Person", "indexedInteger").gt(85)
					.property("_Person", "indexedInteger").le(95)
				.close()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select * from com.braintribe.model.processing.query.test.model.Person _Person where (_Person.indexedInteger > 85 and _Person.indexedInteger <= 95)"));
	}

	@Test
	public void singleSourceSimpleIndexChain() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Owner.class, "_Owner")
				.where()
				.property("_Owner", "indexedCompany.indexedName").eq("C1")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select * from com.braintribe.model.processing.query.test.model.Owner _Owner where _Owner.indexedCompany.indexedName = 'C1'"));
	}
}
