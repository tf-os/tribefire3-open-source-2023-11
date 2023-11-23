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
package tribefire.extension.elastic.templates.wire.space;

import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchDeleteMetaData;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingMetaData;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.elastic.ElasticConstants;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.elastic.templates.api.ElasticTemplateContext;
import tribefire.extension.elastic.templates.wire.contract.ElasticMetaDataContract;
import tribefire.extension.elastic.templates.wire.contract.ElasticTemplatesContract;

@Managed
public class ElasticMetaDataSpace implements WireSpace, ElasticMetaDataContract {

	@Import
	private ElasticTemplatesContract documentsTemplates;

	@Override
	@Managed
	public GmMetaModel reflectionModel(ElasticTemplateContext context) {
		GmMetaModel rawReflectionModel = (GmMetaModel) context.lookup("model:" + ElasticConstants.REFLECTION_MODEL_QUALIFIEDNAME);

		GmMetaModel model = context.create(GmMetaModel.T, InstanceConfiguration.currentInstance());

		setModelDetails(model, ElasticConstants.REFLECTION_MODEL_QUALIFIEDNAME + "-" + context.getName().toLowerCase(), rawReflectionModel);
		return model;

	}

	@Override
	@Managed
	public GmMetaModel serviceModel(ElasticTemplateContext context) {
		GmMetaModel model = context.create(GmMetaModel.T, InstanceConfiguration.currentInstance());
		GmMetaModel rawServiceModel = (GmMetaModel) context.lookup("model:" + ElasticConstants.SERVICE_MODEL_QUALIFIEDNAME);
		setModelDetails(model, ElasticConstants.SERVICE_MODEL_QUALIFIEDNAME + "-" + context.getName().toLowerCase(), rawServiceModel);
		return model;
	}

	@Override
	public void metaData(ElasticTemplateContext context) {
		// TODO?
	}

	@Override
	@Managed
	public ElasticsearchIndexingMetaData indexingMetaData(ElasticTemplateContext context) {
		ElasticsearchIndexingMetaData bean = context.create(ElasticsearchIndexingMetaData.T, InstanceConfiguration.currentInstance());
		bean.setCascade(true);
		bean.setInherited(true);
		return bean;
	}

	private static void setModelDetails(GmMetaModel targetModel, String modelName, GmMetaModel... dependencies) {
		targetModel.setName(modelName);
		targetModel.setVersion(ElasticConstants.MAJOR_VERSION + ".0");
		if (dependencies != null) {
			for (GmMetaModel dependency : dependencies) {
				if (dependency != null) {
					targetModel.getDependencies().add(dependency);
				}
			}
		}
	}

	@Override
	@Managed
	public ElasticsearchDeleteMetaData deleteMetaData(ElasticTemplateContext context) {
		ElasticsearchDeleteMetaData bean = context.create(ElasticsearchDeleteMetaData.T, InstanceConfiguration.currentInstance());
		bean.setInherited(true);
		return bean;
	}
}
