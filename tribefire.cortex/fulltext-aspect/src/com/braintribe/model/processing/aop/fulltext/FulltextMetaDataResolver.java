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
package com.braintribe.model.processing.aop.fulltext;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.CascadingDelete;
import com.braintribe.model.meta.data.display.DisplayInfo;
import com.braintribe.model.meta.data.fulltext.AnalyzedProperty;
import com.braintribe.model.meta.data.fulltext.FulltextEntity;
import com.braintribe.model.meta.data.fulltext.FulltextProperty;
import com.braintribe.model.meta.data.fulltext.StorageHint;
import com.braintribe.model.meta.data.fulltext.StorageOption;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.utils.i18n.I18nTools;



/**
 * @author gunther.schenk
 *
 */
public class FulltextMetaDataResolver {
	/**
	 * The logger.
	 */
	protected static final Logger logger = Logger.getLogger(FulltextMetaDataResolver.class);
	
	private FulltextEntity defaultFulltextEntity = createDefaultFulltextEntity();
	private FulltextProperty defaultFullProperty = createDefaultFulltextProperty();
	private StorageHint defaultStorageHint = createDefaultStorageHint();
	private AnalyzedProperty defaultAnalyzedProperty = createDefaultAnalyzedProperty();
	private boolean defaultCascadingDelete = false;
	private GenericModelTypeReflection typeReflection;
	private final CmdResolver resolver;
	
	// **************************************************************************
	// Constructor
	// **************************************************************************

	public FulltextMetaDataResolver(CmdResolver resolver) {
		this.resolver = resolver;
	}
	// **************************************************************************
	// Getter/Setter
	// **************************************************************************

	/**
	 * @param defaultAnalyzedProperty the defaultAnalyzedProperty to set
	 */
	public void setDefaultAnalyzedProperty(AnalyzedProperty defaultAnalyzedProperty) {
		this.defaultAnalyzedProperty = defaultAnalyzedProperty;
	}
	
	/**
	 * @param defaultFullProperty the defaultFullProperty to set
	 */
	public void setDefaultFullProperty(FulltextProperty defaultFullProperty) {
		this.defaultFullProperty = defaultFullProperty;
	}
	
	/**
	 * @param defaultFulltextEntity the defaultFulltextEntity to set
	 */
	public void setDefaultFulltextEntity(FulltextEntity defaultFulltextEntity) {
		this.defaultFulltextEntity = defaultFulltextEntity;
	}
	
	/**
	 * @param defaultStorageHint the defaultStorageHint to set
	 */
	public void setDefaultStorageHint(StorageHint defaultStorageHint) {
		this.defaultStorageHint = defaultStorageHint;
	}
	
	/**
	 * @param defaultCascadingDelete the defaultCascadingDelete to set
	 */
	public void setDefaultCascadingDelete(boolean defaultCascadingDelete) {
		this.defaultCascadingDelete = defaultCascadingDelete;
	}
	
	public void setTypeReflection(GenericModelTypeReflection typeReflection) {
		this.typeReflection = typeReflection;
	}
	
	public GenericModelTypeReflection getTypeReflection() {
		if (typeReflection == null) {
			typeReflection = GMF.getTypeReflection();
		}
		return typeReflection;
	}
	
	// **************************************************************************
	// Interface methods
	// **************************************************************************
	
	
	public GenericModelType getIdType(String typeSignature) {
		return this.resolver.getIdType(typeSignature);
	}

	public FulltextProperty getFulltextPropertyMetaData (EntityType<GenericEntity> entityType, String propertyName) {
		return getMetaData(entityType, propertyName, FulltextProperty.T, defaultFullProperty);
	}
	
	public FulltextEntity getFulltextEntityMetaData (EntityType<GenericEntity> entityType) {
		return getMetaData(entityType,  FulltextEntity.T, defaultFulltextEntity);
	}
	
	public StorageHint getStorageHintMetaData (EntityType<GenericEntity> entityType, String propertyName) {
		return getMetaData(entityType, propertyName, StorageHint.T, defaultStorageHint);
	}
	
	public AnalyzedProperty getAnalyzedPropertyMetaData (EntityType<GenericEntity> entityType, String propertyName) {
		return getMetaData(entityType, propertyName, AnalyzedProperty.T, defaultAnalyzedProperty);
	}
	
	public boolean isPropertyAnalyzed (EntityType<GenericEntity> entityType, String propertyName) {
		return getAnalyzedPropertyMetaData(entityType,propertyName).getAnalyzed();
	}
	
	public boolean isPropertyIndexed (EntityType<GenericEntity> entityType, String propertyName) {
		return getFulltextPropertyMetaData(entityType, propertyName).getIndexed();
	}

	public boolean isPropertyStored (EntityType<GenericEntity> entityType, String propertyName) {
		return getFulltextPropertyMetaData(entityType, propertyName).getStored();
	}

	public boolean isEntityIndexed (EntityType<GenericEntity> entityType) {
		return getFulltextEntityMetaData(entityType).getIndexed();
	}

	public boolean isEntityStored (EntityType<GenericEntity> entityType) {
		return getFulltextEntityMetaData(entityType).getStored();
	}
	
	public boolean isCascadingDeleteProperty (EntityType<GenericEntity> entityType, String propertyName) {
		CascadingDelete cascadingDelete = getMetaData(entityType, propertyName, CascadingDelete.T, null);
		return cascadingDelete != null ? cascadingDelete.isTrue() : defaultCascadingDelete;
	}
	
	public <T extends MetaData> T getMetaData (EntityType<GenericEntity> entityType, EntityType<T> metaDataClass, T defaultMetaData) {
		return getMetaData(entityType, null, metaDataClass, defaultMetaData);
	}
	
	public <T extends MetaData> T getMetaData (EntityType<GenericEntity> entityType, String propertyName, EntityType<T> metaDataClass, T defaultMetaData) {
		T metaData = null;
		if (propertyName == null) {
			metaData = getEntityMetaData(entityType, metaDataClass);
		} else {
			metaData = getPropertyMetaData(entityType, propertyName, metaDataClass);
		}
		
		if (metaData == null) {
			metaData = defaultMetaData;
		}
		return metaData;
	}

	public List<Property> getFulltextProperties (EntityType<GenericEntity> entityType) {
		List<Property> properties = new ArrayList<Property>();
		for (Property property : entityType.getProperties()) {
			if (isPropertyIndexed(entityType, property.getName())) {
				properties.add(property);
			}
		}
		return properties;
	}

	public String getDefaultPropertyDisplayName(EntityType<GenericEntity> entityType, String propertyName)  {
		DisplayInfo info = getMetaData(entityType, propertyName, DisplayInfo.T, null);
		String result = propertyName;
		if (info != null && info.getName() != null) {
			result = I18nTools.getDefault(info.getName(), "");
		}
		return result;
	}

	public String getDefaultPropertyDisplayDescription(EntityType<GenericEntity> entityType, String propertyName)  {
		DisplayInfo info = getMetaData(entityType, propertyName, DisplayInfo.T, null);
		String result = propertyName;
		if (info != null && info.getDescription() != null) {
			result = I18nTools.getDefault(info.getDescription(), "");
		}
		return result;
	}

	
	// **************************************************************************
	// Helper methods
	// **************************************************************************
	
	private <T extends MetaData> T getEntityMetaData(EntityType<GenericEntity> entityType, EntityType<T> comparison) {
		return resolver.getMetaData().entityType(entityType).meta(comparison).exclusive();
	}
	
	private <T extends MetaData> T getPropertyMetaData(EntityType<GenericEntity> entityType, String propertyName, EntityType<T> comparison) {
		return resolver.getMetaData().entityType(entityType).property(propertyName).meta(comparison).exclusive();
	}

	public static FulltextEntity createDefaultFulltextEntity () {
		FulltextEntity metaData = FulltextEntity.T.create();
		metaData.setIndexed(false);
		metaData.setStored(false);
		return metaData;
	}

	public static FulltextProperty createDefaultFulltextProperty () {
		FulltextProperty metaData= FulltextProperty.T.create();
		metaData.setIndexed(false);
		metaData.setStored(true);
		return metaData;
	}

	public static StorageHint createDefaultStorageHint() {
		StorageHint metaData = StorageHint.T.create();
		metaData.setOption(StorageOption.reference);
		return metaData;
	}

	public static AnalyzedProperty createDefaultAnalyzedProperty() {
		AnalyzedProperty metaData = AnalyzedProperty.T.create();
		metaData.setAnalyzed(false);
		return metaData;
	}

	public static String getBuildVersion() {

		return "$Build_Version$ $Id: FulltextMetaDataResolver.java 98190 2017-04-21 15:07:40Z peter.gazdik $";
	}
}
