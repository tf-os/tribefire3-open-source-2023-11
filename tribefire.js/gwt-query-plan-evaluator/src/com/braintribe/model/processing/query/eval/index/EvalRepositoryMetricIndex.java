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
package com.braintribe.model.processing.query.eval.index;

import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.api.repo.IndexingRepository;
import com.braintribe.model.processing.query.eval.tools.TupleIterable;
import com.braintribe.model.queryplan.index.RepositoryMetricIndex;

/**
 * 
 */
public class EvalRepositoryMetricIndex extends EvalRepositoryIndex implements EvalMetricIndex {

	public EvalRepositoryMetricIndex(IndexingRepository repository, RepositoryMetricIndex repositoryIndex) {
		super(repository, repositoryIndex);
	}

	@Override
	public Iterable<Tuple> getIndexRange(Object from, Boolean fromInclusive, Object to, Boolean toInclusive) {
		return new TupleIterable(componentIndex, repository.getIndexRange(indexId, from, fromInclusive, to, toInclusive));
	}

	@Override
	public Iterable<Tuple> getFullRange(boolean reverseOrder) {
		return new TupleIterable(componentIndex, repository.getFullRange(indexId, reverseOrder));
	}



}
