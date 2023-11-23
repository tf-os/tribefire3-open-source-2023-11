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
package com.braintribe.gwt.gmview.metadata.client;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * This helper class is responsible for getting MetaData information, using a selector context.
 * @author michel.docouto
 *
 */
public class MetaDataReevaluationHelper {
	
	//private static CascadingMetaDataResolver cmdResolver;
	
	protected MetaDataReevaluationHelper() {}
	
	@SuppressWarnings("unused")
	public static void setGmSession(PersistenceGmSession gmSession) {
		//cmdResolver = cascadingMetaDataResolver;
	}
	
	/*public static MetaDataAndTrigger<PropertyEditable> getPropertyEditableData(EntityInfo entityInfo, String propertyName, SelectorContext selectorContext) {
		MetaDataAndTrigger<PropertyEditable> editableData = null;
		PropertyEditable propertyEditable =  metaDataResolver.getPropertyEditable(entityInfo.getEntityType(), propertyName);
		if (propertyEditable != null) {
			if (selectorContext == null) {
				selectorContext = preparePropertySelectorContext(propertyEditable.getSelector(), entityInfo.getEntity(), entityInfo.getEntityType());
			}
			editableData = metaDataResolver.getPropertyEditable(entityInfo.getEntityType(), propertyName, selectorContext);
		}
		return editableData;
	}*/
	
	/*public static MetaDataAndTrigger<PropertySharesEntities> getPropertySharesEntitiesData(EntityTreeModel entityTreeModel, String propertyName, SelectorContext selectorContext) {
		EntityInfo entityInfo = prepareEntityInfo(entityTreeModel);
		return getPropertySharesEntitiesData(entityInfo, propertyName, selectorContext);
	}*/
	
	/*public static MetaDataAndTrigger<PropertySharesEntities> getPropertySharesEntitiesData(EntityInfo entityInfo, String propertyName, SelectorContext selectorContext) {
		PropertySharesEntities propertySharesEntities = metaDataResolver.getPropertySharesEntities(entityInfo.getEntityType(), propertyName);
		MetaDataAndTrigger<PropertySharesEntities> sharesEntitiesData = null;
		if (propertySharesEntities != null) {
			if (selectorContext == null) {
				selectorContext = preparePropertySelectorContext(propertySharesEntities.getSelector(), entityInfo.getEntity(), entityInfo.getEntityType());
			}
			sharesEntitiesData = metaDataResolver.getPropertySharesEntities(entityInfo.getEntityType(), propertyName, selectorContext);
		}
		return sharesEntitiesData;
	}*/
	
	/*public static MetaDataAndTrigger<PropertyVisibility> getPropertyVisibilityData(EntityTreeModel entityTreeModel, String propertyName, SelectorContext selectorContext) {
		EntityInfo entityInfo = prepareEntityInfo(entityTreeModel);
		return getPropertyVisibilityData(entityInfo, propertyName, selectorContext);
	}*/
	
	/*public static MetaDataAndTrigger<PropertyVisibility> getPropertyVisibilityData(EntityInfo entityInfo, String propertyName, SelectorContext selectorContext) {
		PropertyVisibility propertyVisibility = metaDataResolver.getPropertyVisibility(entityInfo.getEntityType(), propertyName);
		MetaDataAndTrigger<PropertyVisibility> visibleData = null;
		if (propertyVisibility != null) {
			if (selectorContext == null) {
				selectorContext = preparePropertySelectorContext(propertyVisibility.getSelector(), entityInfo.getEntity(), entityInfo.getEntityType());
			}
			visibleData = metaDataResolver.getPropertyVisibility(entityInfo.getEntityType(), propertyName, selectorContext);
		}
		return visibleData;
	}
	
	public static MetaDataAndTrigger<EntityInstantiationDisabled> getEntityInstantiationDisabledData(EntityInfo entityInfo, SelectorContext selectorContext) {
		if (selectorContext == null) {
			selectorContext = prepareEntitySelectorContext(entityInfo.getEntity(), entityInfo.getEntityType());
		}
		return metaDataResolver.getEntityInstantiationDisabled(entityInfo.getEntityType(), selectorContext);
	}
	
	public static MetaDataAndTrigger<EntitySimplification> getEntitySimplificationData(EntityType<?> entityType, GenericEntity entity, SelectorContext selectorContext) {
		if (selectorContext == null) {
			selectorContext = prepareEntitySelectorContext(entity, entityType);
		}
		return metaDataResolver.getEntitySimplification(entityType, selectorContext);
	}*/
	
	/*public static MetaDataAndTrigger<MandatoryProperty> getMandatoryPropertyData(EntityTreeModel entityTreeModel, String propertyName, SelectorContext selectorContext) {
		EntityInfo entityInfo = prepareEntityInfo(entityTreeModel);
		return getMandatoryPropertyData(entityInfo, propertyName, selectorContext);
	}*/
	
	/*public static MetaDataAndTrigger<MandatoryProperty> getMandatoryPropertyData(EntityInfo entityInfo, String propertyName, SelectorContext selectorContext) {
		MandatoryProperty mandatoryProperty = metaDataResolver.getMandatoryProperty(entityInfo.getEntityType(), propertyName);
		MetaDataAndTrigger<MandatoryProperty> mandatoryData = null;
		if (mandatoryProperty != null) {
			if (selectorContext == null) {
				selectorContext = preparePropertySelectorContext(mandatoryProperty.getSelector(), entityInfo.getEntity(), entityInfo.getEntityType());
			}
			mandatoryData = metaDataResolver.getMandatoryProperty(entityInfo.getEntityType(), propertyName, selectorContext);
		}
		return mandatoryData;
	}*/
	
