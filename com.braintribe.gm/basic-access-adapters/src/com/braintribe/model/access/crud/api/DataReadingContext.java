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
package com.braintribe.model.access.crud.api;

import com.braintribe.model.access.crud.api.read.EntityReadingContext;
import com.braintribe.model.access.crud.api.read.PopulationReadingContext;
import com.braintribe.model.access.crud.api.read.PropertyReadingContext;
import com.braintribe.model.access.crud.api.read.QueryContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.Query;

/**
 * Base type for all context objects passed to individual {@link DataReader}
 * implementations.
 * 
 * @see EntityReadingContext
 * @see PopulationReadingContext
 * @see PropertyReadingContext
 * 
 * @author gunther.schenk
 */
public interface DataReadingContext<T extends GenericEntity> extends CrudExpertContext<T> {
	
	QueryContext getQueryContext();
	
	default boolean hasQueryContext() {
		return getQueryContext() != null;
	}
	
	default Query originalQuery() {
		return hasQueryContext() ? getQueryContext().getQuery() : null;
	}
	
	default Ordering originalOrdering() {
		return hasQueryContext() ? getQueryContext().getOriginalOrdering() : null;
	}
	
	default Paging originalPaging() {
		return hasQueryContext() ? getQueryContext().getOriginalPaging() : null;
	}

}
