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

import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.query.test.model.Rectangle;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class ConstantConditionSelectQueryTests extends AbstractSelectQueryTests {
	private static final String MATCH_ALL = "";

	/* We had a bug, that the condition was evaluated as true iff there was a String property with non-null value having
	 * given string as substring. But (for now) we want a full-text with empty string to match everything, even if there
	 * is no string property. */
	@Test
	public void queryEntitiesWithFulltextConditionWhereNoStringProperty() {
		Rectangle e = Rectangle.T.create();
		smood.registerEntity(e, false);

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Rectangle.class, "r").where().fullText("r", MATCH_ALL)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(e);
	}

	@Test
	public void fulltextMatchingAll_Negated() {
		b.person("john").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p").where().negation().fullText("p", MATCH_ALL)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}

	@Test
	public void matchingAll_1_eq_1() {
		Person p = b.person("john").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p").where()
					.value(1).eq().value(1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p);
	}

	@Test
	public void matchingNothing_1_eq_0() {
		b.person("john").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.value(1).eq().value(0)
				.orderBy().property("p", "companyName")
				.paging(10, 2)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}

}
