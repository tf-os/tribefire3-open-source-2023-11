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

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.absenting;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.manifestation;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.HashSet;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.braintribe.model.generic.manipulation.AbsentingManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.processing.manipulation.testdata.manipulation.TestEntity;

/**
 * 
 */
public class SimpleNormalizationTests extends AbstractSimpleNormalizerTests {

	@Test
	public void instantiationAndUninstantiation() {
		record(session -> {
			session.create(TestEntity.T);
		});

		recordedManipulations.add(inverse(getRecordedManipulation(0)));

		normalize();
		assertEmpty();
	}

	@Test
	public void instantiationSettingSimpleValuesAndUninstantiation() {
		record(session -> {
			TestEntity entity = session.create(TestEntity.T);
			entity.setProperty1("value1");
			entity.setProperty2("value2");
		});

		recordedManipulations.add(inverse(getRecordedManipulation(0)));

		normalize();
		assertEmpty();
	}

	@Test
	public void instantiationSettingCollectionValuesAndUninstantiation() {
		record(session -> {
			TestEntity entity = session.create(TestEntity.T);
			entity.setSomeSet(new HashSet<TestEntity>());
			entity.getSomeSet().add(entity);
		});

		recordedManipulations.add(inverse(getRecordedManipulation(0)));

		normalize();
		assertEmpty();
	}

	@Test
	public void settingPropsAndAsPropAndThenDeletingPersistentEntity() {
		final TestEntity entity = createEntity("test", 45);

		record(session -> {
			TestEntity tmpEntity = session.create(TestEntity.T); // will be removed from the stack
			entity.setParentEntity(tmpEntity); // will be normalized to entity.setParentEntity(null);
			tmpEntity.setParentEntity(entity); // will be removed from the stack
		});

		recordedManipulations.add(inverse(getRecordedManipulation(0)));

		normalize();
		assertManiCount(1);
		assertPositions(-1, 0, -1, -1);

		// check that second manipulation is change to entity.setParentEntity(null);
		ChangeValueManipulation cvm = (ChangeValueManipulation) normalizedManipulations.get(0);
		Assertions.assertThat(cvm.getNewValue()).as("New value for 'entity.parentEntity' should be null!").isNull();
	}

	@Test
	public void insertingIntoCollectionAndChangingCollectionValue() {
		final TestEntity entity = session.create(TestEntity.T);

		record(session -> {
			entity.setSomeSet(new HashSet<TestEntity>());
			entity.getSomeSet().add(entity);
			entity.setSomeSet(new HashSet<TestEntity>());
		});

		normalize();
		assertManiCount(1);
		assertPositions(-1, -1, 0);
	}

	@Test
	public void insertingIntoCollectionAndClearingCollection() {
		final TestEntity entity = session.create(TestEntity.T);

		record(session -> {
			entity.setSomeList(newList());
			entity.getSomeList().add(entity);
			entity.getSomeList().add(entity);
			entity.getSomeList().clear();
		});

		normalize();
		assertManiCount(1);
		assertPositions(-1, -1, -1, 0);
	}

	@Test
	public void singleSettingOfProperty() {
		final TestEntity entity = session.create(TestEntity.T);

		record(session -> {
			entity.setProperty1("1");
			entity.setProperty2("2");
		});

		normalize();
		assertManiCount(2);
	}

	@Test
	public void multipleSettingOfSameProperty() {
		final TestEntity entity = session.create(TestEntity.T);

		record(session -> {
			entity.setProperty1("x");
			entity.setProperty2("");
			entity.setProperty1("y");
		});

		normalize();
		assertManiCount(2);

		assertPositions(-1, 0, 1);
	}

	@Test
	public void multipleSettingOfSameProperty_Persistent() {
		final TestEntity entity = session.create(TestEntity.T);
		entity.setId(1L);

		record(session -> {
			entity.setProperty1("x");
			entity.setProperty2("");
			entity.setProperty1("y");
		});

		normalize();
		assertManiCount(2);

		assertPositions(-1, 0, 1);
	}

	@Test
	public void keepsNonNormalizableManipulations() {
		record(session -> {
			// nothing
		});

		recordedManipulations.add(manifestation(null));
		recordedManipulations.add(absentingManipulation());

		normalize();
		assertManiCount(2);
	}

	private AbsentingManipulation absentingManipulation() {
		return absenting(null, EntityProperty.T.create());
	}

}
