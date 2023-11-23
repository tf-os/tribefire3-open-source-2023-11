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
package com.braintribe.model.query;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * This class encapsulates the paging related properties of the returned results. One can restrict the amount of the returned results
 * {@link #setPageSize(int)} and set from which index value the results should be displayed {@link #setStartIndex(int)}.
 */
public interface Paging extends GenericEntity {

	EntityType<Paging> T = EntityTypes.T(Paging.class);

	/** Maximum number of rows returned. Zero and negative values are interpreted as positive infinity (well, Integer.MAX_VALUE). */
	int getPageSize();
	void setPageSize(int pageSize);

	int getStartIndex();
	void setStartIndex(int page);

	static Paging create(int startIndex, int pageSize) {
		Paging paging = Paging.T.create();
		paging.setStartIndex(startIndex);
		paging.setPageSize(pageSize);
		return paging;
	}

}
