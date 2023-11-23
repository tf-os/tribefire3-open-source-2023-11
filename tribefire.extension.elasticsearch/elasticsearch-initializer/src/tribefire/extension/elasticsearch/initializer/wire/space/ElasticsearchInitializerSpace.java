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
package tribefire.extension.elasticsearch.initializer.wire.space;

import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.elasticsearch.initializer.wire.contract.ElasticsearchInitializerContract;
import tribefire.extension.elasticsearch.initializer.wire.contract.ElasticsearchInitializerModelsContract;
import tribefire.extension.elasticsearch.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.elasticsearch.model.deployment.ElasticsearchProcessor;

@Managed
public class ElasticsearchInitializerSpace extends AbstractInitializerSpace implements ElasticsearchInitializerContract {

	@Import
	private ElasticsearchInitializerModelsContract models;

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Managed
	private ElasticsearchProcessor elasticsearchProcessor() {
		ElasticsearchProcessor bean = create(ElasticsearchProcessor.T);

		bean.setName("Elasticsearch Processor");
		bean.setExternalId("serviceProcessor.elasticsearch");
		bean.setModule(existingInstances.elasticsearchModule());
		return bean;
	}

	@Override
	@Managed
	public ProcessWith processWithElasticsearch() {
		ProcessWith bean = create(ProcessWith.T);

		bean.setProcessor(elasticsearchProcessor());

		return bean;
	}

}
