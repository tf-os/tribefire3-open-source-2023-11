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
package com.braintribe.model.elasticsearchdeployment.indexing;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.elasticsearchdeployment.IndexedElasticsearchConnector;
import com.braintribe.model.extensiondeployment.Worker;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.DeployableComponent;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@DeployableComponent
public interface ElasticsearchIndexingWorker extends Worker {

	final EntityType<ElasticsearchIndexingWorker> T = EntityTypes.T(ElasticsearchIndexingWorker.class);

	String access = "access";
	String elasticsearchConnector = "elasticsearchConnector";
	String threadCount = "threadCount";

	void setAccess(IncrementalAccess access);
	IncrementalAccess getAccess();

	void setElasticsearchConnector(IndexedElasticsearchConnector elasticsearchConnector);
	IndexedElasticsearchConnector getElasticsearchConnector();

	void setThreadCount(Integer threadCount);
	@Initializer("2")
	Integer getThreadCount();

	void setQueueSize(Integer queueSize);
	@Initializer("1000")
	Integer getQueueSize();

}
