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

import java.util.concurrent.TimeUnit;

/**
 * @author peter.gazdik
 */
public interface ScheduledTask {

	/**
	 * Cancels given scheduled task.
	 */
	boolean cancel();

	/**
	 * Cancels given task (if needed) and returns only after the job terminates, i.e. is not running and won't be scheduled again.
	 */
	void awaitTermination();

	/**
	 * Cancels given task (if needed) and waits a given period of time for it to terminate. <tt>false</tt> is returned only if the waiting for
	 * termination times out.
	 * 
	 * @return true iff task was already cancelled (now or even before) and isn't running (meaning it won't be scheduled again), or was running and
	 *         terminated within given timeout. If time has expired, false is returned.
	 * @throws RuntimeException
	 *             (caused by InterruptedException) if interrupted while waiting
	 */
	boolean awaitTermination(long time, TimeUnit unit);

}
