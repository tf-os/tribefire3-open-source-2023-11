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
package tribefire.platform.impl.thread;

import com.braintribe.thread.api.ThreadContextScope;
import com.braintribe.utils.collection.api.MinimalStack;

public class StandardThreadContextScope <T> implements ThreadContextScope {

	private T context;
	private MinimalStack<T> stack;

	public StandardThreadContextScope(T context, MinimalStack<T> stack) {
		this.context = context;
		this.stack = stack;
	}

	@Override
	public void push() {
		stack.push(context);
	}

	@Override
	public void pop() {
		stack.pop();
	}

}
