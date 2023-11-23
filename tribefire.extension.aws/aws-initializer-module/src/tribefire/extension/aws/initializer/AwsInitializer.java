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
package tribefire.extension.aws.initializer;

import com.braintribe.model.aws.deployment.cloudfront.CloudFrontConfiguration;
import com.braintribe.model.aws.resource.S3Source;
import com.braintribe.model.aws.service.AcquiredCloudFrontKeyPair;
import com.braintribe.model.aws.service.AwsRequest;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.aws.initializer.wire.AwsInitializerModuleWireModule;
import tribefire.extension.aws.initializer.wire.contract.AwsInitializerModuleContract;
import tribefire.extension.aws.initializer.wire.contract.AwsInitializerModuleMainContract;
import tribefire.extension.aws.initializer.wire.contract.RuntimePropertiesContract;

/**
 * <p>
 * This {@link AbstractInitializer initializer} initializes targeted accesses with our custom instances available from
 * initializer's contracts.
 * </p>
 */
public class AwsInitializer extends AbstractInitializer<AwsInitializerModuleMainContract> {

	@Override
	public WireTerminalModule<AwsInitializerModuleMainContract> getInitializerWireModule() {
		return AwsInitializerModuleWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<AwsInitializerModuleMainContract> initializerContext,
			AwsInitializerModuleMainContract initializerMainContract) {

		TribefireRuntime.setPropertyPrivate("S3_ACCESS_KEY", "S3_SECRET_ACCESS_KEY");

		GmMetaModel cortexModel = initializerMainContract.coreInstancesContract().cortexModel();
		cortexModel.getDependencies().add(initializerMainContract.initializerModelsContract().configuredDeploymentModel());

		GmMetaModel cortexServiceModel = initializerMainContract.coreInstancesContract().cortexServiceModel();
		cortexServiceModel.getDependencies().add(initializerMainContract.initializerModelsContract().configuredServiceModel());

		RuntimePropertiesContract properties = initializerMainContract.propertiesContract();
		if (properties.S3_CREATE_DEFAULT_STORAGE_BINARY_PROCESSOR()) {
			if (!StringTools.isAnyBlank(properties.S3_ACCESS_KEY(), properties.S3_SECRET_ACCESS_KEY())) {
				initializerMainContract.initializerContract().s3DefaultStorageBinaryProcessor();
				addMetaDataToModelsBinaryProcess(context, initializerMainContract);
			}
		}
		initializerMainContract.initializerContract().serviceRequestProcessor();
		initializerMainContract.initializerContract().functionalCheckBundle();
		addMetaDataToModelsProcess(context, initializerMainContract);
		addMetaDataToDeploymentModel(context, initializerMainContract);
		addMetaDataToServiceModel(context, initializerMainContract);
	}

	private void addMetaDataToModelsBinaryProcess(PersistenceInitializationContext context,
			AwsInitializerModuleMainContract initializerMainContract) {
		ModelMetaDataEditor modelEditor = BasicModelMetaDataEditor.create(initializerMainContract.initializerModelsContract().configuredDataModel())
				.withEtityFactory(context.getSession()::create).done();
		modelEditor.onEntityType(S3Source.T).addMetaData(initializerMainContract.initializerContract().binaryProcessWith());
	}

	private void addMetaDataToModelsProcess(PersistenceInitializationContext context, AwsInitializerModuleMainContract initializerMainContract) {
		ModelMetaDataEditor modelEditor = BasicModelMetaDataEditor
				.create(initializerMainContract.initializerModelsContract().configuredServiceModel()).withEtityFactory(context.getSession()::create)
				.done();
		modelEditor.onEntityType(AwsRequest.T).addMetaData(initializerMainContract.initializerContract().serviceProcessWith());
	}

	private void addMetaDataToDeploymentModel(PersistenceInitializationContext context, AwsInitializerModuleMainContract initializerMainContract) {
		ModelMetaDataEditor modelEditor = BasicModelMetaDataEditor
				.create(initializerMainContract.initializerModelsContract().configuredDeploymentModel())
				.withEtityFactory(context.getSession()::create).done();

		AwsInitializerModuleContract initializerContract = initializerMainContract.initializerContract();
		//@formatter:off
		modelEditor.onEntityType(CloudFrontConfiguration.T)
			.addPropertyMetaData(CloudFrontConfiguration.publicKeyPem, initializerContract.outline());
		//@formatter:on

	}

	private void addMetaDataToServiceModel(PersistenceInitializationContext context, AwsInitializerModuleMainContract initializerMainContract) {
		ModelMetaDataEditor modelEditor = BasicModelMetaDataEditor
				.create(initializerMainContract.initializerModelsContract().configuredServiceModel()).withEtityFactory(context.getSession()::create)
				.done();

		AwsInitializerModuleContract initializerContract = initializerMainContract.initializerContract();
		//@formatter:off
		modelEditor.onEntityType(AcquiredCloudFrontKeyPair.T)
			.addPropertyMetaData(AcquiredCloudFrontKeyPair.publicKeyPem, initializerContract.outline());
		//@formatter:on

	}

}
