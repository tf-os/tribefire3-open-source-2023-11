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



/**
 * This represents a property value of some other value (which is expected to be an entity). Typical example would be a property of some
 * source set, e.g in :
 * 
 * "from Person p where p.lastName like '% Smith'"
 * 
 * the left operand of the <tt>like</tt> operator would be expressed as a {@linkplain ValueProperty} with <tt>value</tt> being an instance
 * of {@link TupleComponent} (representing entity "Person") and <tt>propertyPath</tt> would be the string "lastName". In other words:
 * 
 * <code>
 * FilteredSet {
 * 		operand: SourceSet; 
 * 		filter: Like {
 * 			leftOperand: ValueProperty {
 * 				value: TupleComponent {tupleComponentPosition: SourceSet ;}
 * 				propertyPath: "lastName"
 * 			}
 * 			rightOperand: StaticValue {value: "% Smith"}
 * 		}		 		
 * }
 * </code>
 * 
 */
public interface ValueProperty extends Value {

	EntityType<ValueProperty> T = EntityTypes.T(ValueProperty.class);

	Value getValue();
	void setValue(Value value);

	String getPropertyPath();
	void setPropertyPath(String propertyPath);

	@Override
	default ValueType valueType() {
		return ValueType.valueProperty;
	}

}
