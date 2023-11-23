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
package tribefire.extension.artifact.processing_initializer.wire.space;

import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.artifact.processing.deployment.ArtifactProcessingExpert;
import com.braintribe.model.artifact.processing.service.request.ArtifactProcessingRequest;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;

import tribefire.extension.artifact.processing_initializer.wire.contract.ArtifactProcessingInitializerContract;
import tribefire.extension.artifact.processing_initializer.wire.contract.ArtifactProcessingInitializerModelsContract;
import tribefire.extension.artifact.processing_initializer.wire.contract.ExistingInstancesContract;

@Managed
public class ArtifactProcessingInitializerSpace extends AbstractInitializerSpace implements ArtifactProcessingInitializerContract {

	 private static final String NAME = "artifactProcessingExpert";
	 private static final String PRC = "serviceProcessor." + NAME;
	 
	@Import
	ExistingInstancesContract existingInstancesContract;
	
	@Import
	CoreInstancesContract coreInstancesContract;
	
	@Import
	ArtifactProcessingInitializerModelsContract artifactProcessingInitializerModelsContract;
	
	@Managed	
	public ArtifactProcessingExpert artifactProcessingExpert() {
		ArtifactProcessingExpert bean = create( ArtifactProcessingExpert.T);
		bean.setExternalId( PRC);
		bean.setName( "Artifact Processing Expert");
		bean.setModule(existingInstancesContract.artifactProcessingModule());
		bean.setConfigurationAccess( configurationAccess());
		return bean;
	}
	
	@Managed
	@Override
	public ProcessWith processWithArtifactProcessingExpert() {
		ProcessWith bean = create( ProcessWith.T);
		bean.setProcessor( artifactProcessingExpert());
		return bean;
	}
	
	@Managed
	@Override
	public ServiceDomain serviceDomain() {
		ServiceDomain bean = create( ServiceDomain.T);
		bean.setServiceModel( artifactProcessingInitializerModelsContract.configuredServiceModel());
		bean.setExternalId(ArtifactProcessingRequest.DOMAIN_ID);
		return bean;
	}
	
	@Managed
	@Override
	public CollaborativeSmoodAccess configurationAccess() {
		CollaborativeSmoodAccess bean = create(CollaborativeSmoodAccess.T);
		
		bean.setExternalId("access.repositoryConfiguration");
		bean.setName("Repository Configuration Access");
		bean.setMetaModel( existingInstancesContract.artifactProcessingAccessModel());
		bean.setWorkbenchAccess(configurationWorkbenchAccess());
		
		return bean;
	}
	
	@Managed
	private CollaborativeSmoodAccess configurationWorkbenchAccess() {
		CollaborativeSmoodAccess bean = create(CollaborativeSmoodAccess.T);
		bean.setExternalId("access.repositoryConfiguration.wb");
		bean.setName("Repository Configuration Workbench Access");

		bean.setMetaModel(artifactProcessingInitializerModelsContract.configurationAccessWorkbenchModel());
		bean.setWorkbenchAccess(coreInstancesContract.workbenchAccess());
		return bean;
	}
}
