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

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkingSet;

import com.braintribe.devrock.api.selection.EnhancedSelectionExtracter;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.importer.ProjectImporter;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.version.FuzzyVersion;


/**
 * imports the projects currently selected jars (not projects, but JAR references from containers)
 * 
 * @author pit
 *
 */
public class DynamicJarImportCommandItem extends ContributionItem {
	private static Logger log = Logger.getLogger(DynamicJarImportCommandItem.class);
	private Image image;
	private UiSupport uisupport =  DevrockPlugin.instance().uiSupport();
	
	public DynamicJarImportCommandItem() {
		//ImageDescriptor dsc = org.eclipse.jface.resource.ImageDescriptor.createFromFile( DynamicJarImportCommandItem.class, "importJar.png");
		image = uisupport.images().addImage( "cmd-import", DynamicJarImportCommandItem.class, "importJar.png");
	}
	
	public DynamicJarImportCommandItem(String id) {
		super( id);
	}
	
	@Override
	public void fill(Menu menu, int index) {
		long before = System.currentTimeMillis();
		ISelection selection = SelectionExtracter.currentSelection();
		IWorkingSet workingSet = SelectionExtracter.selectedWorkingSet(selection);

		// get the jars selected - 
		List<EnhancedCompiledArtifactIdentification> selectedJars = EnhancedSelectionExtracter.extractSelectedJars( selection);
						
		if (selectedJars == null || selectedJars.size() == 0) {
			return;
		}
		
		List<EnhancedCompiledArtifactIdentification> toBeImported = new ArrayList<>( selectedJars.size());
		
		//  
		// find, filter and match the source-projects 
		//
		for (EnhancedCompiledArtifactIdentification jarEcai : selectedJars) {
			// find all projects with the same groupId/artifactId
			String expression = jarEcai.getGroupId() + ":" + jarEcai.getArtifactId();
			List<EnhancedCompiledArtifactIdentification> queried = DevrockPlugin.instance().quickImportController().runSourceArtifactQuery( expression);
			if (queried == null || queried.size() == 0) {
				String msg = "cannot import : the jar [" + jarEcai.asString() + "] has no corresponding project";						
				log.debug(msg);
				DevrockPluginStatus status = new DevrockPluginStatus(msg, IStatus.INFO);
				DevrockPlugin.instance().log(status);
				continue;
			}
			// match them, i.e. they need to match a fuzzied range from the selected jar 
			FuzzyVersion fv = FuzzyVersion.from(  jarEcai.getVersion());
			List<EnhancedCompiledArtifactIdentification> matching = queried.stream().filter( q-> fv.matches( q.getVersion())).collect(Collectors.toList());
			matching.sort( new Comparator<EnhancedCompiledArtifactIdentification>() {
				@Override
				public int compare(EnhancedCompiledArtifactIdentification o1, EnhancedCompiledArtifactIdentification o2) {					
					return o1.getVersion().compareTo(o2.getVersion());
				}							 				
			});
			
			// last one is highest
			if (matching.size() > 0) {
				EnhancedCompiledArtifactIdentification topEcai = matching.get( matching.size() - 1);
				toBeImported.add(topEcai);
			}
			else {
				String msg = "cannot import : the jar [" + jarEcai.asString() + "] has no corresponding project in sources, present are/is [" + queried.stream().map( q -> q.asString()).collect( Collectors.joining(",")) + "]";
				log.debug(msg);
				DevrockPluginStatus status = new DevrockPluginStatus(msg, IStatus.INFO);
				DevrockPlugin.instance().log(status);
			}
							
		}
		
		// build the list of projects that can be imported
		final List<EnhancedCompiledArtifactIdentification> projectsToBeImported = toBeImported.stream().filter( ecai -> new File( ecai.getOrigin(), ".project").exists()).collect( Collectors.toList());
		
		List<String> namesToBeImported = projectsToBeImported.stream().map( ecai -> (ArtifactIdentification) ecai).map( ai -> ai.asString()).collect(Collectors.toList());
		
		List<EnhancedCompiledArtifactIdentification> nonImportableProjects = new ArrayList<>( selectedJars);
		
		List<EnhancedCompiledArtifactIdentification> droppedSelectedEcais = nonImportableProjects.stream().filter( ecai -> !namesToBeImported.contains( ((ArtifactIdentification) ecai).asString()) ).collect(Collectors.toList());		
		
		
		String toBeImportedNames = projectsToBeImported.stream().map( ecai -> ecai.asString()).collect( Collectors.joining(","));
		String notToBeImportedNames = droppedSelectedEcais.stream().map( ecai -> ecai.asString()).collect( Collectors.joining(","));

		MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
		// label for menu
		String numbersForImport = projectsToBeImported.size() + " of " + selectedJars.size();
		
		String txt = "Import "+ numbersForImport + " jar dependencies as projects";
		
		String numbersForDropped = droppedSelectedEcais.size() + " of " + selectedJars.size();
		if (droppedSelectedEcais.size() > 0) {
			txt = txt + " (" + numbersForDropped + " have no associated project)";
		}
		txt += ":";				
	    menuItem.setText(txt);
	    
	    // label for tooltip
	    String ttxt = "Import the following ("+  projectsToBeImported.size() + ") projects: " + toBeImportedNames;
	    if (droppedSelectedEcais.size() > 0) {
	    	ttxt += "\n artifacts that cannot be imported: " + notToBeImportedNames;
	    }	    
	    menuItem.setToolTipText( ttxt);
	    menuItem.setImage(  image);	   	   
	    
	    menuItem.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	            	Job job = new Job("Running JarImporter") {
	        			@Override
	        			protected IStatus run(IProgressMonitor arg0) {
	        				ProjectImporter.importProjects( workingSet, projectsToBeImported, null);
	        				return Status.OK_STATUS;
	        			}			
	        		};
	        		job.schedule();
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
