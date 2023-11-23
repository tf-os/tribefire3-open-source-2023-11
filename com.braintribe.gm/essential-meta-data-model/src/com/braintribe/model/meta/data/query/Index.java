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
package com.braintribe.model.meta.data.query;


import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.ModelSkeletonCompatible;
import com.braintribe.model.meta.data.PropertyMetaData;

/**
 * Specifies that given property should be indexed in the persistence layer, with the possibility to specify {@link IndexType type} of index
 * it should be.
 */

public interface Index extends PropertyMetaData, ModelSkeletonCompatible {

	EntityType<Index> T = EntityTypes.T(Index.class);

	// @formatter:off
	/**
	 * Specifies the {@link IndexType type} of given index. If value is <tt>null</tt>, it is treated as
	 * {@link IndexType#auto}.
	 */
	IndexType getIndexType();
	void setIndexType(IndexType indexType);
	// @formatter:on

}
