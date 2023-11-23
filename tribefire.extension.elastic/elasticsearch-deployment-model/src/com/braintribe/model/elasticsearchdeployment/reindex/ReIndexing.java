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
package com.braintribe.model.elasticsearchdeployment.reindex;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;

public interface ReIndexing extends StandardIdentifiable {

	final EntityType<ReIndexing> T = EntityTypes.T(ReIndexing.class);

	String access = "access";
	String query = "query";
	String reIndexingStatus = "reIndexingStatus";
	String message = "message";
	String report = "report";
	String indexedEntities = "indexedEntities";
	String duration = "duration";

	void setAccess(IncrementalAccess access);
	IncrementalAccess getAccess();

	/**
	 * The query to specify which data should be indexed
	 */
	void setQuery(EntityQuery query);
	EntityQuery getQuery();

	void setReIndexingStatus(ReIndexingStatus reIndexingStatus);
	ReIndexingStatus getReIndexingStatus();

	String getMessage();
	void setMessage(String message);

	Resource getReport();
	void setReport(Resource report);

	void setIndexableEntities(Integer indexedEntities);
	Integer getIndexableEntities();

	void setDuration(Long duration);
	Long getDuration();
}
