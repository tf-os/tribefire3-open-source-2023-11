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
package com.braintribe.gwt.gmsession.client;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.common.EvaluatingAccessService;
import com.braintribe.model.access.impl.AccessServiceDelegatingAccess;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.processing.session.api.persistence.AccessDescriptor;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.processing.async.api.AsyncCallback;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

@JsType
@SuppressWarnings("unusable-by-js")
public class AccessServiceGwtPersistenceGmSession extends GwtPersistenceGmSession implements ModelEnvironmentDrivenGmSession {

	protected ModelEnvironment modelEnvironment;

	@Override
	public void configureModelEnvironment(ModelEnvironment me) {
		cleanup();
		configureAccessDescriptor(new AccessDescriptor(me.getDataAccessId(), me.getDataModel(), me.getDataAccessDenotationType()));
		this.modelEnvironment = me;
	}

	@Override
	public void cleanup() {
		super.cleanup();
		this.modelEnvironment = null;

	}

	@JsIgnore
	@Override
	public void configureModelEnvironment(ModelEnvironment modelEnvironment, AsyncCallback<Void> asyncCallback) {
		try {
			configureModelEnvironment(modelEnvironment);
			asyncCallback.onSuccess(null);
		} catch (Throwable t) {
			asyncCallback.onFailure(t);
		}
	}

	@Override
	public ModelEnvironment getModelEnvironment() {
		return modelEnvironment;
	}
	
	@Override
	protected IncrementalAccess getIncrementalAccess() {
		AccessServiceDelegatingAccess access = new AccessServiceDelegatingAccess();
		access.setAccessId(modelEnvironment.getDataAccessId());
		access.setAccessService(new EvaluatingAccessService(getRequestEvaluator()));
		return access;
	}

}
