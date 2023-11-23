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

import java.util.List;

import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.QueryResult;
import com.braintribe.model.query.SelectQuery;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType
@SuppressWarnings("unusable-by-js")
public interface QueryResultConvenience {
	
	/**
	 * Returns the envelope object (an instance of {@link QueryResult}) returned by the query.
	 * Concrete derivations of {@link QueryResultConvenience} may override this method and specify a concrete implementation of {@link QueryResult}
	 */
	@JsMethod (name = "resultSync")
	QueryResult result() throws GmSessionException;
	
	/**
	 * Returns the query result as {@link List} 
	 */
	<E> List<E> list() throws GmSessionException;
	
	/**
	 * Convenience method to return the first instance of the query result, or <code>null</code> if the query returns not results. 
	 */
	<E> E first() throws GmSessionException;
	
	/**
	 * Convenience method to return a single instance of the query result, or <code>null</code> if the query returns no results.
	 * This method throws an {@link GmSessionException} if more then one result is returned by the query. 
	 */
	<E> E unique() throws GmSessionException;

	/**
	 * Returns the actual query result value. Depending on the used query type the type of the returned value could vary from
	 * {@link List} (e.g.: for {@link EntityQuery}s) to the according type of the requested property in a {@link SelectQuery} or {@link PropertyQuery}.   
	 */
	<E> E value() throws GmSessionException;
	
	/**
	 * Fills the variable context for the query evaluation.
	 */
	QueryResultConvenience setVariable(String name, Object value);
	
	/**
	 * Sets the passed traversingCriterion to the {@link Query}. An already existing {@link TraversingCriterion} will be overridden by this method.
	 */
	QueryResultConvenience setTraversingCriterion(TraversingCriterion traversingCriterion);

}
