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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.ant.validator;

import java.io.File;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.model.malaclypse.cfg.preferences.ac.AntRunnerPreferences;
import com.braintribe.plugin.commons.preferences.validator.SettingsValidator;
import com.braintribe.plugin.commons.preferences.validator.ValidationResult;

public class AntSettingsValidator implements SettingsValidator {
	private static final String NO_ANT_HOME = "No ANT home has been set";
	private static final String NO_EXISTING_ANT_HOME = "the specified ANT home [%s] doesn't exist";
	private static final String NO_ANT_RUNNER = "No ANT runner has been set";
	private static final String NO_EXISTING_ANT_RUNNER = "The specified ANT runner [%s] in [%s] doesn't exist";
	private static final String name = "ANT Ingeration Validation";
	private String overrideAntHome;
	private String overrideAntRunner;
	
	public AntSettingsValidator() {
		// TODO Auto-generated constructor stub
	}
	
	public AntSettingsValidator(String antHome, String antRunner) {
		this.overrideAntHome = antHome;
		this.overrideAntRunner = antRunner;
		
	}
	@Override
	public String getName() {	
		return name;
	}


	@Override
	public ValidationResult validate() {
		ValidationResult result = ValidationResult.T.create();
		result.setValidationState(true);
		result.setName("Ant integration");
		result.setTooltip("Validation results of the preferences as set in the Ant Integration preferences page");
		
		AntRunnerPreferences antPreferences = ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getAntRunnerPreferences();
		
		String antHome = null;
		if (overrideAntHome == null) {
			antHome = antPreferences.getAntHome();
		}
		else {
			antHome = overrideAntHome;
		}
		
		if (antHome == null || antHome.length() == 0) {
			result.getMessages().add( NO_ANT_HOME);
			result.setValidationState( false);
			return result;
		}
		else {
			File antHomeFile = new File( antHome);
			if (!antHomeFile.exists()) {
				result.getMessages().add( String.format(NO_EXISTING_ANT_HOME, antHome));
				result.setValidationState( false);
			}
		}
		
		String antRunner = null;
		if (overrideAntRunner == null) {
			antRunner = antPreferences.getAntRunner();
		}
		else {
			antRunner = overrideAntRunner;
		}
		
		if (antRunner == null || antRunner.length() == 0) {
			result.getMessages().add( NO_ANT_RUNNER);
			result.setValidationState(false);
		}
		else {
			File antRunnerFile = new File( antHome + File.separator + antRunner);
			if (!antRunnerFile.exists()) {
				result.getMessages().add( String.format(NO_EXISTING_ANT_RUNNER, antRunner, antHome));
				result.setValidationState(false);
			}
		}
		
		return result;
	}

}
