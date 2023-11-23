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
package com.braintribe.model.access.smart.test.query;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.query.SelectQuery;

/**
 * Note the test-cases are copied from SmoodTests (from a class with exact same name -
 * ConstantConditionSelectQueryTests).
 */
public class ConstantConditionSelect_Tests extends AbstractSmartQueryTests {
	private static final String MATCH_ALL = "";

	private SmartPersonA p;

	@Before
	public void initPerson() {
		PersonA pA = bA.personA("john smith").create();
		bB.personB("john smith").create();

		p = smartPerson(pA);
	}

	@Test
	public void fulltextMatchingAll_Negated() throws Exception {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.class, "p").where().negation().fullText("p", MATCH_ALL)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}

	@Test
	public void matchingAll_1_eq_1() throws Exception {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.class, "p").where()
					.value(1).eq().value(1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p);
	}

	@Test
	public void matchingNothing_1_eq_0() throws Exception {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.class, "p")
				.where()
					.value(1).eq().value(0)
				.orderBy().property("p", "companyNameA")
				.limit(5)
				.paging(10, 2)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}

}
