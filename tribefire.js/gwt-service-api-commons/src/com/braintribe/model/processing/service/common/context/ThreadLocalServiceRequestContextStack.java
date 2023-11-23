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
package com.braintribe.model.processing.service.common.context;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.model.processing.service.api.ServiceRequestContextStack;
import com.braintribe.utils.collection.impl.AttributeContexts;

/**
 * 
 * @author Dirk Scheffler
 * @deprecated use {@link AttributeContexts} instead
 */
@Deprecated
public class ThreadLocalServiceRequestContextStack implements ServiceRequestContextStack {

	@Override
	public void push(AttributeContext element) {
		AttributeContexts.push(element);
	}

	@Override
	public AttributeContext peek() {
		return AttributeContexts.peek();
	}

	@Override
	public AttributeContext pop() {
		return AttributeContexts.pop();
	}

	@Override
	public boolean isEmpty() {
		return AttributeContexts.stack.isEmpty();
	}
}