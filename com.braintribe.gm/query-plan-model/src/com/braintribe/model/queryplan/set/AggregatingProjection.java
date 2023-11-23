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
 * This entity is meant to represent the SELECT clause and is very similar to {@link Projection}. The difference is this gives information that
 * aggregate functions are being used, which means a completely different implementation of the projection must be used (this one has to store the
 * entire result set in the memory).
 * 
 * <h4>Example:</h4>
 * 
 * <tt>select p.name, count(p.id) from Person p</tt>
 * 
 * <code>
 * AggregatingProjection {
 * 		operand: SourceSet;
 * 		values:[
 * 			ValueProperty{
 * 				value: TupleComponent{tupleComponentPosition: SourceSet;},
 * 				propertyPath: "name"
 * 			},
 * 			AggregateFunction {
 * 	 			operand: ValueProperty{
 * 					value: TupleComponent{tupleComponentPosition: SourceSet;},
 * 					propertyPath: "id"
 * 				}
 * 				aggregateType: Aggregation
 * 			}
 * 		]
 * }
 * </code>
 */

public interface AggregatingProjection extends Projection {

	EntityType<AggregatingProjection> T = EntityTypes.T(AggregatingProjection.class);

	// TODO note this is optional, only if it also was in the query

	List<Value> getGroupByValues();
	void setGroupByValues(List<Value> groupByValues);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.aggregatingProjection;
	}

}
