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
package com.braintribe.devrock.greyface.views.dependency.tabs.capability.clipboard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import com.braintribe.devrock.greyface.view.tab.GenericViewTab;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.AbstractDependencyViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionController;

public class ClipboardCopyActionContainer extends AbstractDependencyViewActionContainer implements ViewActionContainer<GenericViewTab>, ViewActionController<GenericViewTab> {

	private Action copyToClipboardAction;
	private Clipboard clipboard;
	
	@Override
	public void controlAvailablity(GenericViewTab tab) {
		if (tab instanceof ClipboardCopyCapable) {
			copyToClipboardAction.setEnabled(true);
		}
		else {
			copyToClipboardAction.setEnabled(false);
		}		
	}

	@Override
	public ViewActionController<GenericViewTab> create() {
		clipboard = new Clipboard(display);
		
		ImageDescriptor clipboardCopyImageDescriptor = ImageDescriptor.createFromFile( ClipboardCopyActionContainer.class, "copy_edit.gif");
	
		copyToClipboardAction = new Action("copy contents to clipboard", clipboardCopyImageDescriptor) {		
			@Override
			public void run() {				
				GenericViewTab viewTab = activeTabProvider.provideActiveTab();
				if (viewTab instanceof ClipboardCopyCapable) {					
					ClipboardCopyCapable clipboardCapable = (ClipboardCopyCapable) viewTab; 
					String contents = clipboardCapable.copyContents();
					if (contents == null) {
						return;
					}
					TextTransfer textTransfer = TextTransfer.getInstance();			
					clipboard.setContents( new String[] { contents}, new Transfer[] { textTransfer});
				}
			}
			
		};
		toolbarManager.add(copyToClipboardAction);
		menuManager.add( copyToClipboardAction);
		return this;
	}

}
