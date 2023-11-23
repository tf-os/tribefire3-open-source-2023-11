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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.action.adapter.gxt.client.MenuItemActionAdapter;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.MultilineAction;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.TriggerFieldAction;
import com.braintribe.gwt.gme.propertypanel.client.action.ChangeBasedTypeToExistingAction;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmview.action.client.ChangeInstanceAction;
import com.braintribe.gwt.gmview.action.client.CopyTextAction;
import com.braintribe.gwt.gmview.action.client.GIMADialogOpenerAction;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.data.constraint.MinLength;
import com.braintribe.model.meta.data.prompt.WorkWithVisible;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;

public class PropertyPanelHelperMenu extends Menu {
	
	private final PropertyPanel propertyPanel;
	private GIMADialogOpenerAction gimaDialogOpenerAction;
	private MenuItem workWithItem;
	private ChangeBasedTypeToExistingAction changeBasedTypeToExistingAction;
	private MenuItem changeInstanceItem;
	private ChangeInstanceAction changeInstanceAction;
	private MenuItem setNullItem;
	private MenuItem resetBooleanItem;
	private MenuItem clearCollectionItem;
	private MenuItem clearStringItem;
	private final CopyTextAction copyTextAction;
	private MultilineAction multilineAction = null;
	private final MenuItem emptyMenuItem;
	//private final MenuItem multilineItem;
	protected PropertyModel helperMenuPropertyModel;
	
