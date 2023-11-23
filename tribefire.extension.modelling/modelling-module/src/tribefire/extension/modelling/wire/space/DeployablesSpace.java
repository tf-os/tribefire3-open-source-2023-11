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
package tribefire.extension.modelling.wire.space;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.modelling.management.api.ModellingManagementRequest;
import tribefire.extension.modelling.model.api.request.ModellingRequest;
import tribefire.extension.modelling.processing.management.ModellingManagementProcessor;
import tribefire.extension.modelling.processing.modelling.ModellingProcessor;
import tribefire.module.wire.contract.PlatformResourcesContract;
import tribefire.module.wire.contract.SystemUserRelatedContract;

@Managed
public class DeployablesSpace implements WireSpace {

	@Import
	SystemUserRelatedContract systemUser;
	
	@Import
	PlatformResourcesContract resources;
	
	@Managed
	public ModellingManagementProcessor<ModellingManagementRequest, Object> modellingManagementProcessor(
			ExpertContext<tribefire.extension.modelling.model.deployment.ModellingManagementProcessor> context) {
		ModellingManagementProcessor<ModellingManagementRequest, Object> bean = new ModellingManagementProcessor<>();
		
		bean.setSessionFactory(systemUser.sessionFactory());
		
		bean.setTempDir(resources.tmp("model-management").asFile());
		
		bean.setExplorerUrl(context.getDeployable().getExplorerUrl());
		
		// APE
		bean.setRepositoryConfigurationName(context.getDeployable().getRepositoryConfigurationName());
		
		return bean;
	}
	
	@Managed
	public ModellingProcessor<ModellingRequest, Object> modellingProcessor() {
		ModellingProcessor<ModellingRequest, Object> bean = new ModellingProcessor<>();
		
		bean.setCortexSessionProvider(() -> systemUser.sessionFactory().newSession("cortex"));
		
		return bean;
	}
	
}