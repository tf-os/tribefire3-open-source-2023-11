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
package com.braintribe.gwt.gme.propertypanel.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.braintribe.gwt.gme.propertypanel.client.field.ExtendedInlineField;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.PropertyPath;
import com.braintribe.model.meta.data.display.GroupAssignment;
import com.braintribe.model.meta.data.display.GroupPriority;
import com.braintribe.model.meta.data.display.HideLabel;
import com.braintribe.model.meta.data.prompt.Embedded;
import com.braintribe.model.meta.data.prompt.EntityCompoundViewing;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.prompt.Inline;
import com.braintribe.model.meta.data.prompt.Outline;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

public class PropertyPanelMetadataUtil {
	
	protected static boolean isPropertySimpleOrSimplified(PropertyPanel propertyPanel, ModelMdResolver metaDataResolver, GenericModelType propertyType, Object value, String propertyName,
			GenericEntity parentEntity, EntityType<?> parentEntityType, String useCase, boolean lenient, Set<Class<?>> simplifiedEntityTypes) {
		boolean isSimpleOrSimplified = true;
		if (propertyType.isEntity()) {
			isSimpleOrSimplified = false;
			EntityType<?> propertyEntityType = (EntityType<?>) propertyType;
			if (simplifiedEntityTypes != null && simplifiedEntityTypes.contains(propertyEntityType.getJavaType()))
				isSimpleOrSimplified = true;
			if (metaDataResolver != null) {
				EntityMdResolver entityContextBuilder;
				if (value instanceof GenericEntity) {
					GenericEntity entity = (GenericEntity) value;
					entityContextBuilder = GmSessions.getMetaData(entity).lenient(lenient).entity(entity).useCase(useCase);
				} else
					entityContextBuilder = metaDataResolver.entityType(propertyEntityType).useCase(useCase).lenient(lenient);
				
				isSimpleOrSimplified = entityContextBuilder.is(Inline.T);
				propertyPanel.handleMetadataReevaluation(entityContextBuilder, Inline.T);
			}
			
			return isSimpleOrSimplified;
		}
		
		if (propertyType.isCollection())
			return false;
		
		if (propertyType.isBase())
			return false;
		
		if (metaDataResolver != null) {
			EntityMdResolver entityMdResolver;
			if (parentEntity != null)
				entityMdResolver = metaDataResolver.entity(parentEntity);
			else
				entityMdResolver = metaDataResolver.entityType(parentEntityType);
			Outline outline = entityMdResolver.property(propertyName).meta(Outline.T).exclusive();
			if (outline != null)
				isSimpleOrSimplified = outline.isTrue();
			propertyPanel.handleMetadataReevaluation(entityMdResolver, Outline.T);
		}
		
		return isSimpleOrSimplified;
	}
	
	protected static boolean isPropertyVisible(PropertyPanel propertyPanel, PropertyMdResolver propertyMdResolver,
			boolean editable, boolean isSimpleOrSimplified, boolean hideNonEditableProperties, boolean hideNonSimpleProperties) {
		Visible propertyVisibility = null;
		Hidden hidden = null;
		if (propertyMdResolver != null) {
			propertyVisibility = propertyMdResolver.meta(Visible.T).exclusive();
			hidden = propertyMdResolver.meta(Hidden.T).exclusive();
			propertyPanel.handleMetadataReevaluation(propertyMdResolver, Visible.T);
			propertyPanel.handleMetadataReevaluation(propertyMdResolver, Hidden.T);
		}
		
		if (propertyVisibility != null)
			return propertyVisibility.isTrue();
		
		if (hideNonEditableProperties || hideNonSimpleProperties) {
			if (!hideNonEditableProperties)
				return isSimpleOrSimplified;
			
			if (hideNonSimpleProperties)
				return editable && isSimpleOrSimplified;
			else
				return editable;
		}
		
		return hidden == null ? true : hidden.isTrue();
	}
	
	protected static void handleGroupAssignment(String propertyName, ModelMdResolver metaDataResolver, PropertyModel propertyModel, GenericEntity parentEntity,
			EntityType<?> parentEntityType, String useCase, boolean lenient, String defaultGroup, ManagedGmSession gmSession) {
		if (metaDataResolver == null)
			return;
		
		EntityMdResolver entityMdResolver;
		if (parentEntity != null)
			entityMdResolver = metaDataResolver.entity(parentEntity);
		else
			entityMdResolver = metaDataResolver.entityType(parentEntityType);
		
		GroupAssignment groupAssignment = entityMdResolver.property(propertyName).useCase(useCase).lenient(lenient).meta(GroupAssignment.T)
				.exclusive();
		if (groupAssignment == null)
			return;
		
		propertyModel.setPropertyGroup(groupAssignment.getGroup());
		if (propertyModel.getPropertyGroup() == null)
			propertyModel.setGroupName(defaultGroup);
		
		propertyModel.setGroupIcon(GMEIconUtil.getGroupIcon(groupAssignment, gmSession));
		
		double groupPriorityDouble = defaultGroup.equals(propertyModel.getGroupName()) ? Double.NEGATIVE_INFINITY : 0.0;
		List<GroupPriority> groupPriorities = entityMdResolver.meta(GroupPriority.T).list();
		GroupPriority groupPriority = null;
		if (groupPriorities != null) {
			groupPriority = groupPriorities.stream()
					.filter(gp -> gp.getGroup() != null && gp.getGroup().equals(propertyModel.getPropertyGroup())).findFirst().orElse(null);
		}
		
		propertyModel.setGroupPriority(groupPriority != null ? groupPriority.getPriority() : groupPriorityDouble);
	}
	
