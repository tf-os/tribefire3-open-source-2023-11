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
package com.braintribe.utils.lcd.graph;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.braintribe.utils.lcd.graph.StreamBuilder.StreamBuilder2;

/**
 * @author peter.gazdik
 */
public class StreamBuilderImpl<N> implements StreamBuilder<N>, StreamBuilder2<N> {

	private final N root;

	private Function<N, ? extends Stream<? extends N>> neighborFunction;
	private boolean distinct;
	private TraversalOrder order = TraversalOrder.postOrder;

	public StreamBuilderImpl(N root) {
		this.root = root;
	}

	@Override
	public StreamBuilder2<N> withNeighbors(Function<N, ? extends Collection<? extends N>> neighborFunction) {
		this.neighborFunction = n -> toStream(neighborFunction.apply(n));
		return this;
	}

	private Stream<? extends N> toStream(Collection<? extends N> c) {
		return c == null ? Stream.empty() : c.stream();
	}

	@Override
	public StreamBuilder2<N> withNeighborStream(Function<N, ? extends Stream<? extends N>> neighborFunction) {
		this.neighborFunction = neighborFunction;
		return this;
	}

	@Override
	public StreamBuilder2<N> distinct() {
		this.distinct = true;
		return this;
	}

	@Override
	public StreamBuilder2<N> withOrder(TraversalOrder order) {
		this.order = order;
		return this;
	}

	@Override
	public Stream<N> please() {
		return modelsStream(root, distinct ? newSet() : null);
	}

	private Stream<N> modelsStream(N node, Set<Object> visited) {
		if (node == null) {
			return Stream.empty();
		}

		if (distinct && !visited.add(node)) {
			return Stream.empty();
		}

		Stream<N> result = Stream.of(node);

		Stream<? extends N> neighbors = neighborFunction.apply(node);
		if (neighbors == null) {
			return result;
		}

		Stream<N> neighborStream = neighbors.flatMap(n -> modelsStream(n, visited));
		if (order == TraversalOrder.preOrder) {
			return Stream.concat(result, neighborStream);
		} else {
			return Stream.concat(neighborStream, result);
		}
	}

}
