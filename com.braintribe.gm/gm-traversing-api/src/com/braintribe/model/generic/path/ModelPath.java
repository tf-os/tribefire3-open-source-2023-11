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
package com.braintribe.model.generic.path;

import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;

import com.braintribe.model.generic.pr.criteria.BasicCriterion;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.ListElementCriterion;
import com.braintribe.model.generic.pr.criteria.MapCriterion;
import com.braintribe.model.generic.pr.criteria.MapEntryCriterion;
import com.braintribe.model.generic.pr.criteria.MapKeyCriterion;
import com.braintribe.model.generic.pr.criteria.MapValueCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.pr.criteria.RootCriterion;
import com.braintribe.model.generic.pr.criteria.SetElementCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TypeCode;

@SuppressWarnings("unusable-by-js")
public class ModelPath extends AbstractSequentialList<ModelPathElement> {

	private final ModelPathNode anchor;
	private int size;

	public ModelPath() {
		anchor = createAnchor();
	}

	protected ModelPathNode getAnchor() {
		return anchor;
	}

	protected ModelPathNode createAnchor() {
		ModelPathNode node = new ModelPathNode();
		node.next = node;
		node.previous = node;
		node.path = this;
		return node;
	}

	protected ListIterator<ModelPathElement> listIterator(ModelPathNode node) {
		int index = -1;
		if (node == anchor)
			index = 0;
		else if (node == anchor.previous) {
			index = size;
		}
		return new ListIteratorImpl(node, index);
	}

