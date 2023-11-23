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
package com.braintribe.model.processing.query.planner.context;

import com.braintribe.model.query.SelectQuery;

/**
 * @deprecated You are probably using this with other deprecated classes. Get rid of them and you won't need this either.
 */
@Deprecated
public interface QueryValueResolver {

	/**
	 * Returns true iff given instance if member of {@link SelectQuery#getEvaluationExcludes()}.
	 * 
	 * @see SelectQuery#setEvaluationExcludes(java.util.Set)
	 */
	boolean isEvaluationExclude(Object value);

}
