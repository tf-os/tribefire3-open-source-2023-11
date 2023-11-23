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

import java.util.function.Consumer;

import com.braintribe.utils.collection.api.MinimalStack;

/**
 * @author Neidhart.Orlich
 *
 * @deprecated Please use any other implementation of MinimalStack. This class was created for backwards compatibility
 *             reasons only to support a specific pattern which is used throughout the system that was abusing consumers
 *             to mimic stack behavior.
 */
@Deprecated
public class ConsumerMinimalStackAdapter<T> implements MinimalStack<T> {
	private final Consumer<T> consumer;

	public ConsumerMinimalStackAdapter(Consumer<T> consumer) {
		super();
		this.consumer = consumer;
	}

	@Override
	public void push(T element) {
		consumer.accept(element);
	}

	@Override
	public T peek() {
		throw new RuntimeException(
				"ConsumerMinimalStackAdapter was tried to be used as actual stack - this is not possible as it was just introduced for backwards compatibility reasons.");
	}

	@Override
	public T pop() {
		consumer.accept(null);
		return null;
	}

	@Override
	public boolean isEmpty() {
		throw new RuntimeException(
				"ConsumerMinimalStackAdapter was tried to be used as actual stack - this is not possible as it was just introduced for backwards compatibility reasons.");

	}

}
