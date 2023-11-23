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

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface PropertyValueComparator extends MetaDataSelector {

	EntityType<PropertyValueComparator> T = EntityTypes.T(PropertyValueComparator.class);

	@Mandatory
	Operator getOperator();
	void setOperator(Operator operator);

	/**
	 * If null the current context property will be taken (if available).
	 */
	String getPropertyPath();
	void setPropertyPath(String propertyPath);

	Object getValue();
	void setValue(Object value);
	
	default PropertyValueComparator initPropertyValueComparator(String propertyPath, Operator operator, Object value) {
		setOperator(operator);
		setPropertyPath(propertyPath);
		setValue(value);
		
		return this;
	}
	
	static PropertyValueComparator create(String propertyPath, Operator operator, Object value) {
		return T.create().initPropertyValueComparator(propertyPath, operator, value);
	}

}
