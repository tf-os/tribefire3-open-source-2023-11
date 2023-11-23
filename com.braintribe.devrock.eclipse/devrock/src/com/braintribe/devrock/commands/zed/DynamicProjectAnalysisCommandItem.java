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
package com.braintribe.devrock.commands.zed;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
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
import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectView;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.logging.Logger;


/**
 * dynamic command to run zed on a selected project
 * 
 * @author pit
 *
 */
public class DynamicProjectAnalysisCommandItem extends ContributionItem implements ZedRunnerTrait {
	private static Logger log = Logger.getLogger(DynamicProjectAnalysisCommandItem.class);
	private Image image;
	private UiSupport uiSupport = DevrockPlugin.instance().uiSupport();
	
	public DynamicProjectAnalysisCommandItem() {
		//ImageDescriptor dsc = org.eclipse.jface.resource.ImageDescriptor.createFromFile( DynamicProjectAnalysisCommandItem.class, "report_obj.gif");
		//image = dsc.createImage();
		image = uiSupport.images().addImage( "cmd-zed-prj", DynamicProjectAnalysisCommandItem.class, "report_obj.gif");
	}
	
	public DynamicProjectAnalysisCommandItem(String id) {
		super( id);
	}
	
	@Override
	public void fill(Menu menu, int index) {
		long before = System.currentTimeMillis();
		IProject project = SelectionExtracter.currentProject();
		if (project == null) {
			return;
		}
		
		WorkspaceProjectView workspaceProjectView = DevrockPlugin.instance().getWorkspaceProjectView();

		MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
		
		String token = workspaceProjectView.getProjectDisplayName(project);
	    menuItem.setText("Run zed's analysis on project : " + token);
	    menuItem.setToolTipText( "Runs zed's analysis on currently selected project : " + token + "(" + project.getName() +")");
	    menuItem.setImage(  image);
	    
	    menuItem.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	            	try {
						ZedRunnerTrait.process(project);
					} catch (CoreException e1) {
						DevrockPluginStatus status = new DevrockPluginStatus("cannot run zed's analysis on project : " + project.getName(), e1);
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
