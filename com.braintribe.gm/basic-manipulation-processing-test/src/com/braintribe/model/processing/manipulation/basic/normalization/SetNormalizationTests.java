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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.processing.manipulation.testdata.manipulation.TestEntity;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.utils.CollectionTools;

/**
 * 
 */
public class SetNormalizationTests extends AbstractCollectionNormalizerTests {

	@Test
	public void settingNullValue() {
		normalize(session -> {
			entity.setIntSet(null);
		});

		assertManiCount(1);
		assertChangeValue(/* empty set */);
	}

	@Test
	public void simpleInserts() {
		final Set<Integer> set = entity.getIntSet();

		normalize(session -> {
			set.add(1);
			set.add(2);
			set.add(3);
			set.remove(3);
		});

		assertAdd(1, 2);
		assertRemove(3);
	}

	@Test
	public void insertsWithInitialValue() {
		record(session -> {
			entity.setIntSet(asSet(1, 3));
			Set<Integer> set = entity.getIntSet();
			set.add(2);
			set.remove(3);
		});

		normalize();

		assertManiCount(1);
		assertChangeValue(1, 2);
	}

	@Test
	public void bulkInserts() {
		final Set<Integer> set = entity.getIntSet();

		normalize(session -> {
			set.addAll(asSet(1, 2, 3));
			set.remove(3);
		});

		assertAdd(1, 2);
		assertRemove(3);
	}

	@Test
	public void bulkRemove() {
		final Set<Integer> set = entity.getIntSet();

		normalize(session -> {
			set.addAll(asSet(1, 2, 3, 4, 5));
			set.removeAll(asSet(3, 4, 5));
		});

		assertAdd(1, 2);
		assertRemove(3, 4, 5);
	}

	/**
	 * This would not be possible with a previous version, but since we now assure collection getters do not return <tt>null</tt>, we also have to
	 * support.
	 */
	@Test
	public void bulkRemove_WhenSetToNullBefore() {
		normalize(session -> {
			entity.setIntSet(null);

			Set<Integer> set = entity.getIntSet();
			set.addAll(asSet(1, 2, 3, 4, 5));
			set.removeAll(asSet(3, 4, 5));
		});

		assertManiCount(1);
		assertChangeValue(1, 2);
	}

	@Test
	public void simpleInsertsWithClear() {
		final Set<Integer> set = entity.getIntSet();

		normalize(session -> {
			clear(set);
			set.add(1);
			set.add(2);
			set.add(3);
			set.remove(3);
		});

		assertManiCount(1);
		assertChangeValue(1, 2);
	}

	@Test
	public void bulkInsertsWithClear() {
		final Set<Integer> set = entity.getIntSet();

		normalize(session -> {
			clear(set);
			set.addAll(asSet(1, 2, 3));
			set.remove(3);
		});

		assertManiCount(1);
		assertChangeValue(1, 2);
	}

	@Test
	public void bulkRemoveWithClear() {
		final Set<Integer> set = entity.getIntSet();

		normalize(session -> {
			clear(set);
			set.addAll(asSet(1, 2, 3, 4, 5));
			set.removeAll(asSet(3, 4, 5));
		});

		assertManiCount(1);
		assertChangeValue(1, 2);
	}

	@Test
	public void complexTestWithEntities() {
		final Set<TestEntity> set = entity.getSomeSet();

		final TestEntity e1 = createEntity("E1");
		final TestEntity e2 = createEntity("E2");
		final TestEntity e3 = createEntity("E3");

		normalize(session -> {
			set.add(e1);
			set.removeAll(asSet(e3, e1));
			set.addAll(asSet(e1, e2, e3));
			set.remove(e3);
		});

		EntityReference r1 = getReference(e1);
		EntityReference r2 = getReference(e2);
		EntityReference r3 = getReference(e3);

		assertAdd(r1, r2);
		assertRemove(r3);
	}

