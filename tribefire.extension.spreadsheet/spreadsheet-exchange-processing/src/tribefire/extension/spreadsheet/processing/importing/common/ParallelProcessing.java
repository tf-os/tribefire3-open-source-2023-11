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
package tribefire.extension.spreadsheet.processing.importing.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ParallelProcessing<T> implements AutoCloseable {
	private int threadCount;
	private ExecutorService executor;
	private List<Future<T>> futures = new ArrayList<>();
	
	public ParallelProcessing(int threadCount) {
		this.threadCount = threadCount;
		this.executor = Executors.newFixedThreadPool(threadCount);
	}
	
	public void submitForAllThreads(Callable<T> callable) {
		for (int i = 0; i < threadCount; i++)
			submit(callable);
	}
	
	public void submitForAllThreads(Runnable runnable) {
		submitForAllThreads(callableFromRunnable(runnable));
	}
	
	private Callable<T> callableFromRunnable(Runnable runnable) {
		return () -> {
			runnable.run();
			return null;
		};
	}
	
	public void submit(Callable<T> callable) {
		futures.add(executor.submit(callable));
	}
	
	public void submit(Runnable runnable) {
		submit(callableFromRunnable(runnable));
	}
	
	public int getThreadCount() {
		return threadCount;
	}
	
	public Stream<T> stream(Supplier<? extends Exception> exceptionSupplier) {
		return futures.stream().map(f -> {
			try {
				return f.get();
			} catch (Exception e) {
				exceptionSupplier.get().addSuppressed(e);
				return null;
			}
		}).filter(t -> t != null);
	}
	
	public void consumeResults(Supplier<? extends Exception> exceptionSupplier) {
		stream(exceptionSupplier).count();
	}

	@Override
	public void close() {
		try {
			executor.shutdown();
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// noop
		}
	}

}
