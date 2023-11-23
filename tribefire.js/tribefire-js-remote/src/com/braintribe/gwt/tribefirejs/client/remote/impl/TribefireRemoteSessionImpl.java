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
package com.braintribe.gwt.tribefirejs.client.remote.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gm.model.persistence.reflection.api.GetModelEnvironment;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.tribefirejs.client.TfJsNameSpaces;
import com.braintribe.gwt.tribefirejs.client.remote.api.PersistenceSessionFactory;
import com.braintribe.gwt.tribefirejs.client.remote.api.TribefireRemoteSession;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.processing.async.api.JsPromise;

import jsinterop.annotations.JsType;

@JsType(namespace=TfJsNameSpaces.REMOTE)
@SuppressWarnings("unusable-by-js")
public class TribefireRemoteSessionImpl implements TribefireRemoteSession {
	private final UserSession userSession;
	private final Evaluator<ServiceRequest> remoteEvaluator;
	private final Map<String, Future<PersistenceSessionFactory>> factories = new HashMap<>();
	private final Supplier<PersistenceGmSession> rawSessionFactory;
	
	public TribefireRemoteSessionImpl(UserSession userSession, Evaluator<ServiceRequest> remoteEvaluator, Supplier<PersistenceGmSession> rawSessionFactory) {
		super();
		this.userSession = userSession;
		this.remoteEvaluator = remoteEvaluator;
		this.rawSessionFactory = rawSessionFactory;
	}

	@Override
	public JsPromise<PersistenceSessionFactory> getPeristenceSessionFactory(String accessId) {
		return acquirePersistenceSessionFactory(accessId).toJsPromise();
	}

	private Future<PersistenceSessionFactory> acquirePersistenceSessionFactory(String accessId) {
		return factories.computeIfAbsent(accessId, this::buildSessionFactory);
	}
	
	@Override
	public JsPromise<PersistenceGmSession> openPersistenceSession(String accessId) {
		return acquirePersistenceSessionFactory(accessId) //
				.andThenMap(PersistenceSessionFactory::openSession) //
				.toJsPromise();
	}

	private Future<PersistenceSessionFactory> buildSessionFactory(String accessId) {
		Future<PersistenceSessionFactory> promise = new Future<>();
		GetModelEnvironment getModelEnvironment = GetModelEnvironment.T.create();
		getModelEnvironment.setAccessId(accessId);
		
		getModelEnvironment.eval(remoteEvaluator).get(AsyncCallback.of(modelEnvironment -> {
			GmMetaModel serviceModel = modelEnvironment.getServiceModel();
			GmMetaModel dataModel = modelEnvironment.getDataModel();
			
			GmMetaModel aggregatorModel = GmMetaModel.T.create();
			aggregatorModel.setName("virtual-aggregator");
			if(serviceModel != null)
				aggregatorModel.getDependencies().add(serviceModel);
			if(dataModel != null)
				aggregatorModel.getDependencies().add(dataModel);
			
			GMF.getTypeReflection().deploy(aggregatorModel, AsyncCallback.of(v -> {
				promise.onSuccess(() -> {
					PersistenceGmSession session = rawSessionFactory.get();
					if (session instanceof ModelEnvironmentDrivenGmSession)
						((ModelEnvironmentDrivenGmSession) session).configureModelEnvironment(modelEnvironment);
					return session;
				});
			}, e -> promise.onFailure(e)));
		}, e -> promise.onFailure(e)));
		
		return promise;
	}
	
	@Override
	public UserSession getUserSession() {
		return userSession;
	}

	@Override
	public Evaluator<ServiceRequest> getEvaluator() {
		return remoteEvaluator;
	}
}
