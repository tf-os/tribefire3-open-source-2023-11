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
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil.KeyAndValueGMTypeInstanceBean;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;

/**
 * This action is responsible for removing items from a collection.
 * @author michel.docouto
 *
 */
public class RemoveFromCollectionAction extends ModelAction implements LocalManipulationAction/*, MetaDataReevaluationHandler*/ {
	
	private PropertyRelatedModelPathElement collectionElement;
	//private boolean configureReevaluationTrigger = true;
	private List<ModelPath> selectedValues;
	private LocalManipulationListener listener;
	
	public RemoveFromCollectionAction() {
		setHidden(true);
		setName(LocalizedText.INSTANCE.removeFromCollection());
		setIcon(GmViewActionResources.INSTANCE.remove());
		setHoverIcon(GmViewActionResources.INSTANCE.removeBig());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}
	
	@Override
	public void configureListener(LocalManipulationListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		removeFromCollection();
		if (listener != null)
			listener.onManipulationPerformed();
	}
	
	/*@Override
	public void reevaluateMetaData(SelectorContext selectorContext, MetaData metaData, EntitySignatureAndPropertyName owner) {
		updateVisibility(selectorContext);
	}*/
	
	@Override
	protected void updateVisibility(/*SelectorContext selectorContext*/) {
		isEnabled(modelPaths);
	}
	
	protected PersistenceGmSession getGmSession() {
		return gmContentView.getGmSession();
	}
	
	protected boolean isEnabled(List<List<ModelPath>> modelPaths) {
		this.modelPaths = modelPaths;
		collectionElement = null;
		if (selectedValues != null)
			selectedValues.clear();
		else
			selectedValues = newList();
		
		if (modelPaths == null) {
			setHidden(true);
			return false;
		}
		
		for (List<ModelPath> selection : modelPaths) {
			boolean selectionValid = false;
			for (ModelPath selectedValue : selection) {
				if (selectedValue.size() <= 1)
					continue;
				
				ModelPathElement element = selectedValue.get(selectedValue.size() - 2);
				if ((collectionElement == null || collectionElement.getValue() == element.getValue())
						&& element instanceof PropertyRelatedModelPathElement && element.getType().isCollection()) {
					collectionElement = (PropertyRelatedModelPathElement) element;
					selectedValues.add(selectedValue);
					selectionValid = true;
					break;
				} else {
					collectionElement = null;
				}
			}
			
			if (!selectionValid) {
				setHidden(true);
				return false;
			}
		}
		
		if (collectionElement != null) {
			Property property = collectionElement.getProperty();
			String propertyName = property.getName();
			//handleReevaluation();
			
			GenericEntity entity = collectionElement.getEntity();
			if (GMEMetadataUtil.isPropertyEditable(getMetaData(entity).entity(entity).property(propertyName).useCase(gmContentView.getUseCase()), entity)) {
				/*PropertyDisplayInfo displayInfo = cmdResolver.getMetaData().entity(collectionElement.getEntity()).
						property(propertyName).useCase(useCase).meta(PropertyDisplayInfo.T).exclusive();
				if (displayInfo != null && displayInfo.getName() != null) {
					propertyName = I18nTools.getLocalized(displayInfo.getName());
				}*/
				
				setHidden(false);
				setName(LocalizedText.INSTANCE.removeFromCollectionProperty(/*propertyName*/));
				return true;
			}
		}
		
		setHidden(true);
		return false;
	}
	
	protected void removeFromCollection() {
		List<KeyAndValueGMTypeInstanceBean> keysAndValues = new ArrayList<>();
		//Map<Object, Integer> elementIndices = new HashMap<Object, Integer>();
		//boolean prepareIndices = collectionElement.getValue() instanceof Set && gmContentView instanceof IndexedGmListView;
		for (ModelPath selectedValue : selectedValues) {
			ModelPathElement element = selectedValue.last();
			keysAndValues.add(GMEUtil.prepareKeyAndValueGMTypeInstanceBean(collectionElement, element));
			//if (prepareIndices)
				//elementIndices.put(element.getValue(), ((IndexedGmListView) gmContentView).getRootElementIndex(element.getValue()));
		}
		
		NestedTransaction nestedTransaction = getGmSession().getTransaction().beginNestedTransaction();
		
		//PropertyRelatedModelPathElement collectionElement = this.collectionElement;
		GMEUtil.insertOrRemoveToCollection(collectionElement, keysAndValues, false);
		//List<KeyAndValueGMTypeInstanceBean> keysAndValuesNotRemoved = GMEUtil.insertOrRemoveToCollection(collectionElement, keysAndValues, false);
		//if (!keysAndValuesNotRemoved.isEmpty() && !elementIndices.isEmpty())
			//createUiRemoveFromSetManipulation(keysAndValuesNotRemoved, collectionElement, elementIndices);
		
		nestedTransaction.commit();
	}
	
	/*protected void createUiRemoveFromSetManipulation(List<KeyAndValueGMTypeInstanceBean> keysAndValues, PropertyRelatedModelPathElement collectionElement,
				Map<Object, Integer> elementIndices) {
		RemoveManipulation removeManipulation = new RemoveManipulation();
		removeManipulation.setDescription("created by RemoveFromCollectionAction");
		
		LocalEntityProperty  localEntityProperty = new LocalEntityProperty();
		localEntityProperty.setEntity(collectionElement.getEntity());
		localEntityProperty.setPropertyName(collectionElement.getProperty().getPropertyName());
		
		removeManipulation.setOwner(localEntityProperty);
		
		Map<Object, Object> itemsToRemove = new HashMap<Object, Object>();
		for (KeyAndValueGMTypeInstanceBean bean : keysAndValues) {
			Integer index = elementIndices.get(bean.getValue().getInstance());
			if (index != null && index != -1)
				itemsToRemove.put(index, bean.getValue().getInstance());
		}
		removeManipulation.setItemsToRemove(itemsToRemove);
		
		AddManipulation addManipulation = new AddManipulation();
		addManipulation.setDescription("created by RemoveFromCollectionAction");
		addManipulation.setOwner(localEntityProperty);
		addManipulation.setItemsToAdd(itemsToRemove);
		
		removeManipulation.linkInverse(addManipulation);
		
		gmSession.noticeManipulation(removeManipulation);
	}*/
	
	/*private void handleReevaluation() {
		GenericEntity parentEntity = collectionElement.getEntity();
		EntityType<GenericEntity> parentEntityType = GMF.getTypeReflection().getEntityType(parentEntity);
		MetaDataAndTrigger<PropertyEditable> editableData = MetaDataReevaluationHelper.getPropertyEditableData(
				new EntityInfo(parentEntityType, parentEntity), propertyName, selectorContext);
		boolean editable = editableData == null || !editableData.isValid() ? true : editableData.getMetaData().getEditable();
		
		if (configureReevaluationTrigger) { //Configure only once
			if (editableData != null && editableData.getReevaluationTrigger() != null) {
				MetaDataReevaluationDistributor.getInstance().configureReevaluationTrigger(editableData.getReevaluationTrigger(), this);
			}
			configureReevaluationTrigger = false;
		}
	}*/

}
