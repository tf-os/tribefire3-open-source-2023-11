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
package com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.reposcan;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.AbstractDependencyViewTab;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.AbstractDependencyViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionController;

public class RepositoryScanActionContainer extends AbstractDependencyViewActionContainer implements ViewActionContainer<AbstractDependencyViewTab>, ViewActionController<AbstractDependencyViewTab>{

	private Action repositoryScanAction;
	
	@Override
	public ViewActionController<AbstractDependencyViewTab> create() {
		// run greyface 
		ImageDescriptor greyfaceImageDescriptor = ImageDescriptor.createFromFile( RepositoryScanCapable.class, "repositories.gif");
		repositoryScanAction = new Action("Import external dependency via Greyface", greyfaceImageDescriptor) {

			@Override
			public void run() {				
				AbstractDependencyViewTab viewTab = activeTabProvider.provideActiveTab();
				if (viewTab instanceof RepositoryScanCapable) {
					RepositoryScanCapable loadingTab = (RepositoryScanCapable) viewTab;
					loadingTab.scanRepository();
				}
			}			
		};
		toolbarManager.add(repositoryScanAction);
		menuManager.add(repositoryScanAction);
		return this;
	}

	@Override
	public void controlAvailablity(AbstractDependencyViewTab tab) {
		if (tab instanceof RepositoryScanCapable) {
			repositoryScanAction.setEnabled(true);
		}
		else {
			repositoryScanAction.setEnabled(false);
		}		
	}
	
	

}
