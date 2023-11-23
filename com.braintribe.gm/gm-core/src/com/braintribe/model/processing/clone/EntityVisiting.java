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
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;

public abstract class EntityVisiting {
	private final EnqueuedScan deferringAnchor = new EnqueuedScan(null, null);
	private EnqueuedScan lastNode = deferringAnchor;
	private boolean directPropertyAccess;
	protected boolean visitEnums;
	
	public void setDirectPropertyAccess(boolean directPropertyAccess) {
		this.directPropertyAccess = directPropertyAccess;
	}
	
	public void visit(Object value) {
		visit(BaseType.INSTANCE, value);
	}
	
	public void visit(GenericModelType type, Object value) {
		visit(type, value, 0);
		EnqueuedScan deferringNode = deferringAnchor.next;
		
		while (deferringNode != null) {
			visit(deferringNode.type, deferringNode.value, 0);
			deferringNode = deferringNode.next;
		}
	}
	
	protected boolean include(GenericModelType type) {
		return visitEnums? type.areCustomInstancesReachable(): type.areEntitiesReachable();
	}
	
	private void visit(GenericModelType type, Object value, int depth) {
		if (value == null)
			return;
		
		if (!include(type))
			return;
		
		// break the callstack principle and enqueue
		if (depth > 100) {
			lastNode = lastNode.next = new EnqueuedScan(value, type);
		}
			
		while(true) {
			TypeCode typeCode = type.getTypeCode();
			
			if (typeCode == null) {
				throw new IllegalStateException("Unexpected actual type of object at traversal depth " + depth + ". No GenericModelType assignable for: '" + value + "'");
			}
			
			switch (typeCode) {
			case objectType:
				type = type.getActualType(value);
				break;
				
			case entityType:
				GenericEntity entity = (GenericEntity)value;
				EntityType<GenericEntity> entityType = entity.entityType();
				if (add(entity, entityType)) {
					visit(entity, entityType, depth);
				}
				return;
			case enumType:
				if (visitEnums)
					add((Enum<?>)value, (EnumType)type);
				return;
				
			case listType: 
			case setType:
				GenericModelType elementType = ((LinearCollectionType)type).getCollectionElementType();
				for (Object element: (Collection<?>)value) {
					visit(elementType, element, depth + 1);
				}
				return;
			case mapType:
				MapType mapType = (MapType)type;
				GenericModelType keyType = mapType.getKeyType();
				GenericModelType valueType = mapType.getValueType();
				boolean handleKey = include(keyType);
				boolean handleValue = include(valueType);
				
				if (handleKey && handleValue) {
					for (Map.Entry<?, ?> entry: ((Map<?, ?>)value).entrySet()) {
						visit(keyType, entry.getKey(), depth + 1);
						visit(valueType, entry.getValue(), depth + 1);
					}
				}
				else if (handleKey) {
					for (Object mapKey: ((Map<?, ?>)value).keySet()) {
						visit(keyType, mapKey, depth + 1);
					}
				}
				else {
					for (Object mapValue: ((Map<?, ?>)value).values()) {
						visit(valueType, mapValue, depth + 1);
					}
				}
				return;
				
			default:
				return;
			}
		}
	}

	protected EnqueuedScan newEntityScan(GenericEntity entity, EntityType<?> entityType) {
		return new EnqueuedScan(entity, entityType);
	}
	
	@SuppressWarnings("unused") 
	protected boolean include(Property property, GenericEntity entity, EntityType<?> entityType) {
		return true;
	}
	
	private void visit(GenericEntity entity, EntityType<?> entityType, int depth) {
		if (directPropertyAccess) {
			for (Property property: entityType.getCustomTypeProperties()) {
				Object value = property.getDirectUnsafe(entity);
				
				if (value != null && include(property, entity, entityType)) {
					visit(property.getType(), value, depth + 1);
				}
			}
		}
		else {
			for (Property property: entityType.getCustomTypeProperties()) {
				Object value = property.get(entity);
				
				if (value != null && include(property, entity, entityType)) {
					visit(property.getType(), value, depth + 1);
				}
			}
		}
	}
	
	protected abstract boolean add(GenericEntity entity, EntityType<?> type);
	
	protected abstract void add(Enum<?> constant, EnumType type);

	
	protected static class EnqueuedScan {
		public EnqueuedScan next;
		public Object value;
		public GenericModelType type;
		public EnqueuedScan(Object value, GenericModelType type) {
			super();
			this.value = value;
			this.type = type;
		}
	}
	
}
