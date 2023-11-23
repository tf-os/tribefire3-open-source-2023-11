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
package com.braintribe.gwt.gmview.action.client;

import java.util.Collection;
import java.util.Map;

import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.common.GmSessions;

public class ActionUtil {
	
	/**
	 * Checks and sets the action visibility based on the sharesEntities metaData, CollectionElementCountConstraint metaData, and the PropertyEdtitable metaData,
	 * returning the max possible number of entries to add in the collection.
	 */
	public static int checkSharesEntitiesCollectionActionVisibility(PropertyRelatedModelPathElement collectionElement,
			/* SelectorContext selectorContext, boolean configureReevaluationTrigger, MetaDataReevaluationHandler
			 * metaDataReevaluationHandler, */ ModelAction action, String useCase) {
		Property property = collectionElement.getProperty();
		String propertyName = property.getName();
		
		/*EntityInfo entityInfo = new EntityInfo(GMF.getTypeReflection().getEntityType(collectionElement.getEntity()), collectionElement.getEntity());
		MetaDataAndTrigger<PropertyEditable> editableData = MetaDataReevaluationHelper.getPropertyEditableData(entityInfo, propertyName, selectorContext);
		boolean editable = editableData == null || !editableData.isValid() ? true : editableData.getMetaData().getEditable();
		
		/*MetaDataAndTrigger<PropertySharesEntities> sharesEntitiesData = MetaDataReevaluationHelper.getPropertySharesEntitiesData(entityInfo, propertyName,
				selectorContext);
		boolean sharesEntities = sharesEntitiesData == null || !sharesEntitiesData.isValid() ? true : sharesEntitiesData.getMetaData().getShares();
		
		MetaDataAndTrigger<CollectionElementCountConstraint> collectionElementCountConstraintData =
				MetaDataReevaluationHelper.getCollectionElementCountConstraint(entityInfo, propertyName, selectorContext);
		Integer maxElements = collectionElementCountConstraintData == null || !collectionElementCountConstraintData.isValid() ? null :
				collectionElementCountConstraintData.getMetaData().getMaxCount();
		
		if (configureReevaluationTrigger) {
			if (editableData != null && editableData.getReevaluationTrigger() != null) {
				MetaDataReevaluationDistributor.getInstance().configureReevaluationTrigger(editableData.getReevaluationTrigger(), metaDataReevaluationHandler);
			}
			if (sharesEntitiesData != null && sharesEntitiesData.getReevaluationTrigger() != null) {
				MetaDataReevaluationDistributor.getInstance().configureReevaluationTrigger(sharesEntitiesData.getReevaluationTrigger(), metaDataReevaluationHandler);
			}
			if (collectionElementCountConstraintData != null && collectionElementCountConstraintData.getReevaluationTrigger() != null) {
				MetaDataReevaluationDistributor.getInstance().configureReevaluationTrigger(collectionElementCountConstraintData.getReevaluationTrigger(), metaDataReevaluationHandler);
			}
		}*/
		
		GenericEntity parentEntity = collectionElement.getEntity();
		ModelMdResolver modelMdResolver = GmSessions.getMetaData(parentEntity).useCase(useCase);
		PropertyMdResolver propertyMdResolver = modelMdResolver.entity(parentEntity).property(propertyName);
		
		boolean editable = GMEMetadataUtil.isPropertyEditable(propertyMdResolver, parentEntity);
		int maxEntriesToAdd = getMaxEntriesToAdd(collectionElement, propertyMdResolver);
		
		if (editable && (GMEMetadataUtil.isReferenceable(propertyMdResolver, modelMdResolver) || GMEMetadataUtil.isInstantiable(propertyMdResolver, modelMdResolver)) && maxEntriesToAdd > 0) {
			action.setHidden(false);
			return maxEntriesToAdd;
		} else {
			action.setHidden(true);
			return -1;
		}
	}

	public static int getMaxEntriesToAdd(PropertyRelatedModelPathElement collectionElement, PropertyMdResolver propertyMdResolver) {
		Integer maxElements = GMEMetadataUtil.getPropertyCollectionMaxCount(propertyMdResolver);
		
		int collectionSize = 0;
		Object collection = collectionElement.getValue();
		if (collection instanceof Collection)
			collectionSize = ((Collection<?>) collection).size();
		else if (collection instanceof Map)
			collectionSize = ((Map<?,?>) collection).size();
		
		return maxElements == null ? Integer.MAX_VALUE : maxElements - collectionSize;
	}
	
}
