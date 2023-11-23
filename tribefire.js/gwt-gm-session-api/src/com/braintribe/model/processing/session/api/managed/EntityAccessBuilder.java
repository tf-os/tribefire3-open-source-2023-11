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

import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.processing.async.api.AsyncCallback;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(namespace=GmCoreApiInteropNamespaces.session)
@SuppressWarnings("unusable-by-js")
public interface EntityAccessBuilder<T extends GenericEntity> {

	/**
	 * Returns the specified entity or <code>null</code>, if it doesn't exist.
	 */
	@JsMethod (name = "findSync")
	T find() throws GmSessionException;
	void find(AsyncCallback<T> asyncCallback);

	/**
	 * {@link #find() Finds} the specified entity and throws an exception, if it doesn't exist.
	 * 
	 * @throws NotFoundException
	 *             if the specified entity doesn't exist.
	 */
	@JsMethod (name = "requireSync")
	T require() throws GmSessionException, NotFoundException;
	void require(AsyncCallback<T> asyncCallback);

	@JsMethod (name = "referencesSync")
	ReferencesResponse references() throws GmSessionException;
	void references(AsyncCallback<ReferencesResponse> asyncCallback);
}
