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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

/**
 * @author peter.gazdik
 */
public class ArrayStackTests {

	private final ArrayStack<Integer> stack = new ArrayStack<>();

	@Test
	public void emptyStackWorksFine() throws Exception {
		assertThat(stack.isEmpty()).isTrue();

		Iterator<Integer> it = stack.iterator();
		assertThat(it).isNotNull();
		assertThat(it.hasNext()).isFalse();
	}

	@Test
	public void canPush() throws Exception {
		stack.push(2);
		stack.push(1);
		stack.push(0);

		assertThat(stack.peek()).isEqualTo(0);

		// check iterates in correct order
		Iterator<Integer> it = stack.iterator();
		for (int i = 0; i <= 2; i++) {
			assertThat(it.hasNext()).isTrue();
			assertThat(it.next()).isEqualTo(i);
		}
		assertThat(it.hasNext()).isFalse();
	}

	@Test
	public void canPushPop() throws Exception {
		stack.push(2);
		stack.push(1);
		stack.push(0);

		for (int i = 0; i <= 2; i++) {
			assertThat(stack.peek()).isEqualTo(i);
			assertThat(stack.pop()).isEqualTo(i);
		}

		assertThat(stack.isEmpty()).isTrue();
	}

	@Test(expected = NoSuchElementException.class)
	public void exceptionOnEmptyPop() throws Exception {
		stack.pop();
	}
}
