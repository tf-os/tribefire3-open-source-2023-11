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
package com.braintribe.devrock.mj.commands.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.braintribe.devrock.api.nature.NatureHelper;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.mj.natures.GwtLibraryNature;
import com.braintribe.devrock.mj.plugin.MungoJerryPlugin;
import com.braintribe.devrock.mj.plugin.MungoJerryStatus;
import com.braintribe.logging.Logger;


/**
 * dynamic command to add MJ's GWT-library nature on the selected project
 * 
 * @author pit
 *
 */
public class DynamicProjectAddGwtLibraryNatureCommandItem extends ContributionItem {
	private static Logger log = Logger.getLogger(DynamicProjectAddGwtLibraryNatureCommandItem.class);
	private Image image;
	private UiSupport uisupport = MungoJerryPlugin.instance().uiSupport();
	
	
	public DynamicProjectAddGwtLibraryNatureCommandItem() {
		//ImageDescriptor dsc = org.eclipse.jface.resource.ImageDescriptor.createFromFile( DynamicProjectAddGwtLibraryNatureCommandItem.class, "gwt-logo2.png");
		//image = dsc.createImage();
		image = uisupport.images().addImage( "mj-add-lib-nature", DynamicProjectAddGwtLibraryNatureCommandItem.class, "gwt-logo2.png");		
	}
	
	public DynamicProjectAddGwtLibraryNatureCommandItem(String id) {
		super( id);
	}
	
	@Override
	public void fill(Menu menu, int index) {
		long before = System.currentTimeMillis();
		IProject project = SelectionExtracter.currentProject();
		if (project == null) {
			return;
		}

		MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
	    menuItem.setText("Add the GWT library nature to : " + project.getName());
	    menuItem.setToolTipText( "Adds the GWT library nature tp the currently selected project : " + project.getName());
	    menuItem.setImage(  image);
	    
	    menuItem.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {	            
	            	if (!NatureHelper.addNature(project, GwtLibraryNature.NATURE_ID)) {
	    				MungoJerryStatus status = new MungoJerryStatus("cannot attach nature [" + GwtLibraryNature.NATURE_ID + "] to project [" + project.getName() + "]", IStatus.ERROR);
	    				MungoJerryPlugin.instance().log(status);
	    			}
	            }
	        });		
	    if (log.isDebugEnabled()) {
	    	long after = System.currentTimeMillis();
	    	log.debug( getClass().getName() + " : " + (after - before));
	    }
	}

	@Override
	public void dispose() {
		//image.dispose();
		super.dispose();
	}

}
