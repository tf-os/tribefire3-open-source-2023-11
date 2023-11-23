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
package tribefire.extension.elastic.elasticsearch_initializer.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.elastic.elasticsearch_initializer.wire.contract.ElasticsearchInitializerModuleContract;
import tribefire.extension.elastic.elasticsearch_initializer.wire.contract.ElasticsearchInitializerModuleMainContract;
import tribefire.extension.elastic.elasticsearch_initializer.wire.contract.ElasticsearchInitializerModuleModelsContract;
import tribefire.extension.elastic.elasticsearch_initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.elastic.elasticsearch_initializer.wire.contract.RuntimePropertiesContract;
import tribefire.extension.elastic.templates.wire.contract.ElasticMetaDataContract;

/**
 * @see ElasticsearchInitializerModuleMainContract
 */
@Managed
public class ElasticsearchInitializerModuleMainSpace extends AbstractInitializerSpace implements ElasticsearchInitializerModuleMainContract {

	@Import
	private ElasticsearchInitializerModuleContract initializer;

	@Import
	private ElasticsearchInitializerModuleModelsContract models;

	@Import
	private RuntimePropertiesContract properties;

	@Import
	private ElasticMetaDataContract elasticMetaData;

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Override
	public ElasticsearchInitializerModuleContract initializerContract() {
		return initializer;
	}

	@Override
	public ElasticsearchInitializerModuleModelsContract initializerModelsContract() {
		return models;
	}

	@Override
	public RuntimePropertiesContract propertiesContract() {
		return properties;
	}

	@Override
	public ElasticMetaDataContract elasticMetaDataContract() {
		return elasticMetaData;
	}

	@Override
	public ExistingInstancesContract existingInstancesContract() {
		return existingInstances;
	}

	@Override
	public CoreInstancesContract coreInstancesContract() {
		return coreInstances;
	}
}
