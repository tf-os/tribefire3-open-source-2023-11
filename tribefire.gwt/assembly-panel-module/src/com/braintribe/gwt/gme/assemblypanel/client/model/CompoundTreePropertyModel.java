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

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.prompt.EntityCompoundViewing;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;

/**
 * Model used for the compound property entries in the tree.
 * 
 * @author michel.docouto
 *
 */
public class CompoundTreePropertyModel extends TreePropertyModel {
	
	private String compoundPropertyName;
	private List<String> propertyPathSubList;
	private GenericEntity currentParentEntity;
	private EntityType<?> currentParentEntityType;
	private String currentPropertyName;
	private Object currentValue;
	private Property currentProperty;
	private PropertyMdResolver propertyMdResolver;
	private final ModelMdResolver modelMdResolver;
	private final String useCase;

	public CompoundTreePropertyModel(EntityCompoundViewing entityCompoundViewing, Property property, GenericEntity parentEntity, Double priority,
			boolean absent, VirtualEnum virtualEnum, ModelMdResolver modelMdResolver, String useCase) {
		super(property, parentEntity, priority, false, false, absent, null, virtualEnum);
		
		List<GmProperty> properties = entityCompoundViewing.getPropertyPath().getProperties();
		propertyPathSubList = properties.subList(1, properties.size()).stream().map(p -> p.getName()).collect(Collectors.toList());
		
		this.modelMdResolver = modelMdResolver;
		this.useCase = useCase;
		this.propertyDisplay = GMEMetadataUtil.getPropertyDisplay(getPropertyName(), getPropertyMetaDataContextBuilder());
	}

	@Override
	public String getNormalizedPropertyName() {
		if (compoundPropertyName != null)
			return compoundPropertyName;
		
		compoundPropertyName = super.getPropertyName();
		propertyPathSubList.forEach(p -> compoundPropertyName += "." + p);
		
		return compoundPropertyName;
	}
	
	@Override
	public GenericEntity getParentEntity() {
		if (currentParentEntity != null)
			return currentParentEntity;
					
		GenericEntity currentEntity = property.get(parentEntity);
		for (int i = 0; i < propertyPathSubList.size() - 1; i++) {
			String subPropertyName = propertyPathSubList.get(i);
			Property property = currentEntity.entityType().getProperty(subPropertyName);
			currentEntity = property.get(currentEntity);
		}
		
		currentParentEntity = currentEntity;
		
		return currentParentEntity;
	}
	
	@Override
	public boolean isAbsent() {
		return false;
	}
	
	private EntityType<?> getParentEntityType() {
		if (currentParentEntityType != null)
			return currentParentEntityType;
		
		EntityType<?> currentEntityType = (EntityType<?>) property.getType();
		for (int i = 0; i < propertyPathSubList.size() - 1; i++) {
			String subPropertyName = propertyPathSubList.get(i);
			Property property = currentEntityType.getProperty(subPropertyName);
			currentEntityType = (EntityType<?>) property.getType();
		}
		
		this.currentParentEntityType = currentEntityType;
		return currentParentEntityType;
	}
	
	@Override
	public String getPropertyName() {
		if (currentPropertyName == null)
			currentPropertyName = propertyPathSubList.get(propertyPathSubList.size() - 1);
		
		return currentPropertyName;
	}
	
	@Override
	public Object getValue() {
		if (currentValue != null)
			return currentValue;
		
		GenericEntity parentEntity = getParentEntity();
		if (parentEntity != null) {
			currentValue = parentEntity.entityType().getProperty(getPropertyName()).get(parentEntity);
			return currentValue;
		}
		
		return null;
	}
	
	@Override
	public boolean isNullable() {
		return getCurrentProperty().isNullable();
	}
	
	@Override
	public boolean isReadOnly() {
		return !GMEMetadataUtil.isPropertyEditable(getPropertyMetaDataContextBuilder(), getParentEntity());
	}
	
	@Override
	public GenericModelType getElementType() {
		return getCurrentProperty().getType().getActualType(getValue());
	}
	
	@Override
	public boolean isPassword() {
		return GMEMetadataUtil.isPropertyPassword(getPropertyMetaDataContextBuilder());
	}
	
	@Override
	public void setValue(Object value) {
		this.currentValue = value;
	}
	
	@Override
	public Property getNormalizedProperty() {
		return getCurrentProperty();
	}
	
	private Property getCurrentProperty() {
		if (currentProperty != null)
			return currentProperty;
		
		GenericEntity parentEntity = getParentEntity();
		EntityType<?> entityType;
		if (parentEntity != null)
			entityType = parentEntity.entityType();
		else
			entityType = getParentEntityType();
		
		currentProperty = entityType.getProperty(getPropertyName());
		return currentProperty;
	}
	
	private PropertyMdResolver getPropertyMetaDataContextBuilder() {
		if (propertyMdResolver != null)
			return propertyMdResolver;
		
		GenericEntity parentEntity = getParentEntity();
		if (parentEntity != null)
			propertyMdResolver = getMetaData(parentEntity).entity(parentEntity).property(getPropertyName()).useCase(useCase);
		else
			propertyMdResolver = modelMdResolver.entityType(getParentEntityType()).property(getPropertyName()).useCase(useCase);
		
		return propertyMdResolver;
	}

}
