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
import org.eclipse.core.runtime.IStatus;
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
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.ac.container.resolution.viewer.ContainerResolutionViewer;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;


/**
 * dynamic command to show the resolution of a project
 *  
 * @author pit
 *
 */
public class DynamicContainerResolutionCommandItem extends ContributionItem {
	private static Logger log = Logger.getLogger(DynamicContainerResolutionCommandItem.class);
	
	private UiSupport uiSupport = ArtifactContainerPlugin.instance().uiSupport();
	private Image image;
	private static boolean autoGenerate = true;
	
	public DynamicContainerResolutionCommandItem() {
		//ImageDescriptor dsc = org.eclipse.jface.resource.ImageDescriptor.createFromFile( DynamicContainerResolutionCommandItem.class, "insp_sbook.gif");
		//image = dsc.createImage();
		image = uiSupport.images().addImage( "ac-cmd-cont-resolution", DynamicContainerResolutionCommandItem.class, "insp_sbook.gif");
	}
	
	public DynamicContainerResolutionCommandItem(String id) {
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
		
		String token = DevrockPlugin.instance().getWorkspaceProjectView().getProjectDisplayName(selectedProject);
		

		MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
	    menuItem.setText("Analyze project's resolution of : " + token);
	    menuItem.setToolTipText( "Shows the classpath resolution the container is based on of the currently selected project : " + selectedProject.getName());
	    menuItem.setImage(  image);
	    
	    menuItem.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	            	try {
	            		ArtifactContainer containerOfProject = ArtifactContainerPlugin.instance().containerRegistry().getContainerOfProject(selectedProject);
	        			if (containerOfProject == null) {
	        				return;
	        			}
	        			AnalysisArtifactResolution resolution = containerOfProject.getCompileResolution();
	        			if (resolution == null) {
	        				if (autoGenerate) {
	        					String msg = "no resolution attached to container of project : " +selectedProject.getName() + ", retrying";
	        					log.warn(msg);
	        					ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
	        					ArtifactContainerPlugin.instance().log(status);
	        					
	        					containerOfProject.getClasspathEntries();
	        					resolution = containerOfProject.getCompileResolution();				
	        				}
	        				if (resolution == null) {											
	        					resolution = AnalysisArtifactResolution.T.create();
	        					resolution.setFailure( Reasons.build(NotFound.T).text( "no resolution attached to container of [" + selectedProject.getName() + "]").toReason());								
	        				}				
	        			}
	        			Display display = PlatformUI.getWorkbench().getDisplay();
	        			ContainerResolutionViewer resolutionViewer = new ContainerResolutionViewer( display.getActiveShell());
	        			resolutionViewer.setResolution( resolution);
	        			resolutionViewer.setProjectDependencies(containerOfProject.getProjectDependencies());
	        			resolutionViewer.preemptiveDataRetrieval();
	        			resolutionViewer.open();
					} catch (Exception e1) {
						DevrockPluginStatus status = new DevrockPluginStatus("cannot run analysis on project : " + selectedProject.getName(), e1);
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
