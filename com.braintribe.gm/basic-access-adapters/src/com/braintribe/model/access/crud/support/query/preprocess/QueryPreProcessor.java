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
package com.braintribe.model.access.crud.support.query.preprocess;

import java.util.function.Function;

import com.braintribe.model.access.crud.api.read.QueryContext;

public interface QueryPreProcessor extends Function<QueryContext, QueryContext> {
	
	/**
	 * Every preProcessor is expected to provide a context instance.<br/>
	 * This can either be the instance passed in or a new created one.
	 */
	QueryContext preProcess(QueryContext context);

	@Override
	default QueryContext apply(QueryContext context) {
		return preProcess(context);
	}

}
