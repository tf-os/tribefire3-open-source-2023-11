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
package com.braintribe.devrock.commands.dynamic;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.commands.DependencyFromClipboardPasteCommand;
import com.braintribe.devrock.eclipse.model.actions.VersionModificationAction;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.logging.Logger;

public class DynamicDependencyPasteCommandItem extends ContributionItem {
	private static Logger log = Logger.getLogger(DynamicDependencyPasteCommandItem.class);
	private Image image;
	private UiSupport uisupport = DevrockPlugin.instance().uiSupport();
	
	public DynamicDependencyPasteCommandItem() {
		//ImageDescriptor dsc = org.eclipse.jface.resource.ImageDescriptor.createFromFile( DynamicDependencyPasteCommandItem.class, "pasteFromClipboard.png");
		//image = dsc.createImage();
		image = uisupport.images().addImage( "cmd-dep-paste", DynamicDependencyPasteCommandItem.class, "pasteFromClipboard.png");
	}
	
	public DynamicDependencyPasteCommandItem(String id) {
		super( id);
	}
	

	@Override
	public void fill(Menu menu, int index) {
		long before = System.currentTimeMillis();
		VersionModificationAction pasteMode   = DevrockPlugin.envBridge().storageLocker().getValue( DependencyClipboardRelatedHelper.STORAGE_SLOT_PASTE_MODE, VersionModificationAction.referenced);
				
		IProject target = SelectionExtracter.currentProject();
		if (target == null) {			
			return;
		}
		
		MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
	    menuItem.setText("Paste dependency declaration(s) to " + target.getName() + " " + DependencyClipboardRelatedHelper.getAppropriateActionLabelRepresentation(pasteMode));
	    menuItem.setToolTipText( "Paste the dependency declaration(s) from the clipboard into the pom of " +  target.getName() + " " + DependencyClipboardRelatedHelper.getAppropriateActionTooltipRepresentation(pasteMode));
	    menuItem.setImage(  image);
	    
	    menuItem.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {	            	
	            	DependencyFromClipboardPasteCommand cmd = new DependencyFromClipboardPasteCommand( target);
	            	cmd.process( pasteMode);	            		            	
	            }
	        });
	    
	    long after = System.currentTimeMillis();
	    long delay = after - before;

	    if (log.isDebugEnabled()) {
			log.debug( getClass().getName() + " : " + delay  + "ms");
	    }

	    long maxDelay = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_DYNAMIC_COMMAND_MAX_DELAY, StorageLockerSlots.DEFAULT_DYNAMIC_COMMAND_MAX_DELAY);
	    if (delay > maxDelay) {
			DevrockPluginStatus status = new DevrockPluginStatus( "dynamic command took too long to setup [" + delay + " ms] :" + getClass().getName(), IStatus.WARNING);
			DevrockPlugin.instance().log(status);	
	    }
	}

	@Override
	public void dispose() {
		//image.dispose();
		super.dispose();
	}
	
	
	
	

}
