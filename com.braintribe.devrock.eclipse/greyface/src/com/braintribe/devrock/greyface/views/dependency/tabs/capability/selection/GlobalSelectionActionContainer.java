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
package com.braintribe.devrock.greyface.views.dependency.tabs.capability.selection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.braintribe.devrock.greyface.view.tab.GenericViewTab;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.AbstractDependencyViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionController;

public class GlobalSelectionActionContainer extends AbstractDependencyViewActionContainer implements ViewActionContainer<GenericViewTab>, ViewActionController<GenericViewTab> {

	private Action selectAllAction;
	private Action deselectAllAction;
	
	@Override
	public ViewActionController<GenericViewTab> create() {
		ImageDescriptor selectAllImageDescriptor = ImageDescriptor.createFromFile( GlobalSelectionActionContainer.class, "check_selected.png");
		ImageDescriptor deselectAllImageDescriptor = ImageDescriptor.createFromFile( GlobalSelectionActionContainer.class, "check_unselected.png");

		selectAllAction = new Action("select all entries", selectAllImageDescriptor) {		
			@Override
			public void run() {				
				GenericViewTab viewTab = activeTabProvider.provideActiveTab();
				if (viewTab instanceof GlobalSelectionCapable) {
					GlobalSelectionCapable selectionCapable = (GlobalSelectionCapable) viewTab; 
					selectionCapable.selectAll();					
				}
			}
			
		};
		toolbarManager.add(selectAllAction);
		menuManager.add( selectAllAction);
		
		deselectAllAction = new Action("deselect all entries", deselectAllImageDescriptor) {		
			@Override
			public void run() {				
				GenericViewTab viewTab = activeTabProvider.provideActiveTab();
				if (viewTab instanceof GlobalSelectionCapable) {
					GlobalSelectionCapable selectionCapable = (GlobalSelectionCapable) viewTab; 
					selectionCapable.deselectAll();					
				}
			}
			
		};
		toolbarManager.add(deselectAllAction);
		menuManager.add( deselectAllAction);
		
		return this;
	}

	@Override
	public void controlAvailablity(GenericViewTab tab) {
		if (tab instanceof GlobalSelectionCapable) {
			selectAllAction.setEnabled(true);
			deselectAllAction.setEnabled(true);
		}
		else {
			selectAllAction.setEnabled( false);
			deselectAllAction.setEnabled( false);
		}
			
		
	}
	
	

}
