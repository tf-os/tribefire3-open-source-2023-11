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
package com.braintribe.model.processing.query.eval.context;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.eval.api.repo.IndexingRepository;
import com.braintribe.model.processing.query.eval.index.EvalGeneratedIndex;
import com.braintribe.model.processing.query.eval.index.EvalGeneratedMetricIndex;
import com.braintribe.model.processing.query.eval.index.EvalIndex;
import com.braintribe.model.processing.query.eval.index.EvalMetricIndex;
import com.braintribe.model.processing.query.eval.index.EvalRepositoryIndex;
import com.braintribe.model.processing.query.eval.index.EvalRepositoryMetricIndex;
import com.braintribe.model.queryplan.index.GeneratedIndex;
import com.braintribe.model.queryplan.index.GeneratedMetricIndex;
import com.braintribe.model.queryplan.index.Index;
import com.braintribe.model.queryplan.index.MetricIndex;
import com.braintribe.model.queryplan.index.RepositoryIndex;
import com.braintribe.model.queryplan.index.RepositoryMetricIndex;

/**
 * 
 */
public class IndexRepository {

	protected final QueryEvaluationContext context;
	protected final IndexingRepository repository;
	protected Map<Index, EvalIndex> map = newMap();

	public IndexRepository(QueryEvaluationContext context, IndexingRepository repository) {
		this.context = context;
		this.repository = repository;
	}

	protected EvalMetricIndex resolveMetricIndex(MetricIndex index) {
		return (EvalMetricIndex) resolveIndex(index);
	}

	protected EvalIndex resolveIndex(Index index) {
		EvalIndex result = map.get(index);

		if (result == null) {
			result = newEvalIndexFor(index);
			map.put(index, result);
		}

		return result;
	}

	private EvalIndex newEvalIndexFor(Index index) {
		switch (index.indexType()) {
			case generated:
				return new EvalGeneratedIndex(context, (GeneratedIndex) index);
			case generatedMetric:
				return new EvalGeneratedMetricIndex(context, (GeneratedMetricIndex) index);
			case repository:
				return new EvalRepositoryIndex(repository, (RepositoryIndex) index);
			case repositoryMetric:
				return new EvalRepositoryMetricIndex(repository, (RepositoryMetricIndex) index);
		}

		throw new RuntimeQueryEvaluationException("Unsupported Index: " + index + " of type: " + index.indexType());
	}

}
