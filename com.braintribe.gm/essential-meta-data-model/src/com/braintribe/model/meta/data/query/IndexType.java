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

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * Specifies what type of index should be used for given property ({@link Index#setIndexType(IndexType)}). The two basic types are
 * <tt>lookup</tt> and <tt>metric</tt>. The difference being, that <tt>metric</tt> is also ordered, requiring more complex data structure to
 * hold the data, but providing possibilities to retrieve ranges (from, to).
 * 
 * <p>
 * Right now, metric index could be used with property of any type, but it makes no sense to it for entity type and boolean (enum is
 * questionable).
 */
public enum IndexType implements EnumBase {

	/**
	 * No index should be used.
	 */
	none,

	/**
	 * The repository selects the {@linkplain IndexType} based on the type of the property. In general, it makes no sense to use
	 * <tt>metric</tt> index for entity and boolean, enum is questionable. In other cases (simple types), metric index is selected. Also, it
	 * might not make sense to have a <tt>metric</tt> index for an id property.
	 */
	auto,

	/**
	 * If set, the index will always be a <tt>lookup</tt> index.
	 */
	lookup,

	/**
	 * If set, the index will be <tt>metric</tt> if the repository thinks it makes sense (i.e. might be implementation specific). For simple
	 * types other than boolean, this would always be the case I guess.
	 */
	metric;

	public static final EnumType T = EnumTypes.T(IndexType.class);
	
	@Override
	public EnumType type() {
		return T;
	}	
}
