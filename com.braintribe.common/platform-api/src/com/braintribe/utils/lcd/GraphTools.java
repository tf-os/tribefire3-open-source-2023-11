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
package com.braintribe.utils.lcd;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.braintribe.utils.lcd.graph.StreamBuilder;
import com.braintribe.utils.lcd.graph.StreamBuilderImpl;

/**
 * This class provides utility methods related to Graphs.
 *
 */
public class GraphTools {

	/** returns a {@link StreamBuilder} with given <code>root</code>. */
	public static <N> StreamBuilder<N> stream(N root) {
		return new StreamBuilderImpl<>(root);
	}

	/**
	 * Finds all the <i>independent</i> subsets of nodes in the passed set of <code>nodes</code> and returns them as a set of sets of nodes. Nodes
	 * <i>depend</i> on each other, if there is a direct or indirect relation (or <i>path</i>) between them (regardless of the direction). If the
	 * passed set of <code>nodes</code> is <code>null</code> or empty, an empty set will be returned.
	 */
	public static <T> Set<Set<T>> findIndependentSubsets(final Set<T> nodes, final ReachablesFinder<T> reachableFinder) {

		Arguments.notNullWithNames("nodes", nodes, "reachableFinder", reachableFinder);

		final HashSet<T> nodesPool = new HashSet<>(nodes);
		final Set<Set<T>> independentSubsets = new HashSet<>();
		final Set<T> traversedNodes = new HashSet<>();

		while (!nodesPool.isEmpty()) {

			final T node = nodesPool.iterator().next();
			final Set<T> reachableNodes = reachableFinder.findReachables(node, traversedNodes);

			// Reachable nodes need to be in the same node space
			final List<T> nodesFromSameNodeSpace = CollectionTools.getIntersection(reachableNodes, nodes);

			// The sets that are intersecting with this set (look the loop below), will be removed from the
			// independentSubsets and will unite with the subsetNodesGlue set. Hence the name!
			final Set<T> subsetNodesGlue = new HashSet<>(nodesFromSameNodeSpace);

			final Iterator<Set<T>> independentSubsetsIterator = independentSubsets.iterator();
			boolean haveIntersection = CollectionTools.containsAny(traversedNodes, nodesFromSameNodeSpace);
			traversedNodes.addAll(nodesFromSameNodeSpace);

			while (haveIntersection && independentSubsetsIterator.hasNext()) {

				final Set<T> independentSubset = independentSubsetsIterator.next();
				if (CollectionTools.containsAny(independentSubset, nodesFromSameNodeSpace)) {
					independentSubsetsIterator.remove();
					subsetNodesGlue.addAll(independentSubset);
				}
			}

			independentSubsets.add(new HashSet<>(subsetNodesGlue));
			nodesPool.removeAll(nodesFromSameNodeSpace);

		}

		return independentSubsets;
	}

	/**
	 * {@link #findReachables(Object, Set) Finds} a set of (reachable) nodes.
	 *
	 * @param <T>
	 *            type of the nodes
	 */
	public interface ReachablesFinder<T> {

		/**
		 * Finds a set of (reachable) nodes from a node. This is similar to finding the reachable nodes in graph starting from a node. Some paths can
		 * be avoided while the finder traverse the graph.
		 *
		 * @param rootNode
		 *            the node from where the finder begins to traverse
		 * @param nodesWhereToStopFurtherTraversing
		 *            the nodes that should not be included in the traversing
		 * @return the set of reachable nodes (rootNode included)
		 */
		public Set<T> findReachables(T rootNode, Set<T> nodesWhereToStopFurtherTraversing);

	}

}