	/*public static MetaDataAndTrigger<PropertyPriority> getPropertyPriorityData(EntityTreeModel entityTreeModel, String propertyName, SelectorContext selectorContext) {
		EntityInfo entityInfo = prepareEntityInfo(entityTreeModel);
		return getPropertyPriorityData(entityInfo, propertyName, selectorContext);
	}*/
	
	/*public static MetaDataAndTrigger<PropertyPriority> getPropertyPriorityData(EntityInfo entityInfo, String propertyName, SelectorContext selectorContext) {
		PropertyPriority propertyPriority = metaDataResolver.getPropertyPriority(entityInfo.getEntityType(), propertyName);
		MetaDataAndTrigger<PropertyPriority> priorityData = null;
		if (propertyPriority != null) {
			if (selectorContext == null) {
				selectorContext = preparePropertySelectorContext(propertyPriority.getSelector(), entityInfo.getEntity(), entityInfo.getEntityType());
			}
			priorityData = metaDataResolver.getPropertyPriority(entityInfo.getEntityType(), propertyName, selectorContext);
		}
		return priorityData;
	}*/
	
	/*public static MetaDataAndTrigger<PropertyCreateEntities> getPropertyCreateEntitiesData(EntityTreeModel entityTreeModel, String propertyName, SelectorContext selectorContext) {
		EntityInfo entityInfo = prepareEntityInfo(entityTreeModel);
		return getPropertyCreateEntitiesData(entityInfo, propertyName, selectorContext);
	}*/
	
	/*public static MetaDataAndTrigger<PropertyCreateEntities> getPropertyCreateEntitiesData(EntityInfo entityInfo, String propertyName, SelectorContext selectorContext) {
		PropertyCreateEntities propertyCreateEntities = metaDataResolver.getPropertyCreateEntities(entityInfo.getEntityType(), propertyName);
		MetaDataAndTrigger<PropertyCreateEntities> createEntitiesData = null;
		if (propertyCreateEntities != null) {
			if (selectorContext == null) {
				selectorContext = preparePropertySelectorContext(propertyCreateEntities.getSelector(), entityInfo.getEntity(), entityInfo.getEntityType());
			}
			createEntitiesData = metaDataResolver.getPropertyCreateEntities(entityInfo.getEntityType(), propertyName, selectorContext);
		}
		return createEntitiesData;
	}*/
	
	/*public static MetaDataAndTrigger<CollectionElementCountConstraint> getCollectionElementCountConstraint(EntityTreeModel entityTreeModel, String propertyName,
			SelectorContext selectorContext) {
		EntityInfo entityInfo = prepareEntityInfo(entityTreeModel);
		return getCollectionElementCountConstraint(entityInfo, propertyName, selectorContext);
	}*/
	
	/*public static MetaDataAndTrigger<CollectionElementCountConstraint> getCollectionElementCountConstraint(EntityInfo entityInfo, String propertyName, SelectorContext selectorContext) {
		CollectionElementCountConstraint collectionElementCountConstraint = metaDataResolver.getCollectionElementCountConstraint(entityInfo.getEntityType(), propertyName);
		MetaDataAndTrigger<CollectionElementCountConstraint> collectionElementCountConstraintData = null;
		if (collectionElementCountConstraint != null) {
			if (selectorContext == null) {
				selectorContext = preparePropertySelectorContext(collectionElementCountConstraint.getSelector(), entityInfo.getEntity(), entityInfo.getEntityType());
			}
			collectionElementCountConstraintData = metaDataResolver.getCollectionElementCountConstraint(entityInfo.getEntityType(), propertyName, selectorContext);
		}
		return collectionElementCountConstraintData;
	}
	
	private static <X> PropertySelectorContext preparePropertySelectorContext(MetaDataSelector metaDataSelector, GenericEntity entity, EntityType<GenericEntity> entityType) {
		if (metaDataSelector instanceof SimplePropertyDiscriminator) {
			String propertyName = ((SimplePropertyDiscriminator) metaDataSelector).getDiscriminatorProperty().getName();
			PropertySelectorContext propertySelectorContext = new PropertySelectorContext();
			propertySelectorContext.setEntity(entity);
			propertySelectorContext.setEntityType(entityType);
			propertySelectorContext.setProperty(entityType.getProperty(propertyName));
			if (entity != null)
				propertySelectorContext.setPropertyValue(entityType.getPropertyValue(entity, propertyName));
			return propertySelectorContext;
		}
		
		return null;
	}
	
	private static EntitySelectorContext prepareEntitySelectorContext(GenericEntity entity, EntityType<?> entityType) {
		EntitySelectorContext entitySelectorContext = new EntitySelectorContext();
		entitySelectorContext.setEntity(entity);
		entitySelectorContext.setEntityType(entityType);
		return entitySelectorContext;
	}*/
	
	/*@SuppressWarnings({ "unchecked", "rawtypes" })
	public static EntityInfo prepareEntityInfo(EntityTreeModel entityTreeModel) {
		EntityType<GenericEntity> entityType = (EntityType) entityTreeModel.getElementType();
		GenericEntity entity = (GenericEntity) entityTreeModel.getModelObject();
		return new EntityInfo(entityType, entity);
	}*/
	
	public static class EntityInfo {
		private EntityType<GenericEntity> entityType;
		private GenericEntity entity;
		
		public EntityInfo(EntityType<GenericEntity> entityType, GenericEntity entity) {
			super();
			this.entityType = entityType;
			this.entity = entity;
		}

		public EntityType<GenericEntity> getEntityType() {
			return entityType;
		}

		public GenericEntity getEntity() {
			return entity;
		}
	}

}
