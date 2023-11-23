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

import com.braintribe.model.processing.query.shortening.Qualified;
import com.braintribe.model.processing.query.shortening.Simplified;
import com.braintribe.model.processing.query.shortening.SmartShortening;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

public class ShorteningModeTests extends AbstractSelectQueryTests {
	@Test
	public void qualifiedTest() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.select("_Person", "name")
				.from(Person.class, "_Person")
				.distinct()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Qualified());
		Assertions.assertThat(queryString)
				.isEqualToIgnoringCase("select distinct _Person.name from com.braintribe.model.processing.query.test.model.Person _Person");
	}

	@Test
	public void simplifiedTest() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.select("_Person", "name")
				.from(Person.class, "_Person")
				.distinct()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select distinct _Person.name from Person _Person");
	}

	@Test
	public void smartShorteningTest() {
		// @formatter:off
		SelectQuery selectQuery = query()
			.select("_Person", "name")
			.from(Person.class, "_Person")
			.distinct()
			.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new SmartShortening(getModelOracle()));
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select distinct _Person.name from Person _Person");
	}

	@Test
	public void qualifiedTest2() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.select("_Person", "name")
				.from("Person", "_Person")
				.distinct()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Qualified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select distinct _Person.name from Person _Person");
	}

	@Test
	public void simplifiedTest2() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.select("_Person", "name")
				.from("Person", "_Person")
				.distinct()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select distinct _Person.name from Person _Person");
	}

	@Test
	public void smartShorteningTest2() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.select("_Person", "name")
				.from("Person", "_Person")
				.distinct()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new SmartShortening(getModelOracle()));
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select distinct _Person.name from Person _Person");
	}

	@Test
	public void qualifiedTest3() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.select("_Person", "name")
				.from((String)null, "_Person")
				.distinct()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Qualified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select distinct _Person.name from <?> _Person");
	}

	@Test
	public void simplifiedTest3() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.select("_Person", "name")
				.from((String)null, "_Person")
				.distinct()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new Simplified());
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select distinct _Person.name from <?> _Person");
	}

	@Test
	public void smartShorteningTest3() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.select("_Person", "name")
				.from((String)null, "_Person")
				.distinct()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery, new SmartShortening(getModelOracle()));
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select distinct _Person.name from <?> _Person");
	}
}
