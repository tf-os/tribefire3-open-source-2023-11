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
package com.braintribe.model.processing.session.api.managed;

import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.value.EntityReference;

/**
 * Represents type of a manipulations in the manipulation stack.
 * 
 * @see ManipulationApplicationContext#getMode()
 */
public enum ManipulationMode {

	/**
	 * Manipulations that use {@link LocalEntityProperty} as it's owner and values for properties are directly GM entities.
	 */
	LOCAL,

	/**
	 * Manipulations that use {@link EntityProperty} as owner and values for properties are {@link EntityReference}s.
	 */
	REMOTE
}
