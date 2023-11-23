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
package com.braintribe.model.processing.cortex.service.model;

import com.braintribe.model.cortexapi.model.AddDependencies;
import com.braintribe.model.cortexapi.model.AddToCortexModel;
import com.braintribe.model.cortexapi.model.AddToCortexServiceModel;
import com.braintribe.model.cortexapi.model.CreateModel;
import com.braintribe.model.cortexapi.model.CreateModelResponse;
import com.braintribe.model.cortexapi.model.ExportModelRequest;
import com.braintribe.model.cortexapi.model.ExportModelResponse;
import com.braintribe.model.cortexapi.model.ImportModelRequest;
import com.braintribe.model.cortexapi.model.ImportModelResponse;
import com.braintribe.model.cortexapi.model.MergeModelsResponse;
import com.braintribe.model.cortexapi.model.ModelChangeResponse;
import com.braintribe.model.cortexapi.model.ModelRequest;
import com.braintribe.model.cortexapi.model.ModelResponse;
import com.braintribe.model.cortexapi.model.NotifyModelChanged;
import com.braintribe.model.cortexapi.model.ValidateModel;
import com.braintribe.model.cortexapi.model.ValidateModelResponse;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.cortex.CortexModelNames;

public class ModelRequestProcessor implements AccessRequestProcessor<ModelRequest, ModelResponse> {
	
	private final String defaultBaseModel = GenericModelTypeReflection.rootModelName;
	
	private final AccessRequestProcessor<ModelRequest, ModelResponse> dispatcher = AccessRequestProcessors.dispatcher(config->{
		config.register(CreateModel.T, this::createModel);
		config.register(AddDependencies.T, this::addDependencies);
		config.register(ValidateModel.T, this::validateModel);
		config.register(ExportModelRequest.T, this::exportModel);
		config.register(ImportModelRequest.T, this::importModel);
		config.register(NotifyModelChanged.T, this::notifyModelChange);
		config.register(AddToCortexModel.T, this::addToCortexModel);
		config.register(AddToCortexServiceModel.T, this::addToCortexServiceModel);
	});

	@Override
	public ModelResponse process(AccessRequestContext<ModelRequest> context) {
		return dispatcher.process(context);
	}
	
	public CreateModelResponse createModel(AccessRequestContext<CreateModel> context) {
		return new ModelCreator(context.getRequest(), context.getSession(), this.defaultBaseModel).run();
	}

	public MergeModelsResponse addDependencies(AccessRequestContext<AddDependencies> context){
		return new DependenciesAdder(context.getRequest()).run();
	}

	public ValidateModelResponse validateModel(AccessRequestContext<ValidateModel> context) {
		return new ModelValidator(context.getRequest(), context.getSession()).run();
	}

	public ExportModelResponse exportModel(@SuppressWarnings("unused") AccessRequestContext<ExportModelRequest> context) {
		return null;
	}

	public ImportModelResponse importModel(@SuppressWarnings("unused") AccessRequestContext<ImportModelRequest> context) {
		return null;
	}

	public ModelChangeResponse notifyModelChange(AccessRequestContext<NotifyModelChanged> context ) {
		return new ModelChangeNotifier(context.getRequest(), context.getSession()).run();
	}
	
	public MergeModelsResponse addToCortexModel(AccessRequestContext<AddToCortexModel> context ) {
		return new CortexModelAdder(context.getRequest(), context.getSession(), CortexModelNames.TF_CORTEX_MODEL_NAME).run();
	}
	
	public MergeModelsResponse addToCortexServiceModel(AccessRequestContext<AddToCortexServiceModel> context ) {
		return new CortexModelAdder(context.getRequest(), context.getSession(), CortexModelNames.TF_CORTEX_SERVICE_MODEL_NAME).run();
	}
}
