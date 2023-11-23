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

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface TaskScheduleBuilder {

	/**
	 * Specifies whether to interrupt the task when canceling it on shutdown of the {@link TaskScheduler}.
	 * <p>
	 * This value is passed to the corresponding {@link ScheduledFuture#cancel(boolean)}.
	 */
	TaskScheduleBuilder interruptOnCancel(boolean interrupt);

	/**
	 * Specifies a time to wait for this task on cancel.
	 * <p>
	 * This is relevant when {@link TaskScheduler} is being cancelled w
	 */
	TaskScheduleBuilder waitOnCancel(long timeout, TimeUnit unit);

	/**
	 * Optionally define an error handler.
	 * <p>
	 * This also determines whether the task will be invoked again after an exception is thrown.
	 * <p>
	 * Default implementation, where the error handler simply re-throws the exception, immediately halts after first error.
	 * <p>
	 * Is you want to continue, the error handler's method must not exit with an exception.
	 */
	TaskScheduleBuilder errorHandler(TaskErrorHandler errorHandler);

	/**
	 * Final confirmation to schedules given task.
	 * 
	 * @throws RejectedExecutionException
	 *             if the backing {@link TaskScheduler} was already {@link TaskScheduler#shutdown() shut down}.
	 */
	ScheduledTask done();

}
