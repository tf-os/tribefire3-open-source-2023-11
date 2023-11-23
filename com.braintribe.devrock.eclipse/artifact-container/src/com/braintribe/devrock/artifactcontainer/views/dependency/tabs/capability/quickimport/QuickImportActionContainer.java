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
package com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.quickimport;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.AbstractDependencyViewTab;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.AbstractDependencyViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionController;

public class QuickImportActionContainer extends AbstractDependencyViewActionContainer implements ViewActionContainer<AbstractDependencyViewTab>, ViewActionController<AbstractDependencyViewTab> {
	private Action quickImportAction;

	@Override
	public ViewActionController<AbstractDependencyViewTab> create() {
		ImageDescriptor quickimportImageDescriptor = ImageDescriptor.createFromFile( QuickImportCapable.class, "quickImport.png");
		quickImportAction = new Action("Import dependency via QuickImport", quickimportImageDescriptor) {

			@Override
			public void run() {				
				AbstractDependencyViewTab viewTab = activeTabProvider.provideActiveTab();
				if (viewTab instanceof QuickImportCapable) {
					QuickImportCapable loadingTab = (QuickImportCapable) viewTab;
					loadingTab.quickImportArtifact();
				}
			}			
		};
		toolbarManager.add(quickImportAction);
		menuManager.add(quickImportAction);
				
		return this;
	}

	@Override
	public void controlAvailablity(AbstractDependencyViewTab tab) {
		if (tab instanceof QuickImportCapable) {
			quickImportAction.setEnabled(true);
		}
		else {
			quickImportAction.setEnabled(false);
		}		
	}

	
}
