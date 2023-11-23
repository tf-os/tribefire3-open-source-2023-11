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
package com.braintribe.collections;

import java.util.Iterator;

public class GrowingIterable<E> implements Iterable<E> {
	private final Node firstNode = new Node(null);
	private Node lastNode = firstNode;

	@Override
	public Iterator<E> iterator() {
		return new NodeIterator(firstNode);
	}

	public void add(E element) {
		Node newNode = new Node(element);

		lastNode.next = newNode;
		lastNode = newNode;
	}

	private class Node {
		final E element;
		Node next;

		public Node(E element) {
			this.element = element;
		}
	}

	private class NodeIterator implements Iterator<E> {
		private GrowingIterable<E>.Node node;

		public NodeIterator(Node node) {
			this.node = node;
		}

		@Override
		public boolean hasNext() {
			return node.next != null;
		}

		@Override
		public E next() {
			if (!hasNext()) {
				throw new IllegalStateException("Iterator does not have further elements.");
			}

			node = node.next;
			return node.element;
		}
	}

}
