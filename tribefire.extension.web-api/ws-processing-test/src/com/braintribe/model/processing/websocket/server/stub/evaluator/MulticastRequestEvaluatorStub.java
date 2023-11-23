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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.EvalContextAspect;
import com.braintribe.model.generic.eval.EvalException;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.service.commons.StandardServiceRequestContext;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.InternalPushRequest;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.PushResponse;
import com.braintribe.model.service.api.result.PushResponseMessage;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.processing.async.api.AsyncCallback;

public class MulticastRequestEvaluatorStub extends StandardServiceRequestContext {

	public MulticastRequestEvaluatorStub() {
		super(new EvaluatorImpl());
	}

	private static class TestEvalContext<T> implements EvalContext<T> {

		private final T result;

		public TestEvalContext(T result) {
			this.result = result;
		}

		@Override
		public T get() throws EvalException {

			MulticastRequest multicastRequest = (MulticastRequest) result;
			MulticastResponse multicastResponse = MulticastResponse.T.create();

			InternalPushRequest internalRequest = (InternalPushRequest) multicastRequest.getServiceRequest();
			
			Map<InstanceId, ServiceResult> responses = new LinkedHashMap<>();

			if (!"role_pattern".equals(internalRequest.getRolePattern())
					|| !"client_id_pattern".equals(internalRequest.getClientIdPattern())
					|| !"session_id_pattern".equals(internalRequest.getSessionIdPattern()) || internalRequest.getServiceRequest() == null) {
				throw new EvalException("Unexpected format of internal push request");
			}

			// result stub
			ResponseEnvelope success = ResponseEnvelope.T.create();
			PushResponse pushResponse = PushResponse.T.create();
			List<PushResponseMessage> pushResponseMessages = new ArrayList<>();

			pushResponseMessages.add(creatPushResponseMessageStub("msg1", true, "client_id", "master1"));
			pushResponseMessages.add(creatPushResponseMessageStub("msg2", true, "client_id", "master1"));

			pushResponse.setResponseMessages(pushResponseMessages);
			success.setResult(pushResponse);

			responses.put(InstanceId.T.create("master1"), success);

			success = ResponseEnvelope.T.create();
			pushResponse = PushResponse.T.create();
			pushResponseMessages = new ArrayList<>();

			pushResponseMessages.add(creatPushResponseMessageStub("msg", true, "client_id", "master2"));

			pushResponse.setResponseMessages(pushResponseMessages);
			success.setResult(pushResponse);

			responses.put(InstanceId.T.create("master2"), success);

			Failure failure = Failure.T.create();
			failure.setMessage("f-msg");

			responses.put(InstanceId.T.create("master3"), failure);

			multicastResponse.setResponses(responses);
			return (T) multicastResponse;
		}

		private PushResponseMessage creatPushResponseMessageStub(String msg, boolean success, String clientId, String originId) {
			PushResponseMessage responseMessage = PushResponseMessage.T.create();
			responseMessage.setMessage(msg);
			responseMessage.setSuccessful(success);
			responseMessage.setClientIdentification(clientId);
			responseMessage.setOriginId(InstanceId.T.create(originId));
			return responseMessage;
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

	private static class EvaluatorImpl implements Evaluator<ServiceRequest> {

		@Override
		public <T> EvalContext<T> eval(ServiceRequest evaluable) {
			return new TestEvalContext<T>((T) evaluable);
		}
		
	}

}
