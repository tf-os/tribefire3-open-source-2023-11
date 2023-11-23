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
package com.braintribe.model.processing.securityservice.impl;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.securityservice.api.SecurityService;
import com.braintribe.model.processing.securityservice.api.exceptions.AuthenticationException;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.securityservice.LogoutSession;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.ValidateUserSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.processing.async.api.AsyncCallback;

public class EvaluatingSecurityService implements SecurityService {
	private final Evaluator<ServiceRequest> evaluator;
	private AsyncCallback<Object> callback;

	public EvaluatingSecurityService(Evaluator<ServiceRequest> evaluator) {
		super();
		this.evaluator = evaluator;
	}
	
	public EvaluatingSecurityService(Evaluator<ServiceRequest> evaluator, AsyncCallback<Object> callback) {
		super();
		this.evaluator = evaluator;
		this.callback = callback;
	}

	@Override
	public OpenUserSessionResponse openUserSession(OpenUserSession request) throws AuthenticationException {
		return request.eval(evaluator).get();
	}

	@Override
	public boolean isSessionValid(String sessionId) throws SecurityServiceException {
		ValidateUserSession req = ValidateUserSession.T.create();
		req.setSessionId(sessionId);
		EvalContext<? extends UserSession> evalContext = req.eval(evaluator);
		
		if (callback != null) {
			evalContext.get(callback);
			return true;
		}
		
		UserSession userSession = evalContext.get();
		return userSession != null && !userSession.getIsInvalidated();
	}

	@Override
	public boolean logout(String sessionId) throws SecurityServiceException {
		LogoutSession logout = LogoutSession.T.create();
		logout.setSessionId(sessionId);
		return logout.eval(evaluator).get(); 
	}
}
