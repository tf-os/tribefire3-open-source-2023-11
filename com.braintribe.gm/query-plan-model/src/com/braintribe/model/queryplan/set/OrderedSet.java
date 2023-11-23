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

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * This entity is meant to represent the ORDER BY clause.
 * 
 * <h4>Example:</h4>
 * 
 * <tt>select * from Person p order by p.lastName, p.firstName</tt>
 * 
 * <code>
 * OrderedSet {
 * 		operand: SourceSet;
 * 		sortCriteria:[
 * 			SortCriterion{
 * 				ValueProperty{TupleComponent{SourceSet;}, lastName}
 * 			}
 * 			SortCriterion{
 * 				ValueProperty{TupleComponent{SourceSet;}, firstName}
 * 			}
 * 		]
 * }
 * </code>
 */

public interface OrderedSet extends TupleSet {

	EntityType<OrderedSet> T = EntityTypes.T(OrderedSet.class);

	TupleSet getOperand();
	void setOperand(TupleSet operand);

	List<SortCriterion> getSortCriteria();
	void setSortCriteria(List<SortCriterion> sortCriteria);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.orderedSet;
	}

}
