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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ScopeManager<T> implements BeanLifeCycleExpert {

	private Stack<T> scopeStack = new Stack<>();
	private List<ScopeListener<T>> listeners = new ArrayList<>();
	private BeanLifeCycleExpert beanLifeCycleExpert = null;

	public void setBeanLifeCycleExpert(BeanLifeCycleExpert beanLifeCycleExpert) {
		this.beanLifeCycleExpert = beanLifeCycleExpert;
	}

	public void pushScope(T instance) {
		scopeStack.push(instance);
	}

	public void popScope() {
		scopeStack.pop();
	}

	public T getCurrentScope() {
		return scopeStack.peek();
	}

	public void openScope(T scope) {
		try {
			pushScope(scope);
			fireScopeOpened(scope);
		} finally {
			popScope();
		}
	}

	public void closeScope(T scope) {
		try {
			pushScope(scope);
			fireScopeClosed(scope);
		} finally {
			popScope();
		}
	}

	public void openAndPushScope(T scope) {
		pushScope(scope);
		fireScopeOpened(scope);
	}

	public void closeAndPopScope() {
		T scope = getCurrentScope();
		try {
			fireScopeClosed(scope);
		} finally {
			popScope();
		}
	}

	public void fireScopeOpened(T scope) {
		for (ScopeListener<T> listener : listeners) {
			listener.scopeOpened(scope);
		}
	}

	public void fireScopeClosed(T scope) {
		for (ScopeListener<T> listener : listeners) {
			listener.scopeClosed(scope);
		}
	}

	public void addScopeListener(ScopeListener<T> listener) {
		listeners.add(listener);
	}

	public void removeScopeListener(ScopeListener<T> listener) {
		listeners.remove(listener);
	}

	@Override
	public void intializeBean(Object object) throws Exception {
		if (beanLifeCycleExpert != null) {
			beanLifeCycleExpert.intializeBean(object);
		}
	}

	@Override
	public void disposeBean(Object object) throws Exception {
		if (beanLifeCycleExpert != null) {
			beanLifeCycleExpert.disposeBean(object);
		}
	}
}
