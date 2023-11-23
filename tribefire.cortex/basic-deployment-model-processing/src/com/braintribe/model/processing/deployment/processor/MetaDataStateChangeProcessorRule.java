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
package com.braintribe.model.processing.deployment.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.extensiondeployment.meta.OnChange;
import com.braintribe.model.extensiondeployment.meta.OnCreate;
import com.braintribe.model.extensiondeployment.meta.OnDelete;
import com.braintribe.model.extensiondeployment.meta.StateChangeProcessorConfiguration;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorMatch;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.sp.api.StateChangeProcessorSelectorContext;
import com.braintribe.model.processing.sp.commons.StateChangeProcessorMatchImpl;

public class MetaDataStateChangeProcessorRule<T extends GenericEntity> implements StateChangeProcessorRule {
	private static Logger logger = Logger.getLogger(MetaDataStateChangeProcessorRule.class);
	private String ruleId = MetaDataStateChangeProcessorRule.class.getName();
	private DeployRegistry deployRegistry;

	@Required @Configurable
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}
	
	@Configurable
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	@Override
	public String getRuleId() {	
		return ruleId;
	}
	
	@Override
	public StateChangeProcessor<? extends GenericEntity, ? extends GenericEntity> getStateChangeProcessor( String processorId) {
		int p = processorId.indexOf(':');
		String signature = processorId.substring(0, p);
		String id = processorId.substring(p+1);
		
		EntityType<Deployable> deployableType = GMF.getTypeReflection().getEntityType(signature);
		Deployable deployable = deployableType.create();
		deployable.setExternalId( id);
		DeployedUnit deployedProcessorUnit = deployRegistry.resolve(deployable);
		if (deployedProcessorUnit == null) {
			logger.warn("No processor found for [" + processorId + "]");
			return null;
		}
		StateChangeProcessor<T, GenericEntity> stateChangeProcessor = deployedProcessorUnit.getComponent(com.braintribe.model.extensiondeployment.StateChangeProcessor.T);
		
		return stateChangeProcessor;
	}


	@Override
	public List<StateChangeProcessorMatch> matches(StateChangeProcessorSelectorContext context) {
		List<? extends StateChangeProcessorConfiguration> processorConfigurations = resolveProcessorConfigurations(context);
		
		List<StateChangeProcessorMatch> matches = new ArrayList<StateChangeProcessorMatch>( processorConfigurations.size());
		for (StateChangeProcessorConfiguration processorConfiguration : processorConfigurations) {
			com.braintribe.model.extensiondeployment.StateChangeProcessor processor = processorConfiguration.getProcessor();
            
			// GSC: We can not rely on the state of the deployable since this is a cached snapshot on the model.
			// Also there's anyway another resolving on the deployregistry done which would skip execution if no component is found for the processor (thus undeployed).
			/* 
			 * if (processor.getAutoDeploy() == false)  
			 *	continue;
			 */			
			
			DeployedUnit deployedProcessorUnit = deployRegistry.resolve(processor);
			if (deployedProcessorUnit == null)
				continue;
			StateChangeProcessor<T, GenericEntity> stateChangeProcessor = deployedProcessorUnit.getComponent(com.braintribe.model.extensiondeployment.StateChangeProcessor.T);
			
			StateChangeProcessorMatchImpl match = new StateChangeProcessorMatchImpl(
					processor.entityType().getTypeSignature() + ":" + processor.getExternalId(), stateChangeProcessor);
			matches.add( match);						
		}
		
		return matches;
	}

	private List<? extends StateChangeProcessorConfiguration> resolveProcessorConfigurations(StateChangeProcessorSelectorContext context) {
		switch (context.getManipulation().manipulationType()) {
		case INSTANTIATION:
			// retrieve meta data for instantiation of an entity 
			return context.getCmdResolver().getMetaData()
				.entityType(context.getEntityType())
				.meta(OnCreate.T)
				.list();
			
		case ADD:
		case REMOVE:
		case CHANGE_VALUE:
		case CLEAR_COLLECTION:
			// retrieve meta data change of the relevant property
			return context.getCmdResolver().getMetaData()
				.entityType(context.getEntityType())
				.property(context.getEntityProperty().getPropertyName())
				.meta(OnChange.T)
				.list();
			
		case DELETE:
			// retrieve meta data for deletion of an entity
			return context.getCmdResolver().getMetaData()
					.entityType(context.getEntityType())
				.meta(OnDelete.T)
				.list();
			
		default:
			return Collections.emptyList();
		}
	}
}
