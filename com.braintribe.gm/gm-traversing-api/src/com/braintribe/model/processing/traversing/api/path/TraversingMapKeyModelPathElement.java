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
package com.braintribe.model.processing.traversing.api.path;

import java.util.Map;

import com.braintribe.model.generic.path.api.IMapKeyModelPathElement;
import com.braintribe.model.generic.path.api.ModelPathElementType;
import com.braintribe.model.generic.reflection.GenericModelType;

public class TraversingMapKeyModelPathElement extends TraversingCollectionItemModelPathElement implements IMapKeyModelPathElement {

	private final Object mapValue;
	private final GenericModelType mapValueType;
	private final Map.Entry<?, ?> entry;

	public TraversingMapKeyModelPathElement(TraversingModelPathElement previous, Object value, GenericModelType type, Object mapValue,
			GenericModelType mapValueType, Map.Entry<?, ?> entry) {
		super(previous, value, type);
		this.mapValue = mapValue;
		this.mapValueType = mapValueType;
		this.entry = entry;
	}

	@Override
	public ModelPathElementType getElementType() {
		return ModelPathElementType.MapKey;
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
		return mapValueType.cast();
	}

	@Override
	public <T> T getMapValue() {
		return (T) mapValue;
	}

	@Override
	public <K, V> Map.Entry<K, V> getMapEntry() {
		return (Map.Entry<K, V>) entry;
	}
}
