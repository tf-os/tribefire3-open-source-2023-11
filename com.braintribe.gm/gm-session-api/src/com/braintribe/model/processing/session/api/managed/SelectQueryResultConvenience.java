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
package com.braintribe.model.processing.session.api.managed;

import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.query.SelectQueryResult;

public interface SelectQueryResultConvenience extends QueryResultConvenience {

	/**
	 * This method overrides {@link QueryResultConvenience#result()} and returns an {@link SelectQueryResult}
	 */
	@Override
	SelectQueryResult result() throws GmSessionException;

	/**
	 * This method overrides {@link QueryResultConvenience#setVariable(String, Object)} and returns and returns itself
	 */
	@Override
	SelectQueryResultConvenience setVariable(String name, Object value);
	
	/**
	 * This method overrides {@link QueryResultConvenience#setTraversingCriterion(TraversingCriterion)} and returns and returns itself
	 */
	@Override
	SelectQueryResultConvenience setTraversingCriterion(TraversingCriterion traversingCriterion);
}
