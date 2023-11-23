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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityReferencesVisitor;
import com.braintribe.model.generic.reflection.StandardTraversingContext;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.processing.am.ReferenceCountDelta.RefereeTrackingEntityReferenceVisitor;

/**
 * 
 */
class GarbageCollector {

	private final ReferenceCountDelta delta;

	private final Set<GenericEntity> removes;
	private final ReferenceManager referenceManager;
	private final GenericEntity rootEntity;

	private static BaseType baseType = GMF.getTypeReflection().getBaseType();

	/* If set to true, the root garbage-collection is always performed (i.e. starting from the root entity to check all
	 * the reachable ones and then collecting the (unreachable) rest).
	 * 
	 * this has two purposes: 1 - to make writing tests easier; 2 - if the optimized case (false) did not work/had bug,
	 * we may try to switch to simpler less performant solution */
	static boolean rootGcOnly = false;

	public GarbageCollector(ReferenceCountDelta delta, ReferenceResultant referenceResultant) {
		this.delta = delta;
		this.removes = new HashSet<GenericEntity>(referenceResultant.removes.getEntities());
		this.referenceManager = delta.referenceManager;
		this.rootEntity = delta.rootEntity;
	}

	public void gc() {
		if (removes.isEmpty()) {
			return;
		}

		if (rootGcOnly) {
			rootBasedGc();
			return;
		}

		List<Pass1Entry> pass1Entries = new ArrayList<Pass1Entry>();

		for (GenericEntity entity: removes) {
			// remove pass 1
			SimpleReferenceCounter pass1Counter = new SimpleReferenceCounter();
			SimpleCountingEntityReferencesVisitor visitor = new SimpleCountingEntityReferencesVisitor(pass1Counter);

			StandardTraversingContext traversingContext = new StandardTraversingContext();
			traversingContext.setVisitMatchInclusive(true);
			traversingContext.setTraversingVisitor(visitor);
			baseType.traverse(traversingContext, entity);

			if (pass1Counter.hasReference(rootEntity)) {
				rootBasedGc();
				return;
			}

			pass1Entries.add(new Pass1Entry(entity, pass1Counter, visitor.entities));
		}

		Set<GenericEntity> processedEntities = new HashSet<GenericEntity>();

		Collections.sort(pass1Entries);

		for (Pass1Entry pass1Entry: pass1Entries) {
			if (processedEntities.contains(pass1Entry.rootEntity)) {
				continue;
			}

			Set<GenericEntity> directlyReachableEntities = findDirectlyReachableEntities(pass1Entry);
			Set<GenericEntity> reachableEntities = findReachableEntities(directlyReachableEntities);

			SimpleReferenceTracker pass2Counter = new SimpleReferenceTracker();
			Matcher matcher = new StopAtReferencedEntitiesMatcher(reachableEntities);
			StandardTraversingContext traversingContext = new StandardTraversingContext();
			traversingContext.setVisitMatchInclusive(true);
			traversingContext.setMatcher(matcher);
			traversingContext.setTraversingVisitor(new RefereeTrackingEntityReferenceVisitor(pass2Counter));
			baseType.traverse(traversingContext, pass1Entry.rootEntity);

			delta.apply(pass2Counter, true);

			processedEntities.addAll(pass1Entry.reachableEntities);
		}
	}

	private Set<GenericEntity> findReachableEntities(Set<GenericEntity> directlyReachableEntities) {
		Set<GenericEntity> reachedEntities = new HashSet<GenericEntity>();

		for (GenericEntity entity: directlyReachableEntities) {
			reachedEntities.addAll(findReachableEntities(entity, new StopAtReferencedEntitiesMatcher(reachedEntities)));
		}

		return reachedEntities;
	}

