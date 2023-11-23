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
import com.braintribe.model.processing.session.api.managed.PropertyQueryExecution;
import com.braintribe.model.processing.session.api.managed.PropertyQueryResultConvenience;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.processing.async.api.AsyncCallback;

class BasicPropertyQueryExecution extends AbstractQueryResultConvenience<PropertyQuery, PropertyQueryResult, PropertyQueryResultConvenience> implements PropertyQueryExecution {
	private IncrementalAccess access;
	
	public BasicPropertyQueryExecution(IncrementalAccess access, PropertyQuery propertyQuery) {
		super(propertyQuery);
		this.access = access;
	}

	@Override
	protected PropertyQueryResult resultInternal(PropertyQuery query) throws GmSessionException{
		try {
			return access.queryProperty(query);
		} catch (ModelAccessException e) {
			throw new GmSessionException("error while executing entity query", e);
		}
	}
	
	@Override
	public void result(AsyncCallback<PropertyQueryResultConvenience> callback) {
		try {
			callback.onSuccess(new StaticPropertyQueryResultConvenience(getQuery(), result()));
		}
		catch (Throwable t) {
			callback.onFailure(t);
		}
	}		
	
}
