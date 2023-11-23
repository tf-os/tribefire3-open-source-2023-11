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

import com.braintribe.model.processing.query.test.ProjectionTests;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class ProjectionQueryTests extends AbstractSelectQueryTests {

	/** @see ProjectionTests#selectingEntityAndProperty() */
	@Test
	public void selectingEntityAndProperty() {
		Person p1 = b.person("p1").create();
		Person p2 = b.person("p2").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.select("person")
				.select("person", "name")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p1, "p1");
		assertResultContains(p2, "p2");
		assertNoMoreResults();
	}

	/** @see ProjectionTests#selectingCompoundProperty() */
	@Test
	public void selectingCompoundProperty() {
		Company c1 = b.company("C1").create();
		Company c2 = b.company("C2").create();

		Person p1 = b.owner("p1").company(c1).create();
		Person p2 = b.owner("p2").company(c2).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.T, "person")
				.select("person")
				.select("person", "company.name")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p1, "C1");
		assertResultContains(p2, "C2");
		assertNoMoreResults();
	}

	/** @see ProjectionTests#selectingConstants() */
	@Test
	public void selectingConstants() {
		Person p1 = b.person("p1").create();
		Person p2 = b.person("p2").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.select("person")
				.select().value(99L)
				.select().value("constantString")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p1, 99L, "constantString");
		assertResultContains(p2, 99L, "constantString");
		assertNoMoreResults();
	}

	/** @see ProjectionTests#selectingLocalizedValue() */
	@Test
	public void selectingLocalizedValue() {
		Person p1 = b.person("p1").localizedString("en", "yes", "pt", "sim").create();
		Person p2 = b.person("p2").localizedString("en", "good", "pt", "bom").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.select("person")
				.select().localize("pt").property("person", "localizedString")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p1, "sim");
		assertResultContains(p2, "bom");
	}

	/** @see ProjectionTests#selectingMapKey() */
	@Test
	public void selectingMapKey() {
		Company c1 = b.company("C1").create();
		Company c2 = b.company("C2").create();

		b.owner("p").addToCompanyMap("c1", c1).addToCompanyMap("c2", c2).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.T, "person")
					.join("person", "companyMap", "cs")
				.select().mapKey("cs")
				.select("cs")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("c1", c1);
		assertResultContains("c2", c2);
	}

	/** @see ProjectionTests#selectingMapKey() */
	@Test
	public void selectingMapValue() {
		Company c1 = b.company("C1").create();
		Company c2 = b.company("C2").create();

		b.owner("p").addToCompanyMap("c1", c1).addToCompanyMap("c2", c2).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.T, "person")
				.select("person", "name")
				.select("person", "companyMap")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("p", c1);
		assertResultContains("p", c2);
	}

}
