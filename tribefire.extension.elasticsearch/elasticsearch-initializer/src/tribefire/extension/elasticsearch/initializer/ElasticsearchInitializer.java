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
package tribefire.extension.elasticsearch.initializer;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.elasticsearch.initializer.wire.ElasticsearchInitializerWireModule;
import tribefire.extension.elasticsearch.initializer.wire.contract.ElasticsearchInitializerContract;
import tribefire.extension.elasticsearch.initializer.wire.contract.ElasticsearchInitializerMainContract;
import tribefire.extension.elasticsearch.initializer.wire.contract.ElasticsearchInitializerModelsContract;
import tribefire.extension.elasticsearch.model.api.ElasticsearchRequest;

public class ElasticsearchInitializer extends AbstractInitializer<ElasticsearchInitializerMainContract> {

	@Override
	public WireTerminalModule<ElasticsearchInitializerMainContract> getInitializerWireModule() {
		return ElasticsearchInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<ElasticsearchInitializerMainContract> initializerContext,
			ElasticsearchInitializerMainContract initializerMainContract) {

		CoreInstancesContract coreInstances = initializerMainContract.coreInstances();
		ElasticsearchInitializerModelsContract models = initializerMainContract.models();
		coreInstances.cortexServiceModel().getDependencies().add(models.apiModel());
		addMetaDataToModels(initializerMainContract, models.apiModel());
	}

	private void addMetaDataToModels(ElasticsearchInitializerMainContract initializerMainContract, GmMetaModel apiModel) {
		ElasticsearchInitializerContract initializer = initializerMainContract.initializer();

		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(apiModel);

		editor.onEntityType(ElasticsearchRequest.T).addMetaData(initializer.processWithElasticsearch());

	}
}
