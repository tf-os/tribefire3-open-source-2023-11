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
package com.braintribe.model.query;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.Property;

public interface PropertyOperand extends Operand {

	EntityType<PropertyOperand> T = EntityTypes.T(PropertyOperand.class);

	/**
	 * The property path for given source. Right, the "path", because it doesn't have to be only one property, but a
	 * path in the form: {@code prop1.prop2. ... .propN}
	 */
	String getPropertyName();
	void setPropertyName(String propertyName);

	void setSource(Source source);
	Source getSource();

	static PropertyOperand create(String propertyName) {
		return create(null, propertyName);
	}

	static PropertyOperand create(Property property) {
		return create(property.getName());
	}
	
	static PropertyOperand create(Source source, String propertyName) {
		PropertyOperand operand = PropertyOperand.T.create();
		operand.setPropertyName(propertyName);
		operand.setSource(source);
		return operand;
	}
	
	static PropertyOperand create(Source source, Property property) {
		return create(source, property.getName());
	}
}
