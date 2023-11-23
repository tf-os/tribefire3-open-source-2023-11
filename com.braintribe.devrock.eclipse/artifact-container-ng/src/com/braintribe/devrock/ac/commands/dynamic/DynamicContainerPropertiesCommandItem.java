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
package com.braintribe.devrock.ac.commands.dynamic;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.ac.container.ArtifactContainer;
import com.braintribe.devrock.ac.container.dialog.ContainerInfoDialog;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.logging.Logger;


/**
 * dynamic command to show the properties of a selected project's container
 * @author pit
 *
 */
public class DynamicContainerPropertiesCommandItem extends ContributionItem {
	private static Logger log = Logger.getLogger(DynamicContainerPropertiesCommandItem.class);
	
	private UiSupport uisupport = ArtifactContainerPlugin.instance().uiSupport();
	private Image image;
	
	public DynamicContainerPropertiesCommandItem() {
		//ImageDescriptor dsc = org.eclipse.jface.resource.ImageDescriptor.createFromFile( DynamicContainerPropertiesCommandItem.class, "library_obj.gif");
		//image = dsc.createImage();
		image = uisupport.images().addImage( "ac-cmd-cont-properties", DynamicContainerPropertiesCommandItem.class, "library_obj.gif");
	}
	
	public DynamicContainerPropertiesCommandItem(String id) {
		super( id);
	}
	
	@Override
	public void fill(Menu menu, int index) {
		long before = System.currentTimeMillis();
		// retrieve last used copy mode
		IProject selectedProject = SelectionExtracter.currentProject();
		if (selectedProject == null) {
			return;
		}

		MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
	    menuItem.setText("Show the container properties of : " + selectedProject.getName());
	    menuItem.setToolTipText( "Shows the container properties of the currently selected project : " + selectedProject.getName());
	    menuItem.setImage(  image);
	    
	    menuItem.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	            	try {
	        			ArtifactContainer containerOfProject = ArtifactContainerPlugin.instance().containerRegistry().getContainerOfProject(selectedProject);
	        			if (containerOfProject == null) {
	        				return;
	        			}
	        			Display display = PlatformUI.getWorkbench().getDisplay();
	        			ContainerInfoDialog dialog = new ContainerInfoDialog( display.getActiveShell());
	        			dialog.setContainer(containerOfProject);
	        			dialog.setProject( selectedProject);
	        			dialog.open();
					} catch (Exception e1) {
						DevrockPluginStatus status = new DevrockPluginStatus("can not open the container properties viewer on project : " + selectedProject.getName(), e1);
						DevrockPlugin.instance().log(status);
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
