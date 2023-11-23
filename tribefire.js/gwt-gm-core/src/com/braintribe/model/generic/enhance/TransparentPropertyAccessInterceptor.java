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
package com.braintribe.model.generic.enhance;

import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;

/**
 * A {@link PropertyAccessInterceptor} that has no side effect. This is simply useful as a first PAI in a chain (like
 * what is configured for a session), thus enabling us to change the chain without changing the first PAI, which might
 * be referenced from elsewhere.
 */
public class TransparentPropertyAccessInterceptor extends PropertyAccessInterceptor {

	// empty

}
