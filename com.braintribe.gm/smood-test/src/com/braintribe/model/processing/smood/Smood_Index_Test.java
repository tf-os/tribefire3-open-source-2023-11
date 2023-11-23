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
package com.braintribe.model.processing.smood;

import static com.braintribe.model.processing.smood.population.SmoodIndexTools.indexId;
import static org.fest.assertions.Assertions.assertThat;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.test.builder.PersonBuilder;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.smood.test.AbstractSmoodTests;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class Smood_Index_Test extends AbstractSmoodTests {

	private static final int PERSON_COUNT = 4;
	private Person person;
	private final Person[] persons = new Person[PERSON_COUNT];
	private static final String NAME = "Person Name";

	private static final String ID_INDEX = indexId(Person.T, "id");
	private static final String NAME_INDEX = indexId(Person.T, "indexedName");
	private static final String UNIQUE_NAME_INDEX = indexId(Person.T, "indexedUniqueName");
	private static final String FRIEND_INDEX = indexId(Person.T, "indexedFriend");

	@Before
	public void buildData() {
		for (int i = 0; i < PERSON_COUNT; i++)
			registerAtSmood(persons[i] = PersonBuilder.newPerson().name("person" + i).indexedName("person" + i).indexedUniqueName("person" + i).create());

		registerAtSmood(person = PersonBuilder.newPerson().name(NAME).indexedName(NAME).indexedUniqueName(NAME).create());
	}

	@Test
	public void indexIsThere() {
		assertThat(smood.provideIndexInfo(Person.class.getName(), "indexedName")).isNotNull();
	}

	@Test
	public void findPersonByName() {
		Person foundPerson = (Person) smood.getValueForIndex(NAME_INDEX, NAME);
		assertSamePerson(foundPerson);
	}

	@Test
	public void findPersonByUniqueName() {
		Person foundPerson = (Person) smood.getValueForIndex(UNIQUE_NAME_INDEX, NAME);
		assertSamePerson(foundPerson);
	}

	@Test
	public void findPersonWhenIdIsNull() {
		Person[] nullPersons = new Person[5];
		for (int i = 0; i < 5; i++)
			registerAtSmood(false, nullPersons[i] = PersonBuilder.newPerson().create());

		Collection<?> foundPersons = smood.getAllValuesForIndex(ID_INDEX, null);
		BtAssertions.assertThat(foundPersons).isNotEmpty().containsOnly((Object[]) nullPersons);
	}

	@Test
	public void findPersonWhenUniqueNameIsNull() {
		Person[] nullPersons = new Person[5];
		for (int i = 0; i < 5; i++)
			registerAtSmood(nullPersons[i] = PersonBuilder.newPerson().name(null).create());

		Collection<?> foundPersons = smood.getAllValuesForIndex(UNIQUE_NAME_INDEX, null);
		BtAssertions.assertThat(foundPersons).isNotEmpty().containsOnly((Object[]) nullPersons);
	}

	@Test
	public void findPersonWhereNullUniqueNameWasChanged() {
		Person[] nullPersons = new Person[5];
		for (int i = 0; i < 5; i++)
			registerAtSmood(nullPersons[i] = PersonBuilder.newPerson().name(null).create());
		for (int i = 0; i < 5; i++)
			nullPersons[i].setIndexedUniqueName("indexedUnique" + i);

		Person foundPerson = (Person) smood.getValueForIndex(UNIQUE_NAME_INDEX, "indexedUnique3");
		BtAssertions.assertThat(foundPerson).isNotNull().isEqualTo(nullPersons[3]);
	}

	@Test
	public void findPersonWhereUniqueNameWasChangedToNull() {
		for (int i = 0; i < PERSON_COUNT; i++)
			persons[i].setIndexedUniqueName(null);
		Collection<?> foundPersons = smood.getAllValuesForIndex(UNIQUE_NAME_INDEX, null);

		BtAssertions.assertThat(foundPersons).isNotEmpty().containsOnly((Object[]) persons);
	}

	@Test
	public void findPersonByEntityProperty() {
		Person friend = persons[0];
		person.setIndexedFriend(friend);

		Person foundPerson = (Person) smood.getValueForIndex(FRIEND_INDEX, friend);
		assertSamePerson(foundPerson);
	}

	@Test
	public void findPersonByEntityPropertyWhenReAssigned() {
		Person friend = persons[0];
		person.setIndexedFriend(persons[1]);
		person.setIndexedFriend(friend);

		Person foundPerson = (Person) smood.getValueForIndex(FRIEND_INDEX, friend);
		assertSamePerson(foundPerson);
	}

	@Test
	public void indexIsUpdatedWithChange() {
		person.setIndexedName(NAME + " X");
		Person foundPerson = (Person) smood.getValueForIndex(NAME_INDEX, NAME + " X");

		assertSamePerson(foundPerson);
	}

	@Test
	public void uniqueIndexIsUpdatedWithChange() {
		person.setIndexedUniqueName(NAME + " X");
		Person foundPerson = (Person) smood.getValueForIndex(UNIQUE_NAME_INDEX, NAME + " X");

		assertSamePerson(foundPerson);
	}

	private void assertSamePerson(Person foundPerson) {
		assertThat(foundPerson).isNotNull().isSameAs(person);
	}

	// ###################################
	// ## . . . . Indexed Ranges . . . .##
	// ###################################

	@Test
	public void findPersonsByNameInRange() {
		Collection<?> indexRange;

		indexRange = smood.getIndexRange(NAME_INDEX, "person" + 1, true, "person" + 2, true);
		assertContainsPersons(indexRange, persons[1], persons[2]);

		indexRange = smood.getIndexRange(NAME_INDEX, null, null, "person" + 2, false);
		assertContainsPersons(indexRange, person, persons[0], persons[1]);

		indexRange = smood.getIndexRange(NAME_INDEX, "person" + 2, false, null, null);
		assertContainsPersons(indexRange, persons[3]);

		indexRange = smood.getIndexRange(NAME_INDEX, null, null, null, null);
		assertContainsPersons(indexRange, person, persons);
	}

	@Test
	public void findPersonsByNameInUniqueRange() {
		Collection<?> indexRange;

		indexRange = smood.getIndexRange(UNIQUE_NAME_INDEX, "person" + 1, true, "person" + 2, true);
		assertContainsPersons(indexRange, persons[1], persons[2]);

		indexRange = smood.getIndexRange(UNIQUE_NAME_INDEX, null, null, "person" + 2, false);
		assertContainsPersons(indexRange, person, persons[0], persons[1]);

		indexRange = smood.getIndexRange(UNIQUE_NAME_INDEX, "person" + 2, false, null, null);
		assertContainsPersons(indexRange, persons[3]);

		indexRange = smood.getIndexRange(UNIQUE_NAME_INDEX, null, null, null, null);
		assertContainsPersons(indexRange, person, persons);
	}

	private static void assertContainsPersons(Collection<?> entities, Person p, Person... ps) {
		BtAssertions.assertThat(entities).isNotNull().isNotEmpty().contains(p).contains((Object[]) ps);
	}

}
