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

import java.util.Collection;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


/**
 * Represents a {@link Collection} of {@link GenericEntity entities} which are retrieved from an index with given {@link #getIndexId()
 * indexId} for given {@link #getKeys() keys}. These keys can actually be resolved to a collection, or just a single value, but when it has
 * the type {@link Collection}, we always interpret it as a collection rather than a single value of collection type, because we do not
 * support indices on collection properties.
 */

public interface IndexValue extends ConstantValue {

	EntityType<IndexValue> T = EntityTypes.T(IndexValue.class);

	String getIndexId();
	void setIndexId(String indexId);

	ConstantValue getKeys();
	void setKeys(ConstantValue keys);

	@Override
	default ValueType valueType() {
		return ValueType.indexValue;
	}

}
