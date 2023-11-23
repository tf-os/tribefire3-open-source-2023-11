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
package tribefire.extension.elastic.elasticsearch_initializer;

import com.braintribe.model.elasticsearch.service.ElasticRequest;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchDeleteMetaData;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingMetaData;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.resource.Resource;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.elastic.elasticsearch_initializer.wire.ElasticsearchInitializerModuleWireModule;
import tribefire.extension.elastic.elasticsearch_initializer.wire.contract.ElasticsearchInitializerModuleMainContract;
import tribefire.extension.elastic.templates.api.ElasticTemplateContext;

/**
 * <p>
 * This {@link AbstractInitializer initializer} initializes targeted accesses with our custom instances available from
 * initializer's contracts.
 * </p>
 */
public class ElasticsearchInitializer extends AbstractInitializer<ElasticsearchInitializerModuleMainContract> {

	@Override
	public WireTerminalModule<ElasticsearchInitializerModuleMainContract> getInitializerWireModule() {
		return ElasticsearchInitializerModuleWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context,
			WiredInitializerContext<ElasticsearchInitializerModuleMainContract> initializerContext,
			ElasticsearchInitializerModuleMainContract initializerMainContract) {
		GmMetaModel cortexModel = initializerMainContract.coreInstancesContract().cortexModel();
		cortexModel.getDependencies().add(initializerMainContract.initializerModelsContract().configuredDeploymentModel());

		GmMetaModel cortexServiceModel = initializerMainContract.coreInstancesContract().cortexServiceModel();
		cortexServiceModel.getDependencies().add(initializerMainContract.initializerModelsContract().configuredServiceModel());

		initializerMainContract.initializerContract().adminServlet();
		initializerMainContract.initializerContract().serviceRequestProcessor();
		initializerMainContract.initializerContract().healthCheckProcessor();
		initializerMainContract.initializerContract().functionalCheckBundle();

		addMetaDataToModels(context, initializerMainContract);

		if (initializerMainContract.propertiesContract().ELASTIC_RUN_SERVICE()) {
			initializerMainContract.initializerContract().service();
		}

		if (initializerMainContract.propertiesContract().ELASTIC_CREATE_DEMO_ACCESS()) {
			setupDefaultConfiguration(context, initializerMainContract);
		}
	}

	private void addMetaDataToModels(PersistenceInitializationContext context, ElasticsearchInitializerModuleMainContract initializerMainContract) {
		ModelMetaDataEditor modelEditor = BasicModelMetaDataEditor
				.create(initializerMainContract.initializerModelsContract().configuredServiceModel()).withEtityFactory(context.getSession()::create)
				.done();
		modelEditor.onEntityType(ElasticRequest.T).addMetaData(initializerMainContract.initializerContract().serviceProcessWith());
	}

	private void setupDefaultConfiguration(PersistenceInitializationContext context,
			ElasticsearchInitializerModuleMainContract initializerMainContract) {
		ElasticTemplateContext templateContext = initializerMainContract.initializerContract().defaultElasticTemplateContext();
		initializerMainContract.initializerContract().demoAccess();

		ElasticsearchIndexingMetaData indexingMetaData = initializerMainContract.elasticMetaDataContract().indexingMetaData(templateContext);
		ElasticsearchDeleteMetaData deleteMetaData = initializerMainContract.elasticMetaDataContract().deleteMetaData(templateContext);

		ModelMetaDataEditor modelEditor = BasicModelMetaDataEditor.create(initializerMainContract.initializerModelsContract().demoDocumentModel())
				.withEtityFactory(context.getSession()::create).done();
		modelEditor.onEntityType(Resource.T) //
				.addPropertyMetaData(indexingMetaData) //
				.addPropertyMetaData(deleteMetaData);
	}
}
