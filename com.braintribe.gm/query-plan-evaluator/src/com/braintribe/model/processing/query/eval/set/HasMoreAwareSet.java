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
package com.braintribe.model.processing.query.eval.set;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;

/**
 * Some {@link EvalTupleSet}s may themselves be responsible for cutting the results due to pagination. Not only
 * {@link EvalPaginatedSet}, but for example in smart evaluator we have DelegateQuerySet, which may delegate the
 * pagination to the underlying access. In either case, the hasMore indicates that this {@link EvalTupleSet}, or some
 * underlying one, has cut the results due to pagination.
 */
public interface HasMoreAwareSet {

	boolean hasMore();

}
