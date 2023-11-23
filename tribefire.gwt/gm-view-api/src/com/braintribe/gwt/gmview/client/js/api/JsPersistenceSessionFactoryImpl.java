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
package com.braintribe.gwt.gmview.client.js.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gm.model.persistence.reflection.api.GetModelEnvironment;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.client.js.JsPersistenceSessionFactory;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.processing.async.api.JsPromise;

public class JsPersistenceSessionFactoryImpl implements JsPersistenceSessionFactory{
	
	private final Evaluator<ServiceRequest> remoteEvaluator;
	private final Map<String, Future<PersistenceGmSession>> sessions = new HashMap<>();
	private final Supplier<ModelEnvironmentDrivenGmSession> rawSessionFactory;
	
	public JsPersistenceSessionFactoryImpl(Evaluator<ServiceRequest> remoteEvaluator, Supplier<ModelEnvironmentDrivenGmSession> rawSessionFactory) {
		super();
		this.remoteEvaluator = remoteEvaluator;
		this.rawSessionFactory = rawSessionFactory;
	}

	@Override
	public JsPromise<PersistenceGmSession> openPersistenceSession(String accessId) {
		return acquirePersistenceSession(accessId).toJsPromise();
	}
	
	private Future<PersistenceGmSession> acquirePersistenceSession(String accessId) {
		return sessions.computeIfAbsent(accessId, this::buildSession);
	}
	
	private Future<PersistenceGmSession> buildSession(String accessId) {
		Future<PersistenceGmSession> promise = new Future<>();
		GetModelEnvironment getModelEnvironment = GetModelEnvironment.T.create();
		getModelEnvironment.setAccessId(accessId);
		
		getModelEnvironment.eval(remoteEvaluator).get(AsyncCallback.of( //
				modelEnvironment -> {
					GmMetaModel serviceModel = modelEnvironment.getServiceModel();
					GmMetaModel dataModel = modelEnvironment.getDataModel();

					GmMetaModel aggregatorModel = GmMetaModel.T.create();
					aggregatorModel.setName("virtual-aggregator");
					if (serviceModel != null)
						aggregatorModel.getDependencies().add(serviceModel);
					if (dataModel != null)
						aggregatorModel.getDependencies().add(dataModel);

					GMF.getTypeReflection().deploy(aggregatorModel, AsyncCallback.of(future -> {
						ModelEnvironmentDrivenGmSession session = rawSessionFactory.get();
						session.configureModelEnvironment(modelEnvironment);
						promise.onSuccess(session);
					}, promise::onFailure));
				}, promise::onFailure));
		
		return promise;
	}
	
}
