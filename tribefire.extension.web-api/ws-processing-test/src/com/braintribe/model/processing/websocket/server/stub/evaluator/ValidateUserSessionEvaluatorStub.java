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
package com.braintribe.model.processing.websocket.server.stub.evaluator;

import java.util.Arrays;
import java.util.HashSet;

import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.EvalContextAspect;
import com.braintribe.model.generic.eval.EvalException;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.securityservice.ValidateUserSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.processing.async.api.AsyncCallback;

public class ValidateUserSessionEvaluatorStub implements Evaluator<ServiceRequest> {

	private static class TestEvalContext<T> implements EvalContext<T> {
		
		private final T result;

		public TestEvalContext(T result) {
			this.result = result;
		}

		@Override
		public T get() throws EvalException {
			
			ValidateUserSession validateUserSession = (ValidateUserSession) result;
			if(!validateUserSession.getSessionId().equals("session_id_1") && !validateUserSession.getSessionId().equals("session_id_2") && !validateUserSession.getSessionId().equals("session_id_3")) {
				throw new EvalException("Invalid session id");
			} 
			
			UserSession userSession = UserSession.T.create();
			if(validateUserSession.getSessionId().equals("session_id_1")) {
				userSession.setSessionId("session_id_1");
				userSession.setEffectiveRoles(new HashSet<>(Arrays.asList("tb-admin", "tb-user")));
			}
			
			if(validateUserSession.getSessionId().equals("session_id_2")) {
				userSession.setSessionId("session_id_2");
				userSession.setEffectiveRoles(new HashSet<>(Arrays.asList("tb-user")));
			}
			
			if(validateUserSession.getSessionId().equals("session_id_3")) {
				userSession.setSessionId("session_id_3");
				userSession.setEffectiveRoles(new HashSet<>(Arrays.asList("tb-guest")));
			}
			
			return (T) userSession;
		}

		@Override
		public void get(AsyncCallback<? super T> callback) {
			throw new NotImplementedException();
		}

		@Override
		public <U, A extends EvalContextAspect<? super U>> EvalContext<T> with(Class<A> aspect, U value) {
			throw new NotImplementedException();
		}
	}

	@Override
	public <T> EvalContext<T> eval(ServiceRequest evaluable) {
		return new TestEvalContext<T>((T) evaluable);
	}

}
