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
package tribefire.extension.scripting.statechange.processing;

import java.util.Collections;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.sp.api.AfterStateChangeContext;
import com.braintribe.model.processing.sp.api.BeforeStateChangeContext;
import com.braintribe.model.processing.sp.api.PostStateChangeContext;
import com.braintribe.model.processing.sp.api.ProcessStateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.stateprocessing.api.StateChangeProcessorCapabilities;

public class StateChangeScriptContext implements ProcessStateChangeContext<GenericEntity>, AfterStateChangeContext<GenericEntity>, BeforeStateChangeContext<GenericEntity> {

	private ProcessStateChangeContext<GenericEntity> stateChangeContext;
	private GenericEntity stateChangeCustomContext;
	
	private GenericEntity entity;
	private Property affectedProperty;
	private final StateChangeContext<GenericEntity> originalContext;
	
	public StateChangeScriptContext(StateChangeContext<GenericEntity> originalContext) {
		this.originalContext = originalContext;
	}

	@Override
	public void commitIfNecessary() throws GmSessionException {
		if (originalContext instanceof ProcessStateChangeContext<?>)
			((ProcessStateChangeContext<?>) originalContext).commitIfNecessary();
	}

	@Override
	public Map<EntityReference, PersistentEntityReference> getReferenceMap() {
		if (originalContext instanceof PostStateChangeContext<?>)
			return ((PostStateChangeContext<?>) originalContext).getReferenceMap();
		
		else return Collections.emptyMap();
	}

	@Override
	public void notifyInducedManipulation(Manipulation manipulation) {
		if (originalContext instanceof PostStateChangeContext<?>)
			((PostStateChangeContext<?>) originalContext).notifyInducedManipulation(manipulation);
	}

	@Override
	public EntityProperty getEntityProperty() {
		return originalContext.getEntityProperty();
	}

	@Override
	public PersistenceGmSession getSession() {
		return originalContext.getSession();
	}

	@Override
	public PersistenceGmSession getSystemSession() {
		return originalContext.getSystemSession();
	}

	@Override
	public CmdResolver getCmdResolver() {
		return originalContext.getCmdResolver();
	}

	@Override
	public boolean wasSessionModified() {
		return originalContext.wasSessionModified();
	}

	@Override
	public boolean wasSystemSessionModified() {
		return originalContext.wasSystemSessionModified();
	}

	@Override
	public GenericEntity getProcessEntity()
			throws StateChangeProcessorException {
		return originalContext.getProcessEntity();
	}

	@Override
	public GenericEntity getSystemProcessEntity()
			throws StateChangeProcessorException {
		return originalContext.getSystemProcessEntity();
	}

	@Override
	public <M extends Manipulation> M getManipulation() {
		return originalContext.getManipulation();
	}

	@Override
	public EntityType<GenericEntity> getEntityType() {
		return originalContext.getEntityType();
	}

	@Override
	public <E> E getProcessorContext() {
		return originalContext.getProcessorContext();
	}

	public ProcessStateChangeContext<GenericEntity> getStateChangeContext() {
		return stateChangeContext;
	}

	public GenericEntity getStateChangeCustomContext() {
		return stateChangeCustomContext;
	}

	public void setStateChangeCustomContext(GenericEntity stateChangeCustomContext) {
		this.stateChangeCustomContext = stateChangeCustomContext;
	}

	public GenericEntity getEntity() {
		return entity;
	}

	public void setEntity(GenericEntity entity) {
		this.entity = entity;
	}

	public Property getAffectedProperty() {
		return affectedProperty;
	}

	public void setAffectedProperty(Property affectedProperty) {
		this.affectedProperty = affectedProperty;
	}

	@Override
	public EntityReference getEntityReference() {
		return originalContext.getEntityReference();
	}

	@Override
	public void overrideCapabilities(StateChangeProcessorCapabilities capabilities) {
		originalContext.overrideCapabilities(capabilities);
	}
}
