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
package com.braintribe.model.processing.query.planner.context;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import com.braintribe.model.processing.query.eval.api.repo.IndexInfo;
import com.braintribe.model.query.From;
import com.braintribe.model.queryplan.index.Index;
import com.braintribe.model.queryplan.index.RepositoryIndex;
import com.braintribe.model.queryplan.index.RepositoryMetricIndex;

/**
 * 
 */
class IndexRepository {

	private final QueryPlannerContext context;
	private final Map<IndexInfo, Index> map;

	public IndexRepository(QueryPlannerContext context) {
		this.context = context;
		this.map = newMap();
	}

	public <T extends Index> T acquireIndex(From from, IndexInfo indexInfo) {
		Index result = map.get(indexInfo);

		if (result == null) {
			result = newIndexFor(from, indexInfo);
			map.put(indexInfo, result);
		}

		return (T) result;
	}

	private Index newIndexFor(From from, IndexInfo indexInfo) {
		RepositoryIndex index = indexInfo.hasMetric() ? RepositoryMetricIndex.T.create() : RepositoryIndex.T.create();

		index.setIndexId(indexInfo.getIndexId());
		index.setTupleComponentIndex(context.sourceManager().indexForSource(from));

		return index;
	}

}
