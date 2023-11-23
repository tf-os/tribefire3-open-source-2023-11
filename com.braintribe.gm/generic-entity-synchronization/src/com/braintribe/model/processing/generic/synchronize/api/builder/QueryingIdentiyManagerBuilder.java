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
package com.braintribe.model.processing.generic.synchronize.api.builder;

import com.braintribe.model.processing.generic.synchronize.api.GenericEntitySynchronization;
import com.braintribe.model.processing.generic.synchronize.experts.QueryingIdentityManager;

/**
 * Base interface for builders providing implementations of
 * {@link QueryingIdentityManager}.
 */
public interface QueryingIdentiyManagerBuilder<S extends GenericEntitySynchronization, B extends QueryingIdentiyManagerBuilder<S, B>>
		extends IdentityManagerBuilder<S> {

	/**
	 * If called, the session cache won't be respected during lookup of already
	 * existing entities.
	 */
	B ignoreCache();

	/**
	 * If called, identity properties with a null value will be included in the
	 * query. By default, if at least one of the identity properties has a null
	 * value the lookup query won't be executed and no existing entity is
	 * assumed.
	 */
	B supportNullIdentityProperty();

}
