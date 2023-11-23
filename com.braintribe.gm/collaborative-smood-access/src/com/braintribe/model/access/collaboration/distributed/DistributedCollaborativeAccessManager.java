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
package com.braintribe.model.access.collaboration.distributed;

import static com.braintribe.model.access.collaboration.distributed.tools.CsaOperationBuilder.managePersistence;

import java.util.concurrent.locks.Lock;

import com.braintribe.model.cortexapi.access.collaboration.CollaborativePersistenceRequest;
import com.braintribe.model.cortexapi.access.collaboration.ReadOnlyCollaborativePersistenceRequest;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

/**
 * See {@link DistributedCollaborativeSmoodAccess} to understand how locking / consistency / performance is achieved.
 * 
 * @author peter.gazdik
 */
/* package */ class DistributedCollaborativeAccessManager implements ServiceProcessor<CollaborativePersistenceRequest, Object> {

	private final DistributedCollaborativeSmoodAccess access;
	private final Lock distributedLock;
	private final Lock writeLock;

	private final ServiceProcessor<CollaborativePersistenceRequest, Object> delegate;

	public DistributedCollaborativeAccessManager(DistributedCollaborativeSmoodAccess access) {
		this.access = access;
		this.writeLock = access.getLock().writeLock();
		this.distributedLock = access.distributedLock;
		this.delegate = access.collaborativeAccessManager;
	}

	@Override
	public Object process(ServiceRequestContext requestContext, CollaborativePersistenceRequest request) {
		if (isReadOnly(request))
			return processReadOnly(requestContext, request);
		else
			return processUpdate(requestContext, request);
	}

	private boolean isReadOnly(CollaborativePersistenceRequest request) {
		return request instanceof ReadOnlyCollaborativePersistenceRequest;
	}

	private Object processReadOnly(ServiceRequestContext requestContext, CollaborativePersistenceRequest request) {
		access.ensureUpToDate();

		return delegate.process(requestContext, request);
	}

	/**
	 * POSSIBLE OPTIMIZATION: see {@link DistributedCollaborativeSmoodAccess#applyManipulation}
	 */
	private Object processUpdate(ServiceRequestContext requestContext, CollaborativePersistenceRequest request) {
		// update before acquiring the distributed lock - if there is a big update, don't block the whole cluster
		access.ensureUpToDate();

		distributedLock.lock();
		try {

			writeLock.lock();
			try {
				return dw_processUpdate(requestContext, request);

			} finally {
				writeLock.unlock();
			}

		} finally {
			distributedLock.unlock();
		}
	}

	private Object dw_processUpdate(ServiceRequestContext requestContext, CollaborativePersistenceRequest request) {
		access.dw_ensureUpToDate();
		Object result = delegate.process(requestContext, request);
		access.dw_storeCsaOperation(managePersistence(request));

		return result;

	}

}
