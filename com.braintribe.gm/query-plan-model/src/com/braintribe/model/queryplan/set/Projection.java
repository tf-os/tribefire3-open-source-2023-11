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
 * This entity is meant to represent the SELECT clause. The operand represents the result of the query if we were selecting everything ( "select *
 * from ..."). If however there are some attributes or functions specified for the SELECT clause, we represent each of such attributes as an element
 * in the "values" list. Functions (not supported yet) could be something like type-conversion, substring or even aggregate functions like count(*).
 * 
 * <h4>Example:</h4>
 * 
 * <tt>select p, c.owner, 5 from Person p, Company c where p.companyName = c.name</tt>
 * 
 * <code>
 * Projection {
 * 		operand: CartesianProduct;
 * 		values:[
 * 			TupleComponent{tupleComponentPosition: SourceSet;},
 * 			ValueProperty{
 * 				value: TupleComponent{tupleComponentPosition: SourceSet;},
 * 				propertyPath: "owner"
 * 			},
  			StaticValue ;
 * 		]
 * }
 * </code>
 */

public interface Projection extends TupleSet {

	EntityType<Projection> T = EntityTypes.T(Projection.class);

	TupleSet getOperand();
	void setOperand(TupleSet operand);

	List<Value> getValues();
	void setValues(List<Value> values);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.projection;
	}

}