	protected static boolean isPropertyFlow(PropertyPanel propertyPanel, PropertyMdResolver propertyMdResolver, GenericModelType propertyType,
			ExtendedInlineField extendedInlineField, boolean skipMetadataResolution, String useCase, boolean lenient, ManagedGmSession gmSession,
			Logger logger, Set<Class<?>> simplifiedEntityTypes, String propertyName, GenericEntity parentEntity) {
		if (extendedInlineField != null && propertyType.isEntity())
			return false;
		
		if (propertyMdResolver != null && !propertyType.isEntity() && !isCollection(propertyType, propertyName, parentEntity)) {
			propertyPanel.handleMetadataReevaluation(propertyMdResolver, Inline.T);
			Inline inline = propertyMdResolver.meta(Inline.T).exclusive();
			if (inline != null)
				return !inline.isTrue();
		}
		
		if (!propertyType.isEntity())
			return isCollection(propertyType, propertyName, parentEntity);
		
		if (!skipMetadataResolution) {
			Inline inline = null;
			if (propertyMdResolver != null)
				inline = propertyMdResolver.meta(Inline.T).exclusive();
			
			if (inline == null) {
				inline = getMetaData(skipMetadataResolution, gmSession, logger).lenient(lenient).entityType((EntityType<?>) propertyType)
					.useCase(useCase).meta(Inline.T).exclusive();
			}
			
			if (inline != null)
				return !inline.isTrue();
		}

		return simplifiedEntityTypes == null || !simplifiedEntityTypes.contains(propertyType.getJavaType());
	}
	
	private static boolean isCollection(GenericModelType propertyType, String propertyName, GenericEntity parentEntity) {
		if (propertyType.isCollection())
			return true;
		
		return "initializer".equals(propertyName) && parentEntity instanceof GmProperty && ((GmProperty) parentEntity).getType() != null
				&& ((GmProperty) parentEntity).getType().isGmCollection();
	}
	
	protected static ModelMdResolver getMetaData(boolean skipMetadataResolution, ManagedGmSession gmSession, Logger logger) {
		if (skipMetadataResolution)
			return null;
		
		try {
			return gmSession.getModelAccessory().getMetaData();
		} catch (Exception ex) {
			skipMetadataResolution = true;
			logger.error("Error while geting the ModelMdResolver", ex);
			ex.printStackTrace();
			return null;
		}
	}
	
	protected static boolean isHideLabel(PropertyMdResolver propertyMdResolver, GenericModelType propertyType, ModelMdResolver metaDataResolver) {
		if (propertyMdResolver == null)
			return false;
		
		if (propertyMdResolver.is(HideLabel.T))
			return true;
		
		if (!propertyType.isEntity())
			return false;
		
		if (metaDataResolver == null)
			return false;
		
		return metaDataResolver.entityType((EntityType<?>) propertyType).is(HideLabel.T);
	}
	
	protected static Embedded getEmbedded(ModelMdResolver metaDataResolver, GenericEntity parentEntity, Property property, GenericModelType propertyType, String useCase, boolean lenient) {
		Embedded embedded = metaDataResolver != null
				? metaDataResolver.entity(parentEntity).property(property).useCase(useCase).lenient(lenient).meta(Embedded.T).exclusive()
				: null;
		if (embedded == null) {
			embedded = metaDataResolver != null
					? metaDataResolver.entityType((EntityType<?>) propertyType).useCase(useCase).lenient(lenient).meta(Embedded.T).exclusive()
					: null;
		}
		
		return embedded;
	}
	
	protected static List<PropertyPath> getPropertyPaths(Embedded embedded, List<EntityCompoundViewing> entityCompoundViewings, String propertyName) {
		if (embedded != null || entityCompoundViewings.isEmpty())
			return null;
		
		List<PropertyPath> propertyPaths = null; //TODO: is this code correct? Should the propertyPaths list be created new all the time in the for?
		for (EntityCompoundViewing entityCompoundViewing : entityCompoundViewings) {
			List<GmProperty> pathList = entityCompoundViewing.getPropertyPath().getProperties();
			if (!pathList.isEmpty() && pathList.get(0).getName().equals(propertyName)) {
				PropertyPath propertyPath = PropertyPath.T.create();
				propertyPath.setProperties(pathList.subList(1, pathList.size()));
				propertyPaths = new ArrayList<>();
				propertyPaths.add(propertyPath);
				break;
			}
		}
		
		return propertyPaths;
	}
	
//	protected ModelMdResolver getMetaData() {
//	if (skipMetadataResolution)
//		return null;
//	
//	if (modelMdResolver == null && parentEntity != null)
//		modelMdResolver = GmSessions.getMetaData(parentEntity);
//	
//	return modelMdResolver;
//}

}
