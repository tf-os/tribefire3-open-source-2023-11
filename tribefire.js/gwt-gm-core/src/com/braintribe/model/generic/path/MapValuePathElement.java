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
import com.braintribe.model.generic.path.api.IMapValueModelPathElement;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsType;

@JsType(namespace = ModelPath.MODEL_PATH_NAMESPACE)
@SuppressWarnings("unusable-by-js")
public class MapValuePathElement extends PropertyRelatedModelPathElement implements IMapValueModelPathElement {
	private final Object key;
	private final GenericModelType keyType;
	private final MapKeyPathElement keyElement;

	// TODO validate that adding keyElement is ok with all invokers of this constructor
	@JsConstructor
	public MapValuePathElement(GenericEntity entity, Property property, GenericModelType keyType, Object key, GenericModelType type, Object value, MapKeyPathElement keyElement) {
		super(entity, property, type, value);
		this.keyType = keyType;
		this.key = key;
		this.keyElement = keyElement;
	}

	@Override
	public <T extends GenericModelType> T getKeyType() {
		return (T) keyType;
	}

	@Override
	public <T> T getKey() {
		return (T) key;
	}

	@Override
	public ModelPathElementType getPathElementType() {
		return ModelPathElementType.MapValue;
	}

	@Override
	public MapValuePathElement copy() {
		return new MapValuePathElement(getEntity(), getProperty(), getKeyType(), getKey(), getType(), getValue(), getKeyElement());
	}

	@Override
	public <T extends GenericModelType> T getMapValueType() {
		return getType();
	}

	@Override
	public <T> T getMapValue() {
		return getValue();
	}

	@Override
	public <K, V> Entry<K, V> getMapEntry() {
		return new ModelPathMapEntry<>((K) key, (V) getValue());
	}

	@Override
	public com.braintribe.model.generic.path.api.ModelPathElementType getElementType() {
		return com.braintribe.model.generic.path.api.ModelPathElementType.MapValue;
	}

	@Override
	public MapKeyPathElement getKeyElement() {
		return keyElement;
	}

}
