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
import com.braintribe.model.queryplan.value.Value;

/**
 * 
 * Represent a full Cartesian product of the passed tuple sets.
 * 
 * <h4>Example:</h4>
 * 
 * <tt>select * from Person p, Company c where p.companyName = c.name</tt>
 * 
 * <code>
 * MergeLookupJoin {
 * 		operand: SourceSet ;
 * 		value: ValueProperty {
 * 			value: TupleComponent {
 * 				tupleComponentPosition: SourceSet ;
 * 			}
 * 			propertyPath: "companyName"
 * 		}
 * 		index: GeneratedIndex {
 * 			operand: SourceSet ;
 * 			indexKey: ValueProperty {
 * 				value: TupleComponent {
 * 					tupleComponentPosition: SourceSet ;
 * 				}
 * 				propertyPath: "name"
 * 			}
 * 		}
 * }
 * </code>
 */

public interface MergeLookupJoin extends MergeJoin {

	EntityType<MergeLookupJoin> T = EntityTypes.T(MergeLookupJoin.class);

	Value getValue();
	void setValue(Value value);

	TupleSet getOtherOperand();
	void setOtherOperand(TupleSet otherOperand);

	Value getOtherValue();
	void setOtherValue(Value otherValue);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.mergeLookupJoin;
	}

}
