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

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;

import tribefire.extension.artifact.processing_initializer.wire.contract.ArtifactProcessingInitializerModelsContract;
import tribefire.extension.artifact.processing_initializer.wire.contract.ExistingInstancesContract;

@Managed
public class ArtifactProcessingInitializerModelsSpace extends AbstractInitializerSpace implements ArtifactProcessingInitializerModelsContract {

	private static final String GRP = "tribefire.extension.artifact";
	private static final String ART = "artifact-processing-service-model";
	
	@Import
	ExistingInstancesContract existingInstancesContract;
	
	@Import
	CoreInstancesContract coreInstancesContract;
		

	@Managed
	@Override
	public GmMetaModel configuredServiceModel() {
		GmMetaModel bean = create( GmMetaModel.T);		
		bean.setName( GRP + ":configured-" + ART);
		bean.getDependencies().add( existingInstancesContract.artifactProcessingServiceModel());		
		return bean;
	}
	
	@Managed
	@Override
	public GmMetaModel configurationAccessWorkbenchModel() {
		GmMetaModel bean = create( GmMetaModel.T);		
		
		bean.setName( GRP + ":configuration-access-workbench-model");
		
		bean.getDependencies().add( existingInstancesContract.artifactProcessingAccessModel());		
		bean.getDependencies().add( existingInstancesContract.artifactProcessingServiceModel());		
		
		bean.getDependencies().add(coreInstancesContract.workbenchModel());
		bean.getDependencies().add(coreInstancesContract.essentialMetaDataModel());
		
		return bean;
	}

}
