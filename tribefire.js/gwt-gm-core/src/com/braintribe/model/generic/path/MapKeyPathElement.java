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

import java.util.Map.Entry;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.api.IMapKeyModelPathElement;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsType;

@JsType(namespace = ModelPath.MODEL_PATH_NAMESPACE)
@SuppressWarnings("unusable-by-js")
public class MapKeyPathElement extends PropertyRelatedModelPathElement implements IMapKeyModelPathElement {
	
	private final Object entryValue;
	private final GenericModelType entryValueType;
	
	@JsConstructor
	public MapKeyPathElement(GenericEntity entity, Property property, GenericModelType keyType, Object key, GenericModelType type, Object value ) {
		super(entity, property, keyType, key);
		this.entryValueType = type;
		this.entryValue = value;
	}

	@Override
	public ModelPathElementType getPathElementType() {
		return ModelPathElementType.MapKey;
	}

	@Override
	public MapKeyPathElement copy() {
		return new MapKeyPathElement(getEntity(), getProperty(), getType(), getValue(), entryValueType, entryValue);
	}

	@Override
	public com.braintribe.model.generic.path.api.ModelPathElementType getElementType() {
		return com.braintribe.model.generic.path.api.ModelPathElementType.MapKey;
	}

	@Override
	public <T extends GenericModelType> T getKeyType() {
		return getType();
	}

	@Override
	public <T> T getKey() {
		return getValue();
	}

	@Override
	public <T extends GenericModelType> T getMapValueType() {
		return (T) entryValueType;
	}

	@Override
	public <T> T getMapValue() {
		return (T) entryValue;
	}

	@Override
	public <K, V> Entry<K, V> getMapEntry() {
		return new ModelPathMapEntry<>((K) getValue(), (V) entryValue);
	}
}
