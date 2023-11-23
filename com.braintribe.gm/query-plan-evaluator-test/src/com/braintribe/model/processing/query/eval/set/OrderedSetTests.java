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

import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.valueProperty;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSetTests;
import com.braintribe.model.processing.query.eval.set.base.ModelBuilder;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.value.Value;

/**
 * 
 */
public class OrderedSetTests extends AbstractEvalTupleSetTests {

	private static final int PERSON_COUNT = 10;
	private final Person[] persons = new Person[PERSON_COUNT];

	private SourceSet personSet;
	private Value personNameValue;
	private Value personAgeValue;

	@Before
	public void buildData() {
		personSet = builder.sourceSet(Person.class);
		personNameValue = valueProperty(personSet, "name");
		personAgeValue = valueProperty(personSet, "birthDate");
	}

	@Test
	public void sortByName() throws Exception {
		dataWithName();

		evaluate(builder.orderedSet(personSet, personNameValue, false));

		assertSortedAsInArray();
		assertNoMoreTuples();
	}

	private void dataWithName() {
		for (int i = 0; i < PERSON_COUNT; i++)
			registerAtSmood(persons[i] = ModelBuilder.person("person" + String.valueOf((char) (i + 65))));
	}

	@Test
	public void sortByBirthDate() throws Exception {
		dataWithAge();

		evaluate(builder.orderedSet(personSet, personAgeValue, true));

		assertSortedAsInArray();
		assertNoMoreTuples();
	}

	private void dataWithAge() {
		for (int i = 0; i < PERSON_COUNT; i++)
			registerAtSmood(persons[i] = ModelBuilder.person("person" + i, null, i));
	}

	/**
	 * There is a problem with "natural" ordering on Strings, which says each capital letter is smaller than any
	 * lower-case letter. So the ordering goes: <code>"A", "B", "a", "b"</code>. We want:
	 * <code>"a", "A", "b", "B"</code>
	 */
	@Test
	public void sortWorksWithUpperLowerCase() throws Exception {
		dataWithCasedNames();

		evaluate(builder.orderedSet(personSet, personNameValue, false));

		assertSortedAsInArray();
		assertNoMoreTuples();
	}

	private void dataWithCasedNames() {
		registerAtSmood(persons[0] = ModelBuilder.person("aa"));
		registerAtSmood(persons[1] = ModelBuilder.person("aA"));
		registerAtSmood(persons[2] = ModelBuilder.person("Aa"));
		registerAtSmood(persons[3] = ModelBuilder.person("AA"));
		registerAtSmood(persons[4] = ModelBuilder.person("b"));
		registerAtSmood(persons[5] = ModelBuilder.person("B"));
		registerAtSmood(persons[6] = ModelBuilder.person("c"));
		registerAtSmood(persons[7] = ModelBuilder.person("C"));
		registerAtSmood(persons[8] = ModelBuilder.person("d"));
		registerAtSmood(persons[9] = ModelBuilder.person("D"));
	}

	private void assertSortedAsInArray() {
		for (int i = 0; i < PERSON_COUNT; i++)
			assertNextTuple(persons[i]);
	}

}
