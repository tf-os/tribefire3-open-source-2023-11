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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.queryplan.index.GeneratedIndex;

/**
 * Just a convenience class to facilitate generic handling of (similar) TupleSets (and anything else that has {@link TupleSet} as operand, like
 * {@link GeneratedIndex}). It does not extend {@link TupleSet} so this is not a {@link GenericEntity}, cause there is no reason for this to be part
 * of the meta-model.
 */
@Abstract
public interface OperandSet extends GenericEntity {

	EntityType<OperandSet> T = EntityTypes.T(OperandSet.class);

	TupleSet getOperand();
	void setOperand(TupleSet operand);

}
