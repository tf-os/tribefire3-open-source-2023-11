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
package com.braintribe.model.query;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * Defines the type of {@link Join} used to combine sources. One can be {@link #inner}, {@link #left}, {@link #right},
 * {@link #full}.
 */
public enum JoinType implements EnumBase {
	/**
	 * All the records from both sources where the records intersect.
	 */
	inner,
	/**
	 * All the records from the left source and only those records from the right source that intersect with the left
	 * source.
	 */
	left,
	/**
	 * All the records from the right source and only those records from the left source that intersect with the right
	 * source.
	 */
	right,
	/**
	 * All the records from both sources.
	 */
	full;

	public static final EnumType T = EnumTypes.T(JoinType.class);
	
	@Override
	public EnumType type() {
		return T;
	}	
}
