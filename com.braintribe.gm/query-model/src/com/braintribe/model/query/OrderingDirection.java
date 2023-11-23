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
 * Defines the type of the direction({@link #ascending}, {@link #descending}) in which the results should be ordered
 */
public enum OrderingDirection implements EnumBase {
	/**
	 * From the lowest value to the highest
	 */
	ascending,
	/**
	 * From the highest value to the lowest
	 */
	descending;

	public static final EnumType T = EnumTypes.T(OrderingDirection.class);
	
	@Override
	public EnumType type() {
		return T;
	}	
}
