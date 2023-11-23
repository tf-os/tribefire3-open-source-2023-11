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
package com.braintribe.model.processing.traverse;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;

public class EntityCollector extends EntityVisiting {
	private final Set<GenericEntity> entities = new HashSet<>();
	private final Set<Enum<?>> enums = new HashSet<>();
	private final Set<EntityType<?>> entityTypes = new HashSet<>();
	private final Set<EnumType> enumTypes = new HashSet<>();
	private boolean collectEnums;
	private boolean collectEnumTypes;
	private boolean collectEntityTypes;
	
	public Set<GenericEntity> getEntities() {
		return entities;
	}
	
	public Set<EntityType<?>> getEntityTypes() {
		return entityTypes;
	}
	
	public Set<Enum<?>> getEnums() {
		return enums;
	}
	
	public Set<EnumType> getEnumTypes() {
		return enumTypes;
	}
	
	public void setCollectEntityTypes(boolean collectEntityTypes) {
		this.collectEntityTypes = collectEntityTypes;
	}
	
	
	public void setCollectEnums(boolean collectEnums) {
		this.collectEnums = collectEnums;
	}
	
	public void setCollectEnumTypes(boolean collectEnumTypes) {
		this.collectEnumTypes = collectEnumTypes;
	}
	
	@Override
	protected boolean add(GenericEntity entity, EntityType<?> type) {
		boolean result = entities.add(entity);
		if (result && collectEntityTypes) {
			entityTypes.add(type);
		}
		
		return result;
	}
	
	@Override
	protected void add(Enum<?> constant, EnumType type) {
		if (collectEnums) {
			if (enums.add(constant) && collectEnumTypes) {
				enumTypes.add(type);
			}
		}
		else
			enumTypes.add(type);
	}
	
	protected boolean add(EnumType enumType) {
		return enumTypes.add(enumType);
	}

	protected boolean add(EntityType<?> entityType) {
		return entityTypes.add(entityType);
	}
}
