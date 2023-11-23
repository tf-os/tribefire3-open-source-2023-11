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
package com.braintribe.devrock.artifactcontainer.validator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.plugin.commons.preferences.validator.CompoundValidator;
import com.braintribe.plugin.commons.preferences.validator.SettingsValidator;
import com.braintribe.plugin.commons.preferences.validator.ValidationResult;

public class ArtifactContainerPluginValidator implements CompoundValidator {	
	private List<SettingsValidator> validators = new ArrayList<SettingsValidator>();
	private SubMonitor monitor;
	
	@Override
	public void addValidator(SettingsValidator validator) {
		validators.add(validator);

	}

	@Override
	public boolean validate( IProgressMonitor progressMonitor) {
		boolean overallResult = true;
		List<ValidationResult> results = new ArrayList<ValidationResult>();
		monitor = SubMonitor.convert(progressMonitor, validators.size());
		for (SettingsValidator validator : validators) {
			monitor.subTask("calling validator ");
			ValidationResult validationResult = validator.validate();
			if (!validationResult.getValidationState()) {
				overallResult = false;		
				results.add(validationResult);
			}
			monitor.split(1);
		}
		if (!overallResult) {
			Display display = PlatformUI.getWorkbench().getDisplay();
			display.asyncExec( new Runnable() {			
				@Override
				public void run() {
					Shell shell = display.getActiveShell();					
					ArtifactContainerPluginValidatorDialog dlg = new ArtifactContainerPluginValidatorDialog(shell);
					dlg.setResultsToDisplay(results);
					dlg.open();									
				}
			});
			ArtifactContainerStatus status = new ArtifactContainerStatus( "Validation has found critical errors in setup", IStatus.ERROR);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		else {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "Validation has succeeded without any problems", IStatus.INFO);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		
		return overallResult;
	}

}
