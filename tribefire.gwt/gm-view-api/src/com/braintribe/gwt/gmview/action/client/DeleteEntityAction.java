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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.PerformanceListenerBoundModelAction;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.model.acl.AclOperation;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.constraint.Deletable;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.processing.async.api.AsyncCallback;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;

/**
 * This action is responsible for deleting an Entity.
 * @author stefan.prieler
 *
 */
public class DeleteEntityAction extends PerformanceListenerBoundModelAction implements LocalManipulationAction/*, MetaDataReevaluationHandler*/ {
	
	//private boolean configureReevaluationTrigger = true;
	private final List<ModelPathElement> entityElements = new ArrayList<ModelPathElement>();
	private boolean askConfirmation = true;
	private boolean checkForReferences = false;
	private final RemoveFromCollectionAction removeFromCollectionAction;
	private final ClearPropertyToNullAction clearPropertyToNullAction;
	private boolean removingFromCollection = false;
	private boolean clearingProperty = false;
	private int amountCounter;
	private LocalManipulationListener listener;
	
	public DeleteEntityAction(RemoveFromCollectionAction removeFromCollectionAction, ClearPropertyToNullAction clearPropertyToNullAction) {
		setIcon(GmViewActionResources.INSTANCE.delete());
		setHoverIcon(GmViewActionResources.INSTANCE.deleteBig());
		setName(LocalizedText.INSTANCE.deleteEntity(""));
		setHidden(true);
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		this.removeFromCollectionAction = removeFromCollectionAction;
		this.clearPropertyToNullAction = clearPropertyToNullAction;
	}
	
	/**
	 * Configures whether we should ask for confirmation before deleting entities.
	 * Defaults to true.
	 */
	@Configurable
	public void setAskConfirmation(boolean askConfirmation) {
		this.askConfirmation = askConfirmation;
	}
	
	/**
	 * Configures whether we should check for references prior to deleting the entities.
	 * Defaults to false.
	 */
	@Configurable
	public void setCheckForReferences(boolean checkForReferences) {
		this.checkForReferences = checkForReferences;
	}

	@Override
	public void configureListener(LocalManipulationListener listener) {
		this.listener = listener;
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		amountCounter = 0;
		if (!askConfirmation) {
			if (checkForReferences)
				checkForReferences();
			else
				deleteDirectly();
			
			fireListener();
			return;
		}
		
		String message;
		if (entityElements.size() == 1) {
			PersistenceGmSession gmSession = gmContentView.getGmSession();
			final ModelPathElement entityElement = entityElements.get(0);
			GenericEntity entity = entityElement.getValue();
			EntityType<?> entityType = (EntityType<?>) entityElement.getType();
			
			ModelMdResolver modelMdResolver;
			if (entity != null)
				modelMdResolver = getMetaData(entity);
			else
				modelMdResolver = gmSession.getModelAccessory().getMetaData();
			
			String entityDisplayInfo = GMEMetadataUtil.getEntityNameMDOrShortName(entityType, modelMdResolver, gmContentView.getUseCase());
			String selectiveInfo = SelectiveInformationResolver.resolve(entityType, entity, modelMdResolver, gmContentView.getUseCase(), true);
			message = LocalizedText.INSTANCE.confirmDeletionText(entityDisplayInfo, selectiveInfo);
		} else
			message = LocalizedText.INSTANCE.confirmMultipleDeletionText();
		
		MessageBox confirm = new ConfirmMessageBox(LocalizedText.INSTANCE.confirmDeletionTitle(), message);
		confirm.addDialogHideHandler(new DialogHideHandler() {
			@Override
			public void onDialogHide(DialogHideEvent event) {
				if (PredefinedButton.YES.equals(event.getHideButton())) {
					if (checkForReferences)
						checkForReferences();
					else
						deleteDirectly();

					fireListener();
				}
			}
		});
		confirm.show();
	}
	
	/*@Override
	public void reevaluateMetaData(SelectorContext selectorContext, MetaData metaData, EntitySignatureAndPropertyName owner) {
		updateVisibility(selectorContext);
	}*/
	
	private void deleteDirectly() {
		NestedTransaction nestedTransaction = gmContentView.getGmSession().getTransaction().beginNestedTransaction();
		for (ModelPathElement entityElement : new ArrayList<ModelPathElement>(entityElements)) {
			deleteDirectly(entityElement);
			removingFromCollection = false;
		}
		nestedTransaction.commit();
	}
	
