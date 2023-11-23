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
package com.braintribe.model.processing.session.api.resource;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.resource.Resource;

import jsinterop.annotations.JsType;

@JsType(namespace=GmCoreApiInteropNamespaces.resources)
@SuppressWarnings("unusable-by-js")
public interface ResourceDeleteBuilder {

	/**
	 * <p>
	 * Sets the use case under which the {@link Resource} binary data is to be deleted.
	 * 
	 * @param useCase
	 *            the use case under which the {@link Resource} binary data is to be deleted.
	 */
	ResourceDeleteBuilder useCase(String useCase);

	/**
	 * <p>
	 * Deletes the binary data backed by the provided {@link Resource}.
	 * 
	 * @throws java.io.UncheckedIOException
	 *             If the IO operation fails.
	 */
	void delete();

}
