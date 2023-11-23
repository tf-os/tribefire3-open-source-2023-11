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

import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.newReference;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import org.junit.Test;

import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.manipulation.testdata.manipulation.TestEntity;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 *   
 */
public class FullNormalizationTests extends AbstractNormalizerTests {

	@Test
	public void changeIdProperty_AfterSimpleCvm() {
		record(session -> {
			TestEntity entity = session.create(TestEntity.T);

			entity.setProperty1("ignored"); // this should be dropped
			entity.setId(10);
			entity.setProperty1("valid");
		});

		normalize();

		// @formatter:off
		assertManipulations(
				instantiation(TestEntity.T),
				changeValue(TestEntity.T, "id", 10),
				changeValue(TestEntity.T, "property1", "valid"));
		// @formatter:on
	}

	/**
	 * This was added due to a bug: BTT-7070
	 * 
	 * The problem was, that the original manipulation stack was corrupted by the normalizer. It would not clone the Owner of the CVm for property1 =
	 * "ignored", thus when the normalizer changes the order (first set id, then property1), the owner would now contain a
	 * {@link PersistentEntityReference}, which in the original stack is not valid (because no id has been assigned yet).
	 */
	@Test
	public void changeIdProperty_AfterSimpleCvm_CheckOriginalNotAffected() {
		record(session -> {
			TestEntity entity = session.create(TestEntity.T);

			entity.setProperty1("ignored"); // this should be dropped
			entity.setId(10);
		});

		normalize();

		ChangeValueManipulation setProp1Manipulation = getRecordedManipulation(1);
		EntityProperty ep = (EntityProperty) setProp1Manipulation.getOwner();
		BtAssertions.assertThat(ep.getReference().getClass()).isAssignableTo(PreliminaryEntityReference.class);
	}

	@Test
	public void changeIdProperty_OnPersistent() {
		final TestEntity entity = session.create(TestEntity.T);
		entity.setId(10);
		session.commit();

		record(session -> {
			entity.setId(20);
		});

		normalize();

		assertManipulations(changeValue(TestEntity.T, "id", 20));
	}

	@Test
	public void changeIdProperty_OnPreliminary_SetItToNull() {
		record(session -> {
			TestEntity entity = session.create(TestEntity.T);
			entity.setName("name");
			entity.setId(null);
		});

		normalize();

		// @formatter:off
		assertManipulations(
				instantiation(TestEntity.T),
				changeValue(TestEntity.T, "name", "name"));
		// @formatter:on
	}

	@Test
	public void changeIdProperty_AfterEntityCvm() {
		record(session -> {
			TestEntity entity = session.create(TestEntity.T);
			entity.setParentEntity(entity);
			entity.setId(10);
		});

		normalize();

		// @formatter:off
		assertManipulations(
				instantiation(TestEntity.T),
				changeValue(TestEntity.T, "id", 10),
				changeValue(TestEntity.T, "parentEntity", newReference(true, TestEntity.T, 10, null)));
		// @formatter:on
	}

	@Test
	public void changeIdProperty_AfterSettingSet() {
		record(session -> {
			TestEntity entity = session.create(TestEntity.T);
			entity.setSomeSet(asSet(entity));
			entity.setId(10);
		});

		normalize();

		EntityReference ref = newReference(true, TestEntity.T, 10, null);

		// @formatter:off
		assertManipulations(
				instantiation(TestEntity.T),
				changeValue(TestEntity.T, "id", 10),
				changeValue(TestEntity.T, "someSet", asSet(ref)));
		// @formatter:on
	}

	@Test
	public void changeIdProperty_AfterSettingList() {
		record(session -> {
			TestEntity entity = session.create(TestEntity.T);
			entity.setSomeList(asList(entity));
			entity.setId(10);
		});

		normalize();

		EntityReference ref = newReference(true, TestEntity.T, 10, null);

		// @formatter:off
		assertManipulations(
				instantiation(TestEntity.T),
				changeValue(TestEntity.T, "id", 10),
				changeValue(TestEntity.T, "someList", asList(ref)));
		// @formatter:on
	}

