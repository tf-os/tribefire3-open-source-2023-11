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
package com.braintribe.model.bvd.query;

import java.util.List;
import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

public enum ResultConvenience implements EnumBase {
	
	/**
	 * Returns the envelope object (an instance of com.braintribe.model.query.QueryResult returned by the query.
	 */
	result, 
	/**
	 * Returns the query result as {@link List} 
	 */
	list,
	/**
	 * Convenience to return the first instance of the query result, or <code>null</code> if the query returns not results. 
	 */
	first,
	/**
	 * Convenience method to return a single instance of the query result, or <code>null</code> if the query returns no results.
	 * This method throws an Exception if more then one result is returned by the query. 
	 */
	unique, 
	/**
	 * Returns the actual query result value. Depending on the used query type the type of the returned value could vary from
	 * List (e.g.: for EntityQueries) to the according type of the requested property in a SelectQuery or PropertyQuery.   
	 */
	value;

	public static final EnumType T = EnumTypes.T(ResultConvenience.class);
	
	@Override
	public EnumType type() {
		return T;
	}	
	
}
