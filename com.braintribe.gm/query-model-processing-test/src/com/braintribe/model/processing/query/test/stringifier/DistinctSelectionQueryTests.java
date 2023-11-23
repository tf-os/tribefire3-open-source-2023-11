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

import org.junit.Test;

import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 *
 */
public class DistinctSelectionQueryTests extends AbstractSelectQueryTests {

	@Test
	public void simpleProperty() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.select("_Person", "name")
				.from(Person.class, "_Person")
				.distinct()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString)
				.isEqualToIgnoringCase("select distinct _Person.name from com.braintribe.model.processing.query.test.model.Person _Person");
	}

	@Test
	public void simpleProperty_OrderedByOtherProperty() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.select("_Person", "name")
				.from(Person.class, "_Person")
				.distinct()
				.orderBy(OrderingDirection.descending).property("_Person", "age")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString).isEqualToIgnoringCase(
				"select distinct _Person.name from com.braintribe.model.processing.query.test.model.Person _Person order by _Person.age desc");
	}

	@Test
	public void listProperty() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.select("_Owner", "companyList")
				.from(Owner.class, "_Owner")
				.distinct()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString)
				.isEqualToIgnoringCase("select distinct _Owner.companyList from com.braintribe.model.processing.query.test.model.Owner _Owner");
	}

	@Test
	public void setProperty() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.select("_Person", "nicknames")
				.from(Person.class, "_Person")
				.distinct()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString)
				.isEqualToIgnoringCase("select distinct _Person.nicknames from com.braintribe.model.processing.query.test.model.Person _Person");
	}
}
