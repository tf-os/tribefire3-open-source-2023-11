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
package tribefire.extension.elastic.templates.wire.contract;

import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchDeleteMetaData;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingMetaData;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.elastic.templates.api.ElasticTemplateContext;

public interface ElasticMetaDataContract extends WireSpace {

	void metaData(ElasticTemplateContext context);

	GmMetaModel serviceModel(ElasticTemplateContext context);

	GmMetaModel reflectionModel(ElasticTemplateContext context);

	ElasticsearchIndexingMetaData indexingMetaData(ElasticTemplateContext context);

	ElasticsearchDeleteMetaData deleteMetaData(ElasticTemplateContext context);

}
