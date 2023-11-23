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
package tribefire.extension.modelling_cortex_initializer.wire.space;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.artifact.processing.cfg.repository.SimplifiedRepositoryConfiguration;
import com.braintribe.model.artifact.processing.cfg.repository.details.ChecksumPolicy;
import com.braintribe.model.artifact.processing.cfg.repository.details.Repository;
import com.braintribe.model.artifact.processing.cfg.repository.details.RepositoryPolicy;
import com.braintribe.model.artifact.processing.cfg.repository.details.UpdatePolicy;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.deployables.initializer.support.wire.contract.DefaultDeployablesContract;
import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.js.model.deployment.ViewWithJsUxComponent;
import tribefire.extension.modelling.commons.ModellingConstants;
import tribefire.extension.modelling.model.deployment.Modeller;
import tribefire.extension.modelling.model.deployment.ModellingManagementProcessor;
import tribefire.extension.modelling.model.deployment.ModellingProcessor;
import tribefire.extension.modelling_cortex_initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.modelling_cortex_initializer.wire.contract.ModellingCortexContract;
import tribefire.extension.modelling_cortex_initializer.wire.contract.ModellingCortexModelsContract;
import tribefire.extension.modelling_cortex_initializer.wire.contract.RuntimePropertyDefinitions;

@Managed
public class ModellingCortexSpace extends AbstractInitializerSpace implements ModellingCortexContract, ModellingConstants {

	@Import
	private ModellingCortexModelsContract models;
	
	@Import
	private ExistingInstancesContract existingInstances;
	
	@Import
	private CoreInstancesContract coreInstances;
	
	@Import
	private DefaultDeployablesContract defaultDeployables;
	
	@Import
	private RuntimePropertyDefinitions properties;
	

	//
	// Accesses
	//
	
	@Override
	@Managed
	public CollaborativeSmoodAccess managementAccess() {
		
		CollaborativeSmoodAccess bean = create(CollaborativeSmoodAccess.T);
		
		bean.setExternalId(EXT_ID_ACCESS_MANAGEMENT);
		bean.setName(NAME_ACCESS_MANAGEMENT);
		bean.setMetaModel(existingInstances.managementModel());
		bean.setWorkbenchAccess(managementWbAccess());
		
		bean.setServiceModel(existingInstances.managementApiModel());
		
		bean.setAspectConfiguration(managementAccessAspectConfiguration());
		
		return bean;
		
	}
	
	@Managed
	private AspectConfiguration managementAccessAspectConfiguration() {
		AspectConfiguration bean = create(AspectConfiguration.T);
		bean.setAspects(managementAspects());
		return bean;
	}
	
	private List<AccessAspect> managementAspects() {
		List<AccessAspect> aspects = new ArrayList<>();
		// TODO COREPA-319
		aspects.add(defaultDeployables.securityAspect());
		aspects.add(defaultDeployables.fulltextAspect());
		return aspects;
	}
	
	@Managed
	private CollaborativeSmoodAccess managementWbAccess() {
		CollaborativeSmoodAccess bean = create(CollaborativeSmoodAccess.T);
		
		bean.setExternalId(EXT_ID_ACCESS_MANAGEMENT_WB);
		bean.setName(NAME_ACCESS_MANAGEMENT_WB);
		bean.setMetaModel(models.managementWbModel());
		bean.setWorkbenchAccess(coreInstances.workbenchAccess());
		
		return bean;
	}
	
	@Override
	@Managed
	public CollaborativeSmoodAccess modellingWbAccess() {
		CollaborativeSmoodAccess bean = create(CollaborativeSmoodAccess.T);
		
		bean.setExternalId(EXT_ID_MODELLING_ACCESS_WB);
		bean.setName(NAME_MODELLING_ACCESS_WB);
		bean.setMetaModel(models.projectWbModel());
		bean.setWorkbenchAccess(coreInstances.workbenchAccess());
		
		return bean;
	}
	
	//
	// Processors
	//
	
	@Managed
	private ModellingManagementProcessor modellingManagementProcessor() {
		ModellingManagementProcessor bean = create(ModellingManagementProcessor.T);
		
		bean.setExternalId(EXT_ID_MANAGEMENT_PROCESSOR);
		bean.setName(NAME_MANAGEMENT_PROCESSOR);
		bean.setModule(existingInstances.modellingModule());
		
		bean.setRepositoryConfigurationName(properties.REPOSITORY_CONFIGURATION_NAME());
		bean.setExplorerUrl(properties.TRIBEFIRE_EXPLORER_URL());
		
		return bean;
	}
	
	@Managed
	private ModellingProcessor modellingProcessor() {
		ModellingProcessor bean = create(ModellingProcessor.T);
		
		bean.setExternalId(EXT_ID_MODELLING_PROCESSOR);
		bean.setName(NAME_MODELLING_PROCESSOR);
		bean.setModule(existingInstances.modellingModule());
		
		return bean;
	}
	
	//
	// Meta data
	//
	
	@Override
	@Managed
	public ProcessWith processWithModellingManagementProcessor() {
		ProcessWith bean = create(ProcessWith.T);
		
		bean.setProcessor(modellingManagementProcessor());
		
		return bean;
	}
	
	@Override
	@Managed
	public ProcessWith processWithModellingProcessor() {
		ProcessWith bean = create(ProcessWith.T);
		
		bean.setProcessor(modellingProcessor());
		
		return bean;
	}
	
	//
	// APE configuration
	//
	@Override
	@Managed
	public SimplifiedRepositoryConfiguration repositoryConfiguration() {
		SimplifiedRepositoryConfiguration bean = create(SimplifiedRepositoryConfiguration.T);
		
		bean.setName(properties.REPOSITORY_CONFIGURATION_NAME());
		bean.getRepositories().add(repository());
		
		return bean;
	}
	
	@Managed
	private Repository repository() {
		Repository bean = create(Repository.T);
		
		bean.setUrl(properties.REPOSITORY_CONFIGURATION_URL());
		bean.setUser(properties.REPOSITORY_CONFIGURATION_USER());
		bean.setPassword(properties.REPOSITORY_CONFIGURATION_PASSWORD());
		bean.setName(properties.REPOSITORY_CONFIGURATION_NAME());
		bean.setRemoteIndexCanBeTrusted(true);
		bean.setAllowsIndexing(true);
		bean.setRepositoryPolicyForReleases(repositoryPolicyForReleases());
		
		return bean;
	}
	
	@Managed
	private RepositoryPolicy repositoryPolicyForReleases() {
		RepositoryPolicy bean = create(RepositoryPolicy.T);
		
		bean.setCheckSumPolicy(ChecksumPolicy.ignore);
		bean.setEnabled(true);
		bean.setUpdatePolicy(UpdatePolicy.dynamic);
		bean.setUpdatePolicyParameter(properties.REPOSITORY_RAVENHURST_URL());
		
		return bean;
	}
	
	@Managed
	@Override
	public ViewWithJsUxComponent viewWithModeller() {
		ViewWithJsUxComponent bean = create(ViewWithJsUxComponent.T);
		bean.setComponent(modeller());
		return bean;
	}
	
	@Managed
	private Modeller modeller() {
		Modeller bean = create(Modeller.T);
		bean.setModule(existingInstances.modellerUxModule());
		return bean;
	}
	
}
