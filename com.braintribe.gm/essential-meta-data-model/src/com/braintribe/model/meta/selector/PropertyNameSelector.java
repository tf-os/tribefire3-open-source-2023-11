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
import com.braintribe.model.meta.data.PropertyMetaData;

/**
 * Activates {@link PropertyMetaData} for property with given name. This only makes sense if set for MD defined on
 * {@link GmEntityType#setPropertyMetaData(java.util.Set) entity} level.
 */
public interface PropertyNameSelector extends MetaDataSelector {

	EntityType<PropertyNameSelector> T = EntityTypes.T(PropertyNameSelector.class);

	String getPropertyName();
	void setPropertyName(String propertyName);

}
