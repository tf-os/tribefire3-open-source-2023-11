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
package com.braintribe.utils.collection.api;

/**
 * Interface for the disjoint-set data structure.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Disjoint-set_data_structure">wikipedia</a>
 * 
 * @author peter.gazdik
 */
public interface DisjointSets<E> {

	boolean isSameSet(E e1, E e2);

	void union(E e1, E e2);

	/**
	 * Was thinking whether this is better than returning a representative from the set, but this is definitely cheaper.
	 */
	E findSet(E e);

}
