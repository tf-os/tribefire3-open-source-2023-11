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

import com.braintribe.utils.collection.api.Stack;

/**
 * An alternative to {@code ThreadLocalStackedHolder} providing more direct push/peek/pop methods.
 * 
 * @author Neidhart Orlich
 * @author Dirk Scheffler
 * @author Peter Gazdik
 *
 * @param <T>
 *            The type of elements held in this stack
 */
public class ThreadLocalStack<T> implements Stack<T> {

	private final ThreadLocal<Stack<T>> threadLocal = new ThreadLocal<>();

	@Override
	public void push(T object) {
		requireNonNull(object, "Cannot push null");

		Stack<T> stack = threadLocal.get();

		if (stack == null)
			threadLocal.set(stack = new ArrayStack<>());

		stack.push(object);
	}

	@Override
	public T peek() {
		Stack<T> stack = threadLocal.get();
		return stack != null ? stack.peek() : null;
	}

	@Override
	public T pop() {
		Stack<T> stack = threadLocal.get();
		if (stack == null)
			return null;

		T popped = stack.pop();

		if (stack.isEmpty())
			threadLocal.remove();

		return popped;
	}

	@Override
	public boolean isEmpty() {
		Stack<T> stack = threadLocal.get();
		return stack == null || stack.isEmpty();
	}

}
