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
package com.braintribe.model.processing.service.api;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.utils.collection.api.MinimalStack;

public interface ServiceRequestContextStack extends MinimalStack<AttributeContext> {

	/**
	 * <p>
	 * Pushes the given {@code attributeContext} to this
	 * {@code ServiceRequestContextStack}.
	 * 
	 * <p>
	 * The pushed {@link AttributeContext} is subsequently available through {@link #get()} calls.
	 * 
	 * <p>
	 * In order to release any resources associated with the pushed {@link AttributeContext},
	 * callers must ensure that {@link #pop()} is invoked once the scope for which
	 * the {@link AttributeContext} was created is completed.
	 * 
	 * @param attributeContext
	 *            The {@link AttributeContext} to be pushed.
	 * @deprecated use {@link #push(AttributeContext)} instead
	 */
	@Deprecated
	default void pushDirect(AttributeContext attributeContext) { push(attributeContext); }
}