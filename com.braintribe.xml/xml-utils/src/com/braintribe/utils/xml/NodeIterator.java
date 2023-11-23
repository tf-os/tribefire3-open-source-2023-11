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
package com.braintribe.utils.xml;

import java.util.Iterator;
import java.util.function.Predicate;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class NodeIterator<T extends Node> implements Iterator<T>, Iterable<T> {
	private Node currentNode;
	private Predicate<Node> filter;
	private Node nodeToRemove;
	
	public NodeIterator(Element parent) {
		this(parent, (Predicate<Node>)null);
	}
	
	public NodeIterator(Element parent, Predicate<Node> filter) {
		this.filter = filter;
		this.currentNode = getNextSibling(parent.getFirstChild());
	}
	
	private Node getNextSibling(Node node) {
		while (node != null) {
			if (filter == null || filter.test(node))
				return node;
			
			node = node.getNextSibling();
		}

		return null;
	}
	
	@Override
	public boolean hasNext() {
		return currentNode != null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T next() {
		nodeToRemove = currentNode;
		currentNode = getNextSibling(currentNode.getNextSibling());
		return (T)nodeToRemove;
	}
	
	@Override
	public void remove() {
		nodeToRemove.getParentNode().removeChild(nodeToRemove);
	}
	
	@Override
	public Iterator<T> iterator() {
		return this;
	}
}