	private void deleteDirectly(ModelPathElement entityElement) {
		if (removingFromCollection)
			removeFromCollectionAction.perform(null);
		else if (clearingProperty)
			clearPropertyToNullAction.perform(null);
		GenericEntity entity = entityElement.getValue();
		if (entity != null)
			gmContentView.getGmSession().deleteEntity(entity);
	}

	@Override
	protected void updateVisibility(/*SelectorContext selectorContext*/) {
		entityElements.clear();
		removingFromCollection = false;
		clearingProperty = false;
		
		if (modelPaths == null) {
			setHidden(true);
			return;
		}
		
		for (List<ModelPath> selection : modelPaths) {
			boolean selectionValid = false;
			for (ModelPath selectedValue : selection) {
				ModelPathElement element = selectedValue.last();
				if (!(element.getValue() instanceof GenericEntity))
					continue;
				
				GenericEntity genericEntity = element.getValue();
				if (selectedValue.size() > 1) {
					ModelPathElement parentElement = selectedValue.get(selectedValue.size() - 2);
					if (parentElement.getType().isCollection()) {
						boolean enabled = removeFromCollectionAction == null ? false : removeFromCollectionAction.isEnabled(modelPaths);
						if (!enabled)
							continue;
						
						removingFromCollection = true;
					} else {
						boolean enabled = clearPropertyToNullAction == null ? false : clearPropertyToNullAction.isEnabled(modelPaths);
						if (!enabled)
							continue;
						
						clearingProperty = true;
					}
				}
				
				boolean entityDeletable = getMetaData(genericEntity).entity(genericEntity).useCase(gmContentView.getUseCase()).is(Deletable.T)
						&& GMEUtil.isOperationGranted(genericEntity, AclOperation.DELETE);
				if (entityDeletable) {
					selectionValid = true;
					entityElements.add(element);
				} else {
					selectionValid = false;
					continue;
				}
			}
			
			if (!selectionValid) {
				setHidden(true);
				return;
			}
		}
		
		if (entityElements.size() > 1 && clearingProperty)
			setHidden(true);
		else
			setHidden(entityElements.isEmpty());
		return;
	}
	
	private void checkForReferences() {
		NestedTransaction nestedTransaction = gmContentView.getGmSession().getTransaction().beginNestedTransaction();
		for (ModelPathElement entityElement : entityElements)
			checkForReferences(entityElement, nestedTransaction, entityElements.size());
	}
	
	private void checkForReferences(ModelPathElement entityElement, final NestedTransaction nestedTransaction, final int amount) {
		final GenericEntity entity = entityElement.getValue();
		
		if (entity == null) {
			if (nestedTransaction != null && ++amountCounter == amount)
				nestedTransaction.commit();
			return;
		}
		
		ActionPerformanceContext actionPerformanceContext = new ActionPerformanceContext();
		actionPerformanceContext.setMessage("...checking for references...");
		fireOnBeforePerformAction(actionPerformanceContext);
		
		gmContentView.getGmSession().query().entity(entity.reference()).references(AsyncCallback.of( //
				referencesResponse -> {
					fireOnAfterPerformAction(null);
					processDeletion(entity);
					if (nestedTransaction != null && ++amountCounter == amount)
						nestedTransaction.commit();
				}, e -> {
					fireOnAfterPerformAction(null);
					GlobalState.showError("Error while checkForReferences", e);
					if (nestedTransaction != null && ++amountCounter == amount)
						nestedTransaction.commit();
				}));
	}
	
	protected void processDeletion(GenericEntity entity) {
		if (removingFromCollection)
			removeFromCollectionAction.perform(null);
		else if (clearingProperty)
			clearPropertyToNullAction.perform(null);
		
		ActionPerformanceContext actionPerformanceContext = new ActionPerformanceContext();
		actionPerformanceContext.setMessage("...delete entity...");
		fireOnBeforePerformAction(actionPerformanceContext);
		gmContentView.getGmSession().deleteEntity(entity);
		/*gmSession.commit(new AsyncCallback<ManipulationResponse>() {			
			public void onSuccess(ManipulationResponse manipulationResponse) {
				fireOnAfterPerformAction(null);
				GlobalState.showSuccess("succesfully deleted entity");
			}
			
			public void onFailure(Throwable t) {
				fireOnAfterPerformAction(null);
				GlobalState.showError("Error while deleting entity", t);
			}
		});*/
	}
	
	private void fireListener() {
		if (listener != null)
			listener.onManipulationPerformed();
	}

}
