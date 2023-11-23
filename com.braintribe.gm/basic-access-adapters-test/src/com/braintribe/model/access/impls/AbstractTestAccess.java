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
package com.braintribe.model.access.impls;

import com.braintribe.model.access.BasicAccessAdapter;

/**
 * This implementation simply overrides some methods to avoid cloning of the data before returning it, thus ensuring
 * entities retrieved via queries are exactly the same s ones registered in the smood for the test.
 */
public abstract class AbstractTestAccess extends BasicAccessAdapter {

	// empty (there was code, overriding some methods, but I didn't like it as it was not really testing the BAA then)

}
