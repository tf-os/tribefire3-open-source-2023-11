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

/**
 * Represents the limit and offset part of the query.
 * 
 * <h4>Example:</h4>
 * 
 * <tt>select from Person p limit 10 offset 20</tt>
 * 
 * <code>
 * PagintatedSet {
 * 		operand: SourceSet;
 * 		limit: 10
 * 		offset: 20
 * }
 * </code>
 */

public interface PaginatedSet extends TupleSet {

	EntityType<PaginatedSet> T = EntityTypes.T(PaginatedSet.class);

	TupleSet getOperand();
	void setOperand(TupleSet operand);

	int getLimit();
	void setLimit(int limit);

	int getOffset();
	void setOffset(int offset);

	int getTupleSize();
	void setTupleSize(int tupleSize);

	/**
	 * We had a bug with initial implementation of how hasMore is resolved for {@link PaginatedSet}. Initially, there was only a check whether the
	 * operand iterator has more results (hasNext is still true after getting all the needed tuples). However, in case our operand is for example
	 * concatenation of two different query results (happens in smart access), these results might have pagination already applied. If one of the
	 * query then returns required number of results and the other returns zero, then the delegate iterator has no next, and we would evaluate to
	 * false, but the thing is, the first operand might have more results (i.e. the query result might have had the flag set to true). Therefore, we
	 * have added this flag that says the operand might already have pagination applied, so if no next entry exists, it checks whether the operand
	 * itself says it "has more" (if the flag is set).
	 */
	boolean getOperandMayApplyPagination();
	void setOperandMayApplyPagination(boolean operandMayApplyPagination);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.pagination;
	}

}
