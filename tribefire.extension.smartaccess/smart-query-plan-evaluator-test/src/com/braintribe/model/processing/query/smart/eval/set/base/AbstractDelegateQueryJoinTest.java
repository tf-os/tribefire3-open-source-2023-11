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
package com.braintribe.model.processing.query.smart.eval.set.base;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Arrays;
import java.util.List;

import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.smartqueryplan.ScalarMapping;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;

public abstract class AbstractDelegateQueryJoinTest extends AbstractSmartEvalTupleSetTests {

	/* The model is not good for testing this, so I am correlating persons with themselves */

	protected final List<PersonA> aPersons = newList();
	protected final List<PersonA> bPersons = newList();

	protected final DelegateQueryJoin buildDqj() {
		// @formatter:off
		SelectQuery mQuery = query()
				.from(PersonA.class, "p")
				.select("p", "nameA")
				.select("p", "parentB")
				.done();
		// @formatter:on

		// @formatter:off
		SelectQuery qQuery = query()
				.from(PersonA.class, "p")
				.select("p", "nameA")
				.select("p", "companyNameA")
				.where()
					.conjunction()
						.disjunction()
							.conjunction()
								.property("p","nameA").ne(null)
							.close()
						.close()
					.close()
				.done();
		// @formatter:on

		DelegateQuerySet mSet = builder.delegateQuerySet(setup.accessA, mQuery, mScalarMappings());
		DelegateQuerySet qSet = builder.delegateQuerySet(setup.accessA, qQuery, qScalarMappings());

		DelegateQueryJoin dqj = builder.delegateQueryJoin(mSet, qSet, 1);
		return dqj;
	}

	private List<ScalarMapping> mScalarMappings() {
		ScalarMapping sm1 = builder.scalarMapping(0); // aPerson.nameA
		ScalarMapping sm2 = builder.scalarMapping(1); // aPerson.parentB

		return Arrays.asList(sm1, sm2);
	}

	private List<ScalarMapping> qScalarMappings() {
		ScalarMapping sm1 = builder.scalarMapping(0, 1); // bPerson.nameA (= aPerson.parentB -> position 1)
		ScalarMapping sm2 = builder.scalarMapping(1); // bPerson.companyNameA

		return Arrays.asList(sm1, sm2);
	}

}
