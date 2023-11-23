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
package tribefire.extension.elastic.elasticsearch.wire.space;

import com.braintribe.model.elasticsearchdeployment.ElasticsearchConnector;
import com.braintribe.model.elasticsearchdeployment.ElasticsearchService;
import com.braintribe.model.elasticsearchdeployment.IndexedElasticsearchConnector;
import com.braintribe.model.elasticsearchdeployment.admin.ElasticsearchAdminServlet;
import com.braintribe.model.elasticsearchdeployment.aspect.ExtendedFulltextAspect;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingWorker;
import com.braintribe.model.elasticsearchdeployment.service.ElasticServiceProcessor;
import com.braintribe.model.elasticsearchdeployment.service.HealthCheckProcessor;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class ElasticsearchModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ElasticsearchDeployablesSpace deployables;

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindings.bind(HealthCheckProcessor.T) //
				.component(tfPlatform.binders().checkProcessor()) //
				.expertSupplier(this.deployables::healthCheckProcessor);

		bindings.bind(ElasticsearchService.T) //
				.component(tfPlatform.binders().webTerminal()) //
				.expertFactory(deployables::service);

		bindings.bind(ElasticsearchConnector.T) //
				.component(com.braintribe.model.processing.elasticsearch.ElasticsearchConnector.class) //
				.expertFactory(deployables::connector);

		bindings.bind(IndexedElasticsearchConnector.T) //
				.component(IndexedElasticsearchConnector.T, com.braintribe.model.processing.elasticsearch.IndexedElasticsearchConnector.class) //
				.expertFactory(deployables::indexedConnector) //
				.component(ElasticsearchConnector.T, com.braintribe.model.processing.elasticsearch.ElasticsearchConnector.class) //
				.expertFactory(deployables::connector);

		bindings.bind(ExtendedFulltextAspect.T) //
				.component(tfPlatform.binders().accessAspect()) //
				.expertFactory(deployables::fulltextAspect);

		bindings.bind(ElasticServiceProcessor.T) //
				.component(tfPlatform.binders().accessRequestProcessor()) //
				.expertSupplier(deployables::reflectionProcessor);

		bindings.bind(ElasticsearchAdminServlet.T) //
				.component(tfPlatform.binders().webTerminal()) //
				.expertSupplier(deployables::adminServlet);

		bindings.bind(ElasticsearchIndexingWorker.T) //
				.component(com.braintribe.model.processing.elasticsearch.indexing.ElasticsearchIndexingWorker.class) //
				.expertFactory(deployables::worker) //
				.component(tfPlatform.binders().worker()) //
				.expertFactory(deployables::worker);
	}
}