	@Override
	public ListIterator<ModelPathElement> listIterator(int index) {
		if (index > size || size < 0)
			throw new IndexOutOfBoundsException();

		ModelPathNode node = anchor;

		int ds = index;
		int de = size - index;

		if (ds <= de) {
			// wind from start
			for (int i = 0; i < index; i++) {
				node = node.next;
			}
		} else {
			// rewind from end
			for (int i = 0; i <= de; i++) {
				node = node.previous;
			}
		}

		return new ListIteratorImpl(node, index);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean add(ModelPathElement element) {
		return super.add(element);
	}

	@Override
	public void add(int index, ModelPathElement element) {
		super.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends ModelPathElement> c) {
		return super.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends ModelPathElement> c) {
		return super.addAll(index, c);
	}

	@Override
	public boolean remove(Object o) {
		return super.remove(o);
	}

	@Override
	public ModelPathElement remove(int index) {
		return super.remove(index);
	}

	public ModelPathElement first() {
		if (size == 0)
			throw new IndexOutOfBoundsException();

		return listIterator(0).next();
	}

	public ModelPathElement last() {
		if (size == 0)
			throw new IndexOutOfBoundsException();

		return listIterator(size()).previous();
	}

	public ListIterator<ModelPathElement> iteratorAtEnd() {
		return listIterator(size());
	}

	private enum Operation {
		none,
		iterate,
		add,
		remove
	}

	private class ListIteratorImpl implements ListIterator<ModelPathElement> {
		private ModelPathNode cursor;
		private int index;
		private Operation lastOp = Operation.none;
		private ModelPathNode lastReturnedNode;

		public ListIteratorImpl(ModelPathNode cursor, int index) {
			this.index = index;
			this.cursor = cursor;
		}

		@Override
		public void add(ModelPathElement e) {
			ModelPathNode next = cursor.next;
			ModelPathNode node = new ModelPathNode();
			node.path = ModelPath.this;
			node.element = e;

			// wire bidirectional
			node.previous = cursor;
			cursor.next = node;
			node.next = next;
			next.previous = node;

			// restore the correct cursor
			cursor = node;

			if (index != -1)
				index++;

			// backlink element
			e.onAdopt(node);

			lastOp = Operation.add;
			size++;
		}

		@Override
		public boolean hasNext() {
			return cursor.next != anchor;
		}

		@Override
		public boolean hasPrevious() {
			return cursor != anchor;
		}

		@Override
		public ModelPathElement next() {
			ModelPathNode node = cursor.next;

			if (node != anchor) {
				if (index != -1)
					index++;
				cursor = node;

				lastOp = Operation.iterate;
				lastReturnedNode = node;
				return cursor.element;
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public ModelPathElement previous() {
			if (cursor != anchor) {
				if (index != -1)
					index--;
				ModelPathNode node = cursor;
				cursor = cursor.previous;
				lastOp = Operation.iterate;
				lastReturnedNode = node;
				return node.element;
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public int nextIndex() {
			ensureIndex();
			return index;
		}

		@Override
		public int previousIndex() {
			ensureIndex();
			return index - 1;
		}

		private void ensureIndex() {
			if (index == -1) {
				// check easy case 1
				if (cursor == anchor) {
					index = 0;
				}
				// check easy case 2
				else if (cursor == anchor.previous) {
					index = size;
				}
				// otherwise wire to find index
				else {
					index = 0;
					ModelPathNode curNode = anchor;

					while (curNode != cursor) {
						index++;
						if (index > size)
							throw new IllegalStateException("node not found in list");
						curNode = curNode.next;
					}
				}
			}
		}

		@Override
		public void remove() {
			if (lastOp == Operation.iterate) {
				ModelPathNode p = lastReturnedNode.previous;
				ModelPathNode n = lastReturnedNode.next;

				// unlink the last returned node;
				p.next = n;
				n.previous = p;
				lastReturnedNode.next = null;
				lastReturnedNode.previous = null;
				ModelPathElement element = lastReturnedNode.element;
				element.onOrphaned();
				lastReturnedNode.element = null;
				lastOp = Operation.remove;
				size--;

				// restore valid index and cursor node of this iterator if neccessary
				if (lastReturnedNode == cursor) {
					// returnedNode was returned by next() - thus the cursor needs to be fixed
					cursor = p;
				}
			} else
				throw new IllegalStateException("you have to call next() or previous() before you can remove any element from the list");

		}

		@Override
		public void set(ModelPathElement e) {
			if (lastOp == Operation.iterate) {
				ModelPathElement oldElement = lastReturnedNode.element;
				oldElement.onOrphaned();
				lastReturnedNode.element = e;
				e.onAdopt(lastReturnedNode);
			}
		}
	}

	public ModelPath copy() {
		ModelPath modelPath = new ModelPath();

		for (ModelPathElement element : this) {
			ModelPathElement copy = element.copy();
			modelPath.add(copy);
		}

		return modelPath;
	}

	/**
	 * converts a ModelPath into a {@link TraversingContext} stack which may come handy if one has a {@link TraversingCriterion} for the
	 * StandardMatcher
	 */
	@SuppressWarnings("deprecation")
	public Stack<BasicCriterion> asTraversingCriterionStack() {
		Stack<BasicCriterion> stack = new Stack<BasicCriterion>();
		for (ModelPathElement element : this) {
			switch (element.getPathElementType()) {
				case Root:
				case EntryPoint:
					RootCriterion rootCriterion = RootCriterion.T.create();
					rootCriterion.setTypeSignature(element.getType().getTypeSignature());
					stack.push(rootCriterion);
					break;
				case Property:
					PropertyPathElement propertyPathElement = (PropertyPathElement) element;
					PropertyCriterion propertyCriterion = PropertyCriterion.T.create();
					propertyCriterion.setTypeSignature(propertyPathElement.getType().getTypeSignature());
					propertyCriterion.setPropertyName(propertyPathElement.getProperty().getName());
					stack.push(propertyCriterion);
					break;
				case ListItem:
					ListItemPathElement listItemPathElement = (ListItemPathElement) element;
					ListElementCriterion listElementCriterion = ListElementCriterion.T.create();
					listElementCriterion.setTypeSignature(listItemPathElement.getType().getTypeSignature());
					stack.push(listElementCriterion);
					break;
				case SetItem:
					SetItemPathElement setItemPathElement = (SetItemPathElement) element;
					SetElementCriterion setElementCriterion = SetElementCriterion.T.create();
					setElementCriterion.setTypeSignature(setItemPathElement.getType().getTypeSignature());
					stack.push(setElementCriterion);
					break;
				case MapKey: {
					MapKeyPathElement mapKeyPathElement = (MapKeyPathElement) element;
					ModelPathElement previousElement = mapKeyPathElement.getPrevious();
					if (!(previousElement.getType() instanceof CollectionType))
						previousElement = previousElement.getPrevious();
					CollectionType mapType = previousElement.getType();
					String mapTypeSignature = mapType.getTypeSignature();
					String mapKeyTypeSignature = mapKeyPathElement.getType().getTypeSignature();
					String mapValueTypeSignature = mapType.getParameterization()[1].getTypeSignature();

					MapCriterion mapCriterion = MapCriterion.T.create();
					mapCriterion.setTypeSignature(mapTypeSignature);

					MapEntryCriterion mapEntryCriterion = MapEntryCriterion.T.create();
					mapEntryCriterion.setTypeSignature(mapTypeSignature);

					MapKeyCriterion mapKeyCriterion = MapKeyCriterion.T.create();
					mapKeyCriterion.setTypeSignature(mapKeyTypeSignature);

					stack.push(mapCriterion);
					stack.push(mapEntryCriterion);
					stack.push(mapKeyCriterion);
					break;
				}
				case MapValue: {
					MapValuePathElement mapValuePathElement = (MapValuePathElement) element;
					String mapTypeSignature = mapValuePathElement.getPrevious().getType().getTypeSignature();
					String mapKeyTypeSignature = mapValuePathElement.getKeyType().getTypeSignature();
					String mapValueTypeSignature = mapValuePathElement.getType().getTypeSignature();

					MapCriterion mapCriterion = MapCriterion.T.create();
					mapCriterion.setTypeSignature(mapTypeSignature);

					MapEntryCriterion mapEntryCriterion = MapEntryCriterion.T.create();
					mapEntryCriterion.setTypeSignature(mapTypeSignature);

					MapValueCriterion mapValueCriterion = MapValueCriterion.T.create();
					mapValueCriterion.setTypeSignature(mapValueTypeSignature);

					stack.push(mapCriterion);
					stack.push(mapEntryCriterion);
					stack.push(mapValueCriterion);
					break;
				}
			}

			// push an EntityCriterion if the value has EntityType
			// Object value = element.getValue();
			GenericModelType type = element.getType();
			if (type.getTypeCode() == TypeCode.entityType) {
				EntityType<?> entityType = (EntityType<?>) type;
				String typeSignature = entityType.getTypeSignature();
				EntityCriterion entityCriterion = EntityCriterion.T.create();
				entityCriterion.setTypeSignature(typeSignature);
				stack.push(entityCriterion);
			}
		}

		return stack;
	}

	/**
	 * converts a ModelPath into a {@link TraversingContext} which may come handy if one has a {@link TraversingCriterion} for the StandardMatcher.
	 */
	@SuppressWarnings("deprecation")
	public TraversingContext asTraversingContext() {
		TraversingContext traversingContext = new MpTraversingContext();
		for (ModelPathElement element : this) {
			switch (element.getPathElementType()) {
				case Root:
				case EntryPoint:
					RootCriterion rootCriterion = RootCriterion.T.create();
					rootCriterion.setTypeSignature(element.getType().getTypeSignature());
					traversingContext.pushTraversingCriterion(rootCriterion, element.getValue());
					break;
				case Property:
					PropertyPathElement propertyPathElement = (PropertyPathElement) element;
					PropertyCriterion propertyCriterion = PropertyCriterion.T.create();
					propertyCriterion.setTypeSignature(propertyPathElement.getType().getTypeSignature());
					propertyCriterion.setPropertyName(propertyPathElement.getProperty().getName());
					traversingContext.pushTraversingCriterion(propertyCriterion, propertyPathElement.getValue());
					break;
				case ListItem:
					ListItemPathElement listItemPathElement = (ListItemPathElement) element;
					ListElementCriterion listElementCriterion = ListElementCriterion.T.create();
					listElementCriterion.setTypeSignature(listItemPathElement.getType().getTypeSignature());
					traversingContext.pushTraversingCriterion(listElementCriterion, listItemPathElement.getValue());
					break;
				case SetItem:
					SetItemPathElement setItemPathElement = (SetItemPathElement) element;
					SetElementCriterion setElementCriterion = SetElementCriterion.T.create();
					setElementCriterion.setTypeSignature(setItemPathElement.getType().getTypeSignature());
					traversingContext.pushTraversingCriterion(setElementCriterion, setItemPathElement.getValue());
					break;
				case MapKey: {
					MapKeyPathElement mapKeyPathElement = (MapKeyPathElement) element;
					ModelPathElement mapPathElement = mapKeyPathElement.getPrevious();
					if (!(mapPathElement.getType() instanceof CollectionType))
						mapPathElement = mapPathElement.getPrevious();
					CollectionType mapType = mapPathElement.getType();
					String mapTypeSignature = mapType.getTypeSignature();
					String mapKeyTypeSignature = mapKeyPathElement.getType().getTypeSignature();
					String mapValueTypeSignature = mapType.getParameterization()[1].getTypeSignature();

					MapCriterion mapCriterion = MapCriterion.T.create();
					mapCriterion.setTypeSignature(mapTypeSignature);

					MapEntryCriterion mapEntryCriterion = MapEntryCriterion.T.create();
					mapEntryCriterion.setTypeSignature(mapTypeSignature);

					MapKeyCriterion mapKeyCriterion = MapKeyCriterion.T.create();
					mapKeyCriterion.setTypeSignature(mapKeyTypeSignature);

					Map<Object, Object> map = mapPathElement.getValue();
					Object key = mapKeyPathElement.getValue();
					traversingContext.pushTraversingCriterion(mapCriterion, map);
					traversingContext.pushTraversingCriterion(mapEntryCriterion, new ProxyMapEntry<Object, Object>(map, key));
					traversingContext.pushTraversingCriterion(mapKeyCriterion, key);
					break;
				}
				case MapValue: {
					MapValuePathElement mapValuePathElement = (MapValuePathElement) element;
					ModelPathElement mapPathElement = mapValuePathElement.getPrevious();
					if (!(mapPathElement.getType() instanceof CollectionType))
						mapPathElement = mapPathElement.getPrevious();
					String mapTypeSignature = mapPathElement.getType().getTypeSignature();
					String mapKeyTypeSignature = mapValuePathElement.getKeyType().getTypeSignature();
					String mapValueTypeSignature = mapValuePathElement.getType().getTypeSignature();

					MapCriterion mapCriterion = MapCriterion.T.create();
					mapCriterion.setTypeSignature(mapTypeSignature);

					MapEntryCriterion mapEntryCriterion = MapEntryCriterion.T.create();
					mapEntryCriterion.setTypeSignature(mapTypeSignature);

					MapValueCriterion mapValueCriterion = MapValueCriterion.T.create();
					mapValueCriterion.setTypeSignature(mapValueTypeSignature);

					Map<Object, Object> map = mapPathElement.getValue();
					Object key = mapValuePathElement.getKey();

					traversingContext.pushTraversingCriterion(mapCriterion, mapPathElement.getValue());
					traversingContext.pushTraversingCriterion(mapEntryCriterion, new ProxyMapEntry<Object, Object>(map, key));
					traversingContext.pushTraversingCriterion(mapValueCriterion, mapValuePathElement.getValue());
					break;
				}
			}

			// push an EntityCriterion if the value has EntityType
			Object value = element.getValue();
			GenericModelType type = element.getType();
			if (type.getTypeCode() == TypeCode.entityType) {
				EntityType<?> entityType = (EntityType<?>) type;
				String typeSignature = entityType.getTypeSignature();
				EntityCriterion entityCriterion = EntityCriterion.T.create();
				entityCriterion.setTypeSignature(typeSignature);
				traversingContext.pushTraversingCriterion(entityCriterion, value);
			}
		}

		return traversingContext;
	}

}
