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
package tribefire.extension.artifact.processing_wb_initializer;

import static com.braintribe.wire.api.util.Lists.list;

import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.assets.default_wb_initializer.wire.contract.DefaultWbContract;
import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.artifact.processing_wb_initializer.wire.ArtifactProcessingWbInitializerWireModule;
import tribefire.extension.artifact.processing_wb_initializer.wire.contract.ArtifactProcessingWbInitializerContract;
import tribefire.extension.artifact.processing_wb_initializer.wire.contract.ArtifactProcessingWbInitializerMainContract;

public class ArtifactProcessingWbInitializer extends AbstractInitializer<ArtifactProcessingWbInitializerMainContract> {

	@Override
	public WireTerminalModule<ArtifactProcessingWbInitializerMainContract> getInitializerWireModule() {
		return ArtifactProcessingWbInitializerWireModule.INSTANCE;
	}
	
	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<ArtifactProcessingWbInitializerMainContract> initializerContext, ArtifactProcessingWbInitializerMainContract initializerMainContract) {

		DefaultWbContract workbenchContract = initializerMainContract.workbenchContract();
		ArtifactProcessingWbInitializerContract accessWb = initializerMainContract.artifactProcessingWbInitializerContract();
		
		workbenchContract.defaultRootPerspective().getFolders().add(accessWb.artifactProcessingFolder());
		
		workbenchContract.defaultHomeFolderPerspective().getFolders().addAll(list(
				accessWb.mavenConfigurationFolder(),
				accessWb.simplifiedConfigurationFolder(),
				accessWb.repositoryPolicyFolder(),
				accessWb.resolutionConfigurationFolder(),
				accessWb.assetContextsFolder()
				));
	}
}
