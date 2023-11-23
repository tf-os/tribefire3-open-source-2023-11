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

import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.gwt.tribefirejs.client.TribefireRuntime;
import com.braintribe.gwt.tribefirejs.client.remote.api.RpcSession;
import com.braintribe.gwt.tribefirejs.client.remote.api.TribefireRemoteSession;
import com.braintribe.gwt.tribefirejs.client.remote.api.TribefireRemoteSessionFactory;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.processing.async.api.JsPromise;
import com.braintribe.utils.promise.JsPromiseCallback;

import tribefire.js.model.config.RemoteSessionConfig;

public class TribefireRemoteSessionFactoryImpl implements TribefireRemoteSessionFactory {
	private Function<String, RpcSession> rpcSessionFactory;
	
	@Configurable @Required
	public void setRemoteEvaluatorFactory(Function<String, RpcSession> rpcSessionFactory) {
		this.rpcSessionFactory = rpcSessionFactory;
	}

	private String getEffectiveServicesUrl(RemoteSessionConfig config) {
		String servicesUrl = config.getServicesUrl();
		if (servicesUrl == null)
			servicesUrl = TribefireRuntime.getProperty("TRIBEFIRE_PUBLIC_SERVICES_URL", "/tribefire-services");
		return servicesUrl;
	}
	
	/*private String getSessionIdCookieName() {
		return TribefireRuntime.getProperty("TRIBEFIRE_SESSION_ID_COOKIE_NAME", "tfsessionId");
	}*/
	
	@Override
	public JsPromise<Object> eval(String servicesUrl, ServiceRequest request) {		
		JsPromiseCallback<Object> callback = new JsPromiseCallback<>();
		
		RpcSession rpcSession = rpcSessionFactory.apply(servicesUrl + "/rpc");
		
		Evaluator<ServiceRequest> remoteEvaluator = rpcSession.evaluator();
		
		remoteEvaluator.eval(request).get(callback);
		
		return callback.asPromise();
	}
	
	@Override
	@SuppressWarnings("unusable-by-js")
	public JsPromise<TribefireRemoteSession> open(RemoteSessionConfig config) {
		
		JsPromiseCallback<TribefireRemoteSession> callback = new JsPromiseCallback<>();
		
		String servicesUrl = getEffectiveServicesUrl(config);
		
		RpcSession rpcSession = rpcSessionFactory.apply(servicesUrl + "/rpc");
		
		Evaluator<ServiceRequest> remoteEvaluator = rpcSession.evaluator(); 
		
		Credentials credentials = config.getCredentials();
		
		// TODO: check cookie -> TfJsCookieSessionIdProvider
		
		OpenUserSession openUserSession = OpenUserSession.T.create();
		openUserSession.setCredentials(credentials);
		
		openUserSession.eval(remoteEvaluator).get(AsyncCallback.of(response -> {
			UserSession userSession = response.getUserSession();
			rpcSession.sessionIdHolder().accept(userSession.getSessionId());
			
			RawPersistenceSessionFactory rawPersistenceSessionFactory = new RawPersistenceSessionFactory(rpcSession, servicesUrl, userSession);

			TribefireRemoteSessionImpl tribefireRemoteSession = new TribefireRemoteSessionImpl(response.getUserSession(), remoteEvaluator, rawPersistenceSessionFactory::createRawSession);
			callback.onSuccess(tribefireRemoteSession);
		}, e -> callback.onFailure(e)));
		
		return callback.asPromise();
	}
}
