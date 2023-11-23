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
package com.braintribe.model.meta.data.components;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @see ModelExtension
 */
public interface AccessModelExtension extends ModelExtension {

	EntityType<AccessModelExtension> T = EntityTypes.T(AccessModelExtension.class);

	/**
	 * If <tt>false</tt>, the extension is only applied to a model if no new types are introduced via {@link #getModels()} to the model that is being
	 * extended (the data model of given access).
	 */
	boolean getAllowTypeExtension();
	void setAllowTypeExtension(boolean allowTypeExtension);

	@Override
	default boolean allowTypeExtension() {
		return getAllowTypeExtension();
	}

}
