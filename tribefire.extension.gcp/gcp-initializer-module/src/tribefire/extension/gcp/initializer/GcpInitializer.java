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
package tribefire.extension.gcp.initializer;

import com.braintribe.model.gcp.deployment.GcpConnector;
import com.braintribe.model.gcp.resource.GcpStorageSource;
import com.braintribe.model.gcp.service.GcpRequest;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.prompt.Outline;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.gcp.initializer.wire.GcpInitializerModuleWireModule;
import tribefire.extension.gcp.initializer.wire.contract.GcpInitializerModuleMainContract;
import tribefire.extension.gcp.initializer.wire.contract.RuntimePropertiesContract;

/**
 * <p>
 * This {@link AbstractInitializer initializer} initializes targeted accesses with our custom instances available from
 * initializer's contracts.
 * </p>
 */
public class GcpInitializer extends AbstractInitializer<GcpInitializerModuleMainContract> {

	@Override
	public WireTerminalModule<GcpInitializerModuleMainContract> getInitializerWireModule() {
		return GcpInitializerModuleWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<GcpInitializerModuleMainContract> initializerContext,
			GcpInitializerModuleMainContract initializerMainContract) {

		TribefireRuntime.setPropertyPrivate("GCP_JSON_CREDENTIALS", "GCP_PRIVATE_KEY");

		GmMetaModel cortexModel = initializerMainContract.coreInstancesContract().cortexModel();
		cortexModel.getDependencies().add(initializerMainContract.initializerModelsContract().configuredDeploymentModel());

		GmMetaModel cortexServiceModel = initializerMainContract.coreInstancesContract().cortexServiceModel();
		cortexServiceModel.getDependencies().add(initializerMainContract.initializerModelsContract().configuredServiceModel());

		RuntimePropertiesContract properties = initializerMainContract.propertiesContract();
		if (properties.GCP_CREATE_DEFAULT_STORAGE_BINARY_PROCESSOR()) {
			if (!StringTools.isAnyBlank(properties.GCP_PRIVATE_KEY(), properties.GCP_PRIVATE_KEY_ID())) {
				initializerMainContract.initializerContract().gcpDefaultStorageBinaryProcessor();
				addMetaDataToModelsBinaryProcess(context, initializerMainContract);
			}
		}
		initializerMainContract.initializerContract().serviceRequestProcessor();
		initializerMainContract.initializerContract().functionalCheckBundle();
		addMetaDataToModelsCommon(context, initializerMainContract);
	}

	private void addMetaDataToModelsBinaryProcess(PersistenceInitializationContext context,
			GcpInitializerModuleMainContract initializerMainContract) {
		ModelMetaDataEditor modelEditor = BasicModelMetaDataEditor.create(initializerMainContract.initializerModelsContract().configuredDataModel())
				.withEtityFactory(context.getSession()::create).done();
		modelEditor.onEntityType(GcpStorageSource.T).addMetaData(initializerMainContract.initializerContract().binaryProcessWith());
	}

	private void addMetaDataToModelsCommon(PersistenceInitializationContext context, GcpInitializerModuleMainContract initializerMainContract) {
		ModelMetaDataEditor modelEditor = BasicModelMetaDataEditor
				.create(initializerMainContract.initializerModelsContract().configuredServiceModel()).withEtityFactory(context.getSession()::create)
				.done();
		modelEditor.onEntityType(GcpRequest.T).addMetaData(initializerMainContract.initializerContract().serviceProcessWith());

		Outline outline = context.getSession().create(Outline.T);

		modelEditor = BasicModelMetaDataEditor.create(initializerMainContract.initializerModelsContract().configuredDeploymentModel())
				.withEtityFactory(context.getSession()::create).done();
		modelEditor.onEntityType(GcpConnector.T).addPropertyMetaData(GcpConnector.jsonCredentials, outline);
		modelEditor.onEntityType(GcpConnector.T).addPropertyMetaData(GcpConnector.privateKey, outline);
	}

}
