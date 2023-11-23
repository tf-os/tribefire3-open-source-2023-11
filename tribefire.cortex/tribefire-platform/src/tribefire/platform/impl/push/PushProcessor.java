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
package tribefire.platform.impl.push;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.InternalPushRequest;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.PushRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.PushResponse;
import com.braintribe.model.service.api.result.PushResponseMessage;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;

/**
 * 
 * This class handles:
 * 
 * <ol>
 * <li>{@link PushRequest} in its {@link #push(ServiceRequestContext, PushRequest)} method which converts the request into an
 * {@link InternalPushRequest} and broadcasts it to all master (tribefire-services) instances in the cluster. </br>
 * This class handles a {@link PushRequest} in its {@link #push(ServiceRequestContext, PushRequest)} method which converts the request into
 * <li>{@link InternalPushRequest} in its {@link #internalPush(ServiceRequestContext, InternalPushRequest)} where it is dispatched on the configured
 * {@link #addHandler(ServiceProcessor) handlers}
 * </ol>
 * 
 * @author gunther.schenk
 * @author Dirk Scheffler
 *
 */
public class PushProcessor extends AbstractDispatchingServiceProcessor<PushRequest, PushResponse> implements InitializationAware {

	private static final Logger logger = Logger.getLogger(PushProcessor.class);

	// ############################## Configurable members ##############################

	private Long requestTimeout;
	private String targetApplicationId = TribefireConstants.TRIBEFIRE_SERVICES_APPLICATION_ID;
	private final List<ServiceProcessor<InternalPushRequest, PushResponse>> handlers = Collections.synchronizedList(new ArrayList<>());

	// ############################## Internally used members ##############################

	private InstanceId targetInstanceId;

	// ############################## Setters ##############################

	@Configurable
	public void setRequestTimeout(Long requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	@Configurable
	public void setTargetApplicationId(String targetApplicationId) {
		this.targetApplicationId = targetApplicationId;
	}

	/**
	 * Adds a handler that will further process internal requests
	 * 
	 * Note: This method is thread-safe.
	 */
	@Configurable
	public void addHandler(ServiceProcessor<InternalPushRequest, PushResponse> handler) {
		handlers.add(handler);
	}

	// ############################## Initializing ##############################

	@Override
	public void postConstruct() {
		targetInstanceId = InstanceId.T.create();
		targetInstanceId.setApplicationId(targetApplicationId);
	}

	// ############################## DDSA ##############################

	@Override
	protected void configureDispatching(DispatchConfiguration<PushRequest, PushResponse> dispatching) {
		dispatching.register(PushRequest.T, this::push);
		dispatching.register(InternalPushRequest.T, this::internalPush);

	}

	private PushResponse internalPush(ServiceRequestContext requestContext, InternalPushRequest request) {
		PushResponse response = PushResponse.T.create();

		for (ServiceProcessor<InternalPushRequest, PushResponse> handler : handlers) {
			try {
				PushResponse handlerResponse = handler.process(requestContext, request);
				response.getResponseMessages().addAll(handlerResponse.getResponseMessages());
			} catch (Exception e) {
				logger.error("Error while executing push handler: " + handler, e);
			}
		}

		return response;
	}

	/**
	 * This method processes initial {@link PushRequest} usually evaluated via RPC. </br>
	 * It converts (clones) the passed instance into a {@link InternalPushRequest} instance and broadcasts it to all master (tribefire-services)
	 * instances in the cluster (by wrapping it with a {@link MulticastRequest}). </br>
	 * </br>
	 * 
	 * The individual response messages (from individual {@link PushResponse}s) are collected and accumulated into a single PushResponse which is then
	 * returned to the caller.
	 */
	private PushResponse push(ServiceRequestContext requestContext, PushRequest request) {
		InternalPushRequest internalRequest = cloneToInternalRequest(request);
		MulticastResponse multicastResponse = broadcast(requestContext, internalRequest);

		PushResponse response = PushResponse.T.create();

		Map<InstanceId, ServiceResult> responses = multicastResponse.getResponses();
		for (Map.Entry<InstanceId, ServiceResult> responseEntry : responses.entrySet()) {

			InstanceId instanceId = responseEntry.getKey();
			ServiceResult result = responseEntry.getValue();

			switch (result.resultType()) {
				case success:
					PushResponse individualResponse = (PushResponse) ((ResponseEnvelope) result).getResult();
					if (individualResponse.sentMessages()) {
						response.getResponseMessages().addAll(individualResponse.getResponseMessages());
					}
					break;
				case failure:
					Failure failureResponse = (Failure) result;
					PushResponseMessage failureMessage = PushResponseMessage.T.create();
					failureMessage.setMessage(failureResponse.getMessage());
					failureMessage.setSuccessful(false);
					failureMessage.setOriginId(instanceId);
					response.getResponseMessages().add(failureMessage);
					break;
				default:
					logger.warn("Unsupported multicast response recieved from instance: " + instanceId + ". result: " + result);
			}
		}
		return response;
	}

	/**
	 * <p>
	 * Broadcasts the given {@link InternalPushRequest} to every available tribefire-services (master) instances.
	 * 
	 * @param evaluator
	 *            The master {@link Evaluator} of {@link ServiceRequest serviceRequests}
	 * @param internalRequest
	 *            The {@link InternalPushRequest} to be broadcasted to every available tribefire-services (master) instances.
	 * @return The wrapping the results from the reached instances.
	 */
	private MulticastResponse broadcast(Evaluator<ServiceRequest> evaluator, InternalPushRequest internalRequest) {
		MulticastRequest multicastRequest = MulticastRequest.T.create();
		multicastRequest.setServiceRequest(internalRequest);
		multicastRequest.setAddressee(targetInstanceId);
		if (requestTimeout != null) {
			multicastRequest.setTimeout(requestTimeout);
		}

		MulticastResponse multicastResponse = multicastRequest.eval(evaluator).get();
		return multicastResponse;
	}

	/**
	 * Clones the passed PushRequest into an instance of {@link InternalPushRequest}
	 */
	private InternalPushRequest cloneToInternalRequest(PushRequest request) {
		InternalPushRequest internalPush = InternalPushRequest.T.create();

		for (Property property : request.entityType().getProperties()) {
			internalPush.write(property, request.read(property));
		}

		return internalPush;
	}
}
