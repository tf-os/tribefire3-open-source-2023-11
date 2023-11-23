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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.project.validator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.braintribe.build.artifact.retrieval.multi.retrieval.access.http.HttpAccess;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.QuickImportPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryPairing;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SvnPreferences;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.plugin.commons.preferences.validator.SettingsValidator;
import com.braintribe.plugin.commons.preferences.validator.ValidationResult;

/**
 * validate the project settings
 * <br/>
 * pairings exist, parings have names, local representation is set and directory exists, remote representation is set, and url is valid 
 *  
 * @author pit
 *
 */
public class ProjectSettingsValidator implements SettingsValidator {
	private static final String NO_PAIRINGS = "insufficient source repository pairing information found.";
	private static final String NO_NAME_PAIRING = "a source repository requires a name.";
	private static final String NO_SUCH_FILE = "source repository pairing [%s] : local path [%s] doesn't exist.";
	@SuppressWarnings("unused")
	private static final String NO_SUCH_URL = "source repository pairing [%s] : url [%s] doesn't answer.";
	private static final String NO_VALID_URL = "source repository pairing [%s] : url [%s] is invalid.";
	private static final String NO_URL = "source repository pairing [%s] requires an url.";
	private List<SourceRepositoryPairing> pairings;
	private static final String name ="Project Integration Validation";
	
	
	/**
	 * standard constructor, to be used at startup, gets validation data from the preferences
	 */
	public ProjectSettingsValidator() {		
	}
	
	@Override
	public String getName() {	
		return name;
	}

	/**
	 *  constructor to be used from within the preferences page, gets validation data from the page 
	 * @param pairings
	 */
	public ProjectSettingsValidator(List<SourceRepositoryPairing> pairings) {
		this.pairings = pairings;
		
	}
	
	@SuppressWarnings("unused")
	@Override
	public ValidationResult validate() {
		ValidationResult result = ValidationResult.T.create();
		result.setValidationState(true);
		result.setName( "Project settings");
		result.setTooltip("Validation results of the preferences as set in the Quick Import preferences page");
		
		List<SourceRepositoryPairing> sourceRepositoryPairings = null;
		HttpAccess httpAccess = new HttpAccess();
		if (pairings == null) {
			SvnPreferences svnPreferences = ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getSvnPreferences();
			sourceRepositoryPairings = svnPreferences.getSourceRepositoryPairings();		
		}
		else {
			sourceRepositoryPairings = pairings;
		}
		
		// if any source repository pairings are set, we suppose they're valid
		if (sourceRepositoryPairings.size() == 0) {
			result.setValidationState(false);
			result.getMessages().add(NO_PAIRINGS);
			return result;
		}
		for (SourceRepositoryPairing pairing : sourceRepositoryPairings) {
			if (pairing.getName() == null) {
				result.setValidationState(false);
				result.getMessages().add( String.format(NO_NAME_PAIRING));				
			}
			SourceRepository localRepository = pairing.getLocalRepresentation();
			
			// 
			
			String localUrlAsString  = localRepository.getRepoUrl();
			if (localUrlAsString == null) {
				result.setValidationState(false);
				result.getMessages().add( String.format(NO_URL, pairing.getName()));
				break;
			} 
			URL localUrl = null;
			try {
				localUrl = new URL( localUrlAsString);
			} catch (MalformedURLException e1) {
				result.setValidationState(false);
				result.getMessages().add( String.format(NO_VALID_URL, pairing.getName(), localUrlAsString));
			}
			
			
			String artifactsPath = localUrl.getFile();
			if (new File( artifactsPath).exists() == false) {
				result.setValidationState(false);
				result.getMessages().add( String.format(NO_SUCH_FILE, pairing.getName(), artifactsPath));				
			}
			
			QuickImportPreferences quickImportPreferences = ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getQuickImportPreferences();
			if (!quickImportPreferences.getLocalOnlyNature()) {
				SourceRepository remoteRepository = pairing.getRemoteRepresentation();
				String remoteUrlAsString  = remoteRepository.getRepoUrl();
				if (remoteUrlAsString == null) {
					result.setValidationState(false);
					result.getMessages().add( String.format(NO_URL, pairing.getName()));
					break;
				}
				// ssh is not a valid java.net.URL but seems to be used by GIT.. so we can't validate it here. 
				if (!remoteUrlAsString.toLowerCase().startsWith( "ssh:")) { 
					URL remoteUrl = null;
					try {
						remoteUrl = new URL( remoteUrlAsString);
					} catch (MalformedURLException e) {
						result.setValidationState(false);
						result.getMessages().add( String.format(NO_VALID_URL, pairing.getName(), remoteUrlAsString));
						break;
					}			
				}
			}
		}
		
		return result;
	}
	
	
	public static void main(String [] args) {
		for (String arg : args) {
			try {
				new URL( arg);
				System.out.println("Url [" + arg + "] is fine");
			} catch (MalformedURLException e) {
				System.out.println("Url [" + arg + "] is invalid, " + e);
			}
		}
	}
						

}
