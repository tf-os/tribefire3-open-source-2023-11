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
package com.braintribe.model.processing.smood.manipulation;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.test.tools.meta.ManipulationTrackingMode;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class LocalManipulationTests extends AbstractSmoodManipulationTests {

	@Before
	public void setManipulationMode() {
		defaultManipulationMode = ManipulationTrackingMode.LOCAL;
	}

	@Test
	public void createEntityAndSetId() {
		applyManipulations(ManipulationTrackingMode.LOCAL, session -> {
			Person p = session.create(Person.T);
			p.setId(1L);
		});

		assertEntityCountForType(Owner.T, 0);
		assertEntityCountForType(Person.T, 1);
		assertEntityCountForType(GenericEntity.T, 1);
	}

	@Test
	public void createEntityAndSetProperty() {
		applyManipulations(session -> {
			Person entity = session.create(Person.T);
			entity.setName("My Name");
		});

		assertEntityCountForType(Person.T, 1);
		assertFindsByProperty(Person.T, "name", "My Name");
	}

	@Test
	public void createEntityAndSetIndexedProperty() {
		applyManipulations(session -> {
			Person entity = session.create(Person.T);
			entity.setIndexedName("My Name");
		});

		assertEntityCountForType(Person.T, 1);
		assertFindsByIndexedProperty(Person.T, "indexedName", "My Name");
	}

	@Test
	public void createEntity_AndGetByIdAndReference() throws Exception {
		applyManipulations(session -> {
			session.create(Person.T);
		});

		Person person1 = smood.getEntitiesPerType(Person.T).iterator().next();

		Object person2 = smood.getEntity(Person.T, person1.getId());
		BtAssertions.assertThat(person2).isEqualTo(person1);

		Object person3 = smood.getEntity(person1.reference());
		BtAssertions.assertThat(person3).isEqualTo(person1);
	}

	@Test
	public void createEntity_ChangeId_AndGetByIdAndReference() throws Exception {
		applyManipulations(session -> {
			session.create(Person.T);
		});

		Person person1 = smood.getEntitiesPerType(Person.T).iterator().next();
		person1.setId(150L);

		Object person2 = smood.getEntity(Person.T, person1.getId());
		BtAssertions.assertThat(person2).isEqualTo(person1);

		Object person3 = smood.getEntity(person1.reference());
		BtAssertions.assertThat(person3).isEqualTo(person1);
	}

	@Test
	public void createEntity_ChangeIdToNull_AndGetByIdAndReference() throws Exception {
		applyManipulations(session -> {
			session.create(Person.T);
		});

		Person person1 = smood.getEntitiesPerType(Person.T).iterator().next();
		person1.setId(null);

		Set<Person> allEntities = smood.getEntitiesPerType(Person.T);
		BtAssertions.assertThat(allEntities).isNotEmpty().contains(person1);

		Object person3 = smood.getEntity(person1.reference());
		BtAssertions.assertThat(person3).isEqualTo(person1);
	}

}