	public PropertyPanelHelperMenu(final PropertyPanel propertyPanel) {
		this.propertyPanel = propertyPanel;
		this.setMinWidth(180);
		SelectionHandler<Item> selectionHandler = event -> {
			Item item = event.getSelectedItem();
			if (item == null)
				return;
			
			if (item == setNullItem || item == resetBooleanItem) {
				updateHelperMenuPropertyModel(null);
				propertyPanel.localManipulation = true;
			} else if (item == clearCollectionItem) {
				Object collection = helperMenuPropertyModel.getValue();
				if (collection instanceof Collection)
					((Collection<?>) collection).clear();
				else
					((Map<?,?>) collection).clear();
				propertyPanel.localManipulation = true;
			} else if (item == clearStringItem) {
				updateHelperMenuPropertyModel("");
				propertyPanel.localManipulation = true;
			} else if (item == workWithItem) {
				propertyPanel.localManipulation = false;
				if (helperMenuPropertyModel.getValueElementType().isCollection()) {
					//if (!((CollectionType) helperMenuPropertyModel.getValueElementType()).getCollectionElementType().getJavaType().equals(String.class))
						propertyPanel.fireCollectionPropertySelected(helperMenuPropertyModel);
					//else
						//propertyPanel.fireStringPropertySelected(helperMenuPropertyModel, -1);
				} else if (helperMenuPropertyModel.getValueElementType().isEntity())
					propertyPanel.fireEntityPropertySelected(helperMenuPropertyModel);
				else if (helperMenuPropertyModel.getValueElementType().getJavaType().equals(String.class))
					propertyPanel.fireStringPropertySelected(helperMenuPropertyModel, -1);
			} else if (item == changeInstanceItem)
				changeInstanceAction.perform(null);
		};
		
		if (propertyPanel.navigationEnabled) {
			workWithItem = new MenuItem(LocalizedText.INSTANCE.workWith(), GmViewActionResources.INSTANCE.open());
			workWithItem.addSelectionHandler(selectionHandler);
			workWithItem.setToolTip(LocalizedText.INSTANCE.workWithDescription());
			this.add(workWithItem);
		}
		
		if (!propertyPanel.readOnly) {
			if (propertyPanel.actionManager == null) {
				gimaDialogOpenerAction = new GIMADialogOpenerAction(true);
				gimaDialogOpenerAction.configureGmContentView(propertyPanel);
				MenuItem gimaOpenerItem = new MenuItem();
				MenuItemActionAdapter.linkActionToMenuItem(gimaDialogOpenerAction, gimaOpenerItem);
				this.add(gimaOpenerItem);
				
				changeBasedTypeToExistingAction = new ChangeBasedTypeToExistingAction(propertyPanel, propertyPanel.selectionFutureProviderProvider);
				changeBasedTypeToExistingAction.configureListener(() -> propertyPanel.localManipulation = true);
				MenuItem changeBasedTypeToExistingItem = new MenuItem();
				MenuItemActionAdapter.linkActionToMenuItem(changeBasedTypeToExistingAction, changeBasedTypeToExistingItem);
				this.add(changeBasedTypeToExistingItem);
				
				changeInstanceItem = new MenuItem(LocalizedText.INSTANCE.assign(), PropertyPanelResources.INSTANCE.changeExisting());
				changeInstanceItem.addSelectionHandler(selectionHandler);
				changeInstanceAction = new ChangeInstanceAction();
				changeInstanceAction.setInstanceSelectionFutureProvider(propertyPanel.selectionFutureProviderProvider);
				changeInstanceAction.configureGmContentView(propertyPanel);
				changeInstanceAction.configureListener(() -> propertyPanel.localManipulation = true);
				this.add(changeInstanceItem);
				
				setNullItem = new MenuItem(LocalizedText.INSTANCE.setNull(), GmViewActionResources.INSTANCE.remove());
				setNullItem.addSelectionHandler(selectionHandler);
				setNullItem.setToolTip(LocalizedText.INSTANCE.setPropertyToNull());
				this.add(setNullItem);
				
				clearCollectionItem = new MenuItem(LocalizedText.INSTANCE.clearCollection(), GmViewActionResources.INSTANCE.remove());
				clearCollectionItem.addSelectionHandler(selectionHandler);
				clearCollectionItem.setToolTip(LocalizedText.INSTANCE.clearCollectionDescription());
				this.add(clearCollectionItem);
			}
			
			resetBooleanItem = new MenuItem(LocalizedText.INSTANCE.setNull(), GmViewActionResources.INSTANCE.remove());
			resetBooleanItem.addSelectionHandler(selectionHandler);
			resetBooleanItem.setToolTip(LocalizedText.INSTANCE.setPropertyToNull());
			this.add(resetBooleanItem);
			
			clearStringItem = new MenuItem(LocalizedText.INSTANCE.clearText(), PropertyPanelResources.INSTANCE.clearString());
			clearStringItem.addSelectionHandler(selectionHandler);
			clearStringItem.setToolTip(LocalizedText.INSTANCE.clearTextDescription());
			this.add(clearStringItem);
		}
		
		copyTextAction = new CopyTextAction();
		copyTextAction.setCodecRegistry(propertyPanel.codecRegistry);
		copyTextAction.configureGmContentView(propertyPanel);
		
		MenuItem copyTextItem = new MenuItem();
		MenuItemActionAdapter.linkActionToMenuItem(copyTextAction, copyTextItem);
		this.add(copyTextItem);
		
		multilineAction = new MultilineAction();
		multilineAction.setCodecRegistry(propertyPanel.codecRegistry);
		multilineAction.configureGmContentView(propertyPanel);
		multilineAction.setReadOnly(true);
				
		//multilineItem = new MenuItem();
		//MenuItemActionAdapter.linkActionToMenuItem(multilineAction, multilineItem);
		//this.add(multilineItem);
		
		emptyMenuItem = new MenuItem(LocalizedText.INSTANCE.noOptionsAvailable());
		emptyMenuItem.setEnabled(false);
		emptyMenuItem.setVisible(false);
		this.add(emptyMenuItem);
	}
	
	public Boolean fireMultilineAction(PropertyModel propertyModel, int index) {
		if (multilineAction == null)
			return false;
		
		PropertyModel model = (propertyModel != null) ? propertyModel : helperMenuPropertyModel;
		if (model == null)
			return false;
		
		ModelPath modelPath = PropertyPanel.getModelPath(model);
		if (index > -1 && propertyModel.getValueElementType().isCollection()) {
			ModelPathElement collectionElement = propertyPanel.getCollectionItemPathElement(index, propertyModel, true);
			if (collectionElement != null)
				modelPath.add(collectionElement);
		}		
		
		List<List<ModelPath>> uniqueModelPath = propertyPanel.transformSelection(Collections.singletonList(modelPath));		
		multilineAction.updateState(uniqueModelPath);
		multilineAction.perform(null);
		return true;
	}
	
