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
package com.braintribe.model.processing.smart.query.planner.structure;

import static com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools.isInstantiable;
import static com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools.newConcurrentMap;
import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;
import static java.util.Collections.emptySet;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.index.ConcurrentCachedIndex;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/**
 * 
 */
public class ModelHierarchyExpert {

	private final Map<GmEntityType, Set<GmEntityType>> directSubTypes = newMap();
	private final Map<GmEntityType, Set<GmEntityType>> allSuperTypes = newConcurrentMap();

	public ModelHierarchyExpert(ModelOracle modelOracle) {
		indexHierarchy(modelOracle);
	}

	// I know I could do it without the "visited" set, but just in case the configuration is not 100% OK.
	private void indexHierarchy(ModelOracle modelOracle) {
		Set<GmEntityType> visited = newSet();

		Stream<GmEntityType> gmEntityTypesStream = modelOracle.getTypes().onlyEntities().asGmTypes();
		for (GmEntityType gmEntityType : (Iterable<GmEntityType>) gmEntityTypesStream::iterator)
			indexHierarchy(gmEntityType, visited);
	}

	private void indexHierarchy(GmEntityType gmEntityType, Set<GmEntityType> visited) {
		if (visited.contains(gmEntityType))
			return;

		visited.add(gmEntityType);

		for (GmEntityType superType : nullSafe(gmEntityType.getSuperTypes())) {
			acquireSet(directSubTypes, superType).add(gmEntityType);

			indexHierarchy(superType, visited);
		}
	}

	/**
	 * @return sub-types, according to {@link GmMetaModel} provided via constructor. Note that an entity is not
	 *         considered it's own sub-type.
	 */
	public Set<GmEntityType> getDirectSubTypes(GmEntityType entityType) {
		return nullSafe(directSubTypes.get(entityType));
	}

	/** This works with any types, even if they are not in the meta-model which was used for initialization. */
	public boolean isFirstAssignableFromSecond(GmEntityType et1, GmEntityType et2) {
		return et1 == et2 || acquireSuperTypes(et2).contains(et1);
	}

	private Set<GmEntityType> acquireSuperTypes(GmEntityType et) {
		Set<GmEntityType> result = allSuperTypes.get(et);

		if (result == null) {
			result = newSet();

			for (GmEntityType superType : nullSafe(et.getSuperTypes())) {
				result.add(superType);
				result.addAll(acquireSuperTypes(superType));
			}

			if (result.isEmpty())
				result = emptySet();

			allSuperTypes.put(et, result);
		}

		return result;
	}

	private final RootedHierarchyIndex rootedHierarchyIndex = new RootedHierarchyIndex();

	/**
	 * Returns hierarchy rooted at given smart type, containing only it's instantiable sub-types. Non-instantiable
	 * sub-types are ignored, and it's instantiable sub-types are represented as sub-types of the abstract entity super
	 * types.
	 */
	public EntityHierarchyNode resolveHierarchyRootedAt(GmEntityType smartType) {
		return rootedHierarchyIndex.acquireFor(smartType);
	}

	class RootedHierarchyIndex extends ConcurrentCachedIndex<GmEntityType, EntityHierarchyNode> {

		@Override
		protected EntityHierarchyNode provideValueFor(GmEntityType rootType) {
			EntityHierarchyNode rootNode = new EntityHierarchyNode(rootType, null);
			processInstantiableSubTypes(rootNode, rootType);

			return rootNode;
		}

		private void processInstantiableSubTypes(EntityHierarchyNode entityNode, GmEntityType entityType) {
			for (GmEntityType subType : nullSafe(getDirectSubTypes(entityType))) {
				if (isInstantiable(subType)) {
					EntityHierarchyNode subNode = new EntityHierarchyNode(subType, entityNode);
					entityNode.appendSubNode(subNode);

					processInstantiableSubTypes(subNode, subType);

				} else {
					processInstantiableSubTypes(entityNode, subType);
				}
			}
		}
	}

}
