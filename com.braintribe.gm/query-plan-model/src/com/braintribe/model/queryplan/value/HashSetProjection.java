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
package com.braintribe.model.queryplan.value;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.queryplan.set.TupleSet;

/**
 * This is a very special value, because it references a {@link TupleSet}. Basically it represents a (hash) set of all the values described
 * by {@link HashSetProjection#setValue(Value)} in given {@linkplain TupleSet}. This may for instance be used to enable usage of nested
 * queries as operands in the where clause, so for example a query like:
 * <code>select * from Person p where p.company in (select c from Company c where [someCondition]) </code>, would turn into a plan like
 * this:
 * 
 * <code> 
 * FilteredSet {
 * 		operand: SourceSet ;*
 * 		filter: In {
 * 			leftOperand: TupleComponent {
 * 					tupleComponentIndex: 0
 * 				}
 * 			rightOperand: HashSetProjection {
 * 				tupleSet: FilteredSet {Company;*, [someCondition],}
 * 				value: TupleComponent(tupleComponentIndex:0)
 * 			} 
 * 		}
 * }
 * * - note that both sources have index 0, as they come from different source (which is currently not supported) 
 * </code>
 */

public interface HashSetProjection extends Value {

	EntityType<HashSetProjection> T = EntityTypes.T(HashSetProjection.class);

	TupleSet getTupleSet();
	void setTupleSet(TupleSet tupleSet);

	Value getValue();
	void setValue(Value value);

	@Override
	default ValueType valueType() {
		return ValueType.hashSetProjection;
	}

}
