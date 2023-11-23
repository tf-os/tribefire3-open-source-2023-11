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
package com.braintribe.codec.context;

import java.util.EmptyStackException;
import java.util.Stack;


public class CodingContext {
	private static ThreadLocal<Stack<CodingContext>> threadLocal = new ThreadLocal<Stack<CodingContext>>();
	
	@SuppressWarnings("unchecked")
	public static <T extends CodingContext> T get() {
		Stack<CodingContext> stack = threadLocal.get();
		return stack != null? (T)stack.peek(): null;
	}
	
	public static <T extends CodingContext> void push(T codingContext) {
		Stack<CodingContext> stack = threadLocal.get();
		
		if (stack == null) {
			stack = new Stack<CodingContext>();
			threadLocal.set(stack);
		}
		
		stack.push(codingContext);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends CodingContext> T pop() {
		Stack<CodingContext> stack = threadLocal.get();
		
		if (stack != null) {
			T entry = (T)stack.pop();
			if (stack.isEmpty()) {
				threadLocal.remove();
			}
			return entry;
		}
		else {
			throw new EmptyStackException();
		}
	}
}
