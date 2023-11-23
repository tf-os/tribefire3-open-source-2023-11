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
package com.braintribe.model.access.crud.support.query;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.access.crud.api.read.QueryContext;
import com.braintribe.model.access.crud.api.read.QueryContextAspect;
import com.braintribe.model.query.Query;

public class BasicQueryContext implements QueryContext {
	
	private Query q; 
	protected Map<Class<? extends QueryContextAspect<?>>, Object> sessionAspects = new HashMap<>();  
	
	public BasicQueryContext(Query q) {
		this.q = q;
	}

	@Override
	public Query getQuery() {
		return q;
	}
	
	@Override
	public <T, A extends QueryContextAspect<T>> QueryContext addAspect(Class<A> aspectClass, T value) {
		this.sessionAspects.put(aspectClass, value);
		return this;
	}
	
	@Override
	public <T, A extends QueryContextAspect<T>> T findAspect(Class<A> aspectClass) {
		return (T) this.sessionAspects.get(aspectClass);
	}
	
}
