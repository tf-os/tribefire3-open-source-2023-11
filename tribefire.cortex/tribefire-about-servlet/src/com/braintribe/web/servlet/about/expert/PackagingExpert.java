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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.platform.setup.api.data.SetupDescriptor;
import com.braintribe.model.platformreflection.PackagingInformation;
import com.braintribe.model.platformreflection.request.GetPackagingInformation;
import com.braintribe.model.platformreflection.request.GetSetupDescriptor;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.processing.service.common.topology.InstanceIdComparator;
import com.braintribe.model.service.api.CompositeRequest;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.CompositeResponse;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.web.servlet.TypedVelocityContext;

public class PackagingExpert {

	private static Logger logger = Logger.getLogger(PackagingExpert.class);

	public void processPackagingRequest(Evaluator<ServiceRequest> requestEvaluator, Collection<InstanceId> selectedServiceInstances,
			TypedVelocityContext context, String userSessionId, ExecutorService executor) {

		logger.debug(() -> "Sending a request to return packaging information to " + selectedServiceInstances + " with session " + userSessionId);

		Map<InstanceId, PackagingInformation> packagingInfoMap = new ConcurrentHashMap<>();
		Map<InstanceId, SetupDescriptor> setupDescriptorMap = new ConcurrentHashMap<>();
		Set<InstanceId> returnedInstanceIds = Collections.synchronizedSet(new TreeSet<>(InstanceIdComparator.instance));

		AbstractMulticastingExpert.execute(selectedServiceInstances, executor, "Packaging", i -> {

			GetPackagingInformation getPackaging = GetPackagingInformation.T.create();

			GetSetupDescriptor getSetupDescriptor = GetSetupDescriptor.T.create();

			CompositeRequest compositeRequest = CompositeRequest.T.create();
			compositeRequest.setContinueOnFailure(true);
			compositeRequest.setParallelize(true);
			compositeRequest.getRequests().add(getPackaging);
			compositeRequest.getRequests().add(getSetupDescriptor);

			MulticastRequest mcR = MulticastRequest.T.create();
			mcR.setServiceRequest(compositeRequest);
			mcR.setAddressee(i);
			mcR.setTimeout((long) Numbers.MILLISECONDS_PER_MINUTE);
			mcR.setSessionId(userSessionId);
			EvalContext<? extends MulticastResponse> eval = mcR.eval(requestEvaluator);
			MulticastResponse multicastResponse = eval.get();

			for (Map.Entry<InstanceId, ServiceResult> entry : multicastResponse.getResponses().entrySet()) {

				InstanceId instanceId = entry.getKey();

				logger.debug(() -> "Received a response from instance: " + instanceId);

				ServiceResult result = entry.getValue();

				if (result instanceof Failure) {
					Throwable throwable = FailureCodec.INSTANCE.decode(result.asFailure());
					logger.error("Received failure from " + instanceId, throwable);
				} else if (result instanceof ResponseEnvelope) {

					ResponseEnvelope envelope = (ResponseEnvelope) result;
					Object resultObject = envelope.getResult();

					if (resultObject instanceof CompositeResponse) {
						CompositeResponse cr = (CompositeResponse) resultObject;
						List<ServiceResult> results = cr.getResults();

						for (ServiceResult sr : results) {

							if (sr instanceof Failure) {
								Throwable throwable = FailureCodec.INSTANCE.decode(result.asFailure());
								logger.error("Received failure from " + instanceId, throwable);
							} else if (sr instanceof ResponseEnvelope) {

								ResponseEnvelope subEnvelope = (ResponseEnvelope) sr;
								Object subObject = subEnvelope.getResult();
								returnedInstanceIds.add(instanceId);

								if (subObject instanceof PackagingInformation) {
									PackagingInformation packagingInformation = (PackagingInformation) subObject;
									packagingInfoMap.put(instanceId, packagingInformation);
								} else if (subObject instanceof SetupDescriptor) {
									SetupDescriptor sd = (SetupDescriptor) subObject;
									setupDescriptorMap.put(instanceId, sd);
								}

							}

						}

					}

				} else {
					logger.error("Unsupported response type: " + result);
				}

			}
		});

		Map<InstanceId, Pair<PackagingInformation, SetupDescriptor>> infoMap = new TreeMap<>(InstanceIdComparator.instance);
		for (InstanceId id : returnedInstanceIds) {
			infoMap.put(id, new Pair<>(packagingInfoMap.get(id), setupDescriptorMap.get(id)));
		}

		context.put("packagingMap", infoMap);

		logger.debug(() -> "Done with processing a request to return packaging information and the setup descriptor.");
	}
}
