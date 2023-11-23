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
package com.braintribe.model.generic.path;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.api.IPropertyModelPathElement;
import com.braintribe.model.generic.reflection.Property;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsType;

@JsType(namespace = ModelPath.MODEL_PATH_NAMESPACE)
@SuppressWarnings("unusable-by-js")
public class PropertyPathElement extends PropertyRelatedModelPathElement implements IPropertyModelPathElement {

	@JsConstructor
	public PropertyPathElement(GenericEntity entity, Property property, Object value) {
		super(entity, property, property.getType().getActualType(value), value);
	}
	
	@Override
	public ModelPathElementType getPathElementType() {
		return ModelPathElementType.Property;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
	
	@Override
	public PropertyPathElement copy() {
		return new PropertyPathElement(getEntity(), getProperty(), getValue());
	}

	@Override
	public com.braintribe.model.generic.path.api.ModelPathElementType getElementType() {
		return com.braintribe.model.generic.path.api.ModelPathElementType.Property;
	}
}
