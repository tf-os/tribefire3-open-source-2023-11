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
package com.braintribe.model.processing.session.impl.notifying;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import static com.braintribe.model.generic.manipulation.ManipulationType.ADD;
import static com.braintribe.model.generic.manipulation.ManipulationType.CHANGE_VALUE;
import static com.braintribe.model.generic.manipulation.ManipulationType.DELETE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.generic.enhance.ManipulationTrackingPropertyAccessInterceptor;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.impl.session.collection.CollectionEnhancingPropertyAccessInterceptor;
import com.braintribe.model.processing.session.test.data.Person;

/**
 * 
 */
public class BasicNotifyingGmSessionTest {

	private BasicNotifyingGmSession session;
	private Person person;
	private Person other;

	private final List<ManipulationType> allManipulations = newList();
	private final List<ManipulationType> entityManipulations = newList();
	private final Map<String, List<ManipulationType>> propertyManipulations = newMap();

	@Before
	public void setup() {
		session = new BasicNotifyingGmSession();

		session.listeners().add(new SimpleListener(allManipulations));
		session.interceptors().add(new CollectionEnhancingPropertyAccessInterceptor());
		session.interceptors().add(new ManipulationTrackingPropertyAccessInterceptor());

		session.setSuppressNoticing(true);
		person = session.create(Person.T);
		other = session.create(Person.T);
		session.setSuppressNoticing(false);
	}

	class SimpleListener implements ManipulationListener {
		private final List<ManipulationType> manipulations;

		public SimpleListener(List<ManipulationType> manipulations) {
			this.manipulations = manipulations;
		}

		@Override
		public void noticeManipulation(Manipulation manipulation) {
			if (manipulation instanceof AtomicManipulation) {
				manipulations.add(manipulation.manipulationType());
			}
		}
	}

	@Test
	public void testWithEntity() throws Exception {
		session.listeners().entity(person).add(new SimpleListener(entityManipulations));

		other.setName("other name");
		person.setNumbers(newIntList());
		person.getNumbers().add(10);

		assertAllManis(CHANGE_VALUE, CHANGE_VALUE, ADD);
		assertEntityManis(CHANGE_VALUE, ADD);
	}

	@Test
	public void entityBeingDeleted() throws Exception {
		session.listeners().entity(person).add(new SimpleListener(entityManipulations));

		session.deleteEntity(other);
		session.deleteEntity(person);

		assertAllManis(DELETE, DELETE);
		assertEntityManis(DELETE);
	}

	@Test
	public void testWithSimpleProperty_NewIfPossible() throws Exception {
		session.listeners().entity(person).add(new SimpleListener(entityManipulations));
		session.listeners().entityProperty(person, "name").add(propertyListener("name"));

		other.setName("other name");
		person.setName("John");
		person.setNumbers(newIntList());

		assertAllManis(CHANGE_VALUE, CHANGE_VALUE, CHANGE_VALUE);
		assertEntityManis(CHANGE_VALUE, CHANGE_VALUE);
		assertPropertyManis("name", CHANGE_VALUE);
	}

	private ManipulationListener propertyListener(String propertyName) {
		List<ManipulationType> manipulations = newList();
		propertyManipulations.put(propertyName, manipulations);

		return new SimpleListener(manipulations);
	}

	private List<Integer> newIntList() {
		return newList();
	}

	// ########################################################################################
	// ## . . . . . . . . . . . . . . . Assertions . . . . . . . . . . . . . . . . . . . . . ##
	// ########################################################################################

	private void assertAllManis(ManipulationType... types) {
		assertManis(allManipulations, types, "Wrong 'all' manipulations.");
	}

	private void assertEntityManis(ManipulationType... types) {
		assertManis(entityManipulations, types, "Wrong 'entity' manipulations.");
	}

	private void assertPropertyManis(String propertyName, ManipulationType... types) {
		assertManis(propertyManipulations.get(propertyName), types, "Wrong 'property' manipulations for: " + propertyName);
	}

	private void assertManis(List<ManipulationType> actual, ManipulationType[] types, String msg) {
		assertThat(actual).as(msg).isEqualTo(Arrays.asList(types));
	}

	static <K, V> Map<K, V> newMap() {
		return new HashMap<K, V>();
	}

	static <E> Set<E> newSet() {
		return new HashSet<E>();
	}

	static <E> List<E> newList() {
		return new ArrayList<E>();
	}
}
