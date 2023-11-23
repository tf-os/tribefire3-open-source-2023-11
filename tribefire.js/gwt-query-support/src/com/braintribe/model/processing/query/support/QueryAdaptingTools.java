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
package com.braintribe.model.processing.query.support;

import static com.braintribe.model.processing.query.support.QueryResultBuilder.buildEntityQueryResult;
import static com.braintribe.model.processing.query.support.QueryResultBuilder.buildPropertyQueryResult;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.record.ListRecord;

/**
 * Contains bunch of helper methods which evaluate given {@link EntityQuery} or {@link PropertyQuery} as a
 * {@link SelectQuery} (using {@link IncrementalAccess#query(SelectQuery)}) method.
 */
public class QueryAdaptingTools {

	public static EntityQueryResult queryEntities(EntityQuery entityQuery, IncrementalAccess access) throws ModelAccessException {
		SelectQuery selectQuery = QueryConverter.convertEntityQuery(entityQuery);
		SelectQueryResult sqResult = access.query(selectQuery);

		return buildEntityQueryResult(sqResult.getResults(), sqResult.getHasMore());
	}

	public static PropertyQueryResult queryProperties(PropertyQuery propertyQuery, IncrementalAccess access) throws ModelAccessException {
		PersistentEntityReference ref = propertyQuery.getEntityReference();
		EntityType<?> entityType = ref.valueType();

		Property property = entityType.getProperty(propertyQuery.getPropertyName());
		SelectQuery selectQuery = QueryConverter.convertPropertyQuery(propertyQuery, entityType, property);
		SelectQueryResult sqResult = access.query(selectQuery);

		return buildPropertyQueryResult(extractPropertyValue(sqResult, property.getType(), propertyQuery), sqResult.getHasMore());
	}

	public static Object extractPropertyValue(SelectQueryResult sqResult, GenericModelType propertyType, PropertyQuery query) {
		List<Object> results = sqResult.getResults();

		if (!propertyType.isCollection())
			return results.isEmpty() ? null : first(results);

		CollectionType collectionType = (CollectionType) propertyType;

		switch (collectionType.getCollectionKind()) {
			case map:
				return convertToMap(results);

			case set:
				if (query.getRestriction() == null && query.getOrdering() == null)
					return new HashSet<>(results);
				//$FALL-THROUGH$

			case list:
				return results;

			default:
				throw new RuntimeQueryEvaluationException("Unknown collection type: " + collectionType.getCollectionKind());
		}
	}

	private static final int KEY_POSITION = 0;
	private static final int VALUE_POSITION = 1;

	private static Map<?, ?> convertToMap(List<Object> rows) {
		Map<Object, Object> result = newMap();

		for (Object row : rows) {
			ListRecord record = (ListRecord) row;
			List<Object> rowValues = record.getValues();

			result.put(rowValues.get(KEY_POSITION), rowValues.get(VALUE_POSITION));
		}

		return result;
	}

}
