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
package com.braintribe.model.access.impls;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.braintribe.model.access.BasicAccessAdapter;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;

/**
 * There was a problem related to partitions, which showed up when we had a condition comparing two entities in a query.
 * For example: <code>select p from Person p where p.address = PersistentEntityReference(1, 'accessId')</code>.
 * 
 * The problem was, that the left side of comparison was coming directly from the data result of loadPopulation, where
 * partition was null. the right side - the reference - was resolved with {@link BasicAccessAdapter#getEntity}, which
 * before was delegating to {@link BasicAccessAdapter#queryEntities(com.braintribe.model.query.EntityQuery)}, and this
 * had the partition set. Thus the used
 */
public class PopulationProvidingPartitionRemovingAccess extends BasicAccessAdapter {

	private final Smood dataSource;

	public PopulationProvidingPartitionRemovingAccess(Smood dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	protected EntityQueryResult createEntityQueryResult(EntityQuery query, List<GenericEntity> resultingEntities, boolean hasMore) throws Exception {
		EntityQueryResult result = EntityQueryResult.T.create();
		result.setEntities(resultingEntities);
		result.setHasMore(hasMore);

		return result;
	}

	@Override
	protected Collection<GenericEntity> loadPopulation() throws ModelAccessException {
		Set<GenericEntity> entities = dataSource.getEntitiesPerType(GenericEntity.T);
		Collection<GenericEntity> result = BaseType.INSTANCE.clone(entities, null, null);
		result.forEach(e -> e.setPartition(null));

		return result;
	}

}
