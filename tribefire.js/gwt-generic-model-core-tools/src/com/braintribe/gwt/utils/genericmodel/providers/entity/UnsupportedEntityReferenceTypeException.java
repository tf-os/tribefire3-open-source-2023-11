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
package com.braintribe.gwt.utils.genericmodel.providers.entity;

import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;

/**
 * An exception thrown by {@link EntityProvider}s, if the passed {@link EntityReference} type is not supported, e.g.
 * because the provider can only process {@link PreliminaryEntityReference}s.
 *
 * @author michael.lafite
 */
@SuppressWarnings("serial")
public class UnsupportedEntityReferenceTypeException extends EntityProviderException {

	public UnsupportedEntityReferenceTypeException(final String message) {
		super(message);
	}

	public UnsupportedEntityReferenceTypeException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
