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
package tribefire.platform.impl.service.execution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.execution.persistence.ExecutionPersistenceRequest;
import com.braintribe.model.execution.persistence.ExecutionPersistenceResponse;
import com.braintribe.model.execution.persistence.cleanup.CleanUpJobs;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.lock.api.Locking;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.StringTools;

/**
 * A {@link ServiceProcessor} which processes {@link ExecutionPersistenceRequest}s.
 */
public class ExecutionPersistenceServiceProcessor implements AccessRequestProcessor<ExecutionPersistenceRequest, ExecutionPersistenceResponse> {

	private static final Logger logger = Logger.getLogger(ExecutionPersistenceServiceProcessor.class);

	private ExecutorService cleanupExecutorService;
	private Lock cleanupLock;
	private Supplier<Locking> lockingSupplier;
	private Supplier<PersistenceGmSession> cortexSessionSupplier;

	private long maxAge;

	private Function<String, Supplier<PersistenceGmSession>> accessSessionSupplier;

	private AccessRequestProcessor<ExecutionPersistenceRequest, ExecutionPersistenceResponse> delegate = AccessRequestProcessors
			.dispatcher(dispatching -> {
				dispatching.register(CleanUpJobs.T, this::cleanUp);
			});

	@Override
	public ExecutionPersistenceResponse process(AccessRequestContext<ExecutionPersistenceRequest> context) {
		return delegate.process(context);
	}

	public ExecutionPersistenceResponse cleanUp(AccessRequestContext<CleanUpJobs> context) {

		CleanUpJobs request = context.getRequest();

		boolean cleanAll = request.getCleanAll();
		long actualMaxAge = cleanAll ? 0L : maxAge;
		Long requestedMaxAge = request.getMaxAge();
		if (requestedMaxAge != null && requestedMaxAge >= 0) {
			actualMaxAge = requestedMaxAge;
		}
		IncrementalAccess access = request.getAccess();
		if (access == null) {
			throw new IllegalArgumentException("The CleanUpJobs service request must contain a valid Access Id.");
		}
		String externalId = access.getExternalId();
		if (StringTools.isBlank(externalId)) {
			throw new IllegalArgumentException("The access referenced by the CleanUpJobs service request does not contain a valid external Id.");
		}
		Supplier<PersistenceGmSession> sessionSupplier = accessSessionSupplier.apply(externalId);

		logger.debug(() -> "Starting cleaning up jobs in access " + externalId);

		CleanUpTools.cleanupJobsAndResources(cleanupExecutorService, cortexSessionSupplier, sessionSupplier, access, getLock(), actualMaxAge);

		ExecutionPersistenceResponse response = ExecutionPersistenceResponse.T.create();

		return response;
	}

	private Lock getLock() {
		if (this.cleanupLock == null) {
			synchronized (this) {
				if (this.cleanupLock == null) {
					Locking locking = lockingSupplier.get();
					this.cleanupLock = locking.forIdentifier(ExecutionPersistenceCleanupWorker.class.getName()).writeLock();
				}
			}
		}
		return this.cleanupLock;
	}

	@Required
	public void setMaxAge(long maxAge) {
		this.maxAge = maxAge;
	}
	@Required
	public void setAccessSessionSupplier(Function<String, Supplier<PersistenceGmSession>> accessSessionSupplier) {
		this.accessSessionSupplier = accessSessionSupplier;
	}
	@Required
	public void setLockingSupplier(Supplier<Locking> lockingSupplier) {
		this.lockingSupplier = lockingSupplier;
	}
	@Required
	public void setCleanupExecutorService(ExecutorService cleanupExecutorService) {
		this.cleanupExecutorService = cleanupExecutorService;
	}
	@Required
	public void setCortexSessionSupplier(Supplier<PersistenceGmSession> cortexSessionSupplier) {
		this.cortexSessionSupplier = cortexSessionSupplier;
	}
}
