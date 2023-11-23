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

import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.manipulation.testdata.manipulation.TestEntity;
import com.braintribe.utils.lcd.SetTools;

/**
 * 
 */
public class MapNormalizationTests extends AbstractCollectionNormalizerTests {

	@Test
	public void settingNullValue() {
		normalize(session -> {
			entity.setIntMap(null);
		});

		assertManiCount(1);
		assertChangeValue(/* empty map */);
	}

	@Test
	public void simpleInserts() {
		final Map<Integer, Integer> map = entity.getIntMap();

		normalize(session -> {
			map.put(1, 10);
			map.put(2, 20);
			map.put(3, 30);
			map.remove(3);
		});

		assertAdd(1, 10, 2, 20);
		assertRemove(3, 30);
	}

	@Test
	public void simpleInsertsWithNulls() {
		final Map<Integer, Integer> map = entity.getIntMap();

		normalize(session -> {
			map.put(null, 0);
			map.put(1, 10);
			map.put(2, 20);
			map.put(3, 30);
			map.put(4, null);
			map.remove(3);
			map.remove(null);
		});

		assertAdd(1, 10, 2, 20, 4, null);
		assertRemove(3, 30, null, 0);
	}

	@Test
	public void insertsWithInitialValue() {
		normalize(session -> {
			entity.setIntMap(mapForKeys(1, 3));
			Map<Integer, Integer> map = entity.getIntMap();
			map.put(2, 20);
			map.remove(3);
		});

		assertManiCount(1);
		assertChangeValue(1, 10, 2, 20);
	}

	@Test
	public void bulkInserts() {
		final Map<Integer, Integer> map = entity.getIntMap();

		normalize(session -> {
			map.putAll(mapForKeys(1, 2, 3));
			map.remove(3);
		});

		assertAdd(1, 10, 2, 20);
		assertRemove(3, 30);
	}

	@Test
	public void bulkRemove() {
		final Map<Integer, Integer> map = entity.getIntMap();

		normalize(session -> {
			map.putAll(mapForKeys(1, 2, 3, 4, 5));
			map.keySet().removeAll(asSet(3, 4, 5));
		});

		assertAdd(1, 10, 2, 20);
		assertRemove(3, 30, 4, 40, 5, 50);
	}

	/**
	 * This would not be possible with a previous version, but since we now assure collection getters do not return <tt>null</tt>, we also have to
	 * support.
	 */
	@Test
	public void bulkRemove_WhenSetToNullBefore() {
		normalize(session -> {
			entity.setIntMap(null);
			Map<Integer, Integer> map = entity.getIntMap();

			map.putAll(mapForKeys(1, 2, 3, 4, 5));
			map.keySet().removeAll(SetTools.asSet(3, 4, 5));
		});

		assertManiCount(1);
		assertChangeValue(1, 10, 2, 20);
	}

	@Test
	public void simpleInsertsWithClear() {
		final Map<Integer, Integer> map = entity.getIntMap();

		normalize(session -> {
			clear(map);
			map.put(1, 10);
			map.put(2, 20);
			map.put(3, 30);
			map.remove(3);
		});

		assertManiCount(1);
		assertChangeValue(1, 10, 2, 20);
	}

	@Test
	public void bulkInsertsWithClear() {
		final Map<Integer, Integer> map = entity.getIntMap();

		normalize(session -> {
			clear(map);
			map.putAll(mapForKeys(1, 2, 3));
			map.remove(3);
		});

		assertManiCount(1);
		assertChangeValue(1, 10, 2, 20);
	}

	@Test
	public void bulkRemoveWithClear() {
		final Map<Integer, Integer> map = entity.getIntMap();

		normalize(session -> {
			clear(map);
			map.putAll(mapForKeys(1, 2, 3, 4, 5));
			map.keySet().removeAll(SetTools.asSet(3, 4, 5));
		});

		assertManiCount(1);
		assertChangeValue(1, 10, 2, 20);
	}

	@Test
	public void complexTestWithEntities() {
		entity.setSomeMap(newMap());
		final Map<TestEntity, TestEntity> map = entity.getSomeMap();

		final TestEntity e1 = createEntity("E1");
		final TestEntity e2 = createEntity("E2");
		final TestEntity e3 = createEntity("E3");

		normalize(session -> {
			map.put(e1, e1);
			map.put(e2, e2);
			map.keySet().removeAll(SetTools.asSet(e1, e2, e3));

			Map<TestEntity, TestEntity> map2 = asMap(e1, e1, e2, e2, e3, e3);
			map.putAll(map2);

			map.remove(e3);
		});

		EntityReference r1 = getReference(e1);
		EntityReference r2 = getReference(e2);
		EntityReference r3 = getReference(e3);

		assertAdd(r1, r1, r2, r2);
		assertRemove(r3, r3);
	}

