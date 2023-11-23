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
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.generic.value.ValueDescriptor;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("unusable-by-js")
public class VdePropertyAccessInterceptor extends PropertyAccessInterceptor {

	private Object vdeContext;

	// Once we want this feature this will have a correct type
	public void setVdeContext(Object vdeContext) {
		this.vdeContext = vdeContext;
	}

	@Override
	public Object getProperty(Property property, GenericEntity entity, boolean isVd) {
		Object result = next.getProperty(property, entity, isVd); // TODO this is then a little weird

		if (VdHolder.isVdHolder(result)) {
			if (vdeContext != null)
				return evaluate(((VdHolder) result).vd);

			return property.getDefaultRawValue();
		}

		return result;
	}

	private Object evaluate(@SuppressWarnings("unused") ValueDescriptor vd) {
		throw new UnsupportedOperationException("Method 'VdePropertyAccessInterceptor.evaluate' is not supported yet!");
	}

}
