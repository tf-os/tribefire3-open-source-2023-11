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
package com.braintribe.model.processing.query.eval.api.repo;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.conditions.Condition;

/**
 * 
 */
public interface DelegatingRepository extends Repository {

	/**
	 * Provides entities for given restrictions
	 */
	Iterable<? extends GenericEntity> provideEntities(String typeSignature, Condition condition, Ordering ordering);

	/** @return <tt>true</tt> iff the repository takes care of the fulltext search, and thus it can be ignored by the query evaluator. */
	boolean supportsFulltextSearch();

}
