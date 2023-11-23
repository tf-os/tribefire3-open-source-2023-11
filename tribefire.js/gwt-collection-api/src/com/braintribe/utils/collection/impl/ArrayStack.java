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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import com.braintribe.utils.collection.api.IStack;
import com.braintribe.utils.collection.api.Stack;

/**
 * {@link ArrayList} based implementation of {@link IStack}. This has an advantage over e.g. {@link ArrayDeque} that it supports <tt>null</tt> as a
 * valid element.
 * 
 * @author peter.gazdik
 * 
 * @implSpec not thread-safe
 */
public class ArrayStack<E> implements IStack<E>, Stack<E>, Iterable<E> {

	private final List<E> list = new ArrayList<>();

	@Override
	public void push(E e) {
		list.add(e);
	}

	@Override
	public E peek() {
		return list.get(lastIndex());
	}

	@Override
	public E pop() {
		if (list.isEmpty())
			throw new NoSuchElementException();

		return list.remove(lastIndex());
	}

	private int lastIndex() {
		return list.size() - 1;
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		int size = list.size();
		ListIterator<E> li = list.listIterator(size);
		return Stream.generate(li::previous).limit(size).iterator();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + list;
	}

}
