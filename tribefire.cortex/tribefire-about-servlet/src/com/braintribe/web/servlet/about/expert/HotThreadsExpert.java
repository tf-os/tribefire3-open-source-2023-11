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
package com.braintribe.web.servlet.about.expert;

import static com.braintribe.web.servlet.about.ParameterTools.getSingleParameterAsBoolean;
import static com.braintribe.web.servlet.about.ParameterTools.getSingleParameterAsInteger;
import static com.braintribe.web.servlet.about.ParameterTools.getSingleParameterAsLong;
import static com.braintribe.web.servlet.about.ParameterTools.getSingleParameterAsString;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.platformreflection.hotthreads.HotThreads;
import com.braintribe.model.platformreflection.request.GetHotThreads;
import com.braintribe.model.processing.platformreflection.java.PlatformReflectionTools;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.web.servlet.TypedVelocityContext;

public class HotThreadsExpert {

	private static Logger logger = Logger.getLogger(HotThreadsExpert.class);

	public void processHotThreadsRequest(Evaluator<ServiceRequest> requestEvaluator, Collection<InstanceId> selectedServiceInstances,
			HttpServletRequest request, TypedVelocityContext context, String userSessionId, ExecutorService executor) {

		logger.debug(() -> "Sending a request to return hot threads to " + selectedServiceInstances + " with session " + userSessionId);

		final Map<String, String> hotThreadsMap = Collections.synchronizedMap(new TreeMap<>());

		AbstractMulticastingExpert.execute(selectedServiceInstances, executor, "HotThreads", i -> {
			GetHotThreads getHotThreads = GetHotThreads.T.create();
			getHotThreads.setInterval(getSingleParameterAsLong(request, "interval"));
			getHotThreads.setThreads(getSingleParameterAsInteger(request, "threads"));
			getHotThreads.setIgnoreIdleThreads(getSingleParameterAsBoolean(request, "ignoreIdleThreads"));
			getHotThreads.setSampleType(getSingleParameterAsString(request, "sampleType"));
			getHotThreads.setThreadElementsSnapshotCount(getSingleParameterAsInteger(request, "threadElementsSnapshotCount"));
			getHotThreads.setThreadElementsSnapshotDelayInMs(getSingleParameterAsLong(request, "threadElementsSnapshotDelayInMs"));

			MulticastRequest mcR = MulticastRequest.T.create();
			mcR.setAsynchronous(false);
			mcR.setServiceRequest(getHotThreads);
			mcR.setAddressee(i);
			mcR.setTimeout((long) Numbers.MILLISECONDS_PER_MINUTE);
			mcR.setSessionId(userSessionId);
			EvalContext<? extends MulticastResponse> eval = mcR.eval(requestEvaluator);
			MulticastResponse multicastResponse = eval.get();

			for (Map.Entry<InstanceId, ServiceResult> entry : multicastResponse.getResponses().entrySet()) {

				InstanceId instanceId = entry.getKey();

				logger.debug(() -> "Received a response from instance: " + instanceId);

				String nodeId = instanceId.getNodeId();

				ServiceResult result = entry.getValue();
				if (result instanceof Failure) {
					Throwable throwable = FailureCodec.INSTANCE.decode(result.asFailure());
					logger.error("Received failure from " + instanceId, throwable);
				} else if (result instanceof ResponseEnvelope) {

					ResponseEnvelope envelope = (ResponseEnvelope) result;
					HotThreads hotThreads = (HotThreads) envelope.getResult();

					String stringRepresentation = PlatformReflectionTools.toString(hotThreads);

					hotThreadsMap.put(nodeId, stringRepresentation);

				} else {
					logger.error("Unsupported response type: " + result);
				}

			}
		});

		context.put("hotthreadsMap", hotThreadsMap);

		logger.debug(() -> "Done with processing a request to return hot threads.");
	}
}
