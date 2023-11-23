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
package com.braintribe.model.processing.sp.api;

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;

/**
 * An interface for methods that are common for {@link AfterStateChangeContext} and {@link ProcessStateChangeContext}.
 */
public interface PostStateChangeContext<T extends GenericEntity> extends StateChangeContext<T> {

	/**
	 * Returns a mapping from {@link EntityReference} to {@link PersistentEntityReference}.
	 * 
	 * Note that in some cases not all the persistent references are contained as keys, but all the
	 * {@link PreliminaryEntityReference}s must be resolvable via the returned mapping.
	 */
	Map<EntityReference, PersistentEntityReference> getReferenceMap();

	void notifyInducedManipulation(Manipulation manipulation);

}
