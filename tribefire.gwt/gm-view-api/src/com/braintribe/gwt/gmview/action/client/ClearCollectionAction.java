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

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.prompt.VirtualEnumConstant;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;

public class ClearCollectionAction extends ModelAction implements LocalManipulationAction/*, MetaDataReevaluationHandler*/ {
	
	private PropertyRelatedModelPathElement collectionElement;
	//private boolean configureReevaluationTrigger = true;
	private Object collection;
	private LocalManipulationListener listener;
	
	public ClearCollectionAction() {
		setHidden(true);
		setName(LocalizedText.INSTANCE.clearCollection());
		setIcon(GmViewActionResources.INSTANCE.clearCollection());
		setHoverIcon(GmViewActionResources.INSTANCE.clearCollectionBig());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}
	
	@Override
	public void configureListener(LocalManipulationListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		if (collection instanceof Collection)
			((Collection<?>) collection).clear();
		else if (collection instanceof Map)
			((Map<?,?>) collection).clear();
		
		if (listener != null)
			listener.onManipulationPerformed();
	}
	
	/*@Override
	public void reevaluateMetaData(SelectorContext selectorContext, MetaData metaData, EntitySignatureAndPropertyName owner) {
		updateVisibility(selectorContext);
	}*/
	
	@Override
	protected void updateVisibility(/*SelectorContext selectorContext*/) {
		collection = null;
		if (modelPaths == null || modelPaths.size() != 1) {
			setHidden(true);
			return;
		}
		
		List<ModelPath> selection = modelPaths.get(0);
		for (ModelPath selectedValue : selection) {
			ModelPathElement element = selectedValue.last();
			if (!(element instanceof PropertyRelatedModelPathElement) || !element.getType().isCollection())
				continue;
			
			CollectionType ct = (CollectionType) element.getType();
			if(ct.getCollectionElementType() != VirtualEnumConstant.T) {
				collectionElement = (PropertyRelatedModelPathElement) element;
				Property property = collectionElement.getProperty();
				String propertyName = property.getName();
				
				//handleReevaluation();
				
				GenericEntity parentEntity = collectionElement.getEntity();
				String useCase = gmContentView.getUseCase();
				PropertyMdResolver propertyMetaDataBuilder = getMetaData(parentEntity).entity(parentEntity).property(propertyName).useCase(useCase);
				
				boolean editable = GMEMetadataUtil.isPropertyEditable(propertyMetaDataBuilder, parentEntity);
				
				if (editable/* && (minElements == null || minElements == 0)*/) {
					collection = collectionElement.getValue();
					if (collection == null)
						continue;
					
					propertyName = GMEMetadataUtil.getPropertyDisplay(propertyName, propertyMetaDataBuilder);
					if (collection instanceof Map) {
						if (!((Map<?,?>) collection).isEmpty()) {
							enableAction();
							return;
						}
					} else if (collection instanceof Collection) {
						if (!((Collection<?>) collection).isEmpty()) {
							enableAction();
							return;
						}
					}
				}
			}else
				setHidden(true);
		}
		
		setHidden(true);
	}
	
	private void enableAction() {
		setHidden(false);
		setName(LocalizedText.INSTANCE.clearCollectionProperty(/*propertyName*/));
	}
	
	/*private void handleReevaluation() {
		MetaDataAndTrigger<PropertyEditable> editableData = MetaDataReevaluationHelper.getPropertyEditableData(new EntityInfo(entityType,
				genericEntity), propertyName, selectorContext);
		boolean editable = editableData == null || !editableData.isValid() ? true : editableData.getMetaData().getEditable();
		
		/*MetaDataAndTrigger<CollectionElementCountConstraint> collectionElementCountConstraintData =
		MetaDataReevaluationHelper.getCollectionElementCountConstraint(entityTreeModel, propertyName, selectorContext);
		Integer minElements = collectionElementCountConstraintData == null || !collectionElementCountConstraintData.isValid() ? null :
				collectionElementCountConstraintData.getMetaData().getMinCount();
		
		if (configureReevaluationTrigger) { //Configure only once
			if (editableData != null && editableData.getReevaluationTrigger() != null) {
				MetaDataReevaluationDistributor.getInstance().configureReevaluationTrigger(editableData.getReevaluationTrigger(), this);
			}
			/*if (collectionElementCountConstraintData != null && collectionElementCountConstraintData.getReevaluationTrigger() != null) {
				MetaDataReevaluationDistributor.getInstance().configureReevaluationTrigger(collectionElementCountConstraintData.getReevaluationTrigger(), this);
			}
			configureReevaluationTrigger = false;
		}
	}*/

}
