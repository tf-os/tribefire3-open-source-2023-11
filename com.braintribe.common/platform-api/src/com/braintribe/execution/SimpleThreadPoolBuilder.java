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
package com.braintribe.execution;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.braintribe.utils.lcd.NullSafe;

public class SimpleThreadPoolBuilder {

	private Integer corePoolSize = null;
	private Integer maximumPoolSize = null;
	private long keepAliveTime = 60L;
	private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
	private BlockingQueue<Runnable> workQueue = null;
	private ThreadFactory threadFactory = null;
	private RejectedExecutionHandler rejectionHandler = null;
	private Boolean allowCoreThreadTimeOut = null;

	private SimpleThreadPoolBuilder() {
		//
	}

	public static SimpleThreadPoolBuilder newPool() {
		return new SimpleThreadPoolBuilder();
	}

	public SimpleThreadPoolBuilder poolSize(int corePoolSize, int maximumPoolSize) {
		this.corePoolSize = corePoolSize;
		this.maximumPoolSize = maximumPoolSize;
		return this;
	}

	public SimpleThreadPoolBuilder keepAliveTime(long keepAliveTime, TimeUnit timeUnit) {
		this.keepAliveTime = keepAliveTime;
		this.timeUnit = timeUnit;
		if (allowCoreThreadTimeOut == null) {
			this.allowCoreThreadTimeOut = Boolean.TRUE;
		}
		return this;
	}

	public SimpleThreadPoolBuilder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
		this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
		return this;
	}

	public SimpleThreadPoolBuilder workQueue(BlockingQueue<Runnable> workQueue) {
		this.workQueue = workQueue;
		return this;
	}

	public SimpleThreadPoolBuilder threadFactory(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
		return this;
	}

	public SimpleThreadPoolBuilder rejectionHandler(RejectedExecutionHandler rejectionHandler) {
		this.rejectionHandler = rejectionHandler;
		return this;
	}

	public ThreadPoolExecutor build() {
		NullSafe.nonNull(corePoolSize, "corePoolSize");
		NullSafe.nonNull(maximumPoolSize, "maximumPoolSize");
		NullSafe.nonNull(workQueue, "workQueue");

		ThreadPoolExecutor result = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, workQueue);

		applyConfiguration(result);

		return result;
	}

	private void applyConfiguration(ThreadPoolExecutor result) {
		if (allowCoreThreadTimeOut != null) {
			result.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
		}
		if (threadFactory != null) {
			result.setThreadFactory(threadFactory);
		}
		if (rejectionHandler != null) {
			result.setRejectedExecutionHandler(rejectionHandler);
		}
	}
}
