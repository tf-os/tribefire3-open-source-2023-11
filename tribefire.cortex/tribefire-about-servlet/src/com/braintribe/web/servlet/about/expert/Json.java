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

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletResponse;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.platformreflection.PlatformReflectionJson;
import com.braintribe.model.platformreflection.request.ReflectPlatformJson;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.utils.DateTools;

public class Json {

	private static Logger logger = Logger.getLogger(Json.class);

	public void processJsonRequest(Evaluator<ServiceRequest> requestEvaluator, Collection<InstanceId> selectedServiceInstances,
			HttpServletResponse resp, String userSessionId, ExecutorService executor) throws IOException {

		logger.debug(() -> "Processing a request to create a JSON response.");

		ReentrantLock lock = new ReentrantLock();
		StringBuilder sb = new StringBuilder();
		sb.append("[\n");

		AbstractMulticastingExpert.execute(selectedServiceInstances, executor, "Json", i -> {

			// First, we prepare the actual content
			ReflectPlatformJson reflectPlatform = ReflectPlatformJson.T.create();

			MulticastRequest mcR = MulticastRequest.T.create();
			mcR.setAsynchronous(false);
			mcR.setServiceRequest(reflectPlatform);
			mcR.setAddressee(i);
			mcR.setTimeout((long) Numbers.MILLISECONDS_PER_MINUTE);
			mcR.setSessionId(userSessionId);
			EvalContext<? extends MulticastResponse> eval = mcR.eval(requestEvaluator);
			MulticastResponse multicastResponse = eval.get();

			for (Map.Entry<InstanceId, ServiceResult> entry : multicastResponse.getResponses().entrySet()) {
				InstanceId instance = entry.getKey();

				logger.debug(() -> "Received a response from instance: " + instance);

				ServiceResult result = entry.getValue();
				if (result instanceof Failure) {
					Throwable throwable = FailureCodec.INSTANCE.decode(result.asFailure());
					logger.error("Received failure from " + instance, throwable);
				} else if (result instanceof ResponseEnvelope) {

					ResponseEnvelope envelope = (ResponseEnvelope) result;
					PlatformReflectionJson platformReflection = (PlatformReflectionJson) envelope.getResult();
					lock.lock();
					try {
						if (sb.toString().endsWith("}")) {
							sb.append(",\n");
						}
						sb.append("\n{");
						sb.append("  \"" + instance + "\" : \n");
						sb.append(platformReflection.getPlatformReflectionJson());
						sb.append("\n}");
					} finally {
						lock.unlock();
					}

				} else {
					logger.error("Unsupported response type: " + result);
				}

			}

		});

		sb.append("\n]");

		String now = DateTools.encode(new Date(), DateTools.TERSE_DATETIME_FORMAT_2);

		// Only when we were successful up to this point, we actually write the content to the client
		// This allows the ExceptionFilter to be able to write to the response stream without problems
		resp.setContentType("application/json; charset=UTF-8");
		resp.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", "sysinfo-" + now + ".json"));
		resp.getWriter().append(sb);

		logger.debug(() -> "Done with processing a request to create a JSON response.");
	}

}
