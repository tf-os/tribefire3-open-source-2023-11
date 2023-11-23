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
package com.braintribe.common.concurrent;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;

/**
 * {@link TaskScheduler} implementation backed by a {@link ScheduledExecutorService}.
 * 
 * @author peter.gazdik
 */
public class TaskSchedulerImpl implements TaskScheduler, DestructionAware {

	/* NAMING: fields/methods prefixed with "s_" can only be called from a synchronized method/block. */

	private static final Logger log = Logger.getLogger(TaskSchedulerImpl.class);

	private String name = getClass().getSimpleName() + "#" + System.identityHashCode(this);
	private ScheduledExecutorService executor;
	private long defaultWaitOnTaskCancelMs = 10_000;

	// only accessed in synced block
	private final Set<TaskEntry> s_tasks = newSet();
	private final ReentrantReadWriteLock taskLock = new ReentrantReadWriteLock();
	private volatile boolean s_shutdown;

	@Configurable
	public void setName(String name) {
		this.name = name;
	}

	/** Configures a {@link ScheduledExecutorService} to use for scheduling tasks. */
	@Required
	public void setExecutor(ScheduledExecutorService executor) {
		this.executor = executor;
	}

	/**
	 * Configures the default time period to wait for a task to finish on shutdown, in case no value is specified via
	 * {@link TaskScheduleBuilder#waitOnCancel(long, TimeUnit)}.
	 * <p>
	 * Default value is 10 seconds.
	 * <p>
	 * Value less than 0 is ignored.
	 */
	@Configurable
	public void setDefaultTaskTerminationWaitingPeriod(long period, TimeUnit unit) {
		if (period > 0)
			this.defaultWaitOnTaskCancelMs = unit.toMillis(period);
	}

	@Override
	public TaskScheduleBuilder scheduleAtFixedRate(String name, Runnable task, long initialDelay, long period, TimeUnit unit) {
		return new ScheduleBuilderImpl(name, task, initialDelay, period, unit);
	}

	private void unschedule(TaskEntry taskEntry) {
		taskLock.writeLock().lock();
		try {
			s_tasks.remove(taskEntry);
		} finally {
			taskLock.writeLock().unlock();
		}
	}

	private class ScheduleBuilderImpl implements TaskScheduleBuilder {
		private final String taskName;
		private final Runnable task;
		private final long initialDelayMs;
		private final long periodMs;

		private boolean interrupt = true;
		private long waitOnCancelMs = defaultWaitOnTaskCancelMs;
		private TaskErrorHandler errorHandler = e -> {
			throw e;
		};

		public ScheduleBuilderImpl(String name, Runnable task, long initialDelay, long period, TimeUnit unit) {
			this.taskName = name;
			this.task = task;
			this.initialDelayMs = unit.toMillis(initialDelay);
			this.periodMs = unit.toMillis(period);
		}

		@Override
		public TaskScheduleBuilder interruptOnCancel(boolean interrupt) {
			this.interrupt = interrupt;
			return this;
		}

		@Override
		public TaskScheduleBuilder waitOnCancel(long timeout, TimeUnit unit) {
			this.waitOnCancelMs = unit.toMillis(timeout);
			return this;
		}

		@Override
		public TaskScheduleBuilder errorHandler(TaskErrorHandler errorHandler) {
			this.errorHandler = errorHandler;
			return this;
		}

		@Override
		public ScheduledTask done() {
			synchronized (TaskSchedulerImpl.this) {
				return s_scheduleNewTask();
			}
		}

		private TaskEntry s_scheduleNewTask() {
			if (s_shutdown)
				throw new RejectedExecutionException("Cannot schedule task " + taskName + " in " + name + ". This scheduler was already shut down.");

			TaskEntry taskEntry = new TaskEntry(task, taskName, interrupt, waitOnCancelMs, errorHandler);
			taskEntry.future = executor.scheduleAtFixedRate(taskEntry, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
			s_tasks.add(taskEntry);
			return taskEntry;
		}
	}

	@Override
	public void preDestroy() {
		awaitTerminationUsingTaskTimeout();
	}

	@Override
	public boolean isShutdown() {
		return s_shutdown;
	}

