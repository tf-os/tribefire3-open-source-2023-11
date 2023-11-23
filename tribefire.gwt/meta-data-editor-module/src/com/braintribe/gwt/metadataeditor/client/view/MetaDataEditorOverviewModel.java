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
package com.braintribe.gwt.metadataeditor.client.view;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.meta.override.GmEnumConstantOverride;
import com.braintribe.model.meta.override.GmEnumTypeOverride;
import com.braintribe.model.meta.override.GmPropertyOverride;

public class MetaDataEditorOverviewModel {

	private final String name;
	//private final PropertyMetaData metadata;
	//private final Property property;
	private final GmEntityType gmEntity;
	private final GmProperty gmProperty;
	private final GmEnumType gmEnum;
	private GmEnumConstant gmEnumConstant;
	private final GenericEntity gmOverride;
	private Boolean isVisible = true;
	//private Set<MetaData> listMetaData = new HashSet<MetaData>();
	private Set<MetaData> listMetaData = new HashSet<>();
	private int gmDependencyLevel = 0;
	private GmMetaModel gmDependencyModel = null;
	private GmEntityType gmDependencyEntityType = null;
	private GmEnumType gmDependencyEnumType = null;
	//private Map<String, MetaData> mapMetaData = new HashMap<String, MetaData>();
	//private Object propertyValue;
	private Set<EntityType<? extends MetaData>> listEntityTypeMetaData = new HashSet<>();
	
	/*
	public MetaDataEditorOverviewModel(int id, PropertyMetaData metadata, Property property) {
		this.name = metadata.getClass().getName() + "#" + id;
		this.description = (metadata.getDescription() != null) ? I18nTools.getLocalized(metadata.getDescription()) : null;
		this.metadata = metadata;
		this.property = property;
		this.propertyValue = property != null ? property.getProperty(metadata)  : null;
		this.dataName = property.getPropertyName();
		this.entity = null;
	}
	*/
	public MetaDataEditorOverviewModel(int id, GmEntityType entityType) {
		this.name = entityType.getClass().getName() + "#" + id;
		this.gmProperty = null;
		this.gmEnum = null;
		this.gmEntity = entityType;
		this.gmOverride = null;
		this.gmEnumConstant = null;
	}	
	public MetaDataEditorOverviewModel(int id, GmEnumType enumType) {
		this.name = enumType.getClass().getName() + "#" + id;
		this.gmProperty = null;	
		this.gmEntity = null;
		this.gmEnum = enumType;
		this.gmOverride = null;
		this.gmEnumConstant = null;
	}	
	public MetaDataEditorOverviewModel(int id, GmProperty property) {
		this.name = property.getClass().getName() + "#" + id;
		this.gmProperty = property;	
		this.gmEntity = null;
		this.gmEnum = null;
		this.gmOverride = null;
		this.gmEnumConstant = null;
	}	
	public MetaDataEditorOverviewModel(int id, GmEnumConstant enumConstant) {
		this.name = enumConstant.getClass().getName() + "#" + id;
		this.gmProperty = null;	
		this.gmEntity = null;
		this.gmEnum = null;
		this.gmOverride = null;
		this.gmEnumConstant = enumConstant;
	}	

	public MetaDataEditorOverviewModel(int id, GmEntityTypeOverride entityTypeOverride) {
		this.name = entityTypeOverride.getClass().getName() + "#" + id;
		this.gmProperty = null;
		this.gmEnum = null;
		this.gmEntity = entityTypeOverride.getEntityType();			
		this.gmOverride = entityTypeOverride;
		this.gmEnumConstant = null;
	}	
	public MetaDataEditorOverviewModel(int id, GmEnumTypeOverride enumTypeOverride) {
		this.name = enumTypeOverride.getClass().getName() + "#" + id;
		this.gmProperty = null;	
		this.gmEntity = null;
		this.gmEnum = enumTypeOverride.getEnumType();
		this.gmOverride = enumTypeOverride;
		this.gmEnumConstant = null;
	}	
	public MetaDataEditorOverviewModel(int id, GmPropertyOverride propertyOverride) {
		this.name = propertyOverride.getClass().getName() + "#" + id;
		this.gmProperty = propertyOverride.getProperty();	
		this.gmEntity = null;
		this.gmEnum = null;
		this.gmOverride = propertyOverride;
		this.gmEnumConstant = null;
	}	
	public MetaDataEditorOverviewModel(int id, GmEnumConstantOverride enumConstantOverride) {
		this.name = enumConstantOverride.getClass().getName() + "#" + id;
		this.gmProperty = null;	
		this.gmEntity = null;
		this.gmEnum = null;
		this.gmOverride = enumConstantOverride;
		this.gmEnumConstant = enumConstantOverride.getEnumConstant();
	}	
		
