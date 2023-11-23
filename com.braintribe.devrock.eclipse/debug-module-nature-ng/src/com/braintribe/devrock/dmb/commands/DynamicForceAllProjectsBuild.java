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
package com.braintribe.devrock.dmb.commands;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.braintribe.devrock.api.nature.NatureHelper;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.bridge.eclipse.workspace.BasicWorkspaceProjectInfo;
import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectView;
import com.braintribe.devrock.dmb.builder.WorkspaceDebugModuleClasspathUpdater;
import com.braintribe.devrock.dmb.plugin.DebugModuleBuilderPlugin;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.logging.Logger;


/**
 * dynamic command to add the model nature
 * 
 * @author pit
 *
 */
public class DynamicForceAllProjectsBuild extends ContributionItem {
	private static Logger log = Logger.getLogger(DynamicForceAllProjectsBuild.class);
	private Image image;
	private UiSupport uiSupport = DebugModuleBuilderPlugin.instance().uiSupport();
	
	public DynamicForceAllProjectsBuild() {
		//ImageDescriptor dsc = org.eclipse.jface.resource.ImageDescriptor.createFromFile( DynamicForceAllProjectsBuild.class, "module.carrier.png");
		//image = dsc.createImage();
		image = uiSupport.images().addImage( "dmb-cmd-build-all", DynamicForceAllProjectsBuild.class, "module.carrier.png");
	}
	
	public DynamicForceAllProjectsBuild(String id) {
		super( id);
	}
	
	@Override
	public void fill(Menu menu, int index) {
		long before = System.currentTimeMillis();
		Map<IProject,BasicWorkspaceProjectInfo> projectsInWorkspace = DevrockPlugin.instance().getWorkspaceProjectView().getProjectsInWorkspace();
		if (projectsInWorkspace == null || projectsInWorkspace.size() == 0)
			return;
		
		List<IProject> debugModuleProjects = projectsInWorkspace.keySet().stream().filter( p -> NatureHelper.isDebugModuleArtifact(p)).collect(Collectors.toList());		
		if (debugModuleProjects == null || debugModuleProjects.size() == 0)
			return;
						
		
		WorkspaceProjectView workspaceProjectView = DevrockPlugin.instance().getWorkspaceProjectView();
		
		MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
	    String modelNames = debugModuleProjects.stream().map( p -> workspaceProjectView.getProjectDisplayName(p)).collect(Collectors.joining(","));
		menuItem.setText("Force debug-module classpath build on (" + debugModuleProjects.size() + ") debug module projects");
	    menuItem.setToolTipText( "Triggers a debug-module classpath build on the project(s) of the workspace: " + modelNames);
	    menuItem.setImage(  image);
	    
	    menuItem.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	            	WorkspaceDebugModuleClasspathUpdater updater = new WorkspaceDebugModuleClasspathUpdater( debugModuleProjects);
	            	updater.runAsJob();
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
