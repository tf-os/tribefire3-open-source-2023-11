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
package com.braintribe.model.processing.sp.invocation.multithreaded;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.execution.queue.FifoEntry;
import com.braintribe.execution.virtual.VirtualThreadExecutorBuilder;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.securityservice.api.UserSessionScope;
import com.braintribe.model.processing.securityservice.api.UserSessionScoping;
import com.braintribe.model.processing.sp.invocation.AbstractSpInvocation;
import com.braintribe.model.spapi.StateChangeProcessorInvocationPacket;

/**
 * a StateChange processor invocation implementation that uses an executor service to actually process the invocation,
 * i.e. a single worker acts on the queue, but then dispatches the call of the processor to an executor service. <br/>
 * derives from {@link AbstractSpInvocation} and implements {@link Consumer}<br/>
 * <br/>
 * 
 * About the {@link ExecutorService} : make sure that the executor instance you pass can be used entirely and
 * exclusively by the instance - it may conflict with other uses if you re-use a global executor. It would be best to
 * not set this because things may slow down otherwise.
 * 
 * @author pit
 *
 */
public class MultiThreadedSpInvocation extends AbstractSpInvocation implements Consumer<StateChangeProcessorInvocationPacket>, LifecycleAware {
	private static final Logger logger = Logger.getLogger(MultiThreadedSpInvocation.class);
	private ExecutorService executor;
	private boolean executorCreatedLocally = false;
	private int threadCount = 20;
	private PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<>();
	private String name;
	private volatile boolean shutdown = false;
	private Object shutdownLock = new Object();

	private UserSessionScoping userSessionScoping;

	/**
	 * Sets the executor to use: note that the executor should be exclusively for the engine as it may use up all resources,
	 * so please do not plug a common shared executor into this class, but rather configure a proper one for it<br>
	 * Please note that the Executor must be well crafted to meet the requirements here. It is better to take the default
	 * that is created when this method is not invoked.
	 * 
	 * @param executor
	 *            - the {@link ThreadPoolExecutor} to handle all processor calls
	 */
	@Configurable
	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	@Required
	@Configurable
	public void setUserSessionScoping(UserSessionScoping userSessionScoping) {
		this.userSessionScoping = userSessionScoping;
	}

	@Required
	@Configurable
	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	@Configurable
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void accept(StateChangeProcessorInvocationPacket invocation) throws RuntimeException {
		synchronized (shutdownLock) {

			if (shutdown) {
				preserve(invocation);
				return;
			}

			if (executor == null) {
				throw new RuntimeException("The executor has already been shut down. Not accepting further packets.");
			}

			if (logger.isTraceEnabled()) {

				String activeThreads = "unknown";
				String poolSize = "uknown";
				String largestPoolSize = "uknown";
				String executorQueueSize = "" + queue.size();

				if (executor instanceof ThreadPoolExecutor) {
					ThreadPoolExecutor e = (ThreadPoolExecutor) executor;

					activeThreads = "" + e.getActiveCount();
					poolSize = "" + e.getPoolSize();
					largestPoolSize = "" + e.getLargestPoolSize();
				}

				logger.trace("Took invocation context from queue. Remaining elements: " + queue.size() + ", Executor queue size: " + executorQueueSize
						+ ", Active Threads: " + activeThreads + ", poolSize: " + poolSize + ", largestPoolSize: " + largestPoolSize);

			}

			Caller caller = new Caller(invocation);
			executor.execute(caller);
		}
	}

	private void preserve(StateChangeProcessorInvocationPacket invocation) {
		// TODO Auto-generated method stub

	}

	/**
	 * actual caller
	 * 
	 * @author pit
	 *
	 */
	private class Caller extends FifoEntry<StateChangeProcessorInvocationPacket> implements Runnable {

		public Caller(StateChangeProcessorInvocationPacket invocationPacket) {
			super(invocationPacket);
		}

		@Override
		public void run() {
			try {
				UserSessionScope scope = userSessionScoping.forDefaultUser().push();
				try {
					processInvocationPacket(super.getEntry());
				} finally {
					scope.pop();
				}
			} catch (Exception e) {
				logger.error("error while processing invocation packet: " + super.getEntry(), e);
			}
		}
	}

	@Override
	public void preDestroy() {
		synchronized (shutdownLock) {
			shutdown = true;
		}

		if (executorCreatedLocally && executor != null) {
			executor.shutdown();
			ExecutorService myExecutor = executor;
			executor = null;
			try {
				myExecutor.awaitTermination(20l, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.debug("Interrupted while waiting for executor termination.");
			}
		}
	}
	@Override
	public void postConstruct() {
		if (executor == null) {

			final String description;
			if (name != null) {
				description = "State Change Processing (" + name + ")";
			} else {
				description = "State Change Processing";
			}

			executor = VirtualThreadExecutorBuilder.newPool().concurrency(threadCount).threadNamePrefix("MultiThreadedSpInvocation")
					.description(description).build();
			executorCreatedLocally = true;
		}
	}
}
