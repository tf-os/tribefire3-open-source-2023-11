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
package tribefire.extension.azure.templates.wire.space;

import com.braintribe.model.extensiondeployment.meta.BinaryProcessWith;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.resource.configuration.ExternalResourcesContext;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;

import tribefire.extension.azure.model.deployment.AzureBlobBinaryProcessor;
import tribefire.extension.azure.model.resource.AzureBlobSource;
import tribefire.extension.azure.templates.api.AzureConstants;
import tribefire.extension.azure.templates.api.AzureTemplateContext;
import tribefire.extension.azure.templates.wire.contract.AzureTemplatesContract;

@Managed
public class AzureTemplatesSpace implements AzureTemplatesContract {

	@Override
	@Managed
	public AzureBlobBinaryProcessor binaryProcessor(AzureTemplateContext context) {
		AzureBlobBinaryProcessor bean = context.create(AzureBlobBinaryProcessor.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getAzureModule());
		bean.setName("Azure Blob Storage Binary Processor " + context.getContainerName());
		bean.setStorageConnectionString(context.getStorageConnectionString());
		bean.setContainerName(context.getContainerName());
		bean.setPathPrefix(context.getPathPrefix());
		return bean;
	}

	@Override
	@Managed
	public ExternalResourcesContext externalResourcesContext(AzureTemplateContext context) {
		ExternalResourcesContext bean = ExternalResourcesContext.builder().setBinaryProcessWith(binaryProcessWithAws(context))
				.setPersistenceDataModel((GmMetaModel) context.lookup("model:" + AzureConstants.DATA_MODEL))
				.setResourceSourceType(AzureBlobSource.T).build();
		return bean;
	}

	@Managed
	private BinaryProcessWith binaryProcessWithAws(AzureTemplateContext context) {
		BinaryProcessWith bean = context.create(BinaryProcessWith.T, InstanceConfiguration.currentInstance());

		AzureBlobBinaryProcessor processor = binaryProcessor(context);

		bean.setRetrieval(processor);
		bean.setPersistence(processor);
		return bean;
	}

}
