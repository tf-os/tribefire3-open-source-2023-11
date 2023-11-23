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
package com.braintribe.gwt.logging.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class CycleBufferList<E> implements List<E> {
	private ArrayList<E> elements;
	private int offset;
	private int capacity;
	
	public CycleBufferList(int capacity) {
		elements = new ArrayList<E>(capacity);
		this.capacity = capacity;
	}
	
	@Override
	public boolean add(E element) {
		add(size(), element);
		return true;
	}

	@Override
	public void add(int element, E index) {
		//NOP
	}

	@Override
	public boolean addAll(Collection<? extends E> elements) {
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> elements) {
		return false;
	}

	@Override
	public void clear() {
		elements.clear();
		offset = 0;
	}

	@Override
	public boolean contains(Object element) {
		return elements.contains(element);
	}

	@Override
	public boolean containsAll(Collection<?> elements) {
		return this.elements.containsAll(elements);
	}

	@Override
	public E get(int index) {
		return elements.get((index + offset) % capacity);
	}

	@Override
	public int indexOf(Object element) {
		int index = elements.indexOf(element);
		index -= offset;
		if (index < 0) index += capacity;
		return index;
	}

	@Override
	public boolean isEmpty() {
		return elements.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}

	@Override
	public int lastIndexOf(Object element) {
		return 0;
	}

	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return null;
	}

	@Override
	public E remove(int index) {
		return null;
	}

	@Override
	public boolean remove(Object element) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> elements) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> elements) {
		return false;
	}

	@Override
	public E set(int index, E element) {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public List<E> subList(int start, int end) {
		return null;
	}

	@Override
	public Object[] toArray() {
		return null;
	}

	@Override
	public <T> T[] toArray(T[] array) {
		return null;
	}
	
}
