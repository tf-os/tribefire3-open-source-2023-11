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

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * 
 * Base class for some set operations on tuples.
 * <p>
 * NOTE: Operands must be compatible, i.e. the structure of those tuple sets is the same. By structure we mean the underlying source sets and joins.
 * So for example one can combine two {@link SourceSet}s iff it is the exact same source.
 * 
 * @see Concatenation
 * @see Union
 * @see Intersection
 * 
 * @author pit & dirk
 */
@Abstract
public interface CombinedSet extends TupleSet {

	EntityType<CombinedSet> T = EntityTypes.T(CombinedSet.class);

	TupleSet getFirstOperand();
	void setFirstOperand(TupleSet firstOperand);

	TupleSet getSecondOperand();
	void setSecondOperand(TupleSet secondOperand);

	int getTupleSize();
	void setTupleSize(int tupleSize);

}
