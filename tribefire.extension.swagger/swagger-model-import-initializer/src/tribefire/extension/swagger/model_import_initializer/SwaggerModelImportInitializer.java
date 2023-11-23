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
package tribefire.extension.swagger.model_import_initializer;

import java.util.Set;

import com.braintribe.model.ddra.DdraConfiguration;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.ddra.DdraUrlMethod;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.swagger.ConvertSwaggerFromUrlToModel;
import com.braintribe.swagger.ConvertSwaggerModelRequest;
import com.braintribe.swagger.ExportSwaggerModelRequest;
import com.braintribe.swagger.ImportSwaggerModelRequest;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.swagger.model_import_initializer.wire.SwaggerModelImportInitializerWireModule;
import tribefire.extension.swagger.model_import_initializer.wire.contract.SwaggerModelImportInitializerMainContract;

public class SwaggerModelImportInitializer extends AbstractInitializer<SwaggerModelImportInitializerMainContract> {

	@Override
	public WireTerminalModule<SwaggerModelImportInitializerMainContract> getInitializerWireModule() {
		return SwaggerModelImportInitializerWireModule.INSTANCE;
	}
	
	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<SwaggerModelImportInitializerMainContract> initializerContext,
			SwaggerModelImportInitializerMainContract initializerMainContract) {

		GmMetaModel cortexModel = initializerMainContract.coreInstances().cortexModel();
		GmMetaModel cortexServiceModel = initializerMainContract.coreInstances().cortexServiceModel();

		cortexModel.getDependencies()
				.add(initializerMainContract.existingInstances().deploymentModel());

		cortexServiceModel.getDependencies()
				.add(initializerMainContract.existingInstances().dataModel());
		cortexServiceModel.getDependencies()
				.add(initializerMainContract.existingInstances().serviceModel());

		addMetadataToEntities(initializerMainContract);
		configureDdraMappings(initializerContext, initializerMainContract);
	
	}

	private void addMetadataToEntities(SwaggerModelImportInitializerMainContract initializerMainContract) {
		ModelMetaDataEditor modelEditor = new BasicModelMetaDataEditor(
				initializerMainContract.existingInstances().serviceModel());
		modelEditor.onEntityType(ImportSwaggerModelRequest.T).addMetaData(initializerMainContract.initializer().processWithImportSwaggerModelProcessor());
		modelEditor.onEntityType(ExportSwaggerModelRequest.T).addMetaData(initializerMainContract.initializer().processWithExportSwaggerModelProcessor());
		modelEditor.onEntityType(ConvertSwaggerModelRequest.T).addMetaData(initializerMainContract.initializer().processWithConvertSwaggerModelProcessor());
	}
	
	private void configureDdraMappings(WiredInitializerContext<SwaggerModelImportInitializerMainContract> initializerContext, SwaggerModelImportInitializerMainContract initializerMainContract) {
		DdraConfiguration config = initializerMainContract.existingInstances().ddraConfig();
		Set<DdraMapping> mappings = config.getMappings();
		mappings.add(ddraMapping("/swagger/convert-from-url", initializerContext.lookup("type:"+ConvertSwaggerFromUrlToModel.T.getTypeSignature())));
	}
	
	@Managed
	private DdraMapping ddraMapping(String path, GmEntityType type) {
		DdraMapping bean = DdraMapping.T.create();
		bean.setGlobalId("ddra:/"+path);
		bean.setPath(path);
		bean.setRequestType(type);
		bean.setMethod(DdraUrlMethod.GET);
		bean.setDefaultServiceDomain("cortex");
		return bean;
	}
	
}
