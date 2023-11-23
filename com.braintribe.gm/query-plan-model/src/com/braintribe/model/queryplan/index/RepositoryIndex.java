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



/**
 * @see Index
 */

public interface RepositoryIndex extends Index {

	EntityType<RepositoryIndex> T = EntityTypes.T(RepositoryIndex.class);

	String getIndexId();
	void setIndexId(String indexId);

	/**
	 * This is redundant, but makes evaluator implementation easier, because the implementation returns tuples, so we need the component
	 * position to generate the right tuple.
	 */
	int getTupleComponentIndex();
	void setTupleComponentIndex(int tupleComponentIndex);

	@Override
	default IndexType indexType() {
		return IndexType.repository;
	}

}
