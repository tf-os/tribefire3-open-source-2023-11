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
package com.braintribe.model.processing.query.smart.eval.set;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.smart.eval.set.base.AbstractSmartEvalTupleSetTests;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.smartqueryplan.ScalarMapping;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;

public class DelegateQuerySetTest extends AbstractSmartEvalTupleSetTests {

	private final List<PersonA> persons = newList();

	@Before
	public void buildData() {
		for (int i = 0; i < 10; i++)
			persons.add(bA.personA("p" + i).companyNameA("c" + i).create());
	}

	@Test
	public void simpleDqs() {
		// @formatter:off
		SelectQuery query = query()
				.select("p", "id")
				.select("p", "nameA")
				.select("p", "companyNameA")
				.from(PersonA.T, "p")
				.done();
		// @formatter:on

		DelegateQuerySet dqs = builder.delegateQuerySet(setup.accessA, query, standardScalarMappings());

		evaluate(dqs);

		assertContainsAllPersonData();
	}

	@Test
	public void batchDqs() {
		// @formatter:off
		SelectQuery query = query()
				.select("p", "id")
				.select("p", "nameA")
				.select("p", "companyNameA")
				.from(PersonA.T, "p")
				.orderBy().property("p", "id")
				.done();
		// @formatter:on

		DelegateQuerySet dqs = builder.delegateQuerySet(setup.accessA, query, standardScalarMappings(), 2);

		evaluate(dqs);

		assertContainsAllPersonData();
	}

	private List<ScalarMapping> standardScalarMappings() {
		ScalarMapping sm1 = builder.scalarMapping(0);
		ScalarMapping sm2 = builder.scalarMapping(1);
		ScalarMapping sm3 = builder.scalarMapping(2);

		return Arrays.asList(sm1, sm2, sm3);
	}

	private void assertContainsAllPersonData() {
		for (PersonA p: persons)
			assertContainsTuple(p.getId(), p.getNameA(), p.getCompanyNameA());

		assertNoMoreTuples();
	}

}
