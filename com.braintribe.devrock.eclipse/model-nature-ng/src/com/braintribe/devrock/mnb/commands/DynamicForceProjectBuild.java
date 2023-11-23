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
package com.braintribe.devrock.mnb.commands;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.braintribe.devrock.api.nature.NatureHelper;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectView;
import com.braintribe.devrock.mnb.commands.builder.WorkspaceModelDeclarationUpdater;
import com.braintribe.devrock.mnb.plugin.ModelBuilderPlugin;
import com.braintribe.devrock.mnb.plugin.ModelBuilderStatus;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.logging.Logger;


/**
 * dynamic command activate the builder on the model-projects within the currently selected projects in the workspace
 * 
 * @author pit
 *
 */
public class DynamicForceProjectBuild extends ContributionItem {
	private static Logger log = Logger.getLogger(DynamicForceProjectBuild.class);
	private Image image;
	private UiSupport uisupport = ModelBuilderPlugin.instance().uiSupport();
	
	public DynamicForceProjectBuild() {
		//ImageDescriptor dsc = org.eclipse.jface.resource.ImageDescriptor.createFromFile( DynamicForceProjectBuild.class, "model.png");
		//image = dsc.createImage();
		image = uisupport.images().addImage("mb-cmd-build", DynamicForceProjectBuild.class, "model.png"); 
	}
	
	public DynamicForceProjectBuild(String id) {
		super( id);
	}
	
	@Override
	public void fill(Menu menu, int index) {
		long before = System.currentTimeMillis();
		ISelection currentSelection = SelectionExtracter.currentSelection();
		if (currentSelection == null || currentSelection.isEmpty()) 
			return;
		
		Set<IProject> selectedProjects = SelectionExtracter.selectedProjects(currentSelection);
		
		List<IProject> modelProjects = selectedProjects.stream().filter( p -> NatureHelper.isModelArtifact(p)).collect(Collectors.toList());
		
		if (modelProjects == null || modelProjects.size() == 0)
			return;
								
		
		WorkspaceProjectView workspaceProjectView = DevrockPlugin.instance().getWorkspaceProjectView();
		
		MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
	    String modelNames = modelProjects.stream().map( p -> workspaceProjectView.getProjectDisplayName(p)).collect(Collectors.joining(","));
		menuItem.setText("Force model declaration build on (" + modelProjects.size() + ") selected models");
	    menuItem.setToolTipText( "Triggers a model-declaration build on the selected project(s) : " + modelNames);
	    menuItem.setImage(  image);
	    
	    menuItem.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	            	WorkspaceModelDeclarationUpdater updater = new WorkspaceModelDeclarationUpdater( modelProjects);
	            	updater.runAsJob();
	            }
	        });		
	    long after = System.currentTimeMillis();
	    long delay = after - before;

	    if (log.isDebugEnabled()) {
			log.debug( getClass().getName() + " : " + delay  + "ms");
	    }

	    long maxDelay = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_DYNAMIC_COMMAND_MAX_DELAY, StorageLockerSlots.DEFAULT_DYNAMIC_COMMAND_MAX_DELAY);
	    if (delay > maxDelay) {
	    	ModelBuilderStatus status = new ModelBuilderStatus( "dynamic command took too long to setup [" + delay + " ms] :" + getClass().getName(), IStatus.WARNING);
	    	ModelBuilderPlugin.instance().log(status);	
	    }
	}

	@Override
	public void dispose() {
		//image.dispose();
		super.dispose();
	}

}
