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
package com.braintribe.utils.collection.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.braintribe.utils.collection.api.DisjointSets;

/**
 * Just what it says.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Disjoint-set_data_structure#Disjoint-set_forests">wikipedia</a>
 * 
 * @author peter.gazdik
 */
public class DisjointSetForest<E> implements DisjointSets<E> {

	private final Map<E, SetNode<E>> elementToItsSet;

	public DisjointSetForest() {
		this.elementToItsSet = new HashMap<E, SetNode<E>>();
	}

	public DisjointSetForest(Comparator<? super E> comparator) {
		this.elementToItsSet = new TreeMap<E, SetNode<E>>(comparator);
	}

	private static class SetNode<E> {
		SetNode<E> parent;
		int rank;
		E element;

		private SetNode(E element) {
			this.element = element;
			this.parent = this;
			this.rank = 0;
		}
	}

	@Override
	public boolean isSameSet(E e1, E e2) {
		return findSet(e1) == findSet(e2);
	}

	@Override
	public void union(E e1, E e2) {
		SetNode<E> set1 = findSetNode(e1);
		SetNode<E> set2 = findSetNode(e2);

		if (set1 == set2) {
			return;
		}

		if (set1.rank < set2.rank) {
			set1.parent = set2;

		} else if (set1.rank > set2.rank) {
			set2.parent = set1;

		} else {
			set2.parent = set1;
			set1.rank++;
		}
	}

	@Override
	public E findSet(E e) {
		return findSetNode(e).element;
	}

	public SetNode<E> findSetNode(E e) {
		SetNode<E> elementNode = elementToItsSet.get(e);
		if (elementNode == null) {
			elementNode = new SetNode<E>(e);
			elementToItsSet.put(e, elementNode);
		}

		return findSetNode(elementNode);
	}

	private SetNode<E> findSetNode(SetNode<E> set) {
		while (set.parent != set) {
			set = set.parent;
		}

		return set;
	}

}
