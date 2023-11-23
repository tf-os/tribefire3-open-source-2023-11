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
package com.braintribe.model.processing.ddra.endpoints.api.v1;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.lang.ref.WeakReference;
import java.util.List;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.ddra.endpoints.api.api.v1.DdraMappings;
import com.braintribe.logging.Logger;
import com.braintribe.model.ddra.DdraConfiguration;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.query.tools.PreparedTcs;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.sp.api.AfterStateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.processing.sp.api.StateChangeProcessorMatch;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.sp.api.StateChangeProcessorSelectorContext;
import com.braintribe.utils.DateTools;

/**
 * This class listens to changes to the DdraConfiguration singleton as well as its DdraMappings. It does NOT listen to
 * changes to the requestType or transformRequest of the DdraMapping, for simplicity's sake.
 * 
 * As a result, this class produces a DdraMappings instance that is ALMOST thread-safe, making this fully thread safe
 * would involve much more work and very little added benefit.
 * 
 * In addition, in an environment where multiple TF instances are backed by one Cortex access, any change to the
 * mappings would require a restart of all the TFs to take effect.
 */
public class DdraConfigurationStateChangeProcessor
		implements StateChangeProcessorRule, StateChangeProcessor<GenericEntity, GenericEntity>, StateChangeProcessorMatch {

	private static final Logger logger = Logger.getLogger(DdraConfigurationStateChangeProcessor.class);

	private DdraMappings mappings;

	private final ThreadLocal<WeakReference<PersistenceGmSession>> tlLastUsedSession = new ThreadLocal<>();

	@Override
	public void onAfterStateChange(AfterStateChangeContext<GenericEntity> context, GenericEntity customContext) throws StateChangeProcessorException {
		EntityProperty entityProperty = context.getEntityProperty();
		EntityReference entityReference = context.getEntityReference();

		if (entityProperty != null && DdraConfiguration.lastChangeTimestamp.equals(entityProperty.getPropertyName()) && entityReference != null
				&& DdraConfiguration.T.getTypeSignature().equals(entityReference.getTypeSignature())) {
			return;
		}

		if (this.mappings.isDdraMappingsInitialized()) {
			this.mappings.setDdraMappingsInitialized(false);
		}

		PersistenceGmSession systemSession = context.getSystemSession();

		// Avoid updating lastChangeTimestamp multiple times for a single transaction (which can be recognized by using the same
		// systemSession instance)
		if (isLastUsedSession(systemSession))
			return;

		setLastUsedSession(systemSession);

		DdraConfiguration configuration = systemSession.query().entity(DdraConfiguration.T, "ddra:config")
				.withTraversingCriterion(PreparedTcs.scalarOnlyTc).refresh();

		logger.debug(() -> "Updating timestamp...");

		// remember which session was used to do the change (in a thread-local) and do not do the change if it's found
		configuration.setLastChangeTimestamp(DateTools.getCurrentDateString());
	}

	private boolean isLastUsedSession(PersistenceGmSession systemSession) {
		WeakReference<PersistenceGmSession> ref = tlLastUsedSession.get();
		return ref != null && systemSession == ref.get();
	}

	private void setLastUsedSession(PersistenceGmSession systemSession) {
		tlLastUsedSession.set(new WeakReference<>(systemSession));
	}

	@Override
	public String getProcessorId() {
		return getClass().getName();
	}

	@Override
	public StateChangeProcessor<?, ?> getStateChangeProcessor() {
		return this;
	}

	@Override
	public String getRuleId() {
		return getProcessorId();
	}

	@Override
	public StateChangeProcessor<? extends GenericEntity, ?> getStateChangeProcessor(String processorId) {
		return this;
	}

	@Override
	public List<StateChangeProcessorMatch> matches(StateChangeProcessorSelectorContext context) {
		EntityType<?> et = context.getEntityType();

		// TODO actually, we are only interested in mappings that belong to the CortexConfiguration, but this is good enough
		// this will only happen rarely anyway.
		if (et == DdraConfiguration.T || et == DdraMapping.T)
			return singletonList(this);
		else
			return emptyList();

	}

	@Required
	@Configurable
	public void setMappings(DdraMappings mappings) {
		this.mappings = mappings;
	}
}
