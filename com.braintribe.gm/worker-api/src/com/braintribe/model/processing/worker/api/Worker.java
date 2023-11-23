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
package com.braintribe.model.processing.worker.api;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.PreliminaryEntityReference;

public interface Worker {

	/**
	 * Start the work. This method must return immediately (hence, it must not block). This can be achieved by either forking a separate thread (via
	 * the worker context) or start an asynchronous processing by other means.
	 * 
	 * @param workerContext
	 *            The context that the worker is allowed to use to either access a session or start separate threads,
	 * @throws WorkerException
	 *             Thrown in the event of an error.
	 */
	void start(WorkerContext workerContext) throws WorkerException;

	/**
	 * Instructs the worker to stop the current task. If any threads have been started via the thread context, they would be automatically stopped
	 * later (if they are cancelable, which means that it either repeatedly checks {@link Thread#isInterrupted()} or catching an
	 * {@link InterruptedException} during a blocking operation that supports this exception).
	 */
	void stop(WorkerContext workerContext) throws WorkerException;

	/**
	 * Provides a unique identification object that is used, for example, for leadership management.
	 * 
	 * @return A GenericEntity that identifies this worker.
	 */
	default GenericEntity getWorkerIdentification() {
		if (isSingleton())
			throw new UnsupportedOperationException("'getWorkerIdentification()' must be implemented explicitly for singleton workers! Worker: " + this);

		// TODO this used to be HardwiredWorker, but that is no tf.cortex.
		PreliminaryEntityReference ref = PreliminaryEntityReference.T.create();
		ref.setId(getClass().getName() + "-defaultId");
		
		return ref;
	}

	/**
	 * Tells the system whether this worker can be instantiated (and thus executed) multiple times for parallel processing and/or clustering.
	 * 
	 * @return <code>true</code> iff this worker can be executed only once.
	 */
	default boolean isSingleton() {
		return false;
	}

}
