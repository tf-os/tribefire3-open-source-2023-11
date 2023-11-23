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
import com.braintribe.model.queryplan.value.Value;

/**
 * This is something that can be used together with {@link IndexOrderedSet} to represent the ORDER BY clause, as an alternative to just
 * {@link OrderedSet}.
 * <p>
 * In some cases we might use an index to provide our source in correct order. But we might have additional sorting criteria, for which we need to
 * sort the tuples where the primary sorting values are equal.
 * 
 * <h4>Example:</h4>
 * 
 * <tt>select p from Person p order by p.indexedLastName, p.firstName</tt>
 * 
 * <code>
 * OrderedSetRefinement {
 * 		operand: IndexOrderedSet* {
 * 			typeSignature: "Person"
 * 			propertyName: indexedLastName
 * 			metricIndex: RepositoryMetricIndex {indexId: "person#indexedLastName"}
 * 			descending: false
 * 		}
 * 		
 * 		groupValues: [
 * 			Value{TupleComponent{IndexOrderedSet;}, lastName}
 * 		]
 * 		sortCriteria:[
 * 			SortCriterion{
 * 				ValueProperty{TupleComponent{IndexOrderedSet;}, firstName}
 * 			}
 * 		]
 * }
 * </code>
 */

public interface OrderedSetRefinement extends OrderedSet {

	EntityType<OrderedSetRefinement> T = EntityTypes.T(OrderedSetRefinement.class);

	/**
	 * List of values which the tuples we get from the underlying {@link #getOperand() operand} is already sorted by. This values thus define groups
	 * of tuples, where tuples in a common group have all these values the same (see example in the class description).
	 */
	List<Value> getGroupValues();
	void setGroupValues(List<Value> groupValues);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.orderedSetRefinement;
	}

}
