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
import com.braintribe.model.generic.path.api.ISetItemModelPathElement;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

@SuppressWarnings("unusable-by-js")
public class SetItemPathElement extends PropertyRelatedModelPathElement implements ISetItemModelPathElement {

	public SetItemPathElement(GenericEntity entity, Property property, GenericModelType type, Object value) {
		super(entity, property, type, value);
	}

	@Override
	public ModelPathElementType getPathElementType() {
		return ModelPathElementType.SetItem;
	}

	@Override
	public SetItemPathElement copy() {
		return new SetItemPathElement(getEntity(), getProperty(), getType(), getValue());
	}

	@Override
	public com.braintribe.model.generic.path.api.ModelPathElementType getElementType() {
		return com.braintribe.model.generic.path.api.ModelPathElementType.SetItem;
	}
}