	@Test
	public void settingPropsAndAsPropAndThenDeletingPersistentEntity() {
		final Map<TestEntity, TestEntity> map = entity.getSomeMap();

		final TestEntity e1 = createEntity("E1");
		final TestEntity e2 = createEntity("E2");
		final TestEntity e3 = createEntity("E3");

		record(session -> {
			TestEntity tmpEntity = session.create(TestEntity.T);
			map.put(tmpEntity, e1); // will be removed
			map.put(e1, e1);
			map.put(e2, tmpEntity);
			map.put(e3, tmpEntity);
		});
		recordedManipulations.add(inverse(getRecordedManipulation(0)));

		normalize();

		EntityReference r1 = getReference(e1);

		assertAdd(r1, r1);
	}

	/**
	 * There was a bug (BTT-5909) - we were not checking nulls correctly, this test was failing before we introduced the fix.
	 * 
	 * NOTE: This test is very implementation specific. If we set a map to null, we internally have "adds" variable in the MapTracker which is set to
	 * null. Later, when we want to remove any manipulation related to a deleted entity, we need to check if it is null. We were not doing this
	 * before...
	 */
	@Test
	public void settingMapToNull_WhileSomeEntityWasDeleted() {
		record(session -> {
			session.create(TestEntity.T);
			entity.setSomeMap(null);
		});
		recordedManipulations.add(inverse(getRecordedManipulation(0)));

		normalize();

		assertManiCount(1);
		assertChangeValue(/* empty map */);
	}

	@Test
	public void objectProperty_Clear() {
		normalize(session -> {
			entity.setObjectProperty(newMap());
			Map<String, String> map = (Map<String, String>) entity.getObjectProperty();
			map.put("Cleared", "Cleared");
			map.clear();
			map.put("Hello", "Hello");
		});

		assertManiCount(1);
		assertChangeValue("Hello", "Hello");
	}

	// ####################################
	// ## . . . . . Assertions . . . . . ##
	// ####################################

	@Override
	protected void assertRemove(Object... objs) {
		Map<Object, Object> expected = asMap(objs);

		super.assertRemove(expected.keySet().toArray());

		RemoveManipulation m = getManipulation(RemoveManipulation.class);
		Map<Object, Object> itemsToRemove = m.getItemsToRemove();

		assertEqual(itemsToRemove, expected, "Wrong remove value.");
	}

	private Map<Integer, Integer> mapForKeys(int... keys) {
		Map<Integer, Integer> result = newMap();

		for (int key : keys)
			result.put(key, 10 * key);

		return result;
	}

	private void assertChangeValue(Object... objs) {
		ChangeValueManipulation m = getManipulation(ChangeValueManipulation.class);

		if (objs == null) {
			Assertions.assertThat(m.getNewValue()).isNull();
			return;
		}

		Map<Object, Object> value = extractNewMapValue(m);
		Map<Object, Object> expected = asMap(objs);

		Assertions.assertThat(value).isNotNull();
		assertEqual(value.keySet(), expected.keySet(), "Wrong key-set.");
		assertEqual(value.values(), expected.values(), "Wrong key-values.");
	}

	private Map<Object, Object> extractNewMapValue(ChangeValueManipulation m) {
		Map<Object, Object> result = newMap();

		Map<?, ?> md = (Map<?, ?>) m.getNewValue();

		if (md == null)
			return null;

		for (Map.Entry<?, ?> e : md.entrySet())
			result.put(e.getKey(), e.getValue());

		return result;
	}

	private void assertAdd(Object... objs) {
		AddManipulation m = getManipulation(AddManipulation.class);

		Map<Object, Object> itemsToAdd = m.getItemsToAdd();
		Map<Object, Object> expected = asMap(objs);

		Assertions.assertThat(itemsToAdd).isNotNull().hasSize(expected.size());
		assertEqual(itemsToAdd.keySet(), expected.keySet(), "Wrong key-set.");
		assertEqual(itemsToAdd.values(), expected.values(), "Wrong key-values.");
	}

}
