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
package tribefire.extension.swagger.model_import_initializer.wire.space;

import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.swagger.deployment.ConvertSwaggerModelProcessor;
import com.braintribe.swagger.deployment.ExportSwaggerModelProcessor;
import com.braintribe.swagger.deployment.ImportSwaggerModelProcessor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.swagger.model_import_initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.swagger.model_import_initializer.wire.contract.SwaggerModelImportInitializerContract;

@Managed
public class SwaggerModelImportInitializerSpace extends AbstractInitializerSpace implements SwaggerModelImportInitializerContract {

	@Import
	private ExistingInstancesContract existingInstances;
	
	@Import
	private CoreInstancesContract coreInstances;
	
	@Managed
	@Override
	public ImportSwaggerModelProcessor importSwaggerModelProcessor() {
		ImportSwaggerModelProcessor bean = create(ImportSwaggerModelProcessor.T);
		bean.setExternalId("swaggerImportProcessor.serviceProcessor");
		bean.setName("Swagger Import Service Processor");
		return bean;
	}
	
	@Managed
	@Override
	public ConvertSwaggerModelProcessor convertSwaggerModelProcessor() {
		ConvertSwaggerModelProcessor bean = create(ConvertSwaggerModelProcessor.T);
		bean.setExternalId("swaggerConvertProcessor.serviceProcessor");
		bean.setName("Swagger Convert Service Processor");
		return bean;
	}
	
	@Managed
	@Override
	public ExportSwaggerModelProcessor exportSwaggerModelProcessor() {
		ExportSwaggerModelProcessor bean = create(ExportSwaggerModelProcessor.T);
		bean.setExternalId("swaggerExportProcessor.serviceProcessor");
		bean.setName("Swagger Export Service Processor");
		return bean;
	}

	@Managed
	@Override
	public MetaData processWithImportSwaggerModelProcessor() {
		ProcessWith md = create(ProcessWith.T);
		md.setProcessor(importSwaggerModelProcessor());
		return md;
	}
	
	@Managed
	@Override
	public MetaData processWithExportSwaggerModelProcessor() {
		ProcessWith md = create(ProcessWith.T);
		md.setProcessor(exportSwaggerModelProcessor());
		return md;
	}
	
	@Managed
	@Override
	public MetaData processWithConvertSwaggerModelProcessor() {
		ProcessWith md = create(ProcessWith.T);
		md.setProcessor(convertSwaggerModelProcessor());
		return md;
	}
	
}
