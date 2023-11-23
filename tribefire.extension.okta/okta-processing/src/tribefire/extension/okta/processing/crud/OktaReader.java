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
package tribefire.extension.okta.processing.crud;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.access.crud.api.read.PopulationReadingContext;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.Paging;

public abstract class OktaReader {

	private int defaultPageSize = 100;

	// ***************************************************************************************************
	// Setters
	// ***************************************************************************************************

	@Configurable
	public void setDefaultPageSize(int defaultPageSize) {
		this.defaultPageSize = defaultPageSize;
	}

	// ***************************************************************************************************
	// Query Helpers
	// ***************************************************************************************************

	protected Ordering resolveOrdering(PopulationReadingContext<?> context) {
		// Providing the type specific ordering is currently not supported by the framework (AccessAdapter).
		// So, context.getOrdering() always returns null.
		// Once it is supported we are prepared to use it with this method.
		// For now we are falling back to the originalOrdering of the query, if available.
		Ordering ordering = context.getOrdering();
		return (ordering == null) ? context.originalOrdering() : ordering;
	}

	protected Paging resolvePaging(PopulationReadingContext<?> context) {
		Paging paging = Paging.T.create();
		Paging originalPaging = context.originalPaging();
		if (originalPaging == null) {
			// No original Paging provided in the context. Create Default one.
			paging.setStartIndex(0);
			paging.setPageSize(this.defaultPageSize);
		} else {
			paging.setStartIndex(originalPaging.getStartIndex());
			paging.setPageSize(originalPaging.getPageSize());
		}

		// Now we have a paging (either originally from the request or the default one)
		// We finally need to increment the pageSize in order to let the framework know whether there are more elements
		// available.
		paging.setPageSize(paging.getPageSize() + 1); // This ensures the hasMore indication - this will be removed by
														// the AccessAdapter if available.

		return paging;
	}

}
