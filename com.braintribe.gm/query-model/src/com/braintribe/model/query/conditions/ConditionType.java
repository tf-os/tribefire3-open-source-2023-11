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
package com.braintribe.model.query.conditions;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * Defines the type of {@link Condition} used as a restriction on a query.
 */
public enum ConditionType implements EnumBase {

	// comparisons
	/**
	 * Used to compare a <code>String</code> text to a defined Source
	 */
	fulltextComparison,
	/**
	 * Used to compare a value to a value of a property
	 */
	valueComparison,

	// logical
	/**
	 * Used to combine multiple {@link Condition}s, all of which must be true before the result to be returned. This is
	 * the equivalent of an AND operator.
	 */
	conjunction,
	/**
	 * Used to combine multiple {@link Condition}s, one of which must be true before the result to be returned. This is
	 * the equivalent of an OR operator.
	 */
	disjunction,
	/**
	 * Used to negate the functionality of a {@link Condition}.
	 */
	negation;

	public static final EnumType T = EnumTypes.T(ConditionType.class);
	
	@Override
	public EnumType type() {
		return T;
	}

}
