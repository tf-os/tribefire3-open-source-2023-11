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

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

/**
 *
 */
public class CollectionRelatedQueryTests extends AbstractSelectQueryTests {

	@Test
	public void selectingEmptyCollection_Directly() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.select("_Person", "nicknames")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(
				queryString.equalsIgnoreCase("select _Person.nicknames from com.braintribe.model.processing.query.test.model.Person _Person"));
	}

	@Test
	public void selectingCollection_Directly() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.select("_Person", "nicknames")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(
				queryString.equalsIgnoreCase("select _Person.nicknames from com.braintribe.model.processing.query.test.model.Person _Person"));
	}

	@Test
	public void selectingCollection_ByJoining() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.join("_Person", "nicknames", "_string")
				.select("_string")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select _string from com.braintribe.model.processing.query.test.model.Person _Person join _Person.nicknames _string"));
	}

	@Test
	public void selectingCollection_DirectlyAndByJoining() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.join("_Person", "nicknames", "_string")
				.select("_Person", "nicknames")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select _Person.nicknames from com.braintribe.model.processing.query.test.model.Person _Person join _Person.nicknames _string"));
	}

	@Test
	public void selectingCollection_DirectlyTwiceCausingCartesianProduct() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.select("_Person", "nicknames")
				.select("_Person", "nicknames")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select _Person.nicknames, _Person.nicknames from com.braintribe.model.processing.query.test.model.Person _Person"));
	}

	@Test
	public void selectingCollectionUsingInCondition() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.select("_Person", "name")
				.where()
				.value("bob").in().property("_Person", "nicknames")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select _Person.name from com.braintribe.model.processing.query.test.model.Person _Person where 'bob' in _Person.nicknames"));
	}

	@Test
	public void joiningWithConditionOnMapKey() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Owner.class, "_Owner")
				.join("_Owner", "companyValueMap", "_integer")
				.from(Company.class, "_Company")
				.select("_Company", "name")
				.where()
				.mapKey("_integer").eq().entity("_Company")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select _Company.name from com.braintribe.model.processing.query.test.model.Owner _Owner, com.braintribe.model.processing.query.test.model.Company _Company join _Owner.companyValueMap _integer where mapKey(_integer) = _Company"));
	}
}
