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

import java.util.Collection;

import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.api.repo.IndexingRepository;
import com.braintribe.model.processing.query.eval.tools.TupleIterable;
import com.braintribe.model.queryplan.index.RepositoryIndex;

/**
 * 
 */
public class EvalRepositoryIndex implements EvalIndex {

	protected final IndexingRepository repository;
	protected final String indexId;
	protected final int componentIndex;

	public EvalRepositoryIndex(IndexingRepository repository, RepositoryIndex repositoryIndex) {
		this.repository = repository;
		this.indexId = repositoryIndex.getIndexId();
		this.componentIndex = repositoryIndex.getTupleComponentIndex();
	}

	@Override
	public Iterable<Tuple> getAllValuesForIndex(Object indexValue) {
		return new TupleIterable(componentIndex, repository.getAllValuesForIndex(indexId, indexValue));
	}

	@Override
	public Iterable<Tuple> getAllValuesForIndices(Collection<?> indexValues) {
		return new TupleIterable(componentIndex, repository.getAllValuesForIndices(indexId, indexValues));
	}

}
