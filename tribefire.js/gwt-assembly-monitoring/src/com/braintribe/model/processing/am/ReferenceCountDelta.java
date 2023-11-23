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
package com.braintribe.model.processing.am;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.pr.criteria.CriterionType;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityReferencesVisitor;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.StandardTraversingContext;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TypeCode;

public class ReferenceCountDelta {
	private static BaseType baseType = GMF.getTypeReflection().getBaseType();

	protected final ReferenceManager referenceManager;
	protected final GenericEntity rootEntity;
	protected final boolean isAbsenceResolvable;

	public ReferenceCountDelta(ReferenceManager referenceManager, GenericEntity rootEntity, boolean isAbsenceResolvable) {
		this.referenceManager = referenceManager;
		this.rootEntity = rootEntity;
		this.isAbsenceResolvable = isAbsenceResolvable;
	}

	public void applyReferenceMigration(PropertyManipulation manipulation) {
		ReferenceResultant referenceResultant = prepareReferenceResultant(manipulation);

		// apply adds
		for (GenericEntity entity: referenceResultant.adds.getEntities()) {
			SimpleReferenceTracker counter = new SimpleReferenceTracker();
			Matcher matcher = new StopAtKnownEntitiesMatcher();
			StandardTraversingContext traversingContext = new AssemblyMonitoring.AssemblyMonitoringTraversingContext(isAbsenceResolvable);
			traversingContext.setVisitMatchInclusive(true);
			traversingContext.setMatcher(matcher);
			traversingContext.setTraversingVisitor(new RefereeTrackingEntityReferenceVisitor(counter));

			baseType.traverse(traversingContext, entity);

			apply(counter, false);

			referenceManager.addReference(referenceResultant.ownerEntity, entity);
		}

		// apply removes
		for (Entry<GenericEntity, Counter> entry: referenceResultant.removes.getReferenceMap().entrySet()) {
			GenericEntity entityToRemove = entry.getKey();
			Counter counter = entry.getValue();

			referenceManager.removeReference(referenceResultant.ownerEntity, entityToRemove, counter.count);
		}

		// collect the garbage
		new GarbageCollector(this, referenceResultant).gc();
	}

	/**
	 * For given <tt>ownerEntity</tt> (the manipulation owner) we store the add/remove information. It may happen, that
	 * with one manipulation we both add and remove a reference to the exact same entity (see example below), so the
	 * result returned from this method "neutralizes" such references with each other. (i.e. the resulting multi-sets
	 * for adds/removes are disjoint, but in effect equivalent to the full information from the manipulation).
	 * <p>
	 * Example: Say we have map: [1->1, 2->2], and do map.putAll([1->2, 2->1]). We have both added and removed
	 * references to 1 and 2 (e.g. 1 was a value mapped by key 1, but the value is now 2, so one reference on 1 was
	 * removed, and a reference on 2 was added).
	 */
	private ReferenceResultant prepareReferenceResultant(PropertyManipulation manipulation) {
		ReferenceResultant result = new ReferenceResultant();

		result.ownerEntity = ((LocalEntityProperty) manipulation.getOwner()).getEntity();

		/* What is going on here?
		 * 
		 * The main idea is, that the values to remove are always present in the inverse manipulation, and values to add
		 * in the normal manipulation, as long as we only check the ChangeValue/Insert manipulations. Let's call them
		 * "positive" manipulations for now.
		 * 
		 * E.g. for InsertToCollectionManipulation this is straight forward: it's inverse (RemoveFromCollection) is not
		 * "positive", so we ignore it, but this manipulation is, so we add it's value to the "adds" collection. (Or
		 * remove it from the "removes" collection, if it was already there). On the other hand, if we look at
		 * RemoveFromCollectionManipulation, this manipulation is not positive, so it is ignored, but it's inverse
		 * (InsertToCollection) is, so we add the value of the insert to the "removes" collection (or remove it from
		 * "adds" if it was already there).
		 * 
		 * The most tricky is a BulkInsertToCollection for a Map, because the inverse may be a combination of BulkRemove
		 * plus BulkInsert, but even in that case, the logic of the following code works (think about it!).
		 * 
		 * Also note that RemoveManipulation is created even if no entity is removed locally, due to possible effects on
		 * the server side, when committed. In such case, the inverse manipulation would be VoidManipulation, so the
		 * following call would have no effect (just like we want). */
		addReferences(manipulation.getInverseManipulation(), result.removes, result.adds);
		addReferences(manipulation, result.adds, result.removes);

		return result;
	}

