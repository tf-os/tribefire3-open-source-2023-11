package com.braintribe.build.cmd.assets.impl;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.braintribe.exception.Exceptions;

public interface WatchDog extends AutoCloseable {
	static <T> T run(Callable<T> callable, Runnable monitorCallback, Duration duration) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<?> future = null;
		try {
			future = executor.submit(() -> {
				try {
					Thread.sleep(duration.toMillis());
					monitorCallback.run();
				} catch (InterruptedException e) {
					monitorCallback.run();
				}
			});
			
			return callable.call();
			
			
		} catch (Exception e) {
			throw Exceptions.unchecked(e, e.getMessage());
		}
		finally {
			future.cancel(true);
			executor.shutdown();
		}
	}
	
	static void run(Runnable runnable, Runnable monitorCallback, Duration duration) {
		run(() -> { runnable.run(); return true; }, monitorCallback, duration);
	}
	
}
