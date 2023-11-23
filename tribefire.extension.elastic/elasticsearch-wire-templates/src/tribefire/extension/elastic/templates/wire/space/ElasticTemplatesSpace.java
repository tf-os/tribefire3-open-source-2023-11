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

import com.braintribe.model.elasticsearchdeployment.ElasticsearchConnector;
import com.braintribe.model.elasticsearchdeployment.IndexedElasticsearchConnector;
import com.braintribe.model.elasticsearchdeployment.aspect.ExtendedFulltextAspect;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingWorker;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.elastic.templates.api.ElasticTemplateContext;
import tribefire.extension.elastic.templates.wire.contract.ElasticTemplatesContract;

@Managed
public class ElasticTemplatesSpace implements WireSpace, ElasticTemplatesContract {

	@Import
	private ElasticMetaDataSpace documentsMetaData;

	@Override
	@Managed
	public ElasticsearchConnector connector(ElasticTemplateContext context) {
		ElasticsearchConnector bean = context.create(ElasticsearchConnector.T, InstanceConfiguration.currentInstance());
		bean.setCartridge(context.getElasticCartridge());
		bean.setModule(context.getElasticModule());
		bean.setName("Elasticsearch Connector " + context.getName());
		bean.setClusterName(context.getClusterName());
		bean.setClusterSniff(context.getClusterSniff());
		bean.setPort(context.getConnectorPort());
		bean.setHost(context.getConnectorHost());
		bean.setAutoDeploy(context.getDeployConnector());
		return bean;
	}

	@Override
	@Managed
	public IndexedElasticsearchConnector indexedConnector(ElasticTemplateContext context) {
		IndexedElasticsearchConnector bean = context.create(IndexedElasticsearchConnector.T, InstanceConfiguration.currentInstance());
		bean.setCartridge(context.getElasticCartridge());
		bean.setModule(context.getElasticModule());
		bean.setName("Elasticsearch Connector " + context.getName());
		bean.setClusterName(context.getClusterName());
		bean.setClusterSniff(context.getClusterSniff());
		bean.setPort(context.getConnectorPort());
		bean.setHost(context.getConnectorHost());
		bean.setIndex(context.getIndex());
		bean.setAutoDeploy(context.getDeployConnector());
		return bean;
	}

	@Override
	@Managed
	public ExtendedFulltextAspect fulltextAspect(ElasticTemplateContext context) {
		ExtendedFulltextAspect bean = context.create(ExtendedFulltextAspect.T, InstanceConfiguration.currentInstance());
		bean.setCartridge(context.getElasticCartridge());
		bean.setModule(context.getElasticModule());
		bean.setName("Elasticsearch Fulltext Aspect " + context.getName());
		bean.setElasticsearchConnector(indexedConnector(context));
		bean.setMaxFulltextResultSize(context.getMaxFulltextResultSize());
		bean.setMaxResultWindow(context.getMaxResultWindow());
		bean.setWorker(worker(context));
		bean.setAutoDeploy(context.getDeployConnector());
		return bean;
	}

	@Override
	@Managed
	public ElasticsearchIndexingWorker worker(ElasticTemplateContext context) {
		ElasticsearchIndexingWorker bean = context.create(ElasticsearchIndexingWorker.T, InstanceConfiguration.currentInstance());
		bean.setCartridge(context.getElasticCartridge());
		bean.setModule(context.getElasticModule());
		bean.setName("Elasticsearch Worker " + context.getName());
		bean.setElasticsearchConnector(indexedConnector(context));
		bean.setThreadCount(context.getIndexingThreadCount());
		bean.setQueueSize(context.getIndexingQueueSize());
		bean.setAutoDeploy(context.getDeployConnector());
		bean.setAccess(context.getAccess());
		return bean;
	}

}