	private void addReferences(Manipulation manipulation, SimpleReferenceCounter set, SimpleReferenceCounter inverseSet) {
		if (manipulation instanceof CompoundManipulation) {
			List<Manipulation> manipulations = ((CompoundManipulation) manipulation).getCompoundManipulationList();

			for (Manipulation childManipulation: nullSafe(manipulations)) {
				addReferences(childManipulation, set, inverseSet);
			}

		} else if (manipulation instanceof PropertyManipulation) {
			PropertyManipulation propertyManipulation = (PropertyManipulation) manipulation;

			Object value = getMigrationRelevantValue(propertyManipulation);

			if (value instanceof Collection) {
				for (Object o: (Collection<?>) value) {
					addValue(o, set, inverseSet);
				}

			} else if (value instanceof Map) {
				for (Map.Entry<?, ?> e: ((Map<?, ?>) value).entrySet()) {
					addValue(e.getKey(), set, inverseSet);
					addValue(e.getValue(), set, inverseSet);
				}

			} else {
				addValue(value, set, inverseSet);
			}
		}
	}

	private void addValue(Object o, SimpleReferenceCounter set, SimpleReferenceCounter inverseSet) {
		if (o instanceof GenericEntity) {
			GenericEntity entity = (GenericEntity) o;
			if (inverseSet.hasReference(entity)) {
				inverseSet.removeReference(entity);
			} else {
				set.addReference(entity);
			}
		}
	}

	private Object getMigrationRelevantValue(PropertyManipulation manipulation) {
		switch (manipulation.manipulationType()) {
			case CHANGE_VALUE:
				return ((ChangeValueManipulation) manipulation).getNewValue();
			case ADD:
				if (isMap(manipulation)) {
					return ((AddManipulation) manipulation).getItemsToAdd();
				} else {
					return ((AddManipulation) manipulation).getItemsToAdd().values();
				}
			default:
				return null;
		}
	}

	private boolean isMap(PropertyManipulation propertyManipulation) {
		LocalEntityProperty lep = (LocalEntityProperty) propertyManipulation.getOwner();
		GenericModelType propertyType = lep.getEntity().entityType().getProperty(lep.getPropertyName()).getType();

		return propertyType.getTypeCode() == TypeCode.mapType;
	}

	static class RefereeTrackingEntityReferenceVisitor extends EntityReferencesVisitor {
		private final ReferenceTracker referenceTracker;

		public RefereeTrackingEntityReferenceVisitor(ReferenceTracker referenceTracker) {
			this.referenceTracker = referenceTracker;
		}

		@Override
		protected void visitEntityReference(GenericEntity entity, TraversingContext traversingContext) {
			if (traversingContext.getCurrentCriterionType() != CriterionType.ROOT) {
				referenceTracker.addReference(AssemblyMonitoring.getReferee(traversingContext, null), entity);
			}
		}
	}

	private class StopAtKnownEntitiesMatcher implements Matcher {
		@Override
		public boolean matches(TraversingContext traversingContext) {
			switch (traversingContext.getCurrentCriterionType()) {
				case ROOT:
				case PROPERTY:
				case MAP_KEY:
				case MAP_VALUE:
				case SET_ELEMENT:
				case LIST_ELEMENT:
					Object value = traversingContext.getObjectStack().peek();
					if (value instanceof GenericEntity) {
						return referenceManager.hasReference((GenericEntity) value);
					}
					return false;
				default:
					return false;
			}
		}
	}

	protected void apply(SimpleReferenceTracker counter, boolean remove) {
		for (Map.Entry<GenericEntity, RefereeData> entry: counter.getReferenceMap().entrySet()) {
			GenericEntity entity = entry.getKey();
			RefereeData refereeData = entry.getValue();

			if (remove) {
				referenceManager.removeReferences(entity, refereeData);
			} else {
				referenceManager.addReferences(entity, refereeData);
			}
		}
	}

	private static <E> Collection<E> nullSafe(Collection<E> collection) {
		return collection != null ? collection : Collections.<E> emptySet();
	}

}

class ReferenceResultant {
	GenericEntity ownerEntity;

	SimpleReferenceCounter adds = new SimpleReferenceCounter();
	SimpleReferenceCounter removes = new SimpleReferenceCounter();
}
