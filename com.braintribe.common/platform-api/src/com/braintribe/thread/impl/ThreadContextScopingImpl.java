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
package com.braintribe.thread.impl;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.thread.api.DeferringThreadContextScoping;
import com.braintribe.thread.api.ThreadContextScope;
import com.braintribe.thread.api.ThreadContextScoping;

public class ThreadContextScopingImpl implements DeferringThreadContextScoping {

	private List<Supplier<? extends ThreadContextScope>> scopeSuppliers;

	public void setScopeSuppliers(List<Supplier<? extends ThreadContextScope>> scopeSuppliers) {
		this.scopeSuppliers = scopeSuppliers;
	}

	@Override
	public Runnable bindContext(Runnable runnable) throws ThreadContextScopingRuntimeException {

		List<ThreadContextScope> scopes = scopeSuppliers.stream().map(s -> s.get()).collect(Collectors.toList());

		ContextBoundRunnable cbRunnable = new ContextBoundRunnable(runnable, scopes);

		return cbRunnable;
	}

	@Override
	public <U> Callable<U> bindContext(Callable<U> callable) {

		List<ThreadContextScope> scopes = scopeSuppliers.stream().map(s -> s.get()).collect(Collectors.toList());

		ContextBoundCallable<U> cbCallable = new ContextBoundCallable<>(callable, scopes);

		return cbCallable;
	}

	@Override
	public void runWithContext(Runnable runnable) {

		Runnable boundRunnable = this.bindContext(runnable);
		boundRunnable.run();

	}

	@Override
	public <U> U runWithContext(Callable<U> callable) throws Exception {

		Callable<U> boundCallable = this.bindContext(callable);
		return boundCallable.call();

	}

	@Override
	public ThreadContextScoping defer() {

		List<Supplier<? extends ThreadContextScope>> list = scopeSuppliers.stream().map(s -> s.get())
				.<Supplier<? extends ThreadContextScope>> map(t -> () -> t).collect(Collectors.toList());

		ThreadContextScopingImpl deferringInstance = new ThreadContextScopingImpl();
		deferringInstance.setScopeSuppliers(list);
		return deferringInstance;
	}

}
