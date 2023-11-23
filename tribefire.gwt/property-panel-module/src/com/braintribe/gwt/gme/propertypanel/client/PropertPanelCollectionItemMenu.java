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

import java.util.Collections;
import java.util.List;

import com.braintribe.gwt.gmview.action.client.RemoveFromCollectionAction;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

public class PropertPanelCollectionItemMenu extends Menu {
	
	private final PropertyPanel propertyPanel;
	private MenuItem removeItem;
	private RemoveFromCollectionAction removeAction;
	protected PropertyModel menuPropertyModel;
	
	public PropertPanelCollectionItemMenu(final PropertyPanel propertyPanel) {
		this.propertyPanel = propertyPanel;
		this.setMinWidth(180);
		SelectionHandler<Item> selectionHandler = event -> {
			Item item = event.getSelectedItem();
			if (item == null)
				return;
			
			int index = PropertPanelCollectionItemMenu.this.getData("index");
			propertyPanel.localManipulation = true;
			
			if (item == removeItem) {
				ModelPath modelPath = PropertyPanel.getModelPath(menuPropertyModel);
				ModelPathElement collectionElement = propertyPanel.getCollectionItemPathElement(index, menuPropertyModel, true);	
				modelPath.add(collectionElement);
				
				List<List<ModelPath>> modelPaths = propertyPanel.transformSelection(Collections.singletonList(modelPath));
				removeAction.updateState(modelPaths);
				removeAction.perform(null);
			} 
		};
				
		if (!propertyPanel.readOnly) {
			removeAction = new RemoveFromCollectionAction();
			removeAction.configureGmContentView(propertyPanel);
			removeItem = new MenuItem(removeAction.getName(), removeAction.getIcon());
			removeItem.addSelectionHandler(selectionHandler);
			this.add(removeItem);
		}
	}
	
	protected void updateMenu() {
		if (menuPropertyModel == null)
			return;
		
		GenericModelType modelValueElementType = menuPropertyModel.getValueElementType();
		if (!propertyPanel.readOnly && removeItem != null)
			removeItem.setVisible(menuPropertyModel.isEditable() && !propertyPanel.readOnly && modelValueElementType.isCollection());
						
	}
			
	private void updateHelperMenuPropertyModel(Object newValue) {
		menuPropertyModel.getParentEntityType().getProperty(menuPropertyModel.getPropertyName()).set(menuPropertyModel.getParentEntity(), newValue);
	}
	
	private native boolean isComponentHidden(Component component) /*-{
		return component.@com.sencha.gxt.widget.core.client.Component::hidden;
	}-*/;

}