	protected void updateMenu() {
		if (helperMenuPropertyModel == null)
			return;
		
		List<List<ModelPath>> uniqueModelPath = propertyPanel.transformSelection(Collections.singletonList(PropertyPanel.getModelPath(helperMenuPropertyModel)));
		
		GenericModelType valueElementType = helperMenuPropertyModel.getValueElementType();
		boolean workWithVisible = true;
		if (!propertyPanel.readOnly) {
			boolean allowEmptyString = false;
			if (!propertyPanel.skipMetadataResolution) {
				EntityMdResolver entityContextBuilder;
				if (helperMenuPropertyModel.getParentEntity() != null)
					entityContextBuilder = propertyPanel.getMetaData().entity(helperMenuPropertyModel.getParentEntity());
				else
					entityContextBuilder = propertyPanel.getMetaData().entityType(helperMenuPropertyModel.getParentEntityType());
				PropertyMdResolver propertyMdResolver = entityContextBuilder.property(helperMenuPropertyModel.getPropertyName());
				MinLength propertyEmptyString = propertyMdResolver.useCase(propertyPanel.getUseCase()).meta(MinLength.T).exclusive();
				if (propertyEmptyString != null)
					allowEmptyString = propertyEmptyString.getLength() == 0;
				propertyPanel.handleMetadataReevaluation(propertyMdResolver, MinLength.T);
				
				workWithVisible = propertyMdResolver.is(WorkWithVisible.T);
			}
			
			if (clearCollectionItem != null)
				clearCollectionItem.setVisible(helperMenuPropertyModel.isEditable() && !propertyPanel.readOnly && valueElementType.isCollection());
			
			if (setNullItem != null) {
				setNullItem.setVisible(helperMenuPropertyModel.getValue() != null && !helperMenuPropertyModel.getMandatory()
						&& helperMenuPropertyModel.isNullable() && !propertyPanel.readOnly && helperMenuPropertyModel.isEditable()
						&& LocalizedString.class != valueElementType.getJavaType() && !valueElementType.isCollection());
			}
			
			resetBooleanItem.setVisible(helperMenuPropertyModel.getValue() != null && !helperMenuPropertyModel.getMandatory()
					&& helperMenuPropertyModel.isNullable() && !propertyPanel.readOnly && helperMenuPropertyModel.isEditable()
					&& Boolean.class == valueElementType.getJavaType() && !valueElementType.isCollection());
			
			clearStringItem.setVisible(allowEmptyString && helperMenuPropertyModel.isEditable() && !propertyPanel.readOnly
					&& valueElementType.getJavaType() == String.class);
			
			if (changeBasedTypeToExistingAction != null) {
				changeBasedTypeToExistingAction
						.setHidden(!helperMenuPropertyModel.isBaseTyped() || !helperMenuPropertyModel.isEditable() || propertyPanel.readOnly);
			}
			
			if (gimaDialogOpenerAction != null)
				gimaDialogOpenerAction.updateState(uniqueModelPath);
			
			if (changeInstanceAction != null) {
				if (valueElementType.isEntity() && LocalizedString.class != valueElementType.getJavaType())
					changeInstanceAction.updateState(uniqueModelPath);
				else
					changeInstanceAction.setHidden(true);
				changeInstanceItem.setVisible(!changeInstanceAction.getHidden());
			}
		}
		
		if (workWithItem != null) {
			boolean visible = helperMenuPropertyModel.getValue() != null
					&& ((valueElementType.isEntity() && LocalizedString.class != valueElementType.getJavaType())
							|| isWorkWithValidForCollection(valueElementType, helperMenuPropertyModel.getValue())
							|| valueElementType.getJavaType().equals(String.class))
					&& helperMenuPropertyModel.getVirtualEnum() == null && helperMenuPropertyModel.getDynamicSelectList() == null;
			if (visible)
				visible = workWithVisible;
			
			workWithItem.setVisible(visible);
		}
		
		if (propertyPanel.triggerFieldActionModelMap != null) {
			TriggerFieldAction triggerFieldAction = propertyPanel.triggerFieldActionModelMap.get(helperMenuPropertyModel);
			if (triggerFieldAction != null) {

				GenericModelType elementType = helperMenuPropertyModel.getValueElementType();
				GenericModelType modelType = elementType.isCollection() ? ((CollectionType) elementType).getCollectionElementType() : elementType;
				//RVE - do not show TrigegrActionField Item for String types (Multiline), because have own Open Item (workWithItem) what do same for Strings
				if (!modelType.getJavaType().equals(String.class)) {								
					if (propertyPanel.triggerFieldActionItemMap == null || !propertyPanel.triggerFieldActionItemMap.containsKey(triggerFieldAction)) {
						MenuItem menuItem = new MenuItem();
						MenuItemActionAdapter.linkActionToMenuItem(triggerFieldAction.getTriggerFieldAction(), menuItem);
						if (propertyPanel.triggerFieldActionItemMap == null)
							propertyPanel.triggerFieldActionItemMap = new HashMap<>();
						propertyPanel.triggerFieldActionItemMap.put(triggerFieldAction, menuItem);
						this.add(menuItem);
					}
					
					triggerFieldAction.getTriggerFieldAction().setHidden(false);
					if (changeInstanceItem != null)
						changeInstanceItem.setVisible(false);
				}
			}
			
			propertyPanel.triggerFieldActionModelMap.entrySet().stream().filter(entry -> entry.getKey() != helperMenuPropertyModel)
					.forEach(entry -> entry.getValue().getTriggerFieldAction().setHidden(true));
		}
		
		if (copyTextAction != null)
			copyTextAction.updateState(uniqueModelPath);
		if (multilineAction != null)
			multilineAction.updateState(uniqueModelPath);
		
		handleSubMenuVisibility(this);
		
		boolean emptyMenu = true;
		for (int i = 0; i < this.getWidgetCount(); i++) {
			Widget menuItem = this.getWidget(i);
			if (menuItem != emptyMenuItem && menuItem instanceof Component && !isComponentHidden((Component) menuItem)) {
				emptyMenu = false;
				break;
			}
		}
		emptyMenuItem.setVisible(emptyMenu);
	}
	