	@Test
	public void settingPropsAndAsPropAndThenDeletingPersistentEntity() {
		record(session -> {
			TestEntity tmpEntity = session.create(TestEntity.T); // will be removed from the stack
			entity.getSomeSet().add(tmpEntity); // this should be removed from adds
			entity.getSomeSet().add(entity);
		});
		recordedManipulations.add(inverse(getRecordedManipulation(0)));

		normalize();

		EntityReference ref = getReference(entity);

		assertAdd(ref);
	}

	/** There was a bug BTT-6487 */
	@Test
	public void specialDeletionTest() {
		final GmEntityType ge = session.create(GmEntityType.T); // will be removed from the stack
		ge.setId(44L);
		session.commit();

		final List<EntityReference> mRefList = newList();

		record(session -> {
			Mandatory m = session.create(Mandatory.T);
			m.setId(99L);
			ge.getMetaData().add(m);
			ge.getMetaData().remove(m);

			mRefList.add(m.reference());
		});

		DeleteManipulation inverse = (DeleteManipulation) inverse(getRecordedManipulation(0));
		inverse.setEntity(mRefList.get(0));
		recordedManipulations.add(inverse);

		normalize();

		assertManiCount(0);
	}

	@Test
	public void objectProperty_Clear() {
		normalize(session -> {
			entity.setObjectProperty(newSet());
			Set<String> list = (Set<String>) entity.getObjectProperty();
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

	@Override
	protected void assertRemove(Object... objs) {
		super.assertRemove(objs);

		Map<Object, Object> expectedMap = CollectionTools.getIdentityMap(Arrays.asList(objs));

		RemoveManipulation m = getManipulation(RemoveManipulation.class);
		Map<Object, Object> itemsToRemove = m.getItemsToRemove();

		assertEqual(itemsToRemove, expectedMap, "Wrong remove value.");
	}

	private void assertChangeValue(Object... objs) {
		ChangeValueManipulation m = getManipulation(ChangeValueManipulation.class);

		Set<?> value = (Set<?>) m.getNewValue();
		Set<?> expected = objs == null ? null : asSet(objs);

		assertEqual(value, expected, "Wrong new value.");
	}

	private void assertAdd(Object... objs) {
		AddManipulation m = getManipulation(AddManipulation.class);

		Map<Object, Object> itemsToAdd = m.getItemsToAdd();
		Set<?> expected = asSet(objs);

		Assertions.assertThat(itemsToAdd).isNotNull().hasSize(expected.size());
		assertEqual(itemsToAdd.keySet(), expected, "Wrong key-set.");
		assertEqual(itemsToAdd.values(), expected, "Wrong key-values.");
	}

	/**
	 * We had a bug that something like this would throw an exception, because the normalizer tries to remove all deleted entities from all inserts.
	 * Because our comparator was wrong, removing entities from collections of simple types was throwing an exception. So this test just tests no
	 * exception is thrown.
	 */
	@Test
	public void addingSimpleToCollectionAndDeletingDoesNotCauseException() {
		final TestEntity entity1 = session.create(TestEntity.T);

		normalize(session -> {
			entity.getIntSet().addAll(Arrays.asList(1, 2, 3, 4, 5));
			session.deleteEntity(entity1);
		});
	}

	/** There was a bug.... */
	@Test
	public void normalizationDoesNotChangeRecorded() {
		record(session -> {
			TestEntity other = session.create(TestEntity.T);
			entity.setSomeSet(asSet(other));
			other.setId("other");
		});

		assertManipulationSecondReferencesPreliminaryEntity("recorded");

		normalize();

		assertManipulationSecondReferencesPreliminaryEntity("normalized");
	}

	private void assertManipulationSecondReferencesPreliminaryEntity(String referenceDesc) {
		ChangeValueManipulation cvm = (ChangeValueManipulation) recordedManipulations.get(1);
		Set<?> someSet = (Set<?>) cvm.getNewValue();
		EntityReference ref = first(someSet);
		Assertions.assertThat(ref).as(referenceDesc + " reference should be  preliminary.").isInstanceOf(PreliminaryEntityReference.class);
	}

}
