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
package com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.clipboard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.AbstractDependencyViewTab;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.AbstractDependencyViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionController;


/**
 * a container implementation of actions around clipboard features  
 * @author pit
 *
 */
public class ClipboardActionContainer extends AbstractDependencyViewActionContainer implements ViewActionContainer<AbstractDependencyViewTab>, ViewActionController<AbstractDependencyViewTab> {

	private Action standardCopyToClipboardAction;
	private Action enhancedCopyToClipboardAction;
	private Clipboard clipboard;

	@Override
	public ViewActionController<AbstractDependencyViewTab> create() {
		clipboard = new Clipboard(display);
		
		// copy to clipboard - standard 
		ImageDescriptor copyToClipboardImageDescriptor = ImageDescriptor.createFromFile( ClipboardContentsProviderCapable.class, "copy_edit.gif");
		standardCopyToClipboardAction = new Action("Copy to clipboard", copyToClipboardImageDescriptor) {
			
			@Override
			public void run() {			
				AbstractDependencyViewTab viewTab = activeTabProvider.provideActiveTab();
				if (viewTab instanceof ClipboardContentsProviderCapable) {
					ClipboardContentsProviderCapable provider = (ClipboardContentsProviderCapable) viewTab; 										
					try {
						String contents = provider.apply( ClipboardContentsProviderMode.standard);
						TextTransfer textTransfer = TextTransfer.getInstance();			
						clipboard.setContents( new String[] { contents}, new Transfer[] { textTransfer});
					} catch (RuntimeException e) {
						String msg ="cannot retrieve contents to copy to clipboard";
						ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
						ArtifactContainerPlugin.getInstance().log(status);				
					}														
				}
			}			
		};
		toolbarManager.add(standardCopyToClipboardAction);
		menuManager.add(standardCopyToClipboardAction);
		
		// copy to clipboard enhanced 
		enhancedCopyToClipboardAction = new Action("Enhanced Copy to clipboard", copyToClipboardImageDescriptor) {
			
			@Override
			public void run() {				
				AbstractDependencyViewTab viewTab = activeTabProvider.provideActiveTab();
				if (viewTab instanceof ClipboardContentsProviderCapable) {
					ClipboardContentsProviderCapable provider = (ClipboardContentsProviderCapable) viewTab; 										
					try {
						String contents = provider.apply( ClipboardContentsProviderMode.enhanced);
						TextTransfer textTransfer = TextTransfer.getInstance();			
						clipboard.setContents( new String[] { contents}, new Transfer[] { textTransfer});
					} catch (RuntimeException e) {
						String msg ="cannot retrieve contents to copy to clipboard";
						ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
						ArtifactContainerPlugin.getInstance().log(status);		
					}														
				}
			}			
		};
		menuManager.add( enhancedCopyToClipboardAction);
		return this;
	}

	@Override
	public void controlAvailablity(AbstractDependencyViewTab tab) {
		if (tab instanceof ClipboardContentsProviderCapable) {
			ClipboardContentsProviderCapable provider = (ClipboardContentsProviderCapable) tab;
			if (provider.supportsMode( ClipboardContentsProviderMode.standard)) {
				standardCopyToClipboardAction.setEnabled( true);
			} else {
				standardCopyToClipboardAction.setEnabled( false);
			}
			if (provider.supportsMode( ClipboardContentsProviderMode.enhanced)) {
				enhancedCopyToClipboardAction.setEnabled( true);
			} else {
				enhancedCopyToClipboardAction.setEnabled( false);
			}		
		} else {
			standardCopyToClipboardAction.setEnabled( false);
			enhancedCopyToClipboardAction.setEnabled( false);
		}			
	}
}
