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

import com.braintribe.model.processing.query.test.DisjunctionTests;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

public class DisjunctionQueryTests extends AbstractSelectQueryTests {

	/** @see DisjunctionTests#simpleDisjunctionOnSoureByDirectReference() */
	@Test
	public void simpleDisjunctionOnSoureByDirectReference() {
		Person p1 = b.person("P1").create();
		Person p2 = b.person("P2").create();

		// @formatter:off
		SelectQuery query = query()
				.from(Person.T, "p")
				.where()
					.disjunction()
						.entity("p").eq().entity(p1)
						.entity("p").eq().entity(p2)
					.close()
				.done();
		// @formatter:on

		evaluate(query);

		assertResultContains(p1);
		assertResultContains(p2);
	}

}