	public MetaDataEditorOverviewModel getModel() {
		return this;
	}
	public MetaDataEditorOverviewModel getDeclaredModel() {
		return this;
	}
	public MetaDataEditorOverviewModel getDeclaredOwner() {
		return this;
	}

	public String getName() {
		return this.name;
	}

	//public PropertyMetaData getValue() {
	public GenericEntity getValue() {
		if (this.gmOverride != null) {
			return this.gmOverride;
		} else if (this.gmEnum != null) {
			return this.gmEnum;
		} else	if (this.gmProperty != null) {
			return this.gmProperty;
		} else if (this.gmEnumConstant != null) {
			return this.gmEnumConstant;
		} else {
			return this.gmEntity;		
		}
	}

	/*
	public Property getProperty() {
		return property;
	}
	*/

	public GenericModelType getType() {
		return getValue() != null ? getValue().entityType() : null;
	}

	public void setIsVisible(Boolean isVisible) {
		this.isVisible = isVisible;
	}
	
	public Boolean getIsVisible() {
		return this.isVisible;
	}	
				
	public void addEntityTypeMetaData(EntityType<? extends MetaData> entityTypeMetaData) {
		this.listEntityTypeMetaData.add(entityTypeMetaData);
	}
	public void removeEntityTypeMetaData(EntityType<? extends MetaData> entityTypeMetaData) {
		this.listEntityTypeMetaData.remove(entityTypeMetaData);
	}
	public Set<EntityType<? extends MetaData>> getEntityTypeMetaData() {
		return this.listEntityTypeMetaData;
	}	
	public void clearEntityTypeMetaData() {
		this.listEntityTypeMetaData.clear();
	}
	
	public void addMetaData(MetaData metaData) {
		this.listMetaData.add(metaData);
		//this.mapMetaData.put(key, metaData);
	}
	public void removeMetaData(MetaData metaData) {
		this.listMetaData.remove(metaData);
		//this.mapMetaData.remove(key);
	}
	public Set<MetaData> getMetaData() {
		return this.listMetaData;
	}
	public void clearMetaData() {
		this.listMetaData.clear();
	}
	
	public void setGmDependencyLevel(int level) {
		this.gmDependencyLevel = level;
	}
	public int getGmDependencyLevel() {
		return this.gmDependencyLevel;
	}
	
	public void setGmDependencyModel(GmMetaModel model) {
		this.gmDependencyModel = model;
	}
	public GmMetaModel getGmDependencyModel() {
		return this.gmDependencyModel;
	}
	public void setGmDependencyEntityType(GmEntityType entityType) {
		this.gmDependencyEntityType = entityType;
	}
	public GmEntityType getGmDependencyEntityType() {
		return this.gmDependencyEntityType;
	}
		
	public void setGmDependencyEnumType(GmEnumType enumType) {
		this.gmDependencyEnumType = enumType;
	}
	public GmEnumType getGmDependencyEnumType() {
		return this.gmDependencyEnumType;
	}

	public boolean refersTo(Object object) {
		if (this.gmOverride != null) {
			if (this.gmOverride == object)
				return true;
		}
		if (this.gmEntity != null) {
			if (this.gmEntity == object) {
				return true;
			}
		}
		if (this.gmEnum != null) {
			if (this.gmEnum == object) {
				return true;
			}
		}
		if (this.gmProperty != null) {
			if (this.gmProperty == object) {
				return true;
			}
		}
		if (this.gmEnumConstant != null) {
			if (this.gmEnumConstant == object) {
				return true;
			}
		}
						
		return false;
	}
		
}
