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
package tribefire.extension.azure.initializer.wire.space;

import com.braintribe.model.accessdeployment.smood.SmoodAccess;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.extensiondeployment.meta.BinaryProcessWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.model.check.CheckWeight;
import tribefire.extension.azure.initializer.wire.contract.AzureInitializerContract;
import tribefire.extension.azure.initializer.wire.contract.AzureInitializerModelsContract;
import tribefire.extension.azure.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.azure.initializer.wire.contract.RuntimePropertiesContract;
import tribefire.extension.azure.model.deployment.AzureBlobBinaryProcessor;
import tribefire.extension.azure.model.deployment.AzureServiceProcessor;
import tribefire.extension.azure.model.deployment.HealthCheckProcessor;
import tribefire.extension.azure.templates.api.AzureTemplateContext;
import tribefire.extension.azure.templates.wire.contract.AzureTemplatesContract;

@Managed
public class AzureInitializerSpace extends AbstractInitializerSpace implements AzureInitializerContract {

	@Import
	private AzureInitializerModelsContract models;

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private RuntimePropertiesContract runtime;

	@Import
	private AzureTemplatesContract templates;

	@Override
	public boolean instantiateDefaultAccess() {
		Boolean createAccess = runtime.AZURE_CREATE_DEFAULT_ACCESS();
		return createAccess != null ? createAccess.booleanValue() : false;
	}

	@Managed
	private AzureTemplateContext defaultContext() {
		//@formatter:off
		AzureTemplateContext context = AzureTemplateContext.builder().setIdPrefix(initializerSupport.initializerId())
				.setLookupFunction(super::lookup)
				.setEntityFactory(super::create)
				.setName("Default")
				.setStorageConnectionString(runtime.AZURE_CONNECTION_STRING_ENCRYPTED())
				.setContainerName(runtime.AZURE_CONTAINER_NAME())
				.setAzureModule(existingInstances.module())
				.setPathPrefix(runtime.AZURE_PATH_PREFIX())
				.build();
		//@formatter:on

		return context;
	}

	@Override
	@Managed
	public SmoodAccess defaultAccess() {
		SmoodAccess bean = create(SmoodAccess.T);
		bean.setExternalId("azure.test.access");
		bean.setName("Azure Test Access");
		bean.setMetaModel(models.configuredDataModel());
		bean.setServiceModel(models.configuredServiceModel());
		return bean;

	}

	@Override
	@Managed
	public AzureBlobBinaryProcessor blobProcessor() {
		AzureTemplateContext context = defaultContext();
		return templates.binaryProcessor(context);
	}

	@Override
	@Managed
	public BinaryProcessWith binaryProcessWith() {
		BinaryProcessWith bean = create(BinaryProcessWith.T);

		AzureBlobBinaryProcessor processor = blobProcessor();

		bean.setRetrieval(processor);
		bean.setPersistence(processor);
		return bean;
	}

	@Override
	@Managed
	public CheckBundle connectivityCheckBundle() {
		CheckBundle bean = create(CheckBundle.T);
		bean.setModule(existingInstances.module());
		bean.getChecks().add(healthCheckProcessor());
		bean.setName("Azure Blob Storage Checks");
		bean.setWeight(CheckWeight.under1s);
		bean.setCoverage(CheckCoverage.connectivity);
		bean.setIsPlatformRelevant(false);

		return bean;
	}

	@Managed
	public HealthCheckProcessor healthCheckProcessor() {
		HealthCheckProcessor bean = create(HealthCheckProcessor.T);
		bean.setModule(existingInstances.module());
		bean.setName("Azure Check Processor");
		bean.setExternalId("azure.healthcheck");

		return bean;
	}

	@Override
	@Managed
	public ProcessWith serviceProcessWith() {
		ProcessWith bean = create(ProcessWith.T);
		bean.setProcessor(serviceRequestProcessor());

		return bean;
	}

	@Override
	@Managed
	public AzureServiceProcessor serviceRequestProcessor() {
		AzureServiceProcessor bean = create(AzureServiceProcessor.T);
		bean.setModule(existingInstances.module());
		bean.setExternalId("azure.serviceProcessor");
		bean.setName("Azure Service Processor");

		return bean;
	}
}
