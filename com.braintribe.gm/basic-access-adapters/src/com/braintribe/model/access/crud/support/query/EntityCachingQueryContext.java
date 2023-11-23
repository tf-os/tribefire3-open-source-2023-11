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
import java.util.function.Function;

import com.braintribe.model.access.crud.api.read.QueryContext;
import com.braintribe.model.access.crud.api.read.QueryContextAspect;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.Query;

public class EntityCachingQueryContext implements QueryContext {

	private QueryContext delegateContext = null;
	private Map<Object, GenericEntity> cache = new HashMap<>();
	
	public EntityCachingQueryContext(QueryContext delegateContext) {
		this.delegateContext = delegateContext;
	}
	
	@Override
	public Query getQuery() {
		return delegateContext.getQuery();
	}

	@Override
	public Ordering getOriginalOrdering() {
		return delegateContext.getOriginalOrdering();
	}

	@Override
	public Paging getOriginalPaging() {
		return delegateContext.getOriginalPaging();
	}
	
	@Override
	public <T, A extends QueryContextAspect<T>> QueryContext addAspect(Class<A> aspectClass, T value) {
		return delegateContext.addAspect(aspectClass, value);
	}
	
	@Override
	public <T, A extends QueryContextAspect<T>> T findAspect(Class<A> aspectClass) {
		return delegateContext.findAspect(aspectClass);
	}
	
	
	@Override
	public <T extends GenericEntity> T acquireEntry(Object id,  Function<? super Object, T> factory) {
		return (T) this.cache.computeIfAbsent(id, factory);
	}
	
	
}
