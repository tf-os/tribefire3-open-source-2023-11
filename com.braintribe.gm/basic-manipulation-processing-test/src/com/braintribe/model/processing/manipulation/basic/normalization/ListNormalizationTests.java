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
package com.braintribe.model.processing.manipulation.basic.normalization;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.Map;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.manipulation.testdata.manipulation.TestEntity;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedList;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class ListNormalizationTests extends AbstractCollectionNormalizerTests {

	@Test
	public void settingNullValue() {
		normalize(session -> {
			entity.setIntList(null);
		});

		assertChangeValue(/* empty list */);
	}

	@Test
	public void simpleInserts() {
		final List<Integer> list = entity.getIntList();

		normalize(session -> {
			list.add(10);
			list.add(20);
			list.add(30);
			list.remove(2);
		});

		assertManiCount(1);
		assertAdd(0, 10, 1, 20);
	}

	@Test
	public void insertsOnPosition() {
		final List<Integer> list = entity.getIntList();

		normalize(session -> {
			list.add(0, 40);
			list.add(0, 30);
			list.add(0, 20);
			list.add(0, 10);
		});

		assertManiCount(1);
		assertAdd(0, 10, 1, 20, 2, 30, 3, 40);
	}

	@Test
	public void insertWithPreviousValue() {
		final List<Integer> list = entity.getIntList();
		list.add(100);

		normalize(session -> {
			list.add(10);
		});

		assertManiCount(1);
		assertAdd(1, 10);
	}

	@Test
	public void insertAndRemove() {
		final List<Integer> list = entity.getIntList();
		list.add(30);
		list.add(40);
		list.add(50);

		normalize(session -> {
			list.add(10);
			list.remove(1); // removing 40
			list.remove(0); // removing 30
			list.remove(0); // removing 50
		});

		assertManiCount(2);
		assertRemove(0, 1, 2);
		assertAdd(0, 10);
	}

	@Test
	public void remove() {
		final List<Integer> list = entity.getIntList();
		list.add(10);
		list.add(20);
		list.add(30);
		list.add(40);

		normalize(session -> {
			list.remove(0); // removing 10
			list.remove(2); // removing 40
		});

		assertManiCount(1);
		assertRemove(0, 3);
	}

	@Test
	public void insertAndRemoveComplex() {
		final List<Integer> list = entity.getIntList();
		list.add(30);

		normalize(session -> {
			list.add(10);
			list.add(50);
			list.add(20);
			list.remove(0); // removing 30
			list.remove(1); // removing 50
			list.add(30);
		});

		assertManiCount(2);
		assertRemove(0); // removing the value "30" that already was there
		assertAdd(0, 10, 1, 20, 2, 30);
	}

	@Test
	public void replaceOfInserted() {
		final List<Integer> list = entity.getIntList();

		normalize(session -> {
			list.add(10);
			list.add(30);
			list.set(1, 20);
		});

		assertManiCount(1);
		assertAdd(0, 10, 1, 20);
	}

	@Test
	public void replaceOfPreviousValue() {
		final List<Integer> list = entity.getIntList();
		list.add(10);
		list.add(30);

		normalize(session -> {
			list.add(40);
			list.add(50);
			list.set(1, 20);
		});

		assertManiCount(2);
		assertRemove(1);
		assertRemoveWithItems(1, 30);
		assertAdd(1, 20, 2, 40, 3, 50);
	}

	@Test
	public void doubleReplace() {
		final List<Integer> list = entity.getIntList();
		list.add(10);

		normalize(session -> {
			list.set(0, 20);
			list.set(0, 10); // changes value back to 10, tracker currently does not recognize this
		});

		assertManiCount(2);
		assertRemove(0);
		assertRemoveWithItems(0, 10);
		assertAdd(0, 10);
	}

	@Test
	public void removeAndReplace() {
		final List<Integer> list = entity.getIntList();
		list.add(30);
		list.add(40);
		list.add(50);

		normalize(session -> {
			list.set(1, 10); // change 40 to 10
			list.set(2, 20); // change 50 to 20
			list.remove(0); // removes 30
		});

		assertManiCount(2);
		assertRemove(0, 1, 2);
		assertRemoveWithItems(0, 30, 1, 40, 2, 50);
		assertAdd(0, 10, 1, 20);
	}

	@Test
	public void replaceWithInitialPosition() {
		final List<Integer> list = entity.getIntList();
		list.add(10);
		list.add(20);
		list.add(30);
		list.add(60);
		list.add(70);
		list.add(80);

		normalize(session -> {
			list.remove(0); // remove 10
			list.remove(0); // remove 20
			list.set(0, 33); // change 30 to 33
			list.add(1, 50);
			list.add(1, 40);
			list.set(4, 77); // change 70 to 77
		});

		assertResultList(list, 33, 40, 50, 60, 77, 80);

		assertManiCount(2);
		assertRemove(0, 1, 2, 4);
		assertRemoveWithItems(0, 10, 1, 20, 2, 30, 4, 70);
		assertAdd(0, 33, 1, 40, 2, 50, 4, 77);
	}

	@Test
	public void mixtureOfEverything() {
		final List<Integer> list = entity.getIntList();
		list.add(10);
		list.add(30);

		normalize(session -> {
			list.add(20); // *
			list.add(40); // new value, will be in Bulk INSERT
			list.set(1, 666); // changing 30 for 666
			list.set(0, 11); // changing 10 for 11, will be in Bulk REPLACE
			list.set(2, 22); // * changing 20 for 22
			list.remove(1); // removing 666 (index 1, it was there before -> will end in Bulk REMOVE)
			list.remove(1); // * removing 22 (inserted, replaced, deleted -> same as if all 3 * were ignored)
		});

		assertResultList(list, 11, 40);

		assertManiCount(2);
		assertRemove(0, 1);
		assertRemoveWithItems(0, 10, 1, 30);
		assertAdd(0, 11, 1, 40);
	}

	@Test
	public void insertsWithInitialValue() {
		record(session -> {
			entity.setIntList(asList(10, 30));
			List<Integer> list = entity.getIntList();
			list.add(20);
			list.remove(1); // removing 30
		});

		normalize();

		assertChangeValue(10, 20);
	}

	@Test
	public void bulkInserts() {
		final List<Integer> list = entity.getIntList();

		normalize(session -> {
			list.addAll(asList(10, 20, 30));
			list.remove(2);
		});

		assertManiCount(1);
		assertAdd(0, 10, 1, 20);
	}

	@Test
	public void bulkRemove() {
		final List<Integer> list = entity.getIntList();

		normalize(session -> {
			list.addAll(asList(10, 20, 30, 40, 50));
			list.removeAll(asSet(30, 40, 50));
		});

		assertManiCount(1);
		assertAdd(0, 10, 1, 20);
	}

	/**
	 * This would not be possible with a previous version, but since we now assure collection getters do not return <tt>null</tt>, we also have to
	 * support.
	 */
	@Test
	public void bulkRemove_WhenSetToNullBefore() {
		normalize(session -> {
			entity.setIntList(null);
			List<Integer> list = entity.getIntList();

			list.addAll(asList(10, 20, 30, 40, 50));
			list.removeAll(asSet(30, 40, 50));
		});

		assertManiCount(1);
		assertChangeValue(10, 20);
	}

	@Test
	public void simpleInsertsWithClear() {
		final List<Integer> list = entity.getIntList();

		normalize(session -> {
			clear(list);
			list.add(10);
			list.add(40);
			list.add(30);
			list.remove(2);
			list.set(1, 20);
		});

		assertManiCount(1);
		assertChangeValue(10, 20);
	}

	@Test
	public void bulkInsertsWithClear() {
		final List<Integer> list = entity.getIntList();

		normalize(session -> {
			clear(list);
			list.addAll(asSet(10, 20, 30));
			list.remove(2);
		});

		assertManiCount(1);
		assertChangeValue(10, 20);
	}

	@Test
	public void bulkRemoveWithClear() {
		final List<Integer> list = entity.getIntList();

		normalize(session -> {
			clear(list);
			list.addAll(asList(10, 20, 30, 40, 50));
			list.removeAll(asSet(30, 40, 50));
		});

		assertManiCount(1);
		assertChangeValue(10, 20);
	}

	@Test
	public void complexTestWithEntities() {
		entity.setSomeList(newList());
		final List<TestEntity> list = entity.getSomeList();

		final TestEntity e1 = createEntity("E1");
		final TestEntity e2 = createEntity("E2");
		final TestEntity e3 = createEntity("E3");

		list.add(e1);

		normalize(session -> {
			list.removeAll(asSet(e3, e1)); // removing e1 -> now the list is empty
			list.addAll(asList(e1, e2, e3));
			list.remove(e3);
		});

		EntityReference r1 = getReference(e1);
		EntityReference r2 = getReference(e2);

		assertManiCount(2);
		assertRemove(0);
		assertAdd(0, r1, 1, r2);
	}

	@Test
	public void settingPropsAndAsPropAndThenDeletingPersistentEntity() {
		record(session -> {
			TestEntity tmpEntity = session.create(TestEntity.T);
			entity.getSomeList().add(tmpEntity); // this should be removed from adds
			entity.getSomeList().add(entity);
		});
		recordedManipulations.add(inverse(getRecordedManipulation(0)));

		normalize();

		EntityReference ref = getReference(entity);

		assertAdd(0, ref);
	}

	@Test
	public void objectProperty_Cvm() {
		normalize(session -> {
			entity.setObjectProperty(asList("One"));
			List<String> list = (EnhancedList<String>) entity.getObjectProperty();
			list.add("Two");
		});

		assertManiCount(1);
		assertChangeValue("One", "Two");
	}

	@Test
	public void objectProperty_Add() {
		entity.setObjectProperty(newList());
		List<String> list = (EnhancedList<String>) entity.getObjectProperty();

		normalize(session -> {
			list.add("Hello");
		});

		assertManiCount(1);
		assertAdd(0, "Hello");
	}

	@Test
	public void objectProperty_Cvm_WithIdentityChange() {
		normalize(session -> {
			entity.setId(1L);
			entity.setObjectProperty(asList("First"));
			List<String> list = (EnhancedList<String>) entity.getObjectProperty();
			list.add("Second");
		});

		// @formatter:off
		assertManipulations(
				changeValue(TestEntity.T, "id", 1L),
				changeValue(TestEntity.T, "objectProperty", asList("First", "Second"))
		);
		// @formatter:on
	}

	@Test
	public void objectProperty_Clear() {
		normalize(session -> {
			entity.setObjectProperty(newList());
			List<String> list = (List<String>) entity.getObjectProperty();
			list.add("Cleared");
			list.clear();
			list.add("Hello");
		});

		assertManiCount(1);
		assertChangeValue("Hello");
	}

	// ####################################
	// ## . . . . . Assertions . . . . . ##
	// ####################################

	private void assertResultList(List<Integer> list, Integer... expected) {
		BtAssertions.assertThat(list).as("List is expected to look different").isEqualTo(asList(expected));
	}

	private void assertChangeValue(Object... objs) {
		ChangeValueManipulation m = getManipulation(ChangeValueManipulation.class);

		List<?> value = (List<?>) m.getNewValue();
		List<?> expected = objs == null ? null : asList(objs);

		assertEqual(value, expected, "Wrong new value.");
	}

	private void assertAdd(Object... objs) {
		AddManipulation m = getManipulation(AddManipulation.class);

		Map<Object, Object> itemsToAdd = m.getItemsToAdd();
		Map<Object, Object> expected = asMap(objs);

		Assertions.assertThat(itemsToAdd).isNotNull();
		assertEqual(itemsToAdd.keySet(), expected.keySet(), "WRONG INSERT INDICES.");
		assertEqual(itemsToAdd.values(), expected.values(), "Wrong INSERT VALUES.");
	}

	protected void assertRemoveWithItems(Object... objs) {
		RemoveManipulation m = getManipulation(RemoveManipulation.class);

		Map<Object, Object> items = m.getItemsToRemove();
		Map<Object, Object> expected = asMap(objs);

		Assertions.assertThat(items).isNotNull();
		assertEqual(items.keySet(), expected.keySet(), "WRONG REMOVE INDICES.");
		assertEqual(items.values(), expected.values(), "Wrong REMOVE VALUES.");
	}

}
