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
package com.braintribe.model.queryplan.set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A concatenation of two {@link TupleSet}s. Similar to {@link Union}, but does not check for duplicates, i.e. if some tuple is a member of both
 * operands, then it will be twice in the "set" represented by this entity.
 * <p>
 * Note however, that the intended usage is for cases where both operands are disjoint, in which this is the exact same thing as the aforementioned
 * {@link Union}, but can be implemented without any memory footprint.
 * 
 * <h4>Example:</h4>
 * 
 * <tt>SELECT * FROM A, B, WHERE (a.x > 3) OR (b.x > 3)</tt>
 * 
 * <code>
 * Concatenation {
 * 		firstOperand: CartesianProduct{ FilteredSet;, B} 
 * 		secondOperands: {
 * 			FilteredSet{
 * 				operand: CartesianProduct{ 
 * 							A, 
 * 							FilteredSet;
 * 						}
 * 				filter: NOT(a.x > 3)
 * 			}
 * 		]
 * }
 * </code>
 * 
 * Note that in order for this to work we need to apply the filter for the second set that is the negation of the filter from the first set (thus
 * making sure the two operands of the {@linkplain Concatenation} are disjoint).
 * 
 * 
 */

public interface Concatenation extends CombinedSet {

	EntityType<Concatenation> T = EntityTypes.T(Concatenation.class);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.concatenation;
	}

}
