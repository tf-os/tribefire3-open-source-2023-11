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
package com.braintribe.model.processing.sp.commons;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.Transaction;
import com.braintribe.model.processing.sp.api.StateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.stateprocessing.api.StateChangeProcessorCapabilities;


/**
 * implements the {@link com.braintribe.model.processing.sp.api.StateChangeContext} interface. 
 * 
 * @author pit
 * @author dirk
 *
 * @param <T> - the process entity, derived from GenericEntity
 */
public abstract class AbstractStateChangeContext<T extends GenericEntity> implements StateChangeContext<T> {

	
	private static Logger log = Logger.getLogger(AbstractStateChangeContext.class);
	private final EntityProperty entityProperty;
	private final EntityReference entityReference; 
	private final PersistenceGmSession userSession;
	private final PersistenceGmSession systemSession;
	private T processEntity;
	private T systemProcessEntity;
	private final Manipulation manipulation;
	
	private EntityType<T> entityType;
	private Object processContext;
	private StateChangeProcessorCapabilities capabilities;
	private boolean capabilitiesOverriden;
	
	public AbstractStateChangeContext( PersistenceGmSession userSession, PersistenceGmSession systemSession, EntityReference entityReference, EntityProperty entityProperty, Manipulation manipulation) {
		this.entityProperty = entityProperty;
		this.entityReference = entityReference;
		this.manipulation = manipulation;
		
		this.userSession = userSession;		
		this.systemSession = systemSession;
	}

	public void initializeCapabilities(StateChangeProcessorCapabilities capabilities) {
		this.capabilities = capabilities;
		this.capabilitiesOverriden = false;
	}
	public boolean getCapabilitiesOverriden() {
		return capabilitiesOverriden;
	}
	
	public StateChangeProcessorCapabilities getCapabilities() {
		return capabilities;
	}
	
	@Override
	public void overrideCapabilities(StateChangeProcessorCapabilities capabilities) {
		this.capabilities = capabilities;
		this.capabilitiesOverriden = true;
	}
	
	@Override
	public EntityReference getEntityReference() {
		return entityReference;
	}
	
	@Override
	public EntityProperty getEntityProperty() {	
		return entityProperty;
	}

	public PersistenceGmSession getUserSession() {
		return userSession;
	}
	
	@Override
	public PersistenceGmSession getSystemSession() {
		return systemSession;
	}
			
	@Override
	public CmdResolver getCmdResolver() {
		return userSession.getModelAccessory().getCmdResolver();		
	}

	public PersistenceGmSession getSessionIfPresent() {
		return userSession;
	}
	
	
	@Override
	public boolean wasSessionModified() {

		Transaction transaction = userSession.getTransaction();
		return transaction.hasManipulations(); 
	}
	
	@Override
	public T getProcessEntity() throws StateChangeProcessorException {	
		if (processEntity == null) {	
 
			EntityReference reference = getEntityReference();
			// if the entity reference is a preliminary reference, we return null (can only happen in the BeforeStateContext)
			if (reference instanceof PreliminaryEntityReference)
				return null;
			try {
				processEntity = getSession().query().<T>entity(reference).refresh();
			} catch (Exception e) {
				String msg = "cannot retrieve process entity from entity property as " + e;
				throw new StateChangeProcessorException(msg, e);
			} 			
		}
		return processEntity;
	}
	
	@Override
	public T getSystemProcessEntity() throws StateChangeProcessorException {	
		if (systemProcessEntity == null) {	
			
			EntityReference reference = getEntityReference();
			// if the entity reference is a preliminary reference, we return null (can only happen in the BeforeStateContext)
			if (reference instanceof PreliminaryEntityReference)
				return null;
			try {
				systemProcessEntity = getSystemSession().query().<T>entity(reference).refresh();
			} catch (Exception e) {
				String msg = "cannot retrieve process entity from entity property as " + e;
				throw new StateChangeProcessorException(msg, e);
			} 			
		}
		return systemProcessEntity;
	}
	
	public T getProcessEntityIfPresent() {
		return processEntity;
	}
	
	public void setProcessEntity(T processEntity) {
		this.processEntity = processEntity;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <M extends Manipulation> M getManipulation() {		
		return (M) manipulation;
	}

	@Override
	public <T1 extends T> EntityType<T1> getEntityType() {
		return (EntityType<T1>) entityType;
	}
	
	public void setEntityType(EntityType<T> entityType) {
		this.entityType = entityType;
	}

	public void setProcessorContext(Object processContext) {
		this.processContext = processContext;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <E> E getProcessorContext() {
		return (E) processContext;
	}



	@Override
	public PersistenceGmSession getSession() {	
		return userSession;
	}

	@Override
	public boolean wasSystemSessionModified() {
		Transaction transaction = systemSession.getTransaction();
		return transaction.hasManipulations(); 				
	}
	
	protected void commitIfNecessary( PersistenceGmSession session) throws GmSessionException {
		if (session.getTransaction().hasManipulations()) {
			session.commit();
		}
	}
	
	public void commitIfNecessary() throws GmSessionException{
		try {
			commitIfNecessary(systemSession);
		} catch (GmSessionException e) {
			String msg = "cannot commit system session for entity property [" + entityProperty + "], manipulation [" + manipulation +"]";
			log.error( msg, e);
			throw e;
		}
		try {
			commitIfNecessary(userSession);
		} catch (GmSessionException e) {
			String msg = "cannot commit user session for entity property [" + entityProperty + "], manipulation [" + manipulation +"]";
			log.error( msg, e);
			throw e;
		}
	}

}
