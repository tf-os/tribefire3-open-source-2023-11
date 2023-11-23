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
package com.braintribe.model.processing.mp.builder.impl;

import java.util.List;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ListItemPathElement;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.mp.builder.api.MpBuilder;

public class MpBuilderImpl implements MpBuilder {

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	private ModelPathElement currentElement;
	private GenericEntity lastEntity;
	private Property lastProperty;
	private Object lastValue;
	private EntityType<?> lastEntityType;

	@Override
	public MpBuilder root(Object root) {
		currentElement = new RootPathElement(getType(root), root);
		updateLastValue(root);
		return this;
	}

	@Override
	public MpBuilder listItem(int listIndex) {
		List<?> list = (List<?>) lastValue;		
		Object listItem = list.get(listIndex);
		ListItemPathElement element = new ListItemPathElement(lastEntity, lastProperty, listIndex, getType(listItem), listItem);
		
		append(element, listItem);
		
		return this;
	}

	@Override
	public MpBuilder property(String propertyName) {
		lastProperty = lastEntityType.getProperty(propertyName);
		Object propertyValue = lastProperty.get(lastEntity);
		PropertyPathElement element = new PropertyPathElement(lastEntity, lastProperty, propertyValue);

		append(element, propertyValue);

		return this;
	}

	@Override
	public IModelPathElement build() {
		return currentElement;
	}

	//
	// Helper methods
	//

	private void append(ModelPathElement element, Object value) {
		currentElement.append(element);
		currentElement = element;

		updateLastValue(value);
	}

	private void updateLastValue(Object value) {
		if (value instanceof GenericEntity) {
			lastEntity = (GenericEntity) value;
			lastEntityType = lastEntity.entityType();
		}
		
		lastValue = value;
	}

	private static GenericModelType getType(Object root) {
		return typeReflection.getType(root);
	}
	
}
