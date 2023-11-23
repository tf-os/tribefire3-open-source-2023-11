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
package com.braintribe.model.processing.session.impl.persistence;

import static com.braintribe.utils.promise.JsPromiseCallback.promisify;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.processing.session.api.service.EnvelopeSessionAspect;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.processing.async.api.JsPromise;

public class TransientPersistenceGmSession extends AbstractPersistenceGmSession {

	@Override
	@SuppressWarnings("unusable-by-js")
	public ResourceAccess resources() {
		throw new UnsupportedOperationException("ResourceAccess not supported by " + getClass().getName());
	}

	@Override
	protected IncrementalAccess getIncrementalAccess() {
		return getBackup();
	}

	@Override
	protected EntityQueryResult postProcess(EntityQueryResult result) throws GmSessionException {
		return result;
	}

	@Override
	protected void postProcess(EntityQueryResult result, AsyncCallback<EntityQueryResult> callback, boolean detached)
			throws GmSessionException {
		callback.onSuccess(result);
	}

	@Override
	protected PropertyQueryResult postProcess(PropertyQueryResult result) throws GmSessionException {
		return result;
	}

	@Override
	protected void postProcess(PropertyQueryResult result, AsyncCallback<PropertyQueryResult> callback, boolean detached)
			throws GmSessionException {
		callback.onSuccess(result);
	}

	@Override
	protected SelectQueryResult postProcess(SelectQueryResult result) throws GmSessionException {
		return result;
	}

	@Override
	protected void postProcess(SelectQueryResult result, AsyncCallback<SelectQueryResult> callback, boolean detached)
			throws GmSessionException {
		callback.onSuccess(result);
	}
	
	@Override
	public ManipulationResponse commit() throws GmSessionException {
		transaction.clear();
		ManipulationResponse response = ManipulationResponse.T.create();
		return response;
	}
	
	@Override
	public JsPromise<ManipulationResponse> commitAsync() {
		return promisify(this::commit);
	}
	
	@Override
	@SuppressWarnings("unusable-by-js")
	public JsPromise<Object> evalAsync(ServiceRequest sr){
		return promisify(eval(sr).with(EnvelopeSessionAspect.class, this)::get);
	}
	
	@Override
	@SuppressWarnings("unusable-by-js")
	public void commit(AsyncCallback<ManipulationResponse> callback) {
		callback.onSuccess(commit());
	}
}
