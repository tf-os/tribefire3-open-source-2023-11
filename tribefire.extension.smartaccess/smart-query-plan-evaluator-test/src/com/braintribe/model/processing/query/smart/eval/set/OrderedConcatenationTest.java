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

import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.tupleComponent;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.smart.eval.set.base.AbstractSmartEvalTupleSetTests;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonB;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.smartqueryplan.ScalarMapping;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;
import com.braintribe.model.smartqueryplan.set.OrderedConcatenation;

public class OrderedConcatenationTest extends AbstractSmartEvalTupleSetTests {

	private final List<PersonA> personAs = newList();
	private final List<PersonB> personBs = newList();

	/**
	 * Generating two sets of data (pairs: ${person.name}, ${person.companyName}):
	 * <ul>
	 * <li>p00, cA0</li>
	 * <li>p02, cA1</li>
	 * <li>p04, cA2</li>
	 * ...
	 * </ul>
	 * and
	 * 
	 * <ul>
	 * <li>p01, cB0</li>
	 * <li>p03, cB1</li>
	 * <li>p05, cB2</li>
	 * ...
	 * </ul>
	 */
	@Before
	public void buildData() {
		for (int i = 0; i < 50; i++) {
			personAs.add(bA.personA("p" + toString(2 * i)).companyNameA("cA" + i).create());
			personBs.add(bB.personB("p" + toString(2 * i + 1)).companyNameB("cB" + i).create());
		}
	}

	/**
	 * Now we create a two {@link DelegateQuerySet}s which return the values sorted by person.nameA (or nameB) and we
	 * merge them using {@link OrderedConcatenation}.
	 */
	@Test
	public void testMerging() {
		List<ScalarMapping> scalarMapping = standardScalarMappings();

		// @formatter:off
		SelectQuery queryA = query()
				.from(PersonA.class, "p")
				.select("p", "nameA")
				.select("p", "companyNameA")
				.orderBy().property("p", "nameA")
				.done();
		// @formatter:on
		DelegateQuerySet dqsA = builder.delegateQuerySet(setup.accessA, queryA, scalarMapping);

		// @formatter:off
		SelectQuery queryB = query()
				.from(PersonB.class, "p")
				.select("p", "nameB")
				.select("p", "companyNameB")
				.orderBy().property("p", "nameB")
				.done();
		// @formatter:on
		DelegateQuerySet dqsB = builder.delegateQuerySet(setup.accessB, queryB, scalarMapping);

		Value nameValue = tupleComponent(0);
		OrderedConcatenation oc = builder.orderedConcatenation(dqsA, dqsB, 2, builder.sortCriterium(nameValue, false));

		evaluate(oc);

		assertContainsAllPersonData();
	}

	private List<ScalarMapping> standardScalarMappings() {
		ScalarMapping sm1 = builder.scalarMapping(0);
		ScalarMapping sm2 = builder.scalarMapping(1);

		return Arrays.asList(sm1, sm2);
	}

	/**
	 * So the expected result is:
	 * <ul>
	 * <li>p00, cA0</li>
	 * <li>p01, cB0</li>
	 * <li>p02, cA1</li>
	 * <li>p03, cB1</li>
	 * ...
	 * </ul>
	 */
	private void assertContainsAllPersonData() {
		for (int i = 0; i < 50; i++) {
			assertNextTuple("p" + toString(2 * i), "cA" + i);
			assertNextTuple("p" + toString(2 * i + 1), "cB" + i);
		}
		assertNoMoreTuples();
	}

	private String toString(int i) {
		return (i < 10 ? "0" : "") + i;
	}

}
