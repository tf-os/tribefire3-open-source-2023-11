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
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class DefaultPartitionQueryTests extends AbstractSelectQueryTests {

	private static final String DEFAULT_PARTITION = "default";
	private static final String CUSTOM_PARTITION = "custom";

	@Override
	protected void postConstruct() {
		smood.setDefaultPartition(DEFAULT_PARTITION);
	}

	@Test
	public void queryDefaultPartition() {
		b.person("Person").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p", GenericEntity.partition)
				.from(Person.T, "p")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(DEFAULT_PARTITION);
		assertNoMoreResults();
	}

	@Test
	public void queryWithPartitionCondition_WhenTrue() {
		Person p;
		p = b.person("Person").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p")
				.from(Person.T, "p")
				.where()
					.property("p", GenericEntity.partition).eq(DEFAULT_PARTITION)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p);
		assertNoMoreResults();
	}

	@Test
	public void queryWithPartitionCondition_WhenFalse() {
		b.person("Person").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p")
				.from(Person.T, "p")
				.where()
					.property("p", GenericEntity.partition).eq(CUSTOM_PARTITION)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}
}
