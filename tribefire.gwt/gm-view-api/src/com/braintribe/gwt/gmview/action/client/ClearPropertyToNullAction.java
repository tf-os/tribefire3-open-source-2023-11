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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.prompt.VirtualEnumConstant;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;

/**
 * This action is responsible for setting a property to null.
 * @author michel.docouto
 *
 */
public class ClearPropertyToNullAction extends ModelAction implements LocalManipulationAction/*, MetaDataReevaluationHandler*/ {
	
	private boolean actionEnabled = true; //TODO: this will be a meta data
	//private boolean configureReevaluationTrigger = true;
	private PropertyRelatedModelPathElement propertyElement;
	private PersistenceGmSession gmSession;
	private LocalManipulationListener listener;
	
	public ClearPropertyToNullAction() {
		setHidden(true);
		setName(LocalizedText.INSTANCE.setNull());
		setIcon(GmViewActionResources.INSTANCE.nullAction());
		setHoverIcon(GmViewActionResources.INSTANCE.nullActionBig());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}
	
	@Override
	public void configureListener(LocalManipulationListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		if (!(propertyElement.getProperty().getType() instanceof CollectionType)) {
			GMEUtil.changeEntityPropertyValue(propertyElement.getEntity(), propertyElement.getProperty(), null);
			fireListener();
			return;
		}
		
		NestedTransaction nestedTransaction = getGmSession().getTransaction().beginNestedTransaction();
		
		PropertyRelatedModelPathElement mapElement = (PropertyRelatedModelPathElement) propertyElement.getPrevious();
		if (!(mapElement.getValue() instanceof Map))
			mapElement = (PropertyRelatedModelPathElement) mapElement.getPrevious();
		
		GMEUtil.replaceInCollection(mapElement, propertyElement, null);
		nestedTransaction.commit();
		fireListener();
	}

	/*@Override
	public void reevaluateMetaData(SelectorContext selectorContext, MetaData metaData, EntitySignatureAndPropertyName owner) {
		updateVisibility(selectorContext);
	}*/
	
	@Override
	protected void updateVisibility(/*SelectorContext selectorContext*/) {
		isEnabled(modelPaths);
	}
	
	protected boolean isEnabled(List<List<ModelPath>> modelPaths) {
		this.modelPaths = modelPaths;
		if (modelPaths == null || modelPaths.size() != 1 || !actionEnabled) {
			setHidden(true);
			return false;
		}
		
		List<ModelPath> selection = modelPaths.get(0);
		for (ModelPath selectedValue : selection) {
			ModelPathElement element = selectedValue.last();
			ModelPathElement parentElement = null;
			if (selectedValue.size() > 1)
				parentElement = element.getPrevious();
			if (element.getValue() != null && (parentElement == null || !parentElement.getType().isCollection()) && element instanceof PropertyRelatedModelPathElement) {
				propertyElement = (PropertyRelatedModelPathElement) element;
				Property property = propertyElement.getProperty();
				
				//handleReevaluation();
				GenericModelType propertyType = property.getType();
				if (propertyType.isCollection()) {
					GenericModelType modelType = ((CollectionType) propertyType).getCollectionElementType();
					if (modelType == VirtualEnumConstant.T) {
						//RVE if is VirtualEnum part we need check the metaData for parentElement
						if (parentElement instanceof PropertyRelatedModelPathElement) {
							propertyElement = (PropertyRelatedModelPathElement) parentElement;							
						} else
							continue;
					} else	
						continue;
				}
				
				GenericEntity parentEntity = propertyElement.getEntity();											
				ModelMdResolver modelMdResolver = GmSessions.getMetaData(parentEntity).useCase(gmContentView.getUseCase());
				EntityMdResolver entityMdResolver = modelMdResolver.entity(parentEntity);
				PropertyMdResolver propertyMdResolver = entityMdResolver.entity(parentEntity).property(propertyElement.getProperty());				
				boolean editable = GMEMetadataUtil.isPropertyEditable(propertyMdResolver, parentEntity);

				//boolean editable = GMEMetadataUtil.isPropertyEditable(
				//		getMetaData(parentEntity).entity(parentEntity).property(propertyName).useCase(gmContentView.getUseCase()), parentEntity);
								
				if (editable && property.isNullable()) {
					setHidden(false);
					return true;
				}
			}
		}
		
		setHidden(true);
		return false;
	}
	
	protected PersistenceGmSession getGmSession() {
		if (gmSession == null) {
			gmSession = gmContentView.getGmSession();
		}
		
		return gmSession;
	}
	
	private void fireListener() {
		if (listener != null)
			listener.onManipulationPerformed();
	}
	
	/*private void handleReevaluation() {
		GenericEntity parentEntity = entityElement.getEntity();
		MetaDataAndTrigger<PropertyEditable> editableData = MetaDataReevaluationHelper.getPropertyEditableData(
				new EntityInfo(GMF.getTypeReflection().getEntityType(parentEntity), parentEntity), propertyName, selectorContext);
		boolean editable = editableData == null || !editableData.isValid() ? true : editableData.getMetaData().getEditable();
		
		if (configureReevaluationTrigger) {
			if (editableData != null && editableData.getReevaluationTrigger() != null) {
				MetaDataReevaluationDistributor.getInstance().configureReevaluationTrigger(editableData.getReevaluationTrigger(), this);
			}
			configureReevaluationTrigger = false;
		}
	}*/

}
