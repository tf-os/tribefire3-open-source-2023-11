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
import com.braintribe.model.processing.session.api.managed.SelectQueryExecution;
import com.braintribe.model.processing.session.api.managed.SelectQueryResultConvenience;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.processing.async.api.AsyncCallback;

@SuppressWarnings("unusable-by-js")
class BasicSelectQueryExecution extends AbstractQueryResultConvenience<SelectQuery, SelectQueryResult, SelectQueryResultConvenience> implements SelectQueryExecution {
	private IncrementalAccess access;
	
	public BasicSelectQueryExecution(IncrementalAccess access, SelectQuery selectQuery) {
		super(selectQuery);
		this.access = access;
	}

	@Override
	protected SelectQueryResult resultInternal(SelectQuery query) throws GmSessionException {
		try {
			return access.query(query);
		} catch (ModelAccessException e) {
			throw new GmSessionException("error while executing entity query", e);
		}
	}
	
	@Override
	public void result(AsyncCallback<SelectQueryResultConvenience> callback) {
		try {
			callback.onSuccess(new StaticSelectQueryResultConvenience(getQuery(), result()));
		} catch (Throwable t) {
			callback.onFailure(t);
		}
	}		
	

}
