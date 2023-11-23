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
package tribefire.module.wire.contract;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import com.braintribe.common.concurrent.TaskScheduler;
import com.braintribe.model.processing.worker.api.WorkerManager;
import com.braintribe.wire.api.space.WireSpace;

/**
 * @author peter.gazdik
 */
public interface WorkerContract extends WireSpace {

	WorkerManager manager();

	ExecutorService threadPool();

	ScheduledExecutorService scheduledThreadPool();

	/**
	 * {@link TaskScheduler} for tasks which should be run periodically.
	 * <p>
	 * This scheduler is shut down on server shutdown, but waits for the tasks running at the moment of shutdown to finish.
	 * <p>
	 * The time period as to how long it waits is configurable per task.
	 */
	TaskScheduler taskScheduler();

}
