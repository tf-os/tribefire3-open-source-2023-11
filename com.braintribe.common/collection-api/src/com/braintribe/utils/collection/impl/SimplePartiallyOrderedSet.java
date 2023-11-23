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

import static java.util.Objects.requireNonNull;

import java.util.AbstractCollection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Stream;

import com.braintribe.utils.collection.PartiallyOrderedSet;

/**
 * Simple graph-based {@link PartiallyOrderedSet} implementation.
 *
 * <h3>Thread safety</h3>
 * 
 * This implementations is NOT THREAD SAFE and if one wants to make it such, then the whole add-element-chain (see example above with calls of "with",
 * "before", "after" and "add") must happen as an atomic operation.
 *
 */
public class SimplePartiallyOrderedSet<E, P> extends AbstractCollection<E>
		implements PartiallyOrderedSet<E, P>, PartiallyOrderedSet.PosAdditionContext<E, P> {

	private final Map<P, ElementNode> positionToNode = new LinkedHashMap<>();
	private final Set<E> noPositionElements = new LinkedHashSet<>();

	private ElementNode withNode = null;
	private ElementNode withNodeModifications;

	private List<ElementNode> list;

	private class ElementNode {
		final P position;

		E element;
		boolean initialized = false;
		Set<ElementNode> befores = new HashSet<>();
		Set<ElementNode> afters = new HashSet<>();

		ElementNode(P p) {
			position = p;
		}

		@Override
		public String toString() {
			return "[" + element + "(@" + position + ")]";
		}

		public List<ElementNode> linkAfterNode(ElementNode afterNode) {
			afters.add(afterNode);
			afterNode.befores.add(this);

			return findNewCycle(afterNode);
		}

		// Finds cycle from this node starting with the last afterNode as the first successor.
		private List<ElementNode> findNewCycle(ElementNode afterNode) {
			Deque<ElementNode> pathToMe = findMe(Collections.singleton(afterNode), new HashSet<>(), new ArrayDeque<>());
			if (pathToMe == null)
				return null;

			List<ElementNode> cycle = new ArrayList<>();
			cycle.add(this);
			cycle.addAll(pathToMe);
			return cycle;
		}

		private Deque<ElementNode> findMe(Collection<ElementNode> nodes, Set<ElementNode> visitedNodes, Deque<ElementNode> path) {
			for (ElementNode node : nodes)
				if (visitedNodes.add(node)) {
					path.addLast(node);

					if (node == this)
						return path;

					Deque<ElementNode> result = findMe(node.afters, visitedNodes, path);
					if (result != null)
						return result;

					path.removeLast();
				}

			return null;
		}

	}

	@Override
	public Iterator<E> iterator() {
		return stream().iterator();
	}

	@Override
	public Stream<E> stream() {
		return Stream.concat( //
				nodeList().stream().map(node -> node.element), //
				noPositionElements.stream());
	}

	@Override
	public int size() {
		return nodeList().size() + noPositionElements.size();
	}

	@Override
	public boolean isEmpty() {
		return nodeList().isEmpty() && noPositionElements.isEmpty();
	}

	@Override
	public PosAdditionContext<E, P> with(P positionIdentifier) {
		if (withNode != null)
			throw new IllegalStateException("Cannot start adding element for identifirer '" + positionIdentifier
					+ "' as previously initiated adding for '" + withNode.position + "' is still in progress.");

		withNode = acquireNode(positionIdentifier);
		if (withNode.element != null)
			throw new IllegalArgumentException(
					"There is already a value associated with position " + positionIdentifier + ". Value: " + withNode.element);

		withNodeModifications = new ElementNode(positionIdentifier);

		return this;
	}

	@Override
	public PosAdditionContext<E, P> before(P positionIdentifier) {
		ElementNode afterNode = acquireNode(positionIdentifier);
		withNodeModifications.afters.add(afterNode);
		linkNodes(withNode, afterNode);

		return this;
	}

	@Override
	public PosAdditionContext<E, P> after(P positionIdentifier) {
		ElementNode beforeNode = acquireNode(positionIdentifier);
		withNodeModifications.befores.add(beforeNode);
		linkNodes(beforeNode, withNode);

		return this;
	}

	private void linkNodes(ElementNode beforeNode, ElementNode afterNode) {
		List<ElementNode> cycle = beforeNode.linkAfterNode(afterNode);
		if (cycle == null)
			return;

		P position = withNode.position;
		unlink(withNode, withNodeModifications.befores, withNodeModifications.afters);

		withNode = null;
		withNodeModifications = null;

		StringJoiner sj = new StringJoiner("->");
		cycle.stream().forEach(node -> sj.add(node.position.toString()));

		throw new IllegalArgumentException(
				"Cannot 'add' element with identifier '" + position + "', as the specified order leads to the following cycle: " + sj.toString());
	}

	private ElementNode acquireNode(P positionIdentifier) {
		//Using lambda instead of method reference due to a problem with GWT 2.8.0 super dev mode
		return positionToNode.computeIfAbsent(requireNonNull(positionIdentifier), t -> new ElementNode(t));
	}

	@Override
	public boolean add(E element) {
		if (withNode == null)
			return noPositionElements.add(element);

		withNode.element = element;
		withNode.initialized = true;
		withNode = null;
		clearNodeList();
		return true;
	}

	@Override
	public boolean contains(Object o) {
		return findNodeForElement(o) != null;
	}

	@Override
	public boolean containsPosition(P positionIdentifier) {
		return positionToNode.containsKey(positionIdentifier);
	}

	@Override
	public boolean containsElementOnPosition(P positionIdentifier) {
		ElementNode node = positionToNode.get(positionIdentifier);
		return node != null && node.initialized;
	}

	@Override
	public E replace(P positionIdentifier, E element) {
		requireNonNull(positionIdentifier, "Cannot replace an element, no position identifier specified ('with(positionIdentifier)' method).");

		ElementNode node = positionToNode.get(positionIdentifier);
		if (node == null)
			throw new NoSuchElementException("No element found for indentifier: " + positionIdentifier);

		E prviouslyAssociatedElement = node.element;
		node.element = element;
		clearNodeList();

		return prviouslyAssociatedElement;
	}

	@Override
	public boolean remove(Object element) {
		return noPositionElements.remove(element) ? true : remove(element, false);
	}

	@Override
	public boolean removeSoft(Object element) {
		return noPositionElements.remove(element) ? true : remove(element, true);
	}

	public boolean remove(Object element, boolean soft) {
		ElementNode node = findNodeForElement(element);
		if (node == null)
			return false;

		removeNode(node, soft);
		return true;
	}

	@Override
	public E removeElement(P positionIdentifier) {
		return removeElement(positionIdentifier, false);
	}

	@Override
	public E removeElementSoft(P positionIdentifier) {
		return removeElement(positionIdentifier, true);
	}

	private E removeElement(P positionIdentifier, boolean soft) {
		ElementNode node = positionToNode.get(positionIdentifier);
		if (node == null)
			return null;

		removeNode(node, soft);
		return node.element;
	}

	private void removeNode(ElementNode node, boolean soft) {
		positionToNode.remove(node.position);
		node.initialized = false;
		clearNodeList();

		if (soft)
			return;

		unlink(node, new ArrayList<>(node.befores), new ArrayList<>(node.afters));
	}

	private ElementNode findNodeForElement(Object o) {
		for (ElementNode node : positionToNode.values())
			if (node.initialized)
				if ((o == null && node.element == null) || o.equals(node.element))
					return node;

		return null;
	}

	private void clearNodeList() {
		list = null;
	}

	private List<ElementNode> nodeList() {
		if (list == null)
			list = newOrderedList();
		return list;
	}

	private void unlink(ElementNode nodeToUnlink, Iterable<ElementNode> befores, Iterable<ElementNode> afters) {
		for (ElementNode before : befores) {
			nodeToUnlink.befores.remove(before);
			before.afters.remove(nodeToUnlink);
		}

		for (ElementNode after : afters) {
			nodeToUnlink.afters.remove(after);
			after.befores.remove(nodeToUnlink);
		}
	}

	private List<ElementNode> newOrderedList() {
		List<ElementNode> newList = new ArrayList<>();

		visitNodes(positionToNode.values(), new HashSet<>(), newList);

		return Collections.unmodifiableList(newList);
	}

	private void visitNodes(Collection<ElementNode> nodes, Set<ElementNode> visitedNodes, List<ElementNode> elements) {
		for (ElementNode node : nodes)
			if (node.initialized && visitedNodes.add(node)) {
				visitNodes(node.befores, visitedNodes, elements);
				elements.add(node);
			}
	}
}
