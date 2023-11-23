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
package com.braintribe.plugin.commons.selection;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class SelectionServiceListener implements ISelectionListener {
	private ISelection currentSelection;
	private boolean attached;

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		//System.out.println( selection.getClass().getName());
		if (selection instanceof TreeSelection) {
			currentSelection = selection;
		}
	}
	
	public ISelection getSelection() {
		if (!attached) {
			attach();
			ISelectionService selectionService = getSelectionService();
			if (selectionService != null) {
				ISelection iSelection = selectionService.getSelection();
				if (iSelection instanceof TreeSelection) {
					currentSelection = iSelection;
				}
			}
		}
		return currentSelection;
	}
	
	private void attach() {
		if (attached)
			return;
		ISelectionService selectionService = getSelectionService();
		if (selectionService != null) {
			selectionService.addSelectionListener( this);
			attached = true;
		}		
	}

	private ISelectionService getSelectionService() {
		IWorkbench iworkbench = PlatformUI.getWorkbench();
		IWorkbenchWindow iworkbenchwindow = iworkbench.getActiveWorkbenchWindow();
		if (iworkbenchwindow != null) {
			ISelectionService selectionService = iworkbenchwindow.getSelectionService();
			return selectionService;
		}
		return null;
	}
	
	public void detach() {
		if (!attached) {
			return;
		}
		ISelectionService selectionService = getSelectionService();
		if (selectionService != null) {
			selectionService.removeSelectionListener(this);
		}
		attached = false;
	}

}
