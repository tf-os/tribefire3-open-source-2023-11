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

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Experimental, use at your own risk.
 *
 * @author peter.gazdik
 */
public interface StreamBuilder<N> {

	StreamBuilder2<N> withNeighbors(Function<N, ? extends Collection<? extends N>> neighborFunction);

	StreamBuilder2<N> withNeighborStream(Function<N, ? extends Stream<? extends N>> neighborFunction);

	/**
	 * This is intended as the second step of the builder, after the neighbor function was provided via {@link StreamBuilder}
	 */
	public static interface StreamBuilder2<N> {

		StreamBuilder2<N> distinct();

		/** Stream elements using given traversal order. Default is post-order. */
		StreamBuilder2<N> withOrder(TraversalOrder order);

		Stream<N> please();

	}

	public static enum TraversalOrder {
		// might implement breadth-first in the future
		preOrder,
		postOrder;
	}

}
