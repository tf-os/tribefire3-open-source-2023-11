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
package com.braintribe.model.elasticsearchdeployment;

import java.util.Set;

import com.braintribe.model.extensiondeployment.WebTerminal;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ElasticsearchService extends WebTerminal {

	final EntityType<ElasticsearchService> T = EntityTypes.T(ElasticsearchService.class);

	void setBasePath(String basePath);
	@Initializer("'WEB-INF/res'")
	String getBasePath();

	void setDataPath(String dataPath);
	String getDataPath();

	void setClusterName(String clusterName);
	@Initializer("'elasticsearch.cartridge'")
	String getClusterName();

	void setPluginClasses(Set<String> pluginClasses);
	@Initializer("{'org.elasticsearch.ingest.attachment.IngestAttachmentPlugin'}")
	Set<String> getPluginClasses();

	void setBindHosts(Set<String> bindHosts);
	Set<String> getBindHosts();

	void setPublishHost(String publishHost);
	String getPublishHost();

	void setPort(Integer port);
	@Initializer("9300")
	Integer getPort();

	void setHttpPort(Integer port);
	@Initializer("9200")
	Integer getHttpPort();

	void setRepositoryPaths(Set<String> repositoryPaths);
	Set<String> getRepositoryPaths();

	void setRecoverAfterNodes(Integer recoverAfterNodes);
	Integer getRecoverAfterNodes();

	void setExpectedNodes(Integer expectedNodes);
	Integer getExpectedNodes();

	void setRecoverAfterTimeInS(Integer recoverAfterTimeInS);
	Integer getRecoverAfterTimeInS();

	void setClusterNodes(Set<String> clusterNodes);
	Set<String> getClusterNodes();

	void setElasticPath(String elasticPath);
	@Initializer("'WEB-INF/elastic'")
	String getElasticPath();

}
