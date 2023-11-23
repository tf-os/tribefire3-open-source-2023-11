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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * An entity that expresses the limit and offset properties when applying pagination.
 * <p>
 * For example, if we want to get 10 results skipping the first 30 (i.e. elements 31st-40th) we would specify {@code limit = 10} and
 * {@code offset = 30};
 * <p>
 * If we want all the elements, we use {@code limit = offset = 0}.
 * 
 * @author peter.gazdik
 */
public interface HasPagination extends GenericEntity {

	EntityType<HasPagination> T = EntityTypes.T(HasPagination.class);

	@Description("Specifies the maximum number of elements returned. Value 0 means there is no limit.")
	int getPageLimit();
	void setPageLimit(int pageLimit);

	@Description("Specifies how many elements should be skipped from the original list. Value 0 means start from the beginning.")
	int getPageOffset();
	void setPageOffset(int pageOffset);

	default boolean hasPagination() {
		return getPageLimit() > 0 || getPageOffset() > 0;
	}

}
