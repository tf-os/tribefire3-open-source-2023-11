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
package com.braintribe.model.processing.session.impl.managed;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.api.managed.EntityQueryExecution;
import com.braintribe.model.processing.session.api.managed.EntityQueryResultConvenience;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.processing.async.api.AsyncCallback;

@SuppressWarnings("unusable-by-js")
class BasicEntityQueryExecution extends AbstractQueryResultConvenience<EntityQuery, EntityQueryResult, EntityQueryResultConvenience> implements EntityQueryExecution {
	private IncrementalAccess access;
	
	public BasicEntityQueryExecution(IncrementalAccess access, EntityQuery entityQuery) {
		super(entityQuery);
		this.access = access;
	}

	@Override
	protected EntityQueryResult resultInternal(EntityQuery query) throws GmSessionException{
		try {
			return access.queryEntities(query);
		} catch (ModelAccessException e) {
			throw new GmSessionException("error while executing entity query", e);
		}
	}
	
	@Override
	public void result(AsyncCallback<EntityQueryResultConvenience> callback) {
		try {
			callback.onSuccess(new StaticEntityQueryResultConvenience(getQuery(), result()));
		} catch (Throwable t) {
			callback.onFailure(t);
		}
	}
	
}
