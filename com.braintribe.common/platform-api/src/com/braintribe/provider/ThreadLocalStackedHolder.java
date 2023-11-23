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
package com.braintribe.provider;

import java.util.ArrayDeque;
import java.util.Deque;

public class ThreadLocalStackedHolder<T> implements Hub<T> {

	private final ThreadLocal<Deque<T>> threadLocal = new ThreadLocal<>();

	private String noValueErrorMessage;

	public void setNoValueErrorMessage(String noValueErrorMessage) {
		this.noValueErrorMessage = noValueErrorMessage;
	}

	@Override
	public void accept(T object) throws RuntimeException {
		Deque<T> deque = threadLocal.get();

		if (object != null) {
			if (deque == null) {
				threadLocal.set(deque = new ArrayDeque<>());
			}

			deque.addFirst(object);

		} else if (deque != null) {
			deque.pollFirst();

			if (deque.isEmpty()) {
				threadLocal.remove();
			}
		}

	}

	@Override
	public T get() throws RuntimeException {
		Deque<T> deque = threadLocal.get();

		T result = deque != null ? deque.peekFirst() : null;

		if (result == null && noValueErrorMessage != null) {
			throw new IllegalStateException("No value in thread '" + Thread.currentThread().getName() + "'. Custom message: " + noValueErrorMessage);
		}

		return result;
	}

}
