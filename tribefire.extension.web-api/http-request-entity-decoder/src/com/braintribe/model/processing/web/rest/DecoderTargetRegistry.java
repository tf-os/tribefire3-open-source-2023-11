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
package com.braintribe.model.processing.web.rest;

import javax.servlet.http.HttpServletRequest;

import com.braintribe.model.generic.GenericEntity;

public interface DecoderTargetRegistry {

	/**
	 * Adds a new target entity to receive values from the decoded {@link HttpServletRequest}.
	 * 
	 * @param prefix
	 *            the prefix to use to bypass ordering, must not be {@code null}
	 * @param target
	 *            the target entity to decode into, must not be {@code null}
	 * @param onSet
	 * 			  code that gets executed when the target is actually addressed and set in a request
	 * 
	 * @return this decoder
	 */
	DecoderTargetRegistry target(String prefix, GenericEntity target, Runnable onSet);

}