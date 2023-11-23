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
package com.braintribe.template.processing.api.exception;

public class TemplateResolvingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TemplateResolvingException() {
		super();
	}

	public TemplateResolvingException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public TemplateResolvingException(String msg) {
		super(msg);
	}

	protected TemplateResolvingException(Throwable cause) {
		super(cause);
	}

}
