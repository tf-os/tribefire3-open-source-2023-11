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
import static com.braintribe.utils.lcd.CollectionTools2.acquireMap;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smart.meta.PolymorphicBaseEntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PolymorphicDerivateEntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PolymorphicEntityAssignment;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.DiscriminatedHierarchy;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.DiscriminatedHierarchyNode;

/**
 * @author peter.gazdik
 */
public class DiscriminatorHierarchyExpert {

	private final ModelExpert modelExpert;

	private final Map<IncrementalAccess, Map<GmEntityType, DiscriminatedHierarchy>> cachedHierarchies = newMap();
	private final Map<IncrementalAccess, NodeCache> cachedNodes = newMap();

	public DiscriminatorHierarchyExpert(ModelExpert modelExpert) {
		this.modelExpert = modelExpert;
	}

	public DiscriminatedHierarchy resolveDiscriminatedHierarchyRootedAt(GmEntityType smartType, IncrementalAccess access) {
		Map<GmEntityType, DiscriminatedHierarchy> cacheForEntity = acquireMap(cachedHierarchies, access);
		DiscriminatedHierarchy result = cacheForEntity.get(smartType);

		if (result == null) {
			result = newHierarchyFor(smartType, access);
			cacheForEntity.put(smartType, result);
		}

		return result;
	}

	private DiscriminatedHierarchy newHierarchyFor(GmEntityType smartType, IncrementalAccess access) {
		PolymorphicEntityAssignment pea = resolvePolymorphicAssignment(smartType, access);

		if (pea instanceof PolymorphicBaseEntityAssignment) {
			return newHierarchyFor(smartType, (PolymorphicBaseEntityAssignment) pea, pea, access);

		} else {
			PolymorphicDerivateEntityAssignment derived = (PolymorphicDerivateEntityAssignment) pea;
			return newHierarchyFor(smartType, derived.getBase(), pea, access);
		}
	}

	private DiscriminatedHierarchy newHierarchyFor(GmEntityType smartType, PolymorphicBaseEntityAssignment base, PolymorphicEntityAssignment pea,
			IncrementalAccess access) {

		NodeCache nodeCache = acquireNodeCache(access);

		List<DiscriminatedHierarchyNode> nodes = newList();

		/* Add node for given smartType, if instantiable */
		if (isInstantiable(smartType))
			nodes.add(nodeCache.acquireDiscriminatedHierarchyNode(smartType, pea));

		/* Add node for every instantiable sub-type (note that resolveHierarchyRootedAt has hierarchy consisting of instantiable types only) */
		EntityHierarchyNode staticHierarchy = modelExpert.resolveHierarchyRootedAt(smartType);
		addNodesForSubTypes(nodes, nodeCache, staticHierarchy);

		return new DiscriminatedHierarchy(smartType, base, nodes);
	}

	private void addNodesForSubTypes(List<DiscriminatedHierarchyNode> nodes, NodeCache nodeCache, EntityHierarchyNode staticHierarchy) {
		for (EntityHierarchyNode subNode: staticHierarchy.getSubNodes()) {
			nodes.add(nodeCache.acquireDiscriminatedHierarchyNode(subNode.getGmEntityType()));
			addNodesForSubTypes(nodes, nodeCache, subNode);
		}
	}

	private NodeCache acquireNodeCache(IncrementalAccess access) {
		NodeCache result = cachedNodes.get(access);
		if (result == null) {
			result = new NodeCache(access);
			cachedNodes.put(access, result);
		}
		return result;
	}

	private PolymorphicEntityAssignment resolvePolymorphicAssignment(GmEntityType smartType, IncrementalAccess access) {
		return (PolymorphicEntityAssignment) modelExpert.resolveEntityAssignment(smartType, access, null);
	}

	// #######################################
	// ## . . . . . . NodeCache . . . . . . ##
	// #######################################

	private class NodeCache {
		private final IncrementalAccess access;
		private final Map<GmEntityType, DiscriminatedHierarchyNode> typeToNode = newMap();

		protected NodeCache(IncrementalAccess access) {
			this.access = access;
		}

		protected DiscriminatedHierarchyNode acquireDiscriminatedHierarchyNode(GmEntityType smartType) {
			DiscriminatedHierarchyNode result = typeToNode.get(smartType);
			if (result != null)
				return result;

			return newDiscriminatedHierarchyNode(smartType, resolvePolymorphicAssignment(smartType, access));
		}

		protected DiscriminatedHierarchyNode acquireDiscriminatedHierarchyNode(GmEntityType smartType, PolymorphicEntityAssignment pea) {
			DiscriminatedHierarchyNode result = typeToNode.get(smartType);
			return result == null ? newDiscriminatedHierarchyNode(smartType, pea) : result;
		}

		private DiscriminatedHierarchyNode newDiscriminatedHierarchyNode(GmEntityType smartType, PolymorphicEntityAssignment pea) {
			DiscriminatedHierarchyNode result = new DiscriminatedHierarchyNode(smartType, pea);
			typeToNode.put(smartType, result);

			return result;
		}
	}

}
