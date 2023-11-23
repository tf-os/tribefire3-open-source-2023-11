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
package com.braintribe.model.queryplan.index;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.queryplan.set.OperandSet;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.value.Value;

/**
 * NOTE: Implements OperandSet (which exists just for convenience) even though it is not a {@link TupleSet}. Really, the only point of
 * {@link OperandSet} is as a handle for something that has property operand of type {@link TupleSet}.
 * 
 * @see Index
 */

public interface GeneratedIndex extends Index, OperandSet {

	EntityType<GeneratedIndex> T = EntityTypes.T(GeneratedIndex.class);

	Value getIndexKey();
	void setIndexKey(Value indexKey);

	@Override
	default IndexType indexType() {
		return IndexType.generated;
	}

}
