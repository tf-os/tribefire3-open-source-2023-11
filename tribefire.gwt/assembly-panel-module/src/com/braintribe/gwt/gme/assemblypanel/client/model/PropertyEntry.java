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
package com.braintribe.gwt.gme.assemblypanel.client.model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

public class PropertyEntry implements AbsentModel {
	
	private EntityType<GenericEntity> entityType;
	private String propertyName;
	private GenericEntity entity;
	private boolean absent;
	private Property property;
	private GenericModelType propertyType;
	private int depth;
	private boolean mapAsList;
	private Integer maxSize;
	private boolean isBasedType;
	
	public PropertyEntry(GenericEntity entity, EntityType<GenericEntity> entityType, String propertyName, boolean absent, boolean mapAsList,
			Integer maxSize, Property property, GenericModelType propertyType, int depth, boolean basedType) {
		this.mapAsList = mapAsList;
		setEntity(entity);
		setEntityType(entityType);
		setPropertyName(propertyName);
		setAbsent(absent);
		setMaxSize(maxSize);
		setProperty(property);
		setPropertyType(propertyType);
		setDepth(depth);
		setBasedType(basedType);
	}
	
	public GenericEntity getEntity() {
		return entity;
	}
	
	public void setEntity(GenericEntity entity) {
		this.entity = entity;
	}
	
	public EntityType<GenericEntity> getEntityType() {
		return entityType;
	}
	
	public void setEntityType(EntityType<GenericEntity> entityType) {
		this.entityType = entityType;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	@Override
	public boolean isAbsent() {
		return absent;
	}
	
	@Override
	public void setAbsent(boolean absent) {
		this.absent = absent;
	}
	
	public Property getProperty() {
		return property;
	}
	
	public void setProperty(Property property) {
		this.property = property;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public <V> V getPropertyValue() throws GenericModelException {
		if (property != null)
			return property.get(getEntity());
		return getEntityType().getProperty(getPropertyName()).get(getEntity());
	}
	
	public GenericModelType getPropertyType() {
		return propertyType;
	}
	
	public void setPropertyType(GenericModelType propertyType) {
		this.propertyType = propertyType;
	}
	
	public boolean getMapAsList() {
		return mapAsList;
	}
	
	public boolean isBasedType() {
		return isBasedType;
	}
	
	public void setBasedType(boolean isBasedType) {
		this.isBasedType = isBasedType;
	}
	
	public Integer getMaxSize() {
		return maxSize;
	}
	
	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}

}
