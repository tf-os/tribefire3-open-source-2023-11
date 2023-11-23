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
package com.braintribe.model.meta.selector;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmEntityType;

/**
 * 
 */

public interface AccessTypeSelector extends MetaDataSelector {

	EntityType<AccessTypeSelector> T = EntityTypes.T(AccessTypeSelector.class);

	String accessType = "accessType";

	/**
	 * Type of the access this selector is active for. I.e. this is expected to be a sub-type of IncrementalAccess.
	 */
	GmEntityType getAccessType();
	void setAccessType(GmEntityType AccessType);

	/**
	 * Determines whether we want to compare the access if assignable or equal.
	 */
	boolean getAssignable();
	void setAssignable(boolean assignable);

}
