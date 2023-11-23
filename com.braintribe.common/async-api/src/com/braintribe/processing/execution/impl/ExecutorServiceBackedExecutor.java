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
package com.braintribe.processing.execution.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.braintribe.processing.execution.api.Executor;
import com.braintribe.processing.execution.api.ExecutorCrossCuttingConcern;

public class ExecutorServiceBackedExecutor implements Executor {
	private ExecutorService delegate;
	private ExecutorCrossCuttingConcern crossCuttingConcern;
	
	public ExecutorServiceBackedExecutor(ExecutorService delegate) {
		this.delegate = delegate;
	}

	public void setCrossCuttingConcern(ExecutorCrossCuttingConcern crossCuttingConcern) {
		this.crossCuttingConcern = crossCuttingConcern;
	}
	
	@Override
	public void execute(Runnable command) {
		delegate.execute(enrich(command));
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return delegate.submit(enrich(task));
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return delegate.submit(enrich(task), result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return delegate.submit(enrich(task));
	}
	
	protected Runnable enrich(Runnable runnable) {
		if (crossCuttingConcern == null)
			return runnable;
		
		return () -> {
			crossCuttingConcern.onExecutionStart();
			try {
				runnable.run();
			}
			finally {
				crossCuttingConcern.onExecutionEnd();
			}
		};
	}
	
	protected <T> Callable<T> enrich(Callable<T> callable) {
		if (crossCuttingConcern == null)
			return callable;
		
		return () -> {
			crossCuttingConcern.onExecutionStart();
			try {
				return callable.call();
			}
			finally {
				crossCuttingConcern.onExecutionEnd();
			}
		};
	}
}