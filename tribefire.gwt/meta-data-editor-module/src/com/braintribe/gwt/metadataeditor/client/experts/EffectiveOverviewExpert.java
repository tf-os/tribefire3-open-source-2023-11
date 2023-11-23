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
package com.braintribe.gwt.metadataeditor.client.experts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.meta.data.EnumConstantMetaData;
import com.braintribe.model.meta.data.EnumTypeMetaData;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.ModelMetaData;
import com.braintribe.model.meta.data.Predicate;
import com.braintribe.model.meta.data.PredicateErasure;
import com.braintribe.model.meta.data.PropertyMetaData;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.meta.override.GmEnumConstantOverride;
import com.braintribe.model.meta.override.GmEnumTypeOverride;
import com.braintribe.model.meta.override.GmPropertyOverride;

public class EffectiveOverviewExpert extends AbstractBaseEditorExpert {

	private final IsAssignableTo entityTypeCondition = IsAssignableTo.T.create();

	{
		this.entityTypeCondition.setTypeSignature(EntityTypeMetaData.T.getTypeSignature());
	}

	@SuppressWarnings({ "deprecation" })
	private Set<EntityType<?>> getSubTypes(Class<? extends GenericEntity> entityClass) {
		Set<EntityType<?>> availableEntityTypes = new HashSet<EntityType<?>>();		
		
		
		EntityType<?> entityType = GMF.getTypeReflection().getEntityType(entityClass);
		//Set<EntityType<?>> types2 = entityType.getSubTypes();
		Set<EntityType<?>> types2 = entityType.getInstantiableSubTypes();  //already recursive subtypes
					
		for ( EntityType<?> entity : types2) {
			if (entity != null) {
				if (!entity.isAbstract()) {
					availableEntityTypes.add(entity);		
				}
				//need call RECURSIVE in all subTypes
				/*
				if (entity instanceof GenericEntity) {
					availableEntityTypes.addAll(getSubTypes((Class<? extends GenericEntity>) entity.getClass()));
				}
				*/
			}
		}		
		
			
		return availableEntityTypes;	
	}
			
	@Override
	public void provide(GenericEntity value, CallbackMetaData callback) {
		try {
			List<MetaData> mdList = new ArrayList<MetaData>();
			List<EntityType<?>> mdListEntityType = getEntityTypeList(value);									
			for (EntityType<?> entityType : mdListEntityType) {	
				MetaData metaData = (MetaData) createEntity(entityType);	
				if (metaData != null) {
					mdList.add(metaData);
				}
			}
			
			callback.onSuccess(mdList);				
		} catch (Exception e) {
			callback.onFailure(e);
		}				
	}

	@SuppressWarnings("deprecation")
	private static Set<EntityType<?>> getPredicateMetaDataList(Boolean usePredicateErasure) {
		Set<EntityType<?>> setEntityType;
		if (usePredicateErasure) {
			setEntityType = Predicate.T.getInstantiableSubTypes();
		} else {		
			setEntityType = new HashSet<EntityType<?>>();
			for (EntityType<?> entityType : Predicate.T.getInstantiableSubTypes()) {
				if (!(PredicateErasure.T.isAssignableFrom(entityType)))
					setEntityType.add(entityType);
			}
		}
		return setEntityType;
	}

	protected <T extends GenericEntity> T createEntity(EntityType<T> entityType) {
		T entity = entityType.create();
		return entity;
	}    
	
	@Override
	public void provide(GenericEntity value, CallbackEntityType callback) {
		try {
			List<EntityType<?>> mdListEntityType = getEntityTypeList(value);
			
			callback.onSuccess(mdListEntityType);			
			
		} catch (Exception e) {
			callback.onFailure(e);
		}
	}

	private List<EntityType<?>> getEntityTypeList(GenericEntity value) {
		List<EntityType<?>> mdListEntityType = new ArrayList<EntityType<?>>();
		//Set<EntityType<?>> mdPredicateList = getPredicateMetaDataList();
		mdListEntityType.addAll(getPredicateMetaDataList(false));
		
		if (value instanceof GmMetaModel) { 
			//Set<EntityType<?>> modelList = getSubTypes(ModelMetaData.class);	
			mdListEntityType.addAll(getSubTypes(ModelMetaData.class));			
		} else	if ((value instanceof GmEntityType) || (value instanceof GmEntityTypeOverride)) { 			
			//Set<EntityType<?>> entityList = getSubTypes(EntityTypeMetaData.class);				
			mdListEntityType.addAll(getSubTypes(EntityTypeMetaData.class));			
		} else if ((value instanceof GmProperty) || (value instanceof GmPropertyOverride)) { 			
			//Set<EntityType<?>> propertyList = getSubTypes(PropertyMetaData.class);
			mdListEntityType.addAll(getSubTypes(PropertyMetaData.class));			
		} else if ((value instanceof GmEnumType) || (value instanceof GmEnumTypeOverride)) { 			
			//Set<EntityType<?>> enumTypeList = getSubTypes(EnumTypeMetaData.class);		
			mdListEntityType.addAll(getSubTypes(EnumTypeMetaData.class));			
		} else if ((value instanceof GmEnumConstant) || (value instanceof GmEnumConstantOverride)) { 			
			//Set<EntityType<?>> enumTypeList = getSubTypes(EnumTypeMetaData.class);		
			mdListEntityType.addAll(getSubTypes(EnumConstantMetaData.class));			
		}
		return mdListEntityType;
	}

	@Override
	public void provide(GenericEntity value, GmMetaModel editingModel, GenericEntity entity, CallbackMetaData callback) {
		provide(value, callback);		
	}

	@Override
	public void provide(GenericEntity value, CallbackExpertResultType callback) {
		try {
			List<MetaDataEditorExpertResultType> mdList = new ArrayList<>();
			List<EntityType<?>> mdListEntityType = getEntityTypeList(value);									
			for (EntityType<?> entityType : mdListEntityType) {	
				MetaData metaData = (MetaData) createEntity(entityType);	
				if (metaData != null) {
					MetaDataEditorExpertResultType resultType = new MetaDataEditorExpertResultType();
					resultType.setMetaData(metaData);
					resultType.setOwner(null);
					mdList.add(resultType);
				}
			}
			
			callback.onSuccess(mdList);				
		} catch (Exception e) {
			callback.onFailure(e);
		}				
	}

	@Override
	public void provide(GenericEntity value, GmMetaModel editingModel, GenericEntity entity, CallbackExpertResultType callback) {
		provide(value, callback);		
	}

}
