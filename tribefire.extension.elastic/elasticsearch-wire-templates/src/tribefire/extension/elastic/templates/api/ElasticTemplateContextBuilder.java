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
package tribefire.extension.elastic.templates.api;

import java.util.function.Function;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.deployment.Cartridge;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

public interface ElasticTemplateContextBuilder {

	ElasticTemplateContextBuilder setName(String name);

	ElasticTemplateContextBuilder setClusterName(String clusterName);

	ElasticTemplateContextBuilder setClusterSniff(boolean clusterSniff);

	ElasticTemplateContextBuilder setConnectorHost(String connectorHost);

	ElasticTemplateContextBuilder setConnectorPort(int connectorPort);

	ElasticTemplateContextBuilder setHttpPort(int httpPort);

	ElasticTemplateContextBuilder setDeployConnector(boolean deployConnector);

	ElasticTemplateContextBuilder setIdPrefix(String idPrefix);

	ElasticTemplateContextBuilder setElasticCartridge(Cartridge elasticCartridge);

	ElasticTemplateContextBuilder setElasticModule(com.braintribe.model.deployment.Module elasticModule);

	ElasticTemplateContextBuilder setIndex(String index);

	ElasticTemplateContextBuilder setMaxFulltextResultSize(int maxFulltextResultSize);

	ElasticTemplateContextBuilder setMaxResultWindow(int maxResultWindow);

	ElasticTemplateContextBuilder setIndexingThreadCount(int indexingThreadCount);

	ElasticTemplateContextBuilder setIndexingQueueSize(int indexingQueueSize);

	ElasticTemplateContextBuilder setAccess(IncrementalAccess access);

	ElasticTemplateContextBuilder setEntityFactory(Function<EntityType<?>, GenericEntity> entityFactory);

	ElasticTemplateContextBuilder setLookupFunction(Function<String, ? extends GenericEntity> lookupFunction);

	ElasticTemplateContext build();

}