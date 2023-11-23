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
package tribefire.extension.artifact.processing_initializer;

import com.braintribe.model.artifact.processing.service.request.ArtifactProcessingRequest;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.artifact.processing_initializer.wire.ArtifactProcessingInitializerWireModule;
import tribefire.extension.artifact.processing_initializer.wire.contract.ArtifactProcessingInitializerContract;
import tribefire.extension.artifact.processing_initializer.wire.contract.ArtifactProcessingInitializerMainContract;
import tribefire.extension.artifact.processing_initializer.wire.contract.ExistingInstancesContract;

public class ArtifactProcessingInitializer extends AbstractInitializer<ArtifactProcessingInitializerMainContract> {

	@Override
	public WireTerminalModule<ArtifactProcessingInitializerMainContract> getInitializerWireModule() {
		return ArtifactProcessingInitializerWireModule.INSTANCE;
	}
	private void addMetadataToModels(WiredInitializerContext<ArtifactProcessingInitializerMainContract> context) {
		ArtifactProcessingInitializerContract artifactProcessingInitializerContract = context.contract().artifactProcessingInitializerContract();
		ExistingInstancesContract existingInstancesContract = context.contract().existingInstancesContract();
		
		ModelMetaDataEditor editor = new BasicModelMetaDataEditor( existingInstancesContract.artifactProcessingServiceModel());
		editor.onEntityType(ArtifactProcessingRequest.T).addMetaData( artifactProcessingInitializerContract.processWithArtifactProcessingExpert());		
	}
	
	
	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<ArtifactProcessingInitializerMainContract> initializerContext, ArtifactProcessingInitializerMainContract initializerMainContract) {
		ArtifactProcessingInitializerContract artifactProcessingInitializerContract = initializerMainContract.artifactProcessingInitializerContract();
		artifactProcessingInitializerContract.serviceDomain();
		artifactProcessingInitializerContract.configurationAccess();
		addMetadataToModels(initializerContext);

		ExistingInstancesContract existingInstancesContract = initializerContext.contract().existingInstancesContract();
		CoreInstancesContract coreInstancesContract = initializerContext.contract().coreInstancesContract();
		
		coreInstancesContract.cortexModel().getDependencies().add( existingInstancesContract.artifactProcessingDeploymentModel());	
	}
}
