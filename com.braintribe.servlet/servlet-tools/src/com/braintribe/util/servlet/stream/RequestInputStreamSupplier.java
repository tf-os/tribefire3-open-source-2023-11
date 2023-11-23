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
package com.braintribe.util.servlet.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import javax.servlet.ServletRequest;

/**
 * <p>
 * Supplies the given {@link ServletRequest}'s {@link InputStream}
 */
public class RequestInputStreamSupplier {

	private ServletRequest request;

	public RequestInputStreamSupplier(ServletRequest request) {
		this.request = request;
	}

	public InputStream get() {
		try {
			return request.getInputStream();
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to get the contents of http request body: " + e.getMessage(), e);
		}
	}

}
