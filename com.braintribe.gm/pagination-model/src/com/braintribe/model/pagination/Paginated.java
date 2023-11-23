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
package com.braintribe.model.pagination;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Abstract base type for any result of a computation where {@link HasPagination pagination} was applied, typically given by a request which extends
 * {@link HasPagination}. One could say this instance represents a single page in this context.
 * <p>
 * The actual type must contain exactly one property of type {@link List}, whose elements represent the desired subsequence based on given
 * {@link HasPagination#getPageLimit() limit} and {@link HasPagination#getPageOffset() offset} parameters. If there was a need for more than one List
 * properties on the result, all other lists should be embedded in another entity type which will then be referenced by the result entity type.
 * 
 * @author peter.gazdik
 */
@Abstract
public interface Paginated extends GenericEntity {

	EntityType<Paginated> T = EntityTypes.T(Paginated.class);

	/**
	 * Value <tt>true</tt> means there are more elements in the original result, while <tt>false</tt> means this response already contains the last
	 * element.
	 */
	boolean getHasMore();
	void setHasMore(boolean hasMore);

}
