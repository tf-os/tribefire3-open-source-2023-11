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
package tribefire.extension.azure.initializer;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.azure.initializer.wire.AzureInitializerWireModule;
import tribefire.extension.azure.initializer.wire.contract.AzureInitializerContract;
import tribefire.extension.azure.initializer.wire.contract.AzureInitializerMainContract;
import tribefire.extension.azure.model.api.AzureRequest;
import tribefire.extension.azure.model.resource.AzureBlobSource;

public class AzureInitializer extends AbstractInitializer<AzureInitializerMainContract> {

	@Override
	public WireTerminalModule<AzureInitializerMainContract> getInitializerWireModule() {
		return AzureInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<AzureInitializerMainContract> initializerContext,
			AzureInitializerMainContract initializerMainContract) {

		AzureInitializerContract initializer = initializerMainContract.initializer();

		GmMetaModel cortexServiceModel = initializerMainContract.coreInstances().cortexServiceModel();
		cortexServiceModel.getDependencies().add(initializerMainContract.models().configuredServiceModel());

		if (initializer.instantiateDefaultAccess()) {
			initializer.defaultAccess();

			addMetaDataToModelsCommon(context, initializerMainContract);
		}

		initializer.connectivityCheckBundle();
		addMetaDataToModelsProcess(context, initializerMainContract);
	}

	private void addMetaDataToModelsCommon(PersistenceInitializationContext context, AzureInitializerMainContract initializerMainContract) {
		ModelMetaDataEditor modelEditor = BasicModelMetaDataEditor.create(initializerMainContract.models().configuredDataModel())
				.withEtityFactory(context.getSession()::create).done();
		modelEditor.onEntityType(ResourceSource.T).addMetaData(initializerMainContract.initializer().binaryProcessWith());
		modelEditor.onEntityType(AzureBlobSource.T).addMetaData(initializerMainContract.initializer().binaryProcessWith());
	}
	private void addMetaDataToModelsProcess(PersistenceInitializationContext context, AzureInitializerMainContract initializerMainContract) {
		ModelMetaDataEditor modelEditor = BasicModelMetaDataEditor.create(initializerMainContract.models().configuredServiceModel())
				.withEtityFactory(context.getSession()::create).done();
		modelEditor.onEntityType(AzureRequest.T).addMetaData(initializerMainContract.initializer().serviceProcessWith());
	}

}
