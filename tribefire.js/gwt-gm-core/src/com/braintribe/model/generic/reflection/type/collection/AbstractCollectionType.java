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
package com.braintribe.model.generic.reflection.type.collection;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.reflection.AbstractGenericModelType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;

public abstract class AbstractCollectionType extends AbstractGenericModelType implements CollectionType {

	private Boolean customInstancesReachable;
	private Boolean entitiesReachable;

	public AbstractCollectionType(Class<?> javaType) {
		super(javaType);
	}

	public static final CollectionKind collectionKindFor(Class<?> javaType) {
		if (javaType == Map.class) {
			return CollectionKind.map;
		} else if (javaType == List.class) {
			return CollectionKind.list;
		} else if (javaType == Set.class) {
			return CollectionKind.set;
		}
		throw new GenericModelException("Unsupported java type for collection: " + javaType);
	}

	/** There should be no reason to call this. Use {@link #createPlain()} instead. */
	@Override
	public final Object create() {
		throw new UnsupportedOperationException("Method 'CollectionType.create' is not supported");
	}

	@Override
	public final boolean isAssignableFrom(GenericModelType type) {
		return this == type;
	}

	@Override
	public final boolean isCollection() {
		return true;
	}

	protected static final boolean isSimpleOrEnumContent(GenericModelType type) {
		switch (type.getTypeCode()) {
			case objectType:
			case entityType:
				return false;
			case listType:
			case mapType:
			case setType:
				throw new IllegalArgumentException("Collection parameter type cannot be a collection. Parameter type: " + type);
			default:
				return true;
		}
	}

	@Override
	public final boolean areCustomInstancesReachable() {
		if (customInstancesReachable == null) {
			customInstancesReachable = _areCustomInstancesReachable();
		}
		return customInstancesReachable;
	}

	private boolean _areCustomInstancesReachable() {
		for (GenericModelType type: getParameterization()) {
			if (type.areCustomInstancesReachable())
				return true;
		}
		return false;
	}

	@Override
	public final boolean areEntitiesReachable() {
		if (entitiesReachable == null) {
			entitiesReachable = _areEntitiesReachable();
		}
		return entitiesReachable;
	}

	private final boolean _areEntitiesReachable() {
		for (GenericModelType type: getParameterization()) {
			if (type.areEntitiesReachable())
				return true;
		}
		return false;
	}

	@Override
	public boolean isInstance(Object value) {
		return isInstanceOfThis(value);
	}
	
	protected abstract boolean isInstanceOfThis(Object value);
	
}
