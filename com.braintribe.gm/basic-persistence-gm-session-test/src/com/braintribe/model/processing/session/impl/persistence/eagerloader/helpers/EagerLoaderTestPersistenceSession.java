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
package com.braintribe.model.processing.session.impl.persistence.eagerloader.helpers;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.api.notifying.interceptors.CollectionEnhancer;
import com.braintribe.model.processing.session.api.notifying.interceptors.LazyLoader;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.processing.session.impl.persistence.AbstractPersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.EagerLoader;
import com.braintribe.model.processing.session.impl.persistence.EagerLoaderSupportingAccess;
import com.braintribe.processing.async.api.AsyncCallback;

/**
 * @author peter.gazdik
 */
public class EagerLoaderTestPersistenceSession extends AbstractPersistenceGmSession {

	private final EagerLoaderSupportingAccess access;

	public EagerLoaderTestPersistenceSession(EagerLoaderSupportingAccess access) {
		this.access = access;

		suspendHistory();

		EagerLoader eagerLoader = new EagerLoader(access, this);
		interceptors().with(LazyLoader.class).after(CollectionEnhancer.class).add(eagerLoader);
	}

	@Override
	public ManipulationResponse commit() throws GmSessionException {
		throw new UnsupportedOperationException("Cannot commmit, this is a read-only session.");
	}

	@Override
	public void commit(AsyncCallback<ManipulationResponse> callback) {
		throw new UnsupportedOperationException("Cannot commmit, this is a read-only session.");
	}

	@Override
	public ResourceAccess resources() {
		throw new UnsupportedOperationException("Method 'ReadOnlyPersistenceSession.resources' is not supported!");
	}

	@Override
	protected IncrementalAccess getIncrementalAccess() {
		return access;
	}

}
