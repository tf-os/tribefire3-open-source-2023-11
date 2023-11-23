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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.List;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.modelnotification.InternalModelNotificationRequest;
import com.braintribe.model.modelnotification.InternalOnAccessModelChanged;
import com.braintribe.model.modelnotification.InternalOnServiceDomainModelChanged;
import com.braintribe.model.processing.sp.api.AfterStateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.processing.sp.api.StateChangeProcessorMatch;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.sp.api.StateChangeProcessorSelectorContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessors;
import com.braintribe.model.service.api.AsynchronousRequest;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.model.stateprocessing.api.StateChangeProcessorCapabilities;

/**
 * <p>
 * A {@link StateChangeProcessor} which listens to changes in {@link IncrementalAccess#setMetaModel(com.braintribe.model.meta.GmMetaModel)} and
 * {@link ServiceDomain#setServiceModel(com.braintribe.model.meta.GmMetaModel)} properties, broadcasting the externalId of the changed entity instance
 * as {@link InternalOnAccessModelChanged} and {@link InternalOnServiceDomainModelChanged} respectively.
 * 
 */
public class ModelChangeNotifier implements StateChangeProcessor<GenericEntity, GenericEntity>, StateChangeProcessorRule, StateChangeProcessorMatch {

	// constants
	private static final Logger log = Logger.getLogger(ModelChangeNotifier.class);
	private static final String processorId = ModelChangeNotifier.class.getSimpleName();

	// configurable
	private Evaluator<ServiceRequest> requestEvaluator;

	@Required
	@Configurable
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

	// @formatter:off
	@Override public String getProcessorId() { return processorId; }
	@Override public String getRuleId() { return processorId; }	
	@Override public StateChangeProcessor<?, ?> getStateChangeProcessor() { return this; }
	@Override public StateChangeProcessor<GenericEntity, ?> getStateChangeProcessor(String processorId) { return this; }
	// @formatter:on

	@Override
	public List<StateChangeProcessorMatch> matches(StateChangeProcessorSelectorContext context) {
		if (context.isForProperty() && !context.isForInstantiation() && isRelevantProperty(context))
			return singletonList(this);
		else
			return emptyList();
	}

	private boolean isRelevantProperty(StateChangeProcessorSelectorContext context) {
		EntityType<?> entityType = context.getEntityType();
		String propertyName = context.getEntityProperty().getPropertyName();

		return (IncrementalAccess.T.isAssignableFrom(entityType) && propertyName.equals(IncrementalAccess.metaModel))
				|| (ServiceDomain.T.isAssignableFrom(entityType) && propertyName.equals(ServiceDomain.serviceModel));
	}

	@Override
	public StateChangeProcessorCapabilities getCapabilities() {
		return StateChangeProcessors.afterOnlyCapabilities();
	}

	@Override
	public void onAfterStateChange(AfterStateChangeContext<GenericEntity> context, GenericEntity customContext) throws StateChangeProcessorException {
		String propertyName = context.getEntityProperty().getPropertyName();
		switch (propertyName) {
			case IncrementalAccess.metaModel:
				notifyAccessChange((IncrementalAccess) context.getProcessEntity());
				return;

			case ServiceDomain.serviceModel:
				notifyServiceDomainChange((ServiceDomain) context.getProcessEntity());
				return;

			default:
				log.warn("Property '" + propertyName + "' shouldn't have been inspected");
		}
	}

	protected void notifyAccessChange(IncrementalAccess access) {
		InternalOnAccessModelChanged request = InternalOnAccessModelChanged.T.create();
		request.setAccessId(access.getExternalId());
		notifyChange(request);
	}

	protected void notifyServiceDomainChange(ServiceDomain serviceDomain) {
		InternalOnServiceDomainModelChanged request = InternalOnServiceDomainModelChanged.T.create();
		request.setServiceDomainId(serviceDomain.getExternalId());
		notifyChange(request);
	}

	protected void notifyChange(InternalModelNotificationRequest request) {
		MulticastRequest multiRequest = MulticastRequest.T.create();
		multiRequest.setServiceRequest(request);

		AsynchronousRequest asyncRequest = AsynchronousRequest.T.create();
		asyncRequest.setServiceRequest(multiRequest);

		asyncRequest.eval(requestEvaluator).get();

		log.debug(() -> "Notified " + request);
	}

}
