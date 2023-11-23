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
package com.braintribe.model.elasticsearchdeployment.aspect;

import com.braintribe.model.elasticsearchdeployment.IndexedElasticsearchConnector;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingWorker;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ExtendedFulltextAspect extends AccessAspect {

	final EntityType<ExtendedFulltextAspect> T = EntityTypes.T(ExtendedFulltextAspect.class);

	void setElasticsearchConnector(IndexedElasticsearchConnector elasticsearchConnector);
	IndexedElasticsearchConnector getElasticsearchConnector();

	void setWorker(ElasticsearchIndexingWorker worker);
	ElasticsearchIndexingWorker getWorker();

	void setMaxFulltextResultSize(Integer maxFulltextResultSize);
	@Initializer("100")
	Integer getMaxFulltextResultSize();

	void setCascadingAttachment(boolean cascadingAttachment);
	@Initializer("true")
	boolean getCascadingAttachment();

	void setMaxResultWindow(Integer maxResultWindow);
	@Initializer("100000")
	Integer getMaxResultWindow();

	void setIgnoreUnindexedEntities(boolean ignoreUnindexedEntities);
	boolean getIgnoreUnindexedEntities();

}
