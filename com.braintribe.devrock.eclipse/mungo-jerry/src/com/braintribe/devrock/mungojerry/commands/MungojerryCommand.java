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
package com.braintribe.devrock.mungojerry.commands;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.braintribe.commons.plugin.commands.SingleDropdownHandler;
import com.braintribe.commons.plugin.selection.SelectionExtractor;
import com.braintribe.devrock.mungojerry.analysis.Analyzer;
import com.braintribe.devrock.mungojerry.analysis.AnalyzerException;
import com.braintribe.devrock.mungojerry.dialog.MungojerryDialog;
import com.braintribe.devrock.mungojerry.dialog.experts.AnalysisControllerImpl;
import com.braintribe.devrock.mungojerry.dialog.experts.ModuleDeclarationWriter;
import com.braintribe.devrock.mungojerry.dialog.tab.AnalysisController;
import com.braintribe.devrock.mungojerry.plugin.Mungojerry;

/**
 * command for the artifact cloner / copier
 * @author pit
 *
 */
public class MungojerryCommand extends SingleDropdownHandler {
	
	@Override
	public void process(String parameter) {	
		Display display = PlatformUI.getWorkbench().getDisplay();
		final Shell shell = display.getActiveShell();		
		ISelection selection = SelectionExtractor.getCurrentPackageExplorerSelection();
		final IProject project = SelectionExtractor.extractSelectedProject( selection);
		
		if (project == null) 
			return;
		
		// 
		
		
		Job job = new Job("Collecting data for Mungojerry dialog launch") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				AnalysisController analysisController = new AnalysisControllerImpl();
				analysisController.setProject(project);
				Analyzer analyzer = new Analyzer();
				try {
					analyzer.extractProtocols( analysisController, monitor);
				} catch (AnalyzerException e1) {
					Mungojerry.log( Status.INFO, "Launching the Artifact Wizard failed, [" + e1.getMessage() + "]");
					return Status.CANCEL_STATUS;
				}
				
				ModuleDeclarationWriter moduleDeclarationWriter = new ModuleDeclarationWriter();
				moduleDeclarationWriter.setAnalysisController(analysisController);
				
				try {						
					//												
					System.out.println("Start Dialog");
					shell.getDisplay().asyncExec( new Runnable() {
						
						@Override
						public void run() {
							MungojerryDialog mungoJerryDialog = new MungojerryDialog(shell);
							mungoJerryDialog.setAnalyisController(analysisController);
							moduleDeclarationWriter.setParentPage(mungoJerryDialog);
							mungoJerryDialog.setModuleDeclarationWriter( moduleDeclarationWriter);
							mungoJerryDialog.open();									
						}
					});
					
			
					return Status.OK_STATUS;
				} catch (Throwable e) {					
					Mungojerry.log( Status.INFO, "Launching the Artifact Wizard failed");
				}
				return Status.CANCEL_STATUS;
			}
			
		};
		
		try {
			job.schedule();
		} catch (Throwable e) {
			Mungojerry.log( Status.INFO, "Running the Artifact Wizard failed");
		}
	}	
}
