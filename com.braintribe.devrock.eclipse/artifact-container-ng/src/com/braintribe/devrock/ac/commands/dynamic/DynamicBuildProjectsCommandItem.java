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

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.ac.container.updater.ProjectUpdater;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectView;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.logging.Logger;


/**
 * dynamic command to show the properties of a selected project's container
 * @author pit
 *
 */
public class DynamicBuildProjectsCommandItem extends ContributionItem {
	private static Logger log = Logger.getLogger(DynamicBuildProjectsCommandItem.class);
	
	private UiSupport uisupport = ArtifactContainerPlugin.instance().uiSupport();	
	private Image image;
	
	public DynamicBuildProjectsCommandItem() {
		//ImageDescriptor dsc = org.eclipse.jface.resource.ImageDescriptor.createFromFile( DynamicBuildProjectsCommandItem.class, "refresh-classpath.png");
		image = uisupport.images().addImage( "ac-cmd-build-prjs", DynamicBuildProjectsCommandItem.class, "refresh-classpath.png");		
	}
	
	public DynamicBuildProjectsCommandItem(String id) {
		super( id);
	}
	
	@Override
	public void fill(Menu menu, int index) {	
		long before = System.currentTimeMillis();
		Set<IProject> selectedProjects = SelectionExtracter.selectedProjects( SelectionExtracter.currentSelection());
		if (selectedProjects == null || selectedProjects.size() == 0)
			return;
		
		WorkspaceProjectView workspaceProjectView = DevrockPlugin.instance().getWorkspaceProjectView();
		MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
		if (selectedProjects.size() > 1) {
			menuItem.setText("Refresh the classpath containers of (" + selectedProjects.size() + " selected projects)");			
			String txt = selectedProjects.stream().map(ip -> workspaceProjectView.getProjectDisplayName(ip)).collect( Collectors.joining(", "));
			menuItem.setToolTipText( "Refreshes the classpath containers of the following projects: " + txt);
		}
		else {			
			IProject ip = selectedProjects.stream().findFirst().orElse(null);			
			String token = workspaceProjectView.getProjectDisplayName(ip);			
			menuItem.setText("Refresh the classpath containers of the selected project: " + token);
			menuItem.setToolTipText( "Refreshes the classpath containers of the following project: " + token);
		}
		
	    menuItem.setImage(  image);
	    
	    menuItem.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	            	try {	        			
	            		ProjectUpdater updater = new ProjectUpdater();
	        			updater.setSelectedProjects( selectedProjects);
	        			updater.runAsJob();
					} catch (Exception e1) {
						ArtifactContainerStatus status = new ArtifactContainerStatus("cannot run refresh on projects", e1);
						ArtifactContainerPlugin.instance().log(status);
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
	    	ArtifactContainerStatus status = new ArtifactContainerStatus( "dynamic command took too long to setup [" + delay + " ms] :" + getClass().getName(), IStatus.WARNING);
	    	ArtifactContainerPlugin.instance().log(status);	
	    }
	}

	@Override
	public void dispose() {
		//image.dispose();
		super.dispose();
	}
	
	
	
	

}
