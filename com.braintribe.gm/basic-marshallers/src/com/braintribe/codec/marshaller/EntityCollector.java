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
package com.braintribe.codec.marshaller;

import java.util.Collection;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

public abstract class EntityCollector {
	private final EntityScan deferringAnchor = new EntityScan(null, null);
	private EntityScan lastNode = deferringAnchor;
	private boolean directPropertyAccess;
	private int entityDepth;
	
	public void setDirectPropertyAccess(boolean directPropertyAccess) {
		this.directPropertyAccess = directPropertyAccess;
	}
	
	public void collect(Object value) {
		collect(BaseType.INSTANCE, value);
		EntityScan deferringNode = deferringAnchor.next;
		
		while (deferringNode != null) {
			collect(deferringNode.entity, deferringNode.entityType);
			deferringNode = deferringNode.next;
		}
	}
	
	private void collect(GenericModelType type, Object value) {
		if (value == null)
			return;
		
		while(true) {
			switch (type.getTypeCode()) {
			case objectType:
				type = type.getActualType(value);
				break;
				
			case entityType:
				GenericEntity entity = (GenericEntity)value;
				EntityType<GenericEntity> entityType = entity.entityType();
				if (add(entity, entityType)) {
					collect(entity, entityType);
				}
				return;
			case enumType:
				add((Enum<?>)value, (EnumType)type);
				return;
				
			case listType: 
			case setType:
				GenericModelType elementType = ((CollectionType)type).getCollectionElementType();
				for (Object element: (Collection<?>)value) {
					collect(elementType, element);
				}
				return;
			case mapType:
				GenericModelType[] parameterization = ((CollectionType)type).getParameterization();
				GenericModelType keyType = parameterization[0];
				GenericModelType valueType = parameterization[1];
				boolean handleKey = keyType.areCustomInstancesReachable();
				boolean handleValue = valueType.areCustomInstancesReachable();
				
				if (handleKey && handleValue) {
					for (Map.Entry<?, ?> entry: ((Map<?, ?>)value).entrySet()) {
						collect(keyType, entry.getKey());
						collect(valueType, entry.getValue());
					}
				}
				else if (handleKey) {
					for (Object mapKey: ((Map<?, ?>)value).keySet()) {
						collect(keyType, mapKey);
					}
				}
				else {
					for (Object mapValue: ((Map<?, ?>)value).values()) {
						collect(valueType, mapValue);
					}
				}
				return;
				
			default:
				return;
			}
		}
	}

	private void collect(GenericEntity entity, EntityType<?> entityType) {
		if (entityDepth == 50) {
			lastNode = lastNode.next = new EntityScan(entity, entityType);
			return;
		}
		
		entityDepth++;
		if (directPropertyAccess) {
			for (Property property: entityType.getCustomTypeProperties()) {
				Object value = property.getDirectUnsafe(entity);
				
				if (value != null) {
					collect(property.getType(), value);
				}
			}
		}
		else {
			for (Property property: entityType.getCustomTypeProperties()) {
				Object value = property.get(entity);
				
				if (value != null) {
					collect(property.getType(), value);
				}
			}
		}
		entityDepth--;
	}
	
	protected abstract boolean add(GenericEntity entity, EntityType<?> type);
	
	protected abstract void add(Enum<?> constant, EnumType type);

	
	private static class EntityScan {
		public EntityScan next;
		public GenericEntity entity;
		public EntityType<?> entityType;
		public EntityScan(GenericEntity entity, EntityType<?> entityType) {
			super();
			this.entity = entity;
			this.entityType = entityType;
		}
	}
	
}
