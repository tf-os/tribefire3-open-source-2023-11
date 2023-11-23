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
package com.braintribe.model.processing.manipulator.expert.basic;

import java.util.List;
import java.util.Set;

import com.braintribe.common.lcd.UnknownEnumException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.CollectionManipulation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.manipulator.api.CollectionManipulator;
import com.braintribe.model.processing.manipulator.api.Manipulator;
import com.braintribe.model.processing.manipulator.expert.basic.collection.ListManipulator;
import com.braintribe.model.processing.manipulator.expert.basic.collection.MapManipulator;
import com.braintribe.model.processing.manipulator.expert.basic.collection.SetManipulator;

public abstract class AbstractCollectionManipulator<T extends CollectionManipulation> implements Manipulator<T> {
	
	protected <C, I> CollectionManipulator<C, I> getCollectionManipulator(Object value, CollectionType collectionType) {
		CollectionKind collectionKind = collectionType != null ? collectionType.getCollectionKind() : getCollectionKind(value);
		
		switch (collectionKind) {
			case list:
				return (CollectionManipulator<C, I>) ListManipulator.INSTANCE;
			case map:
				return (CollectionManipulator<C, I>) MapManipulator.INSTANCE;
			case set:
				return (CollectionManipulator<C, I>) SetManipulator.INSTANCE;
			default:
				throw new UnknownEnumException(collectionKind);
		}
	}

	private CollectionKind getCollectionKind(Object value) {
		if (value instanceof List) {
			return CollectionKind.list;

		} else if (value instanceof Set) {
			return CollectionKind.set;

		} else {
			return CollectionKind.map;
		}
	}

	protected GenericModelType getKeyType(Object value, CollectionType collectionType) {
		if (collectionType == null) {
			return value instanceof List ? GenericModelTypeReflection.TYPE_INTEGER : BaseType.INSTANCE;
		}
		
		switch (collectionType.getCollectionKind()) {
			case list:
				return GenericModelTypeReflection.TYPE_INTEGER;
			case set:
				return collectionType.getCollectionElementType();
			case map:
				return collectionType.getParameterization()[0];
			default:
				throw new UnknownEnumException(collectionType.getCollectionKind());
		}
	}

	protected GenericModelType getValueType(CollectionType collectionType) {
		return collectionType != null ? collectionType.getCollectionElementType() : BaseType.INSTANCE;
	}

	protected Object acquireCollectionPropertyValue(GenericEntity entity, EntityType<GenericEntity> entityType, String propertyName,
			GenericModelType propertyType) {

		Property property = entityType.getProperty(propertyName);
		Object collection = property.get(entity);

		if (collection == null) {
			if (!propertyType.isCollection())
				throw new IllegalStateException("Cannot apply collection manipulation on property [" + propertyName + "] of ["
						+ entityType.getTypeSignature() + "] of type [" + propertyType.getTypeSignature()
						+ "]. Current value is null and it's not possible to deduce the right collection type. Entity: " + entity);

			property.set(entity, ((CollectionType) propertyType).createPlain());
			collection = property.get(entity);
		}

		return collection;
	}

}
