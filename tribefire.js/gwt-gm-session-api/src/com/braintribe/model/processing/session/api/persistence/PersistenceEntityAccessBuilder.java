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
package com.braintribe.model.processing.session.api.persistence;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.api.managed.EntityAccessBuilder;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.processing.async.api.AsyncCallback;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(namespace=GmCoreApiInteropNamespaces.session)
@SuppressWarnings("unusable-by-js")
public interface PersistenceEntityAccessBuilder<T extends GenericEntity> extends EntityAccessBuilder<T> {
	public PersistenceEntityAccessBuilder<T> withTraversingCriterion(TraversingCriterion tc);
	
	/**
	 * Returns the specified entity or <code>null</code>, if it doesn't exist. Attention: if the entity is already available in the session,
	 * it will just be returned, i.e. no query will be executed and thus properties will not be updated! This also
	 * means that the {@link #withTraversingCriterion(TraversingCriterion) traversing criterion} (if set) will not be applied!
	 * 
	 * @see PersistenceEntityAccessBuilder#refresh()
	 */
	@Override
	public T find() throws GmSessionException;
	
	/**
	 * Returns the specified entity if it is already available in the session, or creates a new shallow instance
	 * otherwise. Note that this method always returns and entity and never performs any query.
	 * 
	 * @see PersistenceGmSession#shallowifyInstances()
	 */
	public T findLocalOrBuildShallow() throws GmSessionException;

	/**
	 * Refreshes the specified entity. This method works like {@link #require()}, but it always executes a query, 
	 * i.e. even if the entity is already available in the session.
	 */
	@JsMethod (name = "refreshSync")
	public T refresh() throws GmSessionException, NotFoundException;
	
	public void refresh(AsyncCallback<T> asyncCallback);
}
