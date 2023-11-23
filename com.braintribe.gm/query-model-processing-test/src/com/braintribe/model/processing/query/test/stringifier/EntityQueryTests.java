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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.EntityQuery;

public class EntityQueryTests extends AbstractSelectQueryTests {

	@Test
	public void queryEntities() {
		EntityQuery query = EntityQueryBuilder.from(Person.class).done();
		String queryString = stringify(query);

		assertThat(queryString).isEqualToIgnoringCase("from com.braintribe.model.processing.query.test.model.Person");
	}

	@Test
	public void queryEntitiesWithCondition() {
		EntityQuery query = EntityQueryBuilder.from(Person.class).where().property("name").eq("Jack").done();
		String queryString = stringify(query);

		assertThat(queryString).isEqualToIgnoringCase("from com.braintribe.model.processing.query.test.model.Person where name = 'Jack'");
	}

	@Test
	public void queryEntitiesWithPaging() {
		EntityQuery query = EntityQueryBuilder.from(Person.class).where().property("name").eq("Jack").paging(2, 5).done();
		String queryString = stringify(query);

		assertThat(queryString)
				.isEqualToIgnoringCase("from com.braintribe.model.processing.query.test.model.Person where name = 'Jack' limit 2 offset 5");
	}

	@Test
	public void queryEntitiesWithFulltext() {
		EntityQuery query = EntityQueryBuilder.from(Person.class).where().fullText(null,"foo").done();
		String queryString = stringify(query);

		assertThat(queryString).isEqualToIgnoringCase("from com.braintribe.model.processing.query.test.model.Person where fullText( 'foo')");
	}

}
