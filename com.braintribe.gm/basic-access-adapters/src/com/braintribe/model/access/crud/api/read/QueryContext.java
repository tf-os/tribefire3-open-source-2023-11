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
package com.braintribe.model.access.crud.api.read;

import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.Restriction;

public interface QueryContext {

	Query getQuery();
	
	<T, A extends QueryContextAspect<T>> QueryContext addAspect(Class<A> aspectClass, T value);
	<T, A extends QueryContextAspect<T>> T findAspect(Class<A> aspectClass);
	
	default Ordering getOriginalOrdering() {
		return getQuery().getOrdering();
	}

	default Paging getOriginalPaging() {
		Restriction r = getQuery().getRestriction();
		return r != null ? r.getPaging() : null;
	}
	
	default <T extends GenericEntity> T acquireEntry(Object id,  Function<? super Object, T> factory) {
		return factory.apply(id); // no caching by default
	}
	
}
