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
package com.braintribe.model.processing.query.api.shortening;

public class QueryShorteningRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 4862697584058616972L;

	public QueryShorteningRuntimeException() {
		super();
	}

	public QueryShorteningRuntimeException(final String message) {
		super(message);
	}

	public QueryShorteningRuntimeException(final Throwable cause) {
		super(cause);
	}

	public QueryShorteningRuntimeException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public QueryShorteningRuntimeException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