	@Override
	public boolean shutdown() {
		if (s_shutdown)
			return false;

		s_shutdown = true;
		log.info("Shutting down " + name);

		// we need a copy to iterate over as canceling a task removes it from the list
		taskLock.writeLock().lock();
		try {
			return cancelAllTasks(newList(s_tasks));
		} finally {
			taskLock.writeLock().unlock();
		}
	}

	private boolean cancelAllTasks(List<TaskEntry> tasksCopy) {
		boolean result = false;
		for (TaskEntry taskEntry : tasksCopy)
			result |= taskEntry.cancel();

		return result;
	}

	@Override
	public void awaitTermination() {
		awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	}

	@Override
	public boolean awaitTerminationUsingTaskTimeout() {
		long maxTimeoutMs = findMaxTimeout();
		return awaitTermination(maxTimeoutMs, TimeUnit.MILLISECONDS);
	}

	private long findMaxTimeout() {
		taskLock.readLock().lock();
		try {
			return s_tasks.stream() //
					.mapToLong(e -> e.waitOnShutdownMs) //
					.max() //
					.orElse(0);
		} finally {
			taskLock.readLock().unlock();
		}
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit timeUnit) {
		shutdown();

		taskLock.readLock().lock();
		try {
			List<TaskEntry> tasksCopy = newList(s_tasks);

			long waitMsLeft = timeUnit.toMillis(timeout);

			long waitTillMs = System.currentTimeMillis() + waitMsLeft;
			if (waitTillMs < 0)
				waitTillMs = Long.MAX_VALUE;

			for (TaskEntry taskEntry : tasksCopy) {
				if (waitMsLeft < 0)
					return false;

				try {
					if (!taskEntry.awaitTermination(waitMsLeft, TimeUnit.MILLISECONDS))
						return false;

				} catch (Exception e) {
					throw Exceptions.unchecked(e, "Error while waiting to shut down task [" + taskEntry.taskName + "] of [" + name + "]");
				}

				waitMsLeft = waitTillMs - System.currentTimeMillis();
			}

			return true;
		} finally {
			taskLock.readLock().unlock();
		}
	}

	private class TaskEntry implements ScheduledTask, Runnable {
		private final Runnable task;
		private final TaskErrorHandler errorHandler;

		public final String taskName;
		public final boolean interrupt;
		public final long waitOnShutdownMs;
		public ScheduledFuture<?> future;

		private final CountDownLatch cdl = new CountDownLatch(1);
		private volatile boolean isCancelled;
		private volatile boolean isRunning;

		public TaskEntry(Runnable task, String name, boolean interrupt, long waitOnShutdownMs, TaskErrorHandler errorHandler) {
			this.task = task;
			this.taskName = name;
			this.interrupt = interrupt;
			this.waitOnShutdownMs = waitOnShutdownMs;
			this.errorHandler = errorHandler;
		}

		// we assume this is not called while it is still running
		@Override
		public void run() {
			isRunning = true;

			try {
				if (!isCancelled)
					task.run();

			} catch (RuntimeException e) {
				errorHandler.handleError(e);

			} finally {
				isRunning = false;
				// if task ran, it could have taken some time, so let's check the flag again
				if (isCancelled)
					cdl.countDown();
			}

		}

		@Override
		public boolean cancel() {
			if (isCancelled)
				return false;

			synchronized (this) {
				if (isCancelled)
					return false;

				future.cancel(interrupt);
				unschedule(this);
				return isCancelled = true;
			}
		}

		@Override
		public void awaitTermination() {
			awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}

		@Override
		public boolean awaitTermination(long time, TimeUnit unit) {
			cancel();

			// we know isCanceled is true, which means the task will never run again, at most it finishes the current run
			// if isRunning is false, we're done
			if (!isRunning)
				return true;

			// if isRunning is true after we have cancelled, then the moment the task finishes the cdl will be countDown-ed
			try {
				return cdl.await(time, unit);
			} catch (InterruptedException e) {
				throw new RuntimeException("Interrupted while waiting for task " + taskName + " to finish.", e);
			}
		}

	}
}
