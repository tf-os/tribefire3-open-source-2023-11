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

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.delete;
import static com.braintribe.testing.junit.assertions.gm.assertj.core.api.GmAssertions.assertEntity;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Before;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.LifecycleManipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.util.ManipulationBuilder;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.model.processing.manipulation.testdata.manipulation.TestEntity;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.processing.test.tools.meta.ManipulationDriver;
import com.braintribe.model.processing.test.tools.meta.ManipulationDriver.NotifyingSessionRunnable;
import com.braintribe.model.processing.test.tools.meta.ManipulationTrackingMode;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public abstract class AbstractNormalizerTests {

	public static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	protected PersistenceGmSession session;
	protected List<AtomicManipulation> recordedManipulations;
	protected CompoundManipulation normalizedManipulation;
	protected List<AtomicManipulation> normalizedManipulations;

	protected ManipulationTrackingMode trackingMode = ManipulationTrackingMode.PERSISTENT;
	private ManipulationDriver manipulationDriver;

	@Before
	public void setup() {
		manipulationDriver = new ManipulationDriver(new Smood(EmptyReadWriteLock.INSTANCE));
		manipulationDriver.setTrackingMode(trackingMode);
		session = manipulationDriver.newSession();
	}

	protected void normalize(NotifyingSessionRunnable r) {
		record(r);
		normalize();
	}

	protected abstract void normalize();

	protected final void performFullNormalization() {
		CompoundManipulation recordedManipulation = ManipulationBuilder.compound(recordedManipulations);

		if (trackingMode == ManipulationTrackingMode.GLOBAL)
			normalizedManipulation = Normalizer.normalizeGlobal(recordedManipulation);
		else
			normalizedManipulation = Normalizer.normalize(recordedManipulation);
		normalizedManipulations = (List<AtomicManipulation>) (List<?>) normalizedManipulation.getCompoundManipulationList();
	}

	protected void record(NotifyingSessionRunnable r) {
		CompoundManipulation recordedManipulation = manipulationDriver.track(r, session);
		recordedManipulations = recordedManipulation.inline();
	}

	protected static <T> T cast(Object o) {
		return (T) o;
	}

	protected <T extends AtomicManipulation> T getRecordedManipulation(int i) {
		return (T) recordedManipulations.get(i);
	}

	protected AtomicManipulation inverse(AtomicManipulation manipulation) {
		if (manipulation instanceof InstantiationManipulation)
			return createDeleteManipulation(((InstantiationManipulation) manipulation).getEntity());

		AtomicManipulation inverse = (AtomicManipulation) manipulation.getInverseManipulation();

		if (inverse == null)
			throw new RuntimeException("MANIPULATION HAS NO INVERSE: " + manipulation);

		return inverse;
	}

	// ####################################
	// ## . . . . . . Helpers . . . . . .##
	// ####################################

	private AtomicManipulation createDeleteManipulation(GenericEntity entity) {
		return delete(entity, DeleteMode.ignoreReferences);
	}

	protected TestEntity createEntity(String name) {
		return createEntity(name, null);
	}

	protected TestEntity createEntity(String name, Integer id) {
		TestEntity e = session.create(TestEntity.T);
		e.setName(name);
		e.setId(id);
		return e;
	}

	protected EntityReference getReference(TestEntity e) {
		return e.reference();
	}

	// ####################################
	// ## . . . . . Assertions . . . . . ##
	// ####################################

	protected void assertManiCount(int expected) {
		Assertions.assertThat(recordedManipulations).isNotNull().isNotEmpty();
		Assertions.assertThat(normalizedManipulations).isNotNull().hasSize(expected);
	}

	protected void assertManipulations(ManipulationDescriptor<?>... manipulationDescs) {
		assertManiCount(manipulationDescs.length);

		int i = 0;
		for (AtomicManipulation m : normalizedManipulations)
			manipulationDescs[i++].assertMatchesManipulation(m);
	}

	protected void assertOwnerTypes(EntityReferenceType... refTypes) {
		assertManiCount(refTypes.length);

		int i = 0;
		for (AtomicManipulation m : normalizedManipulations)
			assertRefType(m, refTypes[i], i++);
	}

	private void assertRefType(AtomicManipulation m, EntityReferenceType refType, int i) {
		EntityReference ref = extractOwner(m);
		assertThat(ref.referenceType()).as(() -> "Wrong owner type at position [" + i + "] Expected: " + refType).isSameAs(refType);
	}

	private EntityReference extractOwner(AtomicManipulation m) {
		if (m instanceof LifecycleManipulation)
			return (EntityReference) ((LifecycleManipulation) m).getEntity();

		if (m instanceof PropertyManipulation)
			return ((EntityProperty) ((PropertyManipulation) m).getOwner()).getReference();

		throw new IllegalArgumentException("Cannot extract owner from: " + m);
	}

	protected static InstantiationDescriptor instantiation(EntityType<?> ownerType) {
		return new InstantiationDescriptor(ownerType);
	}

	protected static ChangeValueDescriptor changeValue(EntityType<?> ownerType, String propertyName, Object newValue) {
		return new ChangeValueDescriptor(ownerType, propertyName, newValue);
	}

	protected static AddDescriptor add(EntityType<?> ownerType, String propertyName, Object... itemsToAdd) {
		return new AddDescriptor(ownerType, propertyName, asMap(itemsToAdd));
	}

	public abstract static class ManipulationDescriptor<M extends AtomicManipulation> {
		private final EntityType<M> entityType;

		ManipulationDescriptor(EntityType<M> entityType) {
			this.entityType = entityType;
		}

		void assertMatchesManipulation(AtomicManipulation m) {
			String msg = "Wrong manipulation type. Expected: " + entityType.getShortName() + ", actual: " + m;
			assertEntity(m).as(msg).isInstanceOf(entityType);
			assertMatches((M) m);
		}

		protected abstract void assertMatches(M m);

		protected void assertValueEqual(Object actual, Object expected) {
			String msg = "Wrong value. expected: " + expected + ", actual: " + actual;

			if (expected instanceof EntityReference) {
				boolean cmp = EntRefHashingComparator.INSTANCE.compare((EntityReference) actual, (EntityReference) expected);
				BtAssertions.assertThat(cmp).as(msg).isTrue();

			} else if (isListOfReferences(expected)) {
				List<EntityReference> expectedList = cast(expected);
				List<EntityReference> actualList = cast(expected);

				BtAssertions.assertThat(actualList).hasSize(expectedList.size());

				Iterator<EntityReference> expectedIt = expectedList.iterator();
				Iterator<EntityReference> actualIt = actualList.iterator();

				int pos = 0;

				while (expectedIt.hasNext()) {
					EntityReference expectedRef = expectedIt.next();
					EntityReference actualRef = actualIt.next();

					msg = "Wrong reference at position: " + pos++ + " expected: " + expectedRef + ", actual: " + actualRef;

					BtAssertions.assertThat(EntRefHashingComparator.INSTANCE.compare(expectedRef, actualRef)).as(msg).isTrue();
				}

			} else if (isSetOfReferences(expected)) {
				Set<EntityReference> expectedSet = CodingSet.create(EntRefHashingComparator.INSTANCE);
				expectedSet.addAll((Set<EntityReference>) expected);

				Set<EntityReference> actualSet = cast(actual);

				BtAssertions.assertThat(actualSet).as(msg).hasSize(expectedSet.size());
				BtAssertions.assertThat(expectedSet.containsAll(actualSet)).as(msg).isTrue();

			} else if (isMapOfReferences(expected)) {
				Map<EntityReference, EntityReference> expectedMap = CodingMap.create(EntRefHashingComparator.INSTANCE);
				expectedMap.putAll((Map<EntityReference, EntityReference>) expected);

				Map<EntityReference, EntityReference> actualMap = cast(actual);

				BtAssertions.assertThat(actualMap).as(msg).hasSize(expectedMap.size());
				BtAssertions.assertThat(actualMap.keySet().containsAll(actualMap.keySet())).as(msg);

				for (Entry<EntityReference, EntityReference> entry : actualMap.entrySet()) {
					EntityReference actualKey = entry.getKey();
					EntityReference actualValue = entry.getValue();

					EntityReference expectedValue = expectedMap.get(actualKey);

					if (expectedValue == null && !expectedMap.containsKey(actualKey))
						Assert.fail("Actual key not expected: " + actualKey + ". Expected map: " + expectedMap);

					msg = "Wrong value for key: " + actualKey + " expected: " + expectedValue + ", actual: " + actualValue;
					BtAssertions.assertThat(EntRefHashingComparator.INSTANCE.compare(expectedValue, actualValue)).as(msg).isTrue();

				}

			} else {
				BtAssertions.assertThat(actual).isEqualTo(expected);
			}
		}

		private boolean isListOfReferences(Object expected) {
			if (!(expected instanceof List))
				return false;
			else
				return first((List<?>) expected) instanceof EntityReference;
		}

		private boolean isSetOfReferences(Object expected) {
			if (!(expected instanceof Set))
				return false;
			else
				return first((Set<?>) expected) instanceof EntityReference;
		}

		private boolean isMapOfReferences(Object expected) {
			if (!(expected instanceof Map))
				return false;
			else
				return first(((Map<?, ?>) expected).keySet()) instanceof EntityReference;
		}
	}

	public static class InstantiationDescriptor extends ManipulationDescriptor<InstantiationManipulation> {
		private final EntityType<?> ownerType;

		public InstantiationDescriptor(EntityType<?> ownerType) {
			super(InstantiationManipulation.T);
			this.ownerType = ownerType;
		}

		@Override
		protected void assertMatches(InstantiationManipulation m) {
			EntityReference ref = (EntityReference) m.getEntity();
			BtAssertions.assertThat(ref.getTypeSignature()).isEqualTo(ownerType.getTypeSignature());
		}
	}

	public static class ChangeValueDescriptor extends ManipulationDescriptor<ChangeValueManipulation> {
		private final EntityType<?> ownerType;
		private final String propertyName;
		private final Object newValue;

		public ChangeValueDescriptor(EntityType<?> ownerType, String propertyName, Object newValue) {
			super(ChangeValueManipulation.T);
			this.ownerType = ownerType;
			this.propertyName = propertyName;
			this.newValue = newValue;
		}

		@Override
		protected void assertMatches(ChangeValueManipulation m) {
			EntityProperty owner = (EntityProperty) m.getOwner();
			BtAssertions.assertThat(owner.getReference().getTypeSignature()).isEqualTo(ownerType.getTypeSignature());
			BtAssertions.assertThat(owner.getPropertyName()).isEqualTo(propertyName);
			assertValueEqual(m.getNewValue(), newValue);
		}
	}

	public static class AddDescriptor extends ManipulationDescriptor<AddManipulation> {
		private final EntityType<?> ownerType;
		private final String propertyName;
		private final Map<?, ?> itemsToAdd;

		public AddDescriptor(EntityType<?> ownerType, String propertyName, Map<?, ?> itemsToAdd) {
			super(AddManipulation.T);
			this.ownerType = ownerType;
			this.propertyName = propertyName;
			this.itemsToAdd = itemsToAdd;
		}

		@Override
		protected void assertMatches(AddManipulation m) {
			EntityProperty owner = (EntityProperty) m.getOwner();
			BtAssertions.assertThat(owner.getReference().getTypeSignature()).isEqualTo(ownerType.getTypeSignature());
			BtAssertions.assertThat(owner.getPropertyName()).isEqualTo(propertyName);
			assertValueEqual(m.getItemsToAdd(), itemsToAdd);
		}
	}
}
