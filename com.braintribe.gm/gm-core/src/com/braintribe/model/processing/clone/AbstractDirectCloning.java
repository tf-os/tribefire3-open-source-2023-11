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
package com.braintribe.model.processing.clone;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.collection.LinearCollectionBase;
import com.braintribe.model.generic.collection.ListBase;
import com.braintribe.model.generic.collection.MapBase;
import com.braintribe.model.generic.collection.SetBase;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EssentialCollectionTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SetType;

public abstract class AbstractDirectCloning implements CloningApi {
	private CloningVisitor cloningVisitor;
	private boolean shortcutScalars;

	public void setCloningVisitor(CloningVisitor cloningVisitor) {
		this.cloningVisitor = cloningVisitor;
	}
	
	@Override
	public <T> T cloneValue(Object value, GenericModelType type) {
		if (cloningVisitor != null) cloningVisitor.enterRootValue(type, value);
		
		try {
			return (T)doCloneValue(type, value);
		}
		finally {
			if (cloningVisitor != null) cloningVisitor.leaveRootValue(type, value);
		}
	}
	
	@Override
	public <T> T cloneValue(Object value) {
		return cloneValue(value, BaseType.INSTANCE);
	}

	@Override
	public <K, V> MapBase<K, V> cloneMap(MapBase<K, V> map) {
		return cloneMap(map, map.type());
	}

	@Override
	public <K, V> MapBase<K, V> cloneMap(Map<K, V> map) {
		return cloneMap(map, EssentialCollectionTypes.TYPE_MAP);
	}

	@Override
	public <K, V> MapBase<K, V> cloneMap(Map<K, V> map, MapType mapType) {
		return cloneValue(map, mapType);
	}

	@Override
	public <T> SetBase<T> cloneSet(SetBase<T> set) {
		return cloneSet(set, set.type());
	}


	@Override
	public <T> SetBase<T> cloneSet(Set<T> set) {
		return cloneSet(set, EssentialCollectionTypes.TYPE_SET);
	}

	@Override
	public <T> SetBase<T> cloneSet(Set<T> set, SetType setType) {
		return cloneValue(set, setType);
	}


	@Override
	public <T> ListBase<T> cloneList(ListBase<T> list) {
		return cloneList(list, list.type());
	}

	@Override
	public <T> ListBase<T> cloneList(List<T> list, ListType listType) {
		return cloneValue(list, listType);
	}

	@Override
	public <T> ListBase<T> cloneList(List<T> list) {
		return cloneList(list, EssentialCollectionTypes.TYPE_LIST);
	}

	@Override
	public <T> LinearCollectionBase<T> cloneCollection(LinearCollectionBase<T> collection) {
		return cloneValue(collection, collection.type());
	}

	@Override
	public <T> LinearCollectionBase<T> cloneCollection(Collection<T> collection, LinearCollectionType collectionType) {
		return cloneValue(collection, collectionType);
	}

	@Override
	public <T> LinearCollectionBase<T> cloneCollection(Collection<T> collection) {
		return cloneValue(collection, BaseType.INSTANCE);
	}

	@Override
	public <T extends GenericEntity> T cloneEntity(T entity) {
		return cloneValue(entity, entity.entityType());
	}
	
	protected GenericEntity doCloneEntity(GenericEntity entity) {
		CloneTarget cloneTarget = acquireCloneTarget(entity);
		
		GenericEntity clonedEntity = cloneTarget.getEntity();
		
		
		if (cloningVisitor != null)
			cloningVisitor.enterEntity(entity, clonedEntity);
		
		try {
			if (!cloneTarget.shouldCloneTransitively()) {
				return clonedEntity;
			}
			
			for (Property property: entity.entityType().getProperties()) {
				GenericModelType propertyType = property.getType();
	
				Object propertyValue = property.getDirectUnsafe(entity);
				
				if (cloningVisitor != null)
					cloningVisitor.enterPropertyValue(entity, clonedEntity, property, propertyType, propertyValue);
				
				try {
					Object clonedPropertyValue = doCloneValue(propertyType, propertyValue);
					transferProperty(clonedEntity, property, clonedPropertyValue);
				}
				finally {
					if (cloningVisitor != null)
						cloningVisitor.leavePropertyValue(entity, clonedEntity, property, propertyType, propertyValue);
				}
				
			}
			
			return clonedEntity;
		}
		finally {
			if (cloningVisitor != null)
				cloningVisitor.leaveEntity(entity, clonedEntity);
		}
	}
	
