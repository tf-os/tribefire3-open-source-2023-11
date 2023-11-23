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
package tribefire.platform.impl.model;

import java.util.Objects;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.modelnotification.InternalModelNotificationRequest;
import com.braintribe.model.modelnotification.InternalOnModelChanged;
import com.braintribe.model.modelnotification.ModelNotificationRequest;
import com.braintribe.model.modelnotification.ModelNotificationResponse;
import com.braintribe.model.modelnotification.OnModelChanged;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * <p>
 * A {@link ServiceProcessor} which broadcasts the incoming {@link OnModelChanged} signal to all available cartridge nodes.
 * 
 */
public class ModelNotificationProcessor extends AbstractDispatchingServiceProcessor<ModelNotificationRequest, ModelNotificationResponse> {

	// constants
	private static final Logger log = Logger.getLogger(ModelNotificationProcessor.class);

	@Override
	protected void configureDispatching(DispatchConfiguration<ModelNotificationRequest, ModelNotificationResponse> dispatching) {
		dispatching.register(OnModelChanged.T, this::onModelChange);
	}

	private ModelNotificationResponse onModelChange(ServiceRequestContext requestContext, OnModelChanged request) {

		String modelName = request.getModelName();

		Objects.requireNonNull(modelName, "Model name must not be null");

		InternalOnModelChanged internalRequest = InternalOnModelChanged.T.create();
		internalRequest.setModelName(modelName);

		notifyChange(requestContext, internalRequest);

		log.debug(() -> "Notified change on " + modelName);

		return ModelNotificationResponse.T.create();

	}

	private void notifyChange(Evaluator<ServiceRequest> requestEvaluator, InternalModelNotificationRequest request) {
		MulticastRequest multiRequest = MulticastRequest.T.create();
		multiRequest.setServiceRequest(request);
		multiRequest.eval(requestEvaluator).get();

		log.trace(() -> "Notified " + request);
	}

}
