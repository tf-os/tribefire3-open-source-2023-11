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
package com.braintribe.devrock.greyface.views.dependency.tabs.capability.expansion;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.braintribe.devrock.greyface.view.tab.GenericViewTab;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.AbstractDependencyViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionController;

/**
 * actions around expansion / condensation 
 * @author pit
 *
 */
public class ExpansionActionContainer extends AbstractDependencyViewActionContainer implements ViewActionContainer<GenericViewTab>, ViewActionController<GenericViewTab> {

	private Action condenseDisplayAction;
	private Action expandDisplayAction;
	
	@Override
	public ViewActionController<GenericViewTab> create() {
		ImageDescriptor expandAllImageDescriptor = ImageDescriptor.createFromFile( ViewExpansionCapable.class, "expand_all.gif");
		ImageDescriptor collapseAllImageDescriptor = ImageDescriptor.createFromFile( ViewExpansionCapable.class, "collapse_all.gif");

		expandDisplayAction = new Action("Expand display", expandAllImageDescriptor) {		
			@Override
			public void run() {				
				GenericViewTab viewTab = activeTabProvider.provideActiveTab();
				if (viewTab instanceof ViewExpansionCapable) {
					ViewExpansionCapable vec = (ViewExpansionCapable)viewTab; 
					vec.expand();
					expandDisplayAction.setEnabled( vec.isCondensed());
					condenseDisplayAction.setEnabled( !vec.isCondensed());
				}
			}
			
		};
		toolbarManager.add(expandDisplayAction);
		menuManager.add( expandDisplayAction);
		
		// condense display 
		condenseDisplayAction = new Action("Condense display", collapseAllImageDescriptor) {		
			@Override
			public void run() {				
				GenericViewTab viewTab = activeTabProvider.provideActiveTab(); 
				if (viewTab instanceof ViewExpansionCapable) {
					ViewExpansionCapable vec = (ViewExpansionCapable)viewTab; 
					vec.condense();
					expandDisplayAction.setEnabled( vec.isCondensed());
					condenseDisplayAction.setEnabled( !vec.isCondensed());
				}
			}					
		};		
		toolbarManager.add(condenseDisplayAction);
		menuManager.add( condenseDisplayAction);
		return this;
	}

	@Override
	public void controlAvailablity(GenericViewTab tab) {
		if (tab instanceof ViewExpansionCapable && tab.getItemCount() < 100) {
			boolean isCondensed = ((ViewExpansionCapable) tab).isCondensed();
			expandDisplayAction.setEnabled( isCondensed);
			condenseDisplayAction.setEnabled( !isCondensed);			
		} else {
			expandDisplayAction.setEnabled( false);
			condenseDisplayAction.setEnabled( false);			
		}
	}	
}
