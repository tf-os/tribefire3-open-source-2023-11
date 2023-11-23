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

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.braintribe.devrock.api.clipboard.ArtifactToClipboardExpert;
import com.braintribe.devrock.api.selection.EnhancedSelectionExtracter;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.commands.DependencyToClipboardCopyCommand;
import com.braintribe.devrock.eclipse.model.actions.VersionModificationAction;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.eclipse.model.identification.RemoteCompiledDependencyIdentification;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.logging.Logger;


public class DynamicDependencyCopyCommandItem extends ContributionItem {	
	private static Logger log = Logger.getLogger(DynamicDependencyCopyCommandItem.class);
	private Image image;
	private Clipboard clipboard;
	private UiSupport uisupport =  DevrockPlugin.instance().uiSupport();
	
	public DynamicDependencyCopyCommandItem() {
		//ImageDescriptor dsc = org.eclipse.jface.resource.ImageDescriptor.createFromFile( DynamicDependencyCopyCommandItem.class, "copyToClipboard.png");
		image = uisupport.images().addImage("cmd-dep-copy",  DynamicDependencyCopyCommandItem.class, "copyToClipboard.png"); 		 		
	}
	
	public DynamicDependencyCopyCommandItem(String id) {
		super( id);
	}
	

	@Override
	public void fill(Menu menu, int index) {
		long before = System.currentTimeMillis();
		// retrieve last used copy mode
		VersionModificationAction copyMode = DevrockPlugin.envBridge().storageLocker().getValue( DependencyClipboardRelatedHelper.STORAGE_SLOT_COPY_MODE, VersionModificationAction.referenced); 

    	ISelection selection = SelectionExtracter.currentSelection();
		// get selected container entry 
		List<EnhancedCompiledArtifactIdentification> identifiedArtifacts = EnhancedSelectionExtracter.extractEitherJarEntriesOrOwnerArtifacts(selection);

		String namesToBeCopied = identifiedArtifacts.stream().map( ecai -> ecai.asString()).collect(Collectors.joining(","));
		int num = identifiedArtifacts.size();
		
		String text;
		String tooltip;
		if (num > 1) {
			text = "Copy " + num + " dependency declarations " + DependencyClipboardRelatedHelper.getAppropriateActionLabelRepresentation(copyMode);
			tooltip = "Copy " + num + " dependency declarations " + DependencyClipboardRelatedHelper.getAppropriateActionLabelRepresentation(copyMode) + "\n" + namesToBeCopied;
		}
		else {
			text = "Copy dependency declaration of " + namesToBeCopied + ", " + DependencyClipboardRelatedHelper.getAppropriateActionLabelRepresentation(copyMode);
			tooltip = "Copy " + num + " dependency declaration, " + DependencyClipboardRelatedHelper.getAppropriateActionLabelRepresentation(copyMode) + ":" + namesToBeCopied;
		}
		
		MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
	    menuItem.setText(text);
	    menuItem.setToolTipText( tooltip);
	    menuItem.setImage(  image);
	    
	    menuItem.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	            	DependencyToClipboardCopyCommand cmd = new DependencyToClipboardCopyCommand();
	            	cmd.process( null);
	            		        		
	        		if (identifiedArtifacts.size() > 0) {
	        			if (clipboard != null) {
	        				clipboard.dispose();
	        			}
	        			List<RemoteCompiledDependencyIdentification> rcdis = identifiedArtifacts.stream().map( ecai -> RemoteCompiledDependencyIdentification.from( ecai)).collect(Collectors.toList());
	        			clipboard = ArtifactToClipboardExpert.copyToClipboard( copyMode, rcdis);
	        		}
	        		else {
	        			DevrockPluginStatus status = new DevrockPluginStatus( "cannot identify any artifacts from selection", IStatus.WARNING);
	        			DevrockPlugin.instance().log(status);	
	        		}	        		
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
