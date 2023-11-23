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
package com.braintribe.devrock.greyface.views.dependency.tabs.capability.pomloading;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.braintribe.devrock.greyface.view.tab.GenericViewTab;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.AbstractDependencyViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionController;

public class PomLoadingActionContainer extends AbstractDependencyViewActionContainer implements ViewActionContainer<GenericViewTab>, ViewActionController<GenericViewTab> {

	private Action pomLoadingAction;
	
	@Override
	public void controlAvailablity(GenericViewTab tab) {
		if (tab instanceof PomLoadingCapable) {
			pomLoadingAction.setEnabled( true);
		}
		else {
			pomLoadingAction.setEnabled(false);
		}
	}

	@Override
	public ViewActionController<GenericViewTab> create() {
		ImageDescriptor pomImageDescriptor = ImageDescriptor.createFromFile( PomLoadingActionContainer.class, "pom_obj.gif");
	

		pomLoadingAction = new Action("load pom", pomImageDescriptor) {		
			@Override
			public void run() {				
				GenericViewTab viewTab = activeTabProvider.provideActiveTab();
				if (viewTab instanceof PomLoadingCapable) {
					PomLoadingCapable clipboardCapable = (PomLoadingCapable) viewTab; 
					clipboardCapable.loadPom();					
				}
			}
			
		};
		toolbarManager.add(pomLoadingAction);
		menuManager.add( pomLoadingAction);
		return this;
	}

}