	private boolean isWorkWithValidForCollection(GenericModelType type, Object value) {
		if (!type.isCollection() || value == null)
			return false;
		
		if (value instanceof Collection)
			return !((Collection<?>) value).isEmpty();
		if (value instanceof Map)
			return !(((Map<?,?>) value).isEmpty());
		
		return false;
	}
	
	private void handleSubMenuVisibility(Menu menu) {
		for (int i = 0; i < menu.getWidgetCount(); i++) {
			Widget menuItem = menu.getWidget(i);
			if (menuItem instanceof MenuItem && ((MenuItem) menuItem).getSubMenu() != null) {
				Menu subMenu = ((MenuItem) menuItem).getSubMenu();
				menuItem.setVisible(!isHideSubMenu(subMenu));
				handleSubMenuVisibility(subMenu);
			} else if (menuItem instanceof SeparatorMenuItem) {
				//RVE do not show separators inside the menu for edit lines
				menuItem.setVisible(false);
			}
				
		}
	}
	
	private boolean isHideSubMenu(Menu menu) {
		for (int i = 0; i < menu.getWidgetCount(); i++) {
			Widget menuItem = menu.getWidget(i);
			if (menuItem instanceof MenuItem && ((MenuItem) menuItem).getSubMenu() != null) {
				if (!isHideSubMenu(((MenuItem) menuItem).getSubMenu()))
					return false;
			} else if (menuItem instanceof Component && !isComponentHidden((Component) menuItem))
				return false;
		}
		
		return true;
	}
	
	private void updateHelperMenuPropertyModel(Object newValue) {
		helperMenuPropertyModel.getParentEntityType().getProperty(helperMenuPropertyModel.getPropertyName()).set(helperMenuPropertyModel.getParentEntity(), newValue);
	}
	
	private native boolean isComponentHidden(Component component) /*-{
		return component.@com.sencha.gxt.widget.core.client.Component::hidden;
	}-*/;

}
