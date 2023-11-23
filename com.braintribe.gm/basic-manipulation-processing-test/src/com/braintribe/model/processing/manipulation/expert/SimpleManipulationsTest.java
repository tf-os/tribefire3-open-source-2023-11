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
package com.braintribe.model.processing.manipulation.expert;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.updateMapKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.processing.manipulation.AbstractManipulationTest;
import com.braintribe.model.processing.manipulation.basic.normalization.Normalizer;
import com.braintribe.model.processing.manipulation.testdata.manipulation.TestEntity;
import com.braintribe.model.processing.manipulator.expert.basic.collection.ListManipulator;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class SimpleManipulationsTest extends AbstractManipulationTest {

	@Test
	public void instantiate() throws Exception {
		apply(session -> {
			createDefaultEntity(session);
		});

		TestEntity e = queryDefualtEntity(session);
		BtAssertions.assertThat(e).isNotNull();
	}

	@Test
	public void delete() throws Exception {
		instantiate();

		apply(session -> {
			TestEntity entity = queryDefualtEntity(session);
			session.deleteEntity(entity);
		});

		BtAssertions.assertThat(queryDefualtEntity()).isNull();
	}

	@Test
	public void settingProperties() throws Exception {
		apply(session -> {
			TestEntity entity = createDefaultEntity(session);

			entity.setProperty1("value1");
			entity.setProperty1(null);
			entity.setProperty2("value2");

			entity.setIntList(new ArrayList<Integer>());
		});

		TestEntity e = queryDefualtEntity();

		BtAssertions.assertThat(e.getProperty1()).isNull();
		BtAssertions.assertThat(e.getProperty2()).isNotNull();
		BtAssertions.assertThat(e.getIntList()).isNotNull();
	}

	@Test
	public void settingIdProperty() throws Exception {
		apply(session -> {
			TestEntity entity = createDefaultEntity(session);

			entity.setProperty1("value1");
			entity.setId(1);
			entity.setProperty2("value2");

			entity.setIntList(new ArrayList<Integer>());
		});

		TestEntity e = queryDefualtEntity();

		BtAssertions.assertThat(e.getProperty1()).isNotNull();
		BtAssertions.assertThat(e.getProperty2()).isNotNull();
		BtAssertions.assertThat(e.getIntList()).isNotNull();
	}

	// #############################################################
	// ## . . . . . . . . collection manipulations . . . . . . . .##
	// #############################################################

	@Test
	public void listManipulations() throws Exception {
		apply(session -> {
			TestEntity entity = createDefaultEntity(session);

			entity.setIntList(new ArrayList<Integer>());
			entity.getIntList().add(0);
			entity.getIntList().addAll(Arrays.asList(1, 2, 3, 6));
			entity.getIntList().addAll(4, Arrays.asList(4, 5));
			entity.getIntList().addAll(Arrays.asList(7, 8, 9, 10, 99, 100, 101));

			entity.getIntList().remove(10); // removing number 10 on position 10
			entity.getIntList().remove((Object) 99); // removing value 99
			entity.getIntList().removeAll(Arrays.asList(100, 101)); // removing these values

			entity.setSomeList(new ArrayList<TestEntity>());
			entity.getSomeList().add(entity);
			entity.getSomeList().addAll(Arrays.asList(entity, entity));
			entity.getSomeList().remove(2);
			entity.getSomeList().remove(entity);
		});

		TestEntity e = queryDefualtEntity();

		BtAssertions.assertThat(e.getIntList()).isNotNull().isEqualTo(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
		BtAssertions.assertThat(e.getSomeList()).isNotNull().hasSize(1).contains(e);
	}

	/** There was a bug (see comments in BTT-3985) that this would cause an exception by {@link ListManipulator}. */
	@Test
	public void listReordering() throws Exception {
		apply(session -> {
			TestEntity entity = createDefaultEntity(session);

			entity.setIntList(Arrays.asList(0, 1, 2, 3));
		});

		ManipulationRequest request = prepare(session -> {
			TestEntity entity = queryDefualtEntity(session);
			List<Integer> list = entity.getIntList();

			// swap positions 1 and 2
			list.remove(1);
			list.add(2, 1);

			// take last one and insert on second position
			list.remove(3);
			list.add(1, 3);
		});

		Normalizer.normalize(request);

		apply(request);

		TestEntity e = queryDefualtEntity();

		BtAssertions.assertThat(e.getIntList()).isNotNull().isEqualTo(Arrays.asList(0, 3, 2, 1));
	}

	/** We add elements on positions higher than the size of the list. The result is then an append. */
	@Test
	public void listInsert_tooBigIndex() throws Exception {
		apply(session -> {
			TestEntity entity = createDefaultEntity(session);

			entity.setIntList(Arrays.asList(0, 1, 2));
		});

		ManipulationRequest request = prepare(session -> {
			TestEntity entity = queryDefualtEntity(session);
			List<Integer> list = entity.getIntList();

			list.addAll(3, asList(5, 6));
		});

		AddManipulation addManipulation = (AddManipulation) request.getManipulation().stream().findFirst().get();
		updateMapKey(addManipulation.getItemsToAdd(), 3, 99);
		updateMapKey(addManipulation.getItemsToAdd(), 4, 100);

		apply(request);

		TestEntity e = queryDefualtEntity();

		BtAssertions.assertThat(e.getIntList()).isNotNull().isEqualTo(Arrays.asList(0, 1, 2, 5, 6));
	}

	@Test
	public void listInsert_negativeIndex() throws Exception {
		apply(session -> {
			TestEntity entity = createDefaultEntity(session);

			entity.setIntList(Arrays.asList(0, 1, 2));
		});

		ManipulationRequest request = prepare(session -> {
			TestEntity entity = queryDefualtEntity(session);
			List<Integer> list = entity.getIntList();

			list.addAll(3, asList(5));
		});

		AddManipulation addManipulation = (AddManipulation) request.getManipulation().stream().findFirst().get();
		updateMapKey(addManipulation.getItemsToAdd(), 3, -2);

		apply(request);

		TestEntity e = queryDefualtEntity();

		BtAssertions.assertThat(e.getIntList()).isNotNull().isEqualTo(Arrays.asList(0, 1, 5, 2));
	}

	@Test
	public void listInsert_positiveAndNegativeIndex() throws Exception {
		apply(session -> {
			TestEntity entity = createDefaultEntity(session);

			entity.setIntList(Arrays.asList(0, 1, 2));
		});

		ManipulationRequest request = prepare(session -> {
			TestEntity entity = queryDefualtEntity(session);
			List<Integer> list = entity.getIntList();

			list.addAll(3, asList(5, 6, 7));
		});

		AddManipulation addManipulation = (AddManipulation) request.getManipulation().stream().findFirst().get();
		updateMapKey(addManipulation.getItemsToAdd(), 3, -2); // make 5 being added on position -2

		apply(request);

		TestEntity e = queryDefualtEntity();
		// [0,1,2] - initial state of list
		// [0,1,2,6,7] - we add [6,7] on positions 4 and 5 (which are invalid so it's simply appended)
		// [0,1,2,6,5,7] - after we add 5 on position -1
		BtAssertions.assertThat(e.getIntList()).isNotNull().isEqualTo(Arrays.asList(0, 1, 2, 6, 5, 7));
	}

	@Test
	public void setManipulations() throws Exception {
		apply(session -> {
			TestEntity entity = createDefaultEntity(session);

			entity.setIntSet(new HashSet<Integer>());
			entity.getIntSet().add(0);
			entity.getIntSet().addAll(Arrays.asList(1, 2, 3, 99, 100));

			entity.getIntSet().remove(10); // no effect
			entity.getIntSet().remove(99); // removing value 99
			entity.getIntSet().removeAll(Arrays.asList(100, 101)); // removing 100, 101 has no effect

			TestEntity e1 = createEntity(session, "e1");
			TestEntity e2 = createEntity(session, "e2");
			TestEntity e3 = createEntity(session, "e3");

			entity.setSomeSet(new HashSet<TestEntity>());
			entity.getSomeSet().add(entity);
			entity.getSomeSet().addAll(Arrays.asList(e1, e2, e3));
			entity.getSomeSet().remove(e1);
			entity.getSomeSet().removeAll(Arrays.asList(e1, e2, e3));
		});

		TestEntity e = queryDefualtEntity();

		BtAssertions.assertThat(e.getIntSet()).isNotNull().isEqualTo(new HashSet<Integer>(Arrays.asList(0, 1, 2, 3)));
		BtAssertions.assertThat(e.getSomeSet()).isNotNull().hasSize(1).contains(e);
	}

	@Test
	public void mapManipulations() throws Exception {
		apply(session -> {
			TestEntity entity = createDefaultEntity(session);

			entity.setIntMap(new HashMap<Integer, Integer>());
			entity.getIntMap().put(0, 0);
			entity.getIntMap().putAll(asMap(1, 1, 2, 2, 3, 30, 7, 70, 8, 80, 9, 90));
			entity.getIntMap().put(3, 3);
			entity.getIntMap().remove(7);
			entity.getIntMap().keySet().removeAll(asSet(8, 9));

			TestEntity e1 = createEntity(session, "e1");
			TestEntity e2 = createEntity(session, "e2");
			TestEntity e3 = createEntity(session, "e3");

			entity.setSomeMap(new HashMap<TestEntity, TestEntity>());
			entity.getSomeMap().put(entity, entity);
			entity.getSomeMap().putAll(asMap(e1, e3, e2, e2, e3, e3));
			entity.getSomeMap().put(e1, e1);
			entity.getSomeMap().remove(e1);
			entity.getSomeMap().keySet().removeAll(asSet(e2, e3));
		});

		TestEntity e = queryDefualtEntity();

		BtAssertions.assertThat(e.getIntMap()).containsKeys(0, 1, 2, 3).hasSize(4);
		BtAssertions.assertThat(e.getSomeMap()).containsKeys(e).hasSize(1);
	}

	// #############################################################
	// ## . . . . . . Object collection manipulations . . . . . . ##
	// #############################################################

	@Test
	public void listManipulations_Object() throws Exception {
		apply(session -> {
			TestEntity entity = createDefaultEntity(session);

			entity.setObjectProperty(new ArrayList<Integer>());
			List<Object> list = (List<Object>) entity.getObjectProperty();

			list.add(0);
			list.addAll(Arrays.asList(1, 2, 3, 6));
			list.addAll(4, Arrays.asList(4, 5));
			list.addAll(Arrays.asList(7, 8, 9, 10, 99, 100, 101));

			list.remove(10); // removing number 10 on position 10
			list.remove((Object) 99); // removing value 99
			list.removeAll(Arrays.asList(100, 101)); // removing these values
		});

		TestEntity e = queryDefualtEntity();

		BtAssertions.assertThat(e.getObjectProperty()).isNotNull().isEqualTo(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
	}

	@Test
	public void setManipulations_Object() throws Exception {
		apply(session -> {
			TestEntity entity = createDefaultEntity(session);

			entity.setObjectProperty(new HashSet<Integer>());
			Set<Object> set = (Set<Object>) entity.getObjectProperty();

			set.add(0);
			set.addAll(Arrays.asList(1, 2, 3, 99, 100));

			set.remove(10); // no effect
			set.remove(99); // removing value 99
			set.removeAll(Arrays.asList(100, 101)); // removing 100, 101 has no effect
		});

		TestEntity e = queryDefualtEntity();

		BtAssertions.assertThat(e.getObjectProperty()).isNotNull().isEqualTo(new HashSet<Integer>(Arrays.asList(0, 1, 2, 3)));
	}

	@Test
	public void mapManipulations_Object() throws Exception {
		apply(session -> {
			TestEntity entity = createDefaultEntity(session);

			entity.setObjectProperty(new HashMap<Integer, Integer>());
			Map<Object, Object> map = (Map<Object, Object>) entity.getObjectProperty();

			map.put(0, 0);
			map.putAll(asMap(1, 1, 2, 2, 3, 30, 7, 70, 8, 80, 9, 90));
			map.put(3, 3);
			map.remove(7);
			map.keySet().removeAll(asSet(8, 9));
		});

		TestEntity e = queryDefualtEntity();

		BtAssertions.assertThat((Map<?, ?>) e.getObjectProperty()).containsKeys(0, 1, 2, 3).hasSize(4);
	}

}
