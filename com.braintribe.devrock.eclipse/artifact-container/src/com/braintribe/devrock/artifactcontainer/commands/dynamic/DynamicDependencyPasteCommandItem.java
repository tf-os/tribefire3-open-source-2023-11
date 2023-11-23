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
package com.braintribe.devrock.artifactcontainer.commands.dynamic;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.commands.DependencyFromClipboardPasteCommand;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.VersionModificationAction;


public class DynamicDependencyPasteCommandItem extends ContributionItem {
	private Image image;
	
	public DynamicDependencyPasteCommandItem() {
		ImageDescriptor dsc = org.eclipse.jface.resource.ImageDescriptor.createFromFile( DynamicDependencyPasteCommandItem.class, "pasteFromClipboard.png");
		image = dsc.createImage();
	}
	
	public DynamicDependencyPasteCommandItem(String id) {
		super( id);
	}
	

	@Override
	public void fill(Menu menu, int index) {
		VersionModificationAction copyMode = ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getQuickImportPreferences().getLastDependencyPasteMode();
		MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
	    menuItem.setText("Paste dependency declaration(s) " + DependencyClipboardRelatedHelper.getAppropriateActionLabelRepresentation(copyMode));
	    menuItem.setToolTipText( "Paste the dependency declaration(s) from the clipboard into the selected pom " +  DependencyClipboardRelatedHelper.getAppropriateActionTooltipRepresentation(copyMode));
	    menuItem.setImage(  image);
	    
	    menuItem.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	            	DependencyFromClipboardPasteCommand cmd = new DependencyFromClipboardPasteCommand();
	            	cmd.process( null);
	            }
	        });		
	}

	@Override
	public void dispose() {
		image.dispose();
		super.dispose();
	}
	
	
	
	

}
