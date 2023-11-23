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
package tribefire.platform.impl.session;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.modelnotification.InternalModelNotificationRequest;
import com.braintribe.model.modelnotification.InternalModelNotificationResponse;
import com.braintribe.model.modelnotification.InternalOnAccessModelChanged;
import com.braintribe.model.modelnotification.InternalOnModelChanged;
import com.braintribe.model.modelnotification.InternalOnServiceDomainModelChanged;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.managed.ModelChangeListener;
import com.braintribe.model.processing.session.api.managed.NotifiableModelAccessoryFactory;

/**
 * A {@link ServiceProcessor} which notifies the changes represented by the incoming {@link InternalModelNotificationRequest}(s) to the configured
 * {@link NotifiableModelAccessoryFactory}.
 */
@SuppressWarnings({ "unused" })
public class InternalModelNotificationProcessor
		extends AbstractDispatchingServiceProcessor<InternalModelNotificationRequest, InternalModelNotificationResponse> {

	// constants
	private static final Logger log = Logger.getLogger(InternalModelNotificationProcessor.class);
	private static final InternalModelNotificationResponse standardResponse = InternalModelNotificationResponse.T.create();

	// configured
	private List<ModelChangeListener> modelChangeListeners = Collections.emptyList();
	private List<NotifiableModelAccessoryFactory> factories = Collections.emptyList();

	@Configurable
	public void setModelChangeListeners(List<? extends ModelChangeListener> modelChangeListeners) {
		this.modelChangeListeners = (List<ModelChangeListener>) modelChangeListeners;
	}

	@Configurable
	public void setModelAccessoryFactories(List<NotifiableModelAccessoryFactory> factories) {
		this.factories = requireNonNull(factories, "factories must not be null");
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<InternalModelNotificationRequest, InternalModelNotificationResponse> dispatching) {
		dispatching.register(InternalOnModelChanged.T, (c, r) -> onModelChange(r));
		dispatching.register(InternalOnAccessModelChanged.T, (c, r) -> onAccessModelChange(r));
		dispatching.register(InternalOnServiceDomainModelChanged.T, (c, r) -> onServiceDomainModelChanged(r));
	}

	private InternalModelNotificationResponse onModelChange(InternalOnModelChanged request) {
		String modelName = request.getModelName();

		for (ModelChangeListener supplier : modelChangeListeners) {
			try {
				supplier.onModelChange(modelName);
			} catch (Exception e) {
				log.error("Failed to notify change on model '" + modelName + "' to " + supplier, e);
			}
		}

		return standardResponse;
	}

	private InternalModelNotificationResponse onAccessModelChange(InternalOnAccessModelChanged request) {
		String accessId = request.getAccessId();

		for (NotifiableModelAccessoryFactory factory : factories) {
			try {
				factory.onAccessChange(accessId);
			} catch (Exception e) {
				log.error("Failed to notify change on access '" + accessId + "' to " + factory, e);
			}
		}

		return standardResponse;
	}

	private InternalModelNotificationResponse onServiceDomainModelChanged(InternalOnServiceDomainModelChanged request) {
		String serviceDomainId = request.getServiceDomainId();

		for (NotifiableModelAccessoryFactory factory : factories) {
			try {
				factory.onServiceDomainChange(serviceDomainId);
			} catch (Exception e) {
				log.error("Failed to notify change on service domain '" + serviceDomainId + "' to " + factory, e);
			}
		}

		return standardResponse;
	}
}
