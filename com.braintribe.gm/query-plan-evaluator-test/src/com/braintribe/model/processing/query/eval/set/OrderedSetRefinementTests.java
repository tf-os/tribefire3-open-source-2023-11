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
package com.braintribe.model.processing.query.eval.set;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSetTests;
import com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.queryplan.set.IndexOrderedSet;
import com.braintribe.model.queryplan.set.OrderedSetRefinement;
import com.braintribe.model.queryplan.value.Value;

public class OrderedSetRefinementTests extends AbstractEvalTupleSetTests {

	private final List<Person> persons = newList();

	/**
	 * Generating data like (pairs: ${person.indexedName}, ${person.companyName}):
	 * <ul>
	 * <li>p00, i0
	 * <li>p01, i1
	 * <li>...
	 * <li>p09, i9
	 * <li>p10, i0
	 * <li>p11, i1
	 * <li>...
	 * </ul>
	 */
	@Before
	public void buildData() {
		for (int i = 0; i < 100; i++)
			persons.add(b.person("p" + toString(i)).indexedName("i" + (i % 10)).create());
	}

	/**
	 * Now we create a {@link IndexOrderedSet} which returns the values sorted by person.indexedName and we wrap it into a
	 * {@link OrderedSetRefinement}, which applies secondary ordering - by person.name.
	 */
	@Test
	public void testOrdering() {
		IndexOrderedSet ios = builder.indexOrderedSet(Person.class, "indexedName", IndexOrderedSetTests.nameIndex(), false);

		Value nameValue = TupleSetBuilder.valueProperty(ios, "name");
		Value indexedNameValue = TupleSetBuilder.valueProperty(ios, "indexedName");

		OrderedSetRefinement osr = builder.orderedSetRefinement(ios, nameValue, false, Arrays.asList(indexedNameValue));

		evaluate(osr);

		assertContainsAllPersonData();
	}

	/**
	 * So the expected result is:
	 * <ul>
	 * <li>p00, i0
	 * <li>p10, i0
	 * <li>...
	 * <li>p90, i0
	 * <li>p01, i1
	 * <li>p11, i1
	 * <li>...
	 * <li>p91, i1
	 * <li>p02, i2
	 * <li>...
	 * </ul>
	 */
	private void assertContainsAllPersonData() {
		for (int i = 0; i < 10; i++) {
			for (int n = 0; n < 10; n++) {
				int p = 10 * n + i;
				assertNextTuple(persons.get(p));
			}
		}
		assertNoMoreTuples();
	}

	private String toString(int i) {
		return (i < 10 ? "0" : "") + i;
	}

}