	protected void transferProperty(GenericEntity entity, Property property, Object value) {
		property.setDirectUnsafe(entity, value);	
	}
	
	protected Object doCloneValue(GenericModelType type, Object value) {
		
		if (value == null || type.isScalar()) 
			return doCloneScalar(type, value);

		switch (type.getTypeCode()) {
		case objectType: return doCloneValue(type.getActualType(value), value);
		case entityType: return doCloneEntity((GenericEntity)value);
		case listType: return doCloneList((List<?>)value, (ListType)type);
		case mapType: return doCloneMap((Map<?, ?>)value, (MapType)type);
		case setType: return doCloneSet((Set<?>)value, (SetType)type);
		default:
			throw new IllegalStateException("unexpected typecode: " + type.getTypeCode());
		}
	}
	
	protected Object doCloneScalar(@SuppressWarnings("unused") GenericModelType type, Object value) {
		return value;
	}

	protected <T> ListBase<T> doCloneList(List<T> list, ListType listType) {
		ListBase<T> clonedList = (ListBase<T>) listType.createPlain();
		
		GenericModelType elementType = listType.getCollectionElementType();
		
		if (shortcutScalars && elementType.isScalar()) {
			clonedList.addAll(list);
		}
		else {
			int i = 0;
			for (T element: list) {

				GenericModelType actualElementType = elementType.getActualType(element);

				if (cloningVisitor != null)
					cloningVisitor.enterListElement(listType, list, i, actualElementType, element);
				
				try {
					T clonedElement = (T)doCloneValue(actualElementType, element);
					clonedList.add(clonedElement);
				}
				finally {
					if (cloningVisitor != null)
						cloningVisitor.leaveListElement(listType, list, i, actualElementType, element);
				}
				i++;
			}
		}
		
		return clonedList;
	}
	
	
	protected <T> SetBase<T> doCloneSet(Set<T> set, SetType setType) {
		SetBase<T> clonedSet = (SetBase<T>) setType.createPlain();
		
		GenericModelType elementType = setType.getCollectionElementType();
		
		if (shortcutScalars && elementType.isScalar()) {
			clonedSet.addAll(set);
		}
		else {
			for (T element: set) {

				GenericModelType actualElementType = elementType.getActualType(element);

				if (cloningVisitor != null)
					cloningVisitor.enterSetElement(setType, set, actualElementType, element);
				
				try {
					T clonedElement = (T)doCloneValue(actualElementType, element);
					clonedSet.add(clonedElement);
				}
				finally {
					if (cloningVisitor != null)
						cloningVisitor.leaveSetElement(setType, set, actualElementType, element);
				}
			}
		}
		
		return clonedSet;
	}
	
	protected <K, V> MapBase<K, V> doCloneMap(Map<K, V> map, MapType mapType) {
		MapBase<K, V> clonedMap = (MapBase<K,V>)mapType.createPlain();
		
		if (shortcutScalars && mapType.hasSimpleOrEnumContent()) {
			clonedMap.putAll(map);
		}
		else {
			GenericModelType keyType = mapType.getKeyType();
			GenericModelType valueType = mapType.getValueType();
			
			for (Map.Entry<K, V> entry: map.entrySet()) {
				K key = entry.getKey();
				V value = entry.getValue();

				GenericModelType actualKeyType = keyType.getActualType(key);
				GenericModelType actualValueType = valueType.getActualType(value);
				
				
				if (cloningVisitor != null)
					cloningVisitor.enterMapKey(mapType, map, actualKeyType, key);
					
				final K clonedKey;
				
				try {
					clonedKey = (K)doCloneValue(actualKeyType, key);
				}
				finally {
					if (cloningVisitor != null)
						cloningVisitor.leaveMapKey(mapType, map, actualKeyType, key);
				}

				if (cloningVisitor != null)
					cloningVisitor.enterMapValue(mapType, map, actualKeyType, key, actualValueType, value);
					
				final V clonedValue; 
				try {
					clonedValue = (V)doCloneValue(actualValueType, value);
				}
				finally {
					if (cloningVisitor != null)
						cloningVisitor.leaveMapValue(mapType, map, actualKeyType, key, actualValueType, value);
				}
				
				clonedMap.put(clonedKey, clonedValue);
			}
		}
		return clonedMap;
	}
	
	protected abstract CloneTarget acquireCloneTarget(GenericEntity entity);

}
