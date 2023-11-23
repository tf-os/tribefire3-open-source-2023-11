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
package com.braintribe.model.processing.query.tools;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQueryResult;

/**
 * @author peter.gazdik
 */
public interface QueryResults {

	public static SelectQueryResult selectQueryResult(List<Object> results, boolean hasMore) {
		SelectQueryResult result = SelectQueryResult.T.create();
		result.setResults(results);
		result.setHasMore(hasMore);

		return result;
	}

	static EntityQueryResult entityQueryResult(List<?> entities, boolean hasMore) {
		EntityQueryResult entityQueryResult = EntityQueryResult.T.create();
		entityQueryResult.setEntities((List<GenericEntity>) entities);
		entityQueryResult.setHasMore(hasMore);

		return entityQueryResult;
	}

	static PropertyQueryResult propertyQueryResult(Object value, boolean hasMore) {
		PropertyQueryResult result = PropertyQueryResult.T.create();
		result.setPropertyValue(value);
		result.setHasMore(hasMore);

		return result;
	}
}
