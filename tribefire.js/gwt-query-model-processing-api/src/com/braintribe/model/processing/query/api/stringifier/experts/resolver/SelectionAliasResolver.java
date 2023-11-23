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
package com.braintribe.model.processing.query.api.stringifier.experts.resolver;

import com.braintribe.model.query.Source;

public interface SelectionAliasResolver {
	/**
	 * Returns null if no alias can be determined.
	 */
	public String getAliasForSource(Source source);
	
	/**
	 * Returns the propertyName specific for the given source (for example, based on some metadata, such as Name).
	 * @param source - The Source of the query
	 */
	public default String getPropertyNameForSource(Source source, String propertyName) {
		return propertyName;
	}
}