	private Set<GenericEntity> findDirectlyReachableEntities(Pass1Entry pass1Entry) {
		Set<GenericEntity> reachableEntities = new HashSet<GenericEntity>();

		for (Map.Entry<GenericEntity, Counter> entry: pass1Entry.pass1Counter.getReferenceMap().entrySet()) {
			Counter counter = entry.getValue();
			GenericEntity entity = entry.getKey();

			/* true iff there exists a reference to entity which is not reachable from the "pass1Entry.rootEntity"
			 * (because the total number of references is greater than the number of references discovered by traversing
			 * from "pass1Entry.rootEntity"), i.e. we know this entity is still reachable from the global root entity
			 * 
			 * If you think that it is not really true, because this entity might be referenced by other entity, which
			 * is also eligible for garbage-collection, then note that this is not possible As you can see, the
			 * Pass1Entry class is comparable, and we sort such entries from the one covering the most entities to the
			 * ones with smaller coverage. So if we had a situation described, we would have already executed the
			 * garbage-collection of this block before, when dealing with the block that references this one. */
			boolean hasExternalReference = referenceManager.getReferenceCount(entity) > counter.count;

			if (hasExternalReference)
				reachableEntities.add(entity);

		}
		return reachableEntities;
	}

	static class Pass1Entry implements Comparable<Pass1Entry> {
		GenericEntity rootEntity;
		SimpleReferenceCounter pass1Counter;
		Set<GenericEntity> reachableEntities;

		public Pass1Entry(GenericEntity rootEntity, SimpleReferenceCounter pass1Counter, Set<GenericEntity> reachableEntities) {
			this.rootEntity = rootEntity;
			this.pass1Counter = pass1Counter;
			this.reachableEntities = reachableEntities;
		}

		@Override
		public int compareTo(Pass1Entry o) {
			return o.pass1Counter.getEntities().size() - pass1Counter.getEntities().size();
		}

	}

	private void rootBasedGc() {
		Set<GenericEntity> ignoredEntities = findEntitiesReachableFromRoot();

		removes.removeAll(ignoredEntities);

		while (!removes.isEmpty()) {
			GenericEntity entity = removes.iterator().next();

			SimpleReferenceTracker pass2Counter = new SimpleReferenceTracker();
			Matcher matcher = new StopAtReferencedEntitiesMatcher(ignoredEntities);
			StandardTraversingContext traversingContext = new StandardTraversingContext();
			traversingContext.setVisitMatchInclusive(true);
			traversingContext.setMatcher(matcher);
			traversingContext.setTraversingVisitor(new RefereeTrackingEntityReferenceVisitor(pass2Counter));
			baseType.traverse(traversingContext, entity);

			delta.apply(pass2Counter, true);

			Set<GenericEntity> entities = pass2Counter.getEntities();
			removes.removeAll(entities);
			removes.remove(entity);
			ignoredEntities.addAll(entities);
			ignoredEntities.add(entity);
		}
	}

	private Set<GenericEntity> findEntitiesReachableFromRoot() {
		return findReachableEntities(rootEntity, null);
	}

	private Set<GenericEntity> findReachableEntities(GenericEntity entity, Matcher matcher) {
		SimpleRecordingEntityReferencesVisitor entitiesRecorder = new SimpleRecordingEntityReferencesVisitor();

		StandardTraversingContext traversingContext = new StandardTraversingContext();
		traversingContext.setMatcher(matcher);
		traversingContext.setTraversingVisitor(entitiesRecorder);
		baseType.traverse(traversingContext, entity);

		return entitiesRecorder.entities;
	}

	private class SimpleRecordingEntityReferencesVisitor extends EntityReferencesVisitor {
		Set<GenericEntity> entities = new HashSet<GenericEntity>();

		@Override
		protected void visitEntityReference(GenericEntity entity, TraversingContext traversingContext) {
			entities.add(entity);
		}
	}

	private class SimpleCountingEntityReferencesVisitor extends SimpleRecordingEntityReferencesVisitor {
		private final ReferenceCounter referenceCounter;

		public SimpleCountingEntityReferencesVisitor(ReferenceCounter referenceCounter) {
			this.referenceCounter = referenceCounter;
		}

		@Override
		protected void visitEntityReference(GenericEntity entity, TraversingContext traversingContext) {
			referenceCounter.addReference(entity);

			super.visitEntityReference(entity, traversingContext);
		}
	}

	private class StopAtReferencedEntitiesMatcher implements Matcher {
		private final Set<GenericEntity> stopEntities;

		public StopAtReferencedEntitiesMatcher(Set<GenericEntity> stopEntities) {
			this.stopEntities = stopEntities;
		}

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
					return stopEntities.contains(value);
				default:
					return false;
			}
		}
	}

}
