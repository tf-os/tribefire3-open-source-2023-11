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
package com.braintribe.devrock.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkingSet;

import com.braintribe.devrock.api.selection.EnhancedSelectionExtracter;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.importer.ProjectImporter;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.logging.Logger;
import com.braintribe.model.version.FuzzyVersion;

public class JarImportCommand extends AbstractHandler{
	private static Logger log = Logger.getLogger(JarImportCommand.class);
		
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	
		ISelection selection = SelectionExtracter.currentSelection();
		IWorkingSet workingSet = SelectionExtracter.selectedWorkingSet(selection);
		
		List<EnhancedCompiledArtifactIdentification> selectedJars = EnhancedSelectionExtracter.extractSelectedJars( selection);
						
		if (selectedJars == null || selectedJars.size() == 0) {
			return null;
		}
		
		List<EnhancedCompiledArtifactIdentification> toBeImported = new ArrayList<>( selectedJars.size());
		
		for (EnhancedCompiledArtifactIdentification jarEcai : selectedJars) {
			String expression = jarEcai.getGroupId() + ":" + jarEcai.getArtifactId();
			List<EnhancedCompiledArtifactIdentification> queried = DevrockPlugin.instance().quickImportController().runSourceArtifactQuery( expression);
			if (queried == null || queried.size() == 0) {
				String msg = "cannot import : the jar [" + jarEcai.asString() + "] has no corresponding project";						
				log.debug(msg);
				DevrockPluginStatus status = new DevrockPluginStatus(msg, IStatus.INFO);
				DevrockPlugin.instance().log(status);
				continue;
			}
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
			
		List<EnhancedCompiledArtifactIdentification> projectsToBeImported = toBeImported.stream().filter( ecai -> new File( ecai.getOrigin(), ".project").exists()).collect( Collectors.toList());
	
					
		Job job = new Job("Running JarImporter") {
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				ProjectImporter.importProjects( workingSet, projectsToBeImported, null);
				return Status.OK_STATUS;
			}			
		};
		
		job.schedule();
		return null;		
	}
			
		
	
}
