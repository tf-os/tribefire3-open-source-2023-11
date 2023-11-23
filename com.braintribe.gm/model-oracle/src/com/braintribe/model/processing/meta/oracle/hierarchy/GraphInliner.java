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
package com.braintribe.model.processing.meta.oracle.hierarchy;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This tool is used to sort nodes of a given directed acyclic graph. This graph is given by the leaf node and a function to retrieve neighboring
 * nodes, and the resulting order guarantees, that if one node is further from the leaf than another, it will also be on a higher position in the
 * resulting list.
 * 
 * The distance in this case is considered in terms of longest possible path from given node to the leaf, thus the result guarantees, that every node
 * "n1" is on a higher position in the resulting list than any other node "n2" if there is path from "n1" to leaf going through "n2". This property is
 * important when considering e.g. model dependencies, when we want to be sure that every dependency is always further in the list than the depender.
 * 
 * The algorithm goes as follows: first it does a regular breadth-first traversing (with avoiding visiting the same node more than once), and creates
 * a list of nodes based on this order. During this phase, it also remembers the longest possible path from every node to the leaf. Then, the list is
 * sorted in a stable way according to the distance to the leaf, thus creating the final order. This algorithm implies, that a model dependency
 * structure with no multiple paths from one node to another, the resulting order is the breadth-first order. In other words, this algorithm tries to
 * respect the order given by the neighboring function as much as possible, i.e. as long as it does not conflict with the property of the result
 * mentioned in the first paragraph.
 * 
 * @author peter.gazdik
 */
public class GraphInliner<N> {

	/**
	 * This is the method to access the functionality of this class. See {@link GraphInliner class description}.
	 */
	public static <N> InlinedGraph<N> inline(N leafNode, Function<N, ? extends Collection<? extends N>> neighborFunction) {
		return new GraphInliner<>(leafNode, neighborFunction).inline();
	}

	public static <N> GraphInliner<N> with(N leafNode, Function<N, ? extends Collection<? extends N>> neighborFunction) {
		return new GraphInliner<>(leafNode, neighborFunction);
	}

	private final N leafNode;
	private final Function<N, ? extends Collection<? extends N>> neighborFunction;

	private Consumer<? super N> errorHandler = this::throwNullNeighborException;
	private boolean ignoreNulls;

	private final List<N> list = newList();
	private final Map<N, Integer> distance = newMap();

	GraphInliner(N leafNode, Function<N, ? extends Collection<? extends N>> neighborFunction) {
		this.leafNode = leafNode;
		this.neighborFunction = neighborFunction;
	}

	public GraphInliner<N> ignoreNulls(boolean ignoreNulls) {
		this.ignoreNulls = ignoreNulls;
		return this;
	}

	/**
	 * In case a we encounter a null neighbor and we are not {@link #ignoreNulls ignoring nulls}, we can set a default handler for such a situation
	 * here. The default error handler throws an exception, which informs about the node who's neighbor was null.
	 */
	public GraphInliner<N> nullNeighborErrorHandler(Consumer<? super N> errorHandler) {
		this.errorHandler = requireNonNull(errorHandler, "Error handler cannot be null!");
		return this;
	}

	public InlinedGraph<N> inline() {
		visit(leafNode, 0);
		sortResult();

		return new InlinedGraph<>(list, index(), distance);
	}

	private Map<N, Integer> index() {
		Map<N, Integer> result = newMap();

		int index = 0;
		for (N n : list)
			result.put(n, index++);

		return result;
	}

	private void visit(N node, Integer index) {
		Integer nextIndex = index + 1;

		Integer oldIndex = distance.get(node);
		if (oldIndex == null) {
			list.add(node);
			distance.put(node, index);

		} else if (oldIndex < index) {
			distance.put(node, index);
		}

		for (N neighbor : nullSafe(neighborFunction.apply(node))) {
			if (neighbor != null)
				visit(neighbor, nextIndex);
			else if (!ignoreNulls)
				errorHandler.accept(node);
		}
	}

	private void throwNullNeighborException(N node) {
		throw new IllegalArgumentException("Null node encountered in a graph, but ignoring nulls was not configured. Neighbor node: " + node);
	}

	private void sortResult() {
		list.sort(Comparator.comparing(distance::get));
	}

}
