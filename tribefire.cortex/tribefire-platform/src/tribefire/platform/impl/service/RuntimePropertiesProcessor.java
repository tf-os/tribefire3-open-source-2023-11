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
package tribefire.platform.impl.service;

import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.cortexapi.service.MulticastScope;
import com.braintribe.model.cortexapi.service.SetRuntimeProperty;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;

public class RuntimePropertiesProcessor implements ServiceProcessor<SetRuntimeProperty, String>{

	private static Logger logger = Logger.getLogger(RuntimePropertiesProcessor.class);

	protected Evaluator<ServiceRequest> requestEvaluator;

	@Override
	public String process(ServiceRequestContext requestContext, SetRuntimeProperty request) {

		String sessionId = requestContext.getRequestorSessionId();

		MulticastScope multicastScope = request.getMulticastScope();
		if (multicastScope == null) {
			multicastScope = MulticastScope.none;
		}
		
		if (multicastScope != MulticastScope.none) {

			request.setMulticastScope(MulticastScope.none);
			
			MulticastRequest mcR = MulticastRequest.T.create();
			mcR.setAsynchronous(false);
			mcR.setServiceRequest(request);
			mcR.setTimeout((long) Numbers.MILLISECONDS_PER_SECOND * 10);
			mcR.setSessionId(sessionId);
			if (multicastScope == MulticastScope.masters) {
				mcR.setAddressee(InstanceId.of(null, TribefireConstants.TRIBEFIRE_SERVICES_APPLICATION_ID));
			}
			EvalContext<? extends MulticastResponse> eval = mcR.eval(requestEvaluator);
			MulticastResponse multicastResponse = eval.get();

			String oldValue = null;
			
			for (Map.Entry<InstanceId,ServiceResult>  entry : multicastResponse.getResponses().entrySet()) {

				InstanceId instanceId = entry.getKey();

				logger.debug(() -> "Received a response from instance: "+instanceId);

				ServiceResult result = entry.getValue();
				if (result instanceof Failure) {
					Throwable throwable = FailureCodec.INSTANCE.decode(result.asFailure());
					logger.error("Received failure from "+instanceId, throwable);
				} else if (result instanceof ResponseEnvelope) {

					ResponseEnvelope envelope = (ResponseEnvelope) result;
					String responseValue = (String) envelope.getResult();
					if (responseValue != null) {
						oldValue = responseValue;
					}

				} else {
					logger.error("Unsupported response type: "+result);
				}

			}

			return oldValue;

		} else {

			String requestorSessionId = requestContext.getRequestorSessionId();
			String name = requestContext.getRequestorUserName();

			String key = request.getKey();
			String newValue = request.getValue();

			if (logger.isDebugEnabled()) logger.debug("Got a request to set "+key+" to value "+newValue+" from user "+name+" in session "+requestorSessionId);

			String oldValue = TribefireRuntime.setProperty(key, newValue);

			return oldValue;

		}
	}

	@Configurable
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

}
