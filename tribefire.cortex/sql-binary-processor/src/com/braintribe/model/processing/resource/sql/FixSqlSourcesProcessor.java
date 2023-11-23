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
package com.braintribe.model.processing.resource.sql;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.source.SqlSource;
import com.braintribe.model.resourceapi.request.FixSqlSources;
import com.braintribe.model.resourceapi.request.FixSqlSourcesResponse;

public class FixSqlSourcesProcessor implements AccessRequestProcessor<FixSqlSources, FixSqlSourcesResponse> {
	static final Set<String> fixedAccesses = ConcurrentHashMap.newKeySet();
	
	private static final Logger log = Logger.getLogger(FixSqlSourcesProcessor.class);
	
	@Override
	public FixSqlSourcesResponse process(AccessRequestContext<FixSqlSources> context) {
		FixSqlSourcesResponse response = FixSqlSourcesResponse.T.create();
		String accessId = context.getDomainId();
		response.setAccessId(accessId);
		
		// If an access was just or is just being fixed, don't bother
		if (!fixedAccesses.add(accessId) && !context.getRequest().getForceUpdate()) {
			response.setAlreadyUpdated(true);
			log.info("Didn't update missing blobId properties for SqlSources in access '" + accessId + "' because the access was already fixed previously or is being fixed at this moment in another thread.");
			return response;
		}
		
		EntityQuery entityQuery = EntityQueryBuilder.from(SqlSource.T).where().property(SqlSource.blobId).eq(null).done();
		List<SqlSource> sources = context.getSession().query().entities(entityQuery).list();
		sources.forEach(s -> s.setBlobId(s.getId()));
		
		response.setNumUpdated(sources.size());
		
		log.info("Set missing blobId property for " + response.getNumUpdated() + " SqlSource entities in access '" + accessId + "'.");
		
		return response;
	}
	
}
