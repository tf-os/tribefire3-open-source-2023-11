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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.value.ValueDescriptor;

/**
 * Terminal {@link PropertyAccessInterceptor}, one that does not delegate to it's successor, but accesses the property of given entity (via
 * direct access, i.e. get-/setPropertyDirect).
 */
public class FieldAccessingPropertyAccessInterceptor extends PropertyAccessInterceptor {

	public static final FieldAccessingPropertyAccessInterceptor INSTANCE = new FieldAccessingPropertyAccessInterceptor();

	private FieldAccessingPropertyAccessInterceptor() {
	}

	@Override
	public Object getProperty(Property property, GenericEntity entity, boolean isVd) {
		return isVd ? property.getVdDirect(entity) : property.getDirectUnsafe(entity);
	}

	@Override
	public Object setProperty(Property property, GenericEntity entity, Object value, boolean isVd) {
		if (isVd) {
			return property.setVdDirect(entity, (ValueDescriptor) value);

		} else {
			return property.setDirect(entity, value);
		}
	}

}
