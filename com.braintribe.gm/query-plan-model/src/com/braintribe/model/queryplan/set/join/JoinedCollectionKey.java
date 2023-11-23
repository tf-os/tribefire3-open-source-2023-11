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
package com.braintribe.model.queryplan.set.join;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


import com.braintribe.model.queryplan.TupleComponentPosition;

/**
 * A key that identifies an entry in a joined collection (in case the joined property type is List or Map). One can view a {@link ListJoin}
 * and a {@link MapJoin} as a join which adds two new components, the index for each element of the collection, and the collection value
 * itself. For better understanding see example below.
 * 
 * <tt>select * from Person p join p.children c_index, c where c_index < 4</tt>
 * 
 * <code>
 * 
 * 
 * FilteredSet {
 * 		operand: ListJoin {
 * 		  	operand: SourceSet* ;
 * 			listIndex: JoinedListIndex** {
 * 				index: 1
 * 			} 
 * 		  	index: 2
 * 		  	tupleComponentPosition: SourceSet* ;
 * 		  	propertyPath: "children"
 * 	  	}
 * 		filter: LessThan {
 * 			leftOperand: TupleComponent {
 * 				tupleComponentPosition: JoinedListIndex** {
 * 					index: 1
 * 				}
 * 				tupleSlot: SourceSet* ;
 * 				propertyPath: "name"
 * 			}
 * 			rightOperand: StaticValue ; 
 * 		}
 * }
 * * - same instance 
 * ** - same instance
 * }
 * </code>
 * 
 * @see ListJoin
 * @see JoinedListIndex
 * 
 * @see MapJoin
 * @see JoinedMapKey
 * 
 * 
 * 
 */

public interface JoinedCollectionKey extends TupleComponentPosition {

	EntityType<JoinedCollectionKey> T = EntityTypes.T(JoinedCollectionKey.class);

}
