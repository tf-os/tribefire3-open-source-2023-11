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

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.deployment.Cartridge;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.wire.api.scope.InstanceConfiguration;

public interface ElasticTemplateContext {

	String getName();

	String getClusterName();

	String getIdPrefix();

	String getIndex();

	boolean getClusterSniff();

	String getConnectorHost();

	int getConnectorPort();

	int getHttpPort();

	boolean getDeployConnector();

	int getMaxFulltextResultSize();

	int getMaxResultWindow();

	Cartridge getElasticCartridge();

	com.braintribe.model.deployment.Module getElasticModule();

	IncrementalAccess getAccess();

	int getIndexingThreadCount();

	int getIndexingQueueSize();

	<T extends GenericEntity> T lookup(String globalId);

	static ElasticTemplateContextBuilder builder() {
		return new ElasticTemplateContextImpl();
	}

	<T extends GenericEntity> T create(EntityType<T> entityType, InstanceConfiguration instanceConfiguration);
}