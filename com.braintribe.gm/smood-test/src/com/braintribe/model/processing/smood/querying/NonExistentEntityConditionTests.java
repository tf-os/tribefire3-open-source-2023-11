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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import org.junit.Test;

import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

/**
 * In these tests we have an entity that is not contained within Smood as part of the input. Such a case should be
 * handled leniently, giving the correct result (in this case simply ignoring the entity), rather than throw an
 * exception.
 */
public class NonExistentEntityConditionTests extends AbstractSelectQueryTests {

	@Test
	public void propertyEqualsNonExistent() throws Exception {
		Company existing = b.company("c1").create();
		Company nonExisting = newNonSmoodCompany("c2");

		b.owner("o1").company(existing).create();
		b.owner("o2").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.T, "o")
				.where()
					.property("o", "company").eq().entity(nonExisting)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}

	@Test
	public void nonExistentInSetProperty() throws Exception {
		Company existing = b.company("c1").create();
		Company nonExisting = newNonSmoodCompany("c2");

		b.owner("o1").addToCompanySet(existing).create();
		b.owner("o2").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.T, "o")
				.where()
					.entity(nonExisting).in().property("o", "companySet")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}

	@Test
	public void inSetWithNonExistent() {
		Company c1 = b.company("C1").create();
		Company c2 = b.company("C2").create();
		Company c3 = newNonSmoodCompany("C3");

		Person p;

		p = b.owner("P1").company(c1).create();
		p = b.owner("P2").company(c2).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.where()
					.property("person", "company").inEntities(asSet(c2, c3))
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p);
		assertNoMoreResults();
	}

	private Company newNonSmoodCompany(String name) {
		Company c = Company.T.create();
		c.setName(name);
		c.setId(name);
		return c;
	}

}
