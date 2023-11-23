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
package com.braintribe.model.processing.deployment;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.sp.api.AfterStateChangeContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorException;
import com.braintribe.model.processing.sp.api.StateChangeProcessorMatch;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.sp.api.StateChangeProcessorSelectorContext;
import com.braintribe.model.processing.sp.api.StateChangeProcessors;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.stateprocessing.api.StateChangeProcessorCapabilities;


public class DynamicDeployableResolver<T> implements StateChangeProcessor<GenericEntity, GenericEntity>, StateChangeProcessorRule, StateChangeProcessorMatch, Supplier<T> {

	private static final Logger log = Logger.getLogger(DynamicDeployableResolver.class);
	
	private DeployRegistry deployRegistry;
	private Class<? extends GenericEntity> type;
	private String deployableProperty;
	private String instanceSelectorProperty;
	private Object instanceSelectorPropertyValue;
	private EntityType<? extends Deployable> componentType;
	private Supplier<PersistenceGmSession> sessionProvider;
	
	private T instance;
	private boolean mustLookup = true;
	
	private SelectQuery selectQuery;
	
	@Required
	@Configurable
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}

	@Required
	@Configurable
	public void setType(Class<? extends GenericEntity> type) {
		this.type = type;
	}

	@Required
	@Configurable
	public void setDeployableProperty(String deployableProperty) {
		this.deployableProperty = deployableProperty;
	}

	@Required
	@Configurable
	public void setInstanceSelectorProperty(String instanceSelectorProperty) {
		this.instanceSelectorProperty = instanceSelectorProperty;
	}

	@Required
	@Configurable
	public void setInstanceSelectorPropertyValue(Object instanceSelectorPropertyValue) {
		this.instanceSelectorPropertyValue = instanceSelectorPropertyValue;
	}

	@Required
	@Configurable
	public void setComponentType(EntityType<? extends Deployable> componentType) {
		this.componentType = componentType;
	}

	@Required
	@Configurable
	public void setSessionProvider(Supplier<PersistenceGmSession> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}
	
	public DynamicDeployableResolver() {
	}

	@Override
	public T get() throws RuntimeException {

		if (mustLookup) {
			
			T service = null;
			
			Deployable deployable = queryDeployable();
			if (deployable != null) {
				DeployedUnit du = deployRegistry.resolve(deployable);
				if (du != null) {
					service = du.getComponent(componentType);
					mustLookup = false;
					if (log.isDebugEnabled()) {
						log.debug("Updated dynamic service to [ "+service+" ], as retrieved from deploy registry based on [ "+deployable+" ], component [ "+componentType.getTypeSignature()+" ]");
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Found dynamic deployable [ "+deployable+" ], but no deployed unit couldn't be resolved with component [ "+componentType.getTypeSignature()+" ]");
					}
					mustLookup = true;
				}
			} else {
				if (log.isTraceEnabled()) {
					log.trace("No deployable returned from query, setting mustLookup to false.");
				}
				mustLookup = false;
			}
			
			instance = service;
			
		}
		
		return instance;
	}
	

	@Override
	public List<StateChangeProcessorMatch> matches(StateChangeProcessorSelectorContext context) {
		if (context.isForProperty()) {
			if (context.getEntityType() == null) {
				if (log.isDebugEnabled()) {
					log.debug("Context [ "+context+" ] not eligible for [ "+getRuleId()+" ] processing"); 
				}
				return Collections.emptyList();
			}
			
			if (context.getEntityType().getTypeSignature().equals(type.getName())) {
				
				if (context.getProperty() != null) {
					
					if (context.getProperty().getName().equals(deployableProperty)) {
						return Collections.<StateChangeProcessorMatch>singletonList(this);
					}
					
					if (log.isTraceEnabled()) {
						log.trace("Property [ "+context.getProperty()+" ] not handled by [ "+getRuleId()+" ] "); 
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug("null property not handled by [ "+getProcessorId()+" ] "); 
					}
				}
			} else {
				if (log.isTraceEnabled()) {
					log.trace("Type [ "+context.getEntityType()+" ] not handled by [ "+getRuleId()+" ] "); 
				}
			}
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public String getProcessorId() {
		return getRuleId();
	}
	
	@Override
	public String getRuleId() {
		return this.getClass().getSimpleName()+"["+deployableProperty+" from "+type.getName()+" where "+instanceSelectorProperty+" = \""+instanceSelectorPropertyValue+"\"]";
	}
	
	@Override
	public StateChangeProcessor<? extends GenericEntity, ? extends GenericEntity> getStateChangeProcessor(String processorId) {
		return this;
	}
	
	@Override
	public StateChangeProcessor<?, ?> getStateChangeProcessor() {
		return this;
	}

	@Override
	public StateChangeProcessorCapabilities getCapabilities() {
		return StateChangeProcessors.afterOnlyCapabilities();
	}

	@Override
	public void onAfterStateChange(AfterStateChangeContext<GenericEntity> context, GenericEntity customContext) throws StateChangeProcessorException {
		GenericEntity processEntity = context.getProcessEntity();
		
		if (processEntity == null || !type.isAssignableFrom(processEntity.getClass())) {
			log.warn("Unexpected process entity [ "+processEntity+" ] found in context for processor [ "+this.getProcessorId()+" ]");
			return;
		}
		
		Object instanceSelectorPropertyValue = GMF.getTypeReflection().getEntityType(type).getProperty(instanceSelectorProperty).get(processEntity);
		
		if (this.instanceSelectorPropertyValue.equals(instanceSelectorPropertyValue)) {
			if (log.isDebugEnabled()) {
				log.debug("Process entity [ "+processEntity+" ] matches criteria for [ "+this.getProcessorId()+" ]");
			}
			mustLookup = true;
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Process entity [ "+processEntity+" ] does not match criteria for [ "+this.getProcessorId()+" ]");
			}
		}
	}

	@Override
	public String toString() {
		return getRuleId();
	}
	
	protected Deployable queryDeployable() throws RuntimeException {
		
		try {
			PersistenceGmSession gmSession = sessionProvider.get();
			Deployable deployable = gmSession.query().select(getDeployableQuery()).first();
			
			if (log.isDebugEnabled()) {
				if (deployable == null) {
					if (log.isTraceEnabled()) {
						log.trace("No dynamic deployable found based on processor's criteria: [ "+getRuleId()+" ] "); 
					}
				} else {
					log.debug("Dynamic deployable found: [ "+deployable+" ] based on processor's criteria: [ "+getRuleId()+" ] "); 
				}
			}
			
			return deployable;
		} catch (Exception e) {
			throw new RuntimeException("Failed to query deployable: "+e.getMessage(), e);
		}
		
	}
	
	private SelectQuery getDeployableQuery() {
		if (selectQuery == null) {
			selectQuery = new SelectQueryBuilder()
				.from(type, "t")
				.select("t", deployableProperty)
				.where()
					.property("t", instanceSelectorProperty).eq(instanceSelectorPropertyValue)
				.done();
		}
		return selectQuery;
	} 
	
}