	@Test
	public void changeIdProperty_AfterSettingMap() {
		record(session -> {
			TestEntity entity = session.create(TestEntity.T);
			entity.setSomeMap(asMap(entity, entity));
			entity.setId(10);
		});

		normalize();

		EntityReference ref = newReference(true, TestEntity.T, 10, null);

		// @formatter:off
		assertManipulations(
				instantiation(TestEntity.T),
				changeValue(TestEntity.T, "id", 10),
				changeValue(TestEntity.T, "someMap", asMap(ref, ref)));
		// @formatter:on
	}

	@Test
	public void changeIdProperty_AfterAddingToSet() {
		record(session -> {
			TestEntity entity = session.create(TestEntity.T);
			entity.getSomeSet().add(entity);
			entity.setId(10);
		});

		normalize();

		// @formatter:off
		assertManipulations(
				instantiation(TestEntity.T),
				changeValue(TestEntity.T, "id", 10),
				changeValue(TestEntity.T, "someSet", asSet(newReference(true, TestEntity.T, 10, null))));
		// @formatter:on
	}

	@Test
	public void changeIdProperty_AfterAddingToList() {
		record(session -> {
			TestEntity entity = session.create(TestEntity.T);
			entity.getSomeList().add(entity);
			entity.setId(10);
		});

		normalize();

		// @formatter:off
		assertManipulations(
				instantiation(TestEntity.T),
				changeValue(TestEntity.T, "id", 10),
				changeValue(TestEntity.T, "someList", asList(newReference(true, TestEntity.T, 10, null))));
		// @formatter:on
	}

	@Test
	public void changeIdProperty_AfterAddingToMap() {
		record(session -> {
			TestEntity entity = session.create(TestEntity.T);
			entity.getSomeMap().put(entity, entity);
			entity.setId(10);
		});

		normalize();

		EntityReference ref = newReference(true, TestEntity.T, 10, null);

		// @formatter:off
		assertManipulations(
				instantiation(TestEntity.T),
				changeValue(TestEntity.T, "id", 10),
				changeValue(TestEntity.T, "someMap", asMap(ref, ref)));
		// @formatter:on
	}

	/** This was added due to a bug: BTT-6295 */
	@Test
	public void changeId_ThenAddToSet() {
		record(session -> {
			TestEntity e1 = session.create(TestEntity.T);

			e1.setId(10);
			e1.setSomeSet(asSet(e1));
		});

		normalize();

		EntityReference ref = newReference(true, TestEntity.T, 10, null);

		// @formatter:off
		assertManipulations(
				instantiation(TestEntity.T),
				changeValue(TestEntity.T, "id", 10),
				changeValue(TestEntity.T, "someSet", asSet(ref)));
		// @formatter:on
	}

	@Test
	public void changingIdAnddeletingHasNoEffect_Preliminary() {
		record(session -> {
			TestEntity toBeDeleted = session.create(TestEntity.T);
			toBeDeleted.setId(10);
			toBeDeleted.setName("name");
			session.deleteEntity(toBeDeleted);
		});

		normalize();

		assertManipulations(); // this means there is not a single manipulation as a result
	}

	@Test
	public void assigningSameIdNoProblemIfOneEntityDeleted() {
		record(session -> {
			TestEntity toBeDeleted = session.create(TestEntity.T);
			TestEntity createdEntity = session.create(TestEntity.T);

			toBeDeleted.setName("toBeDeleted");
			createdEntity.setName("created");

			toBeDeleted.setId(10);
			session.deleteEntity(toBeDeleted);

			createdEntity.setId(10);
		});

		normalize();

		// @formatter:off
		assertManipulations(
				instantiation(TestEntity.T),
				changeValue(TestEntity.T, "id", 10),
				changeValue(TestEntity.T, "name", "created"));
		// @formatter:on
	}

	@Override
	protected void normalize() {
		performFullNormalization();
	}

}
