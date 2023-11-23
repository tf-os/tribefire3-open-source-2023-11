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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.maven.validator;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.overrides.OverrideMavenProfileActivationExpert;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.model.maven.settings.Mirror;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.model.maven.settings.Repository;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.maven.settings.Settings;
import com.braintribe.plugin.commons.preferences.validator.SettingsValidator;
import com.braintribe.plugin.commons.preferences.validator.ValidationResult;

/**
 * maven settings check <br/> 
 * settings.xml exists, local repository exists, profile exists, repository exists, is valid url
 * 
 * @author pit
 *
 */
public class MavenSettingsValidator implements SettingsValidator {
	private static final String PROTOCOL_HTTPS = "https";
	private static final String PROTOCOL_HTTP = "http";
	private static final String PROTOCOL_FILE = "file";
	private static final String MIRROR_S_HAS_NO_SERVER_ASSOCIATED = "Mirror [%s] has no server associated";
	private static final String SERVER_S_HAS_NO_MATCHING_MIRROR_NOR_MATCHING_REPOSITORY = "Server [%s] has no matching mirror nor matching repository";
	private static final String SERVER_DETECTED_WITHOUT_ID_WHICH_IS_MANDATORY_FOR_SERVERS = "Server detected without ID which is mandatory for servers";
	private static final String MIRROR_DETECTED_WITHOUT_ID_WHICH_IS_MANDATORY_FOR_MIRRORS = "Mirror detected without ID which is mandatory for mirrors";
	private static final String A_REPOSITORY_IN_PROFILE_S_HAS_NO_NAME = "A repository in profile [%s] has no name";
	private static final String NO_LOCAL_REPO = "no local repository set";
	private static final String NO_LOCAL_REPO_EXISTS = "local repository [%s] doesn't exist";
	private static final String NO_ACTIVE_PROFILES = "no active profiles";
	private static final String NO_NAME_FOR_ACTIVE_PROFILE = "a profile requires a name";
	private static final String NO_REPOSITORY_IN_PROFILE = "profile [%s] contains no repository";
	private static final String NO_URL_IN_REPOSITORY_OF_PROFILE = "repository [%s] in profile [%s] has no URL";
	private static final String NO_VALID_URL_IN_REPOSITORY_OF_PROFILE = "url [%s] of repository [%s] in profile [%s] is not a valid URL";
	private static final String NO_SUPPORTED_PROTOCOL_IN_URL = "unsupported protocol [%s] of repository [%s] in profile [%s]";
	private static final String NO_REPOSITORY_POLICY = "repository [%s] in profile [%s] has neither a policy for snapshots nor releases";
	private static final String UNEXPECTED_EXCEPTION = "unexpected exception [%s] while validating. Is the settings.xml a valid file?";
	
	private static final String name ="Maven Integration Validation";

	private MavenSettingsReader reader;


	/**
	 * standard constructor to be used at startup, i.e. if settings are read from the preferences 
	 */
	public MavenSettingsValidator() {
	}
		
	@Override
	public String getName() {	
		return name;
	}

	/**
	 * constructor to be used within a preferences page (not preferences!) - constructs its own {@link MavenSettingsReader}
	 * @param userFileOverride
	 * @param installationFileOverride
	 * @param localRepoOverride
	 */
	public MavenSettingsValidator( String userFileOverride, String installationFileOverride, String localRepoOverride) {
		VirtualPropertyResolver resolver = ArtifactContainerPlugin.getInstance().getVirtualPropertyResolver();
		// 
		ValidationPersistenceOverrideExpert persistenceOverrideExpert = new ValidationPersistenceOverrideExpert(userFileOverride, installationFileOverride);
		persistenceOverrideExpert.setPropertyResolver(resolver);
		// 		
		OverrideMavenProfileActivationExpert profileActivationOverrideExpert = new OverrideMavenProfileActivationExpert();
		profileActivationOverrideExpert.setVirtualPropertyResolver(resolver);
		//
		ValidationLocalRepositoryExpert localRepositoryExpert = new ValidationLocalRepositoryExpert();
		localRepositoryExpert.setPropertyResolver(resolver);
		localRepositoryExpert.setOverride( localRepoOverride);
				
		MavenSettingsExpertFactory factory = new MavenSettingsExpertFactory();
		factory.setSettingsPeristenceExpert(persistenceOverrideExpert);
		factory.setMavenProfileActivationExpert(profileActivationOverrideExpert);
		factory.setInjectedRepositoryRetrievalExpert( localRepositoryExpert);
		
		reader = factory.getMavenSettingsReader();				
	}



	@Override
	public ValidationResult validate() {
		ValidationResult result = ValidationResult.T.create();
		result.setValidationState(true);
		result.setName( "Maven integration");
		result.setTooltip("result.setTooltip(\"Validation results of the preferences as set in the Maven Integration preferences page");
		
		//
		try {
			MavenSettingsReader settingsReader = null;
			if (reader == null) {

				settingsReader = MalaclypseWirings.fullClasspathResolverContract().contract().settingsReader();
			}
			else {
				settingsReader = reader;
			}
			
			// 
			String localRepositoryAsString = settingsReader.getLocalRepository(null);
			if (localRepositoryAsString == null) {
				result.getMessages().add( NO_LOCAL_REPO);
				result.setValidationState(false);
			}
			else {
				File localRepository = new File(localRepositoryAsString);
				if (!localRepository.exists()) {
					result.getMessages().add( String.format(NO_LOCAL_REPO_EXISTS, localRepositoryAsString));
					result.setValidationState(false);
				}
			}
			
			List<String> repositoryIds = new ArrayList<String>();
			List<Profile> activeProfiles = settingsReader.getActiveProfiles();
			if (activeProfiles == null || activeProfiles.isEmpty()) {
				result.getMessages().add( NO_ACTIVE_PROFILES);
				result.setValidationState(false);
			}
			else {
				for (Profile profile : activeProfiles) {
					String name = profile.getId();
					if (name == null) {
						result.getMessages().add( NO_NAME_FOR_ACTIVE_PROFILE);
						result.setValidationState(false);
					}
					List<Repository> repositories = profile.getRepositories();
					if (repositories.size() == 0) {
						result.getMessages().add( String.format( NO_REPOSITORY_IN_PROFILE, name));
						result.setValidationState(false);		
						break;
					}
					for (Repository repository : repositories) {
						String repoName = repository.getId();
						if (repoName == null) {
							result.getMessages().add( String.format( A_REPOSITORY_IN_PROFILE_S_HAS_NO_NAME, name));
							result.setValidationState(false);		
						}
						String repoUrlAsString = repository.getUrl();
						if (repoUrlAsString == null) {
							result.getMessages().add( String.format( NO_URL_IN_REPOSITORY_OF_PROFILE, repoName, name));
							result.setValidationState(false);		
							break;
						}
						URL repoUrl = null;
						try {
							repoUrl = new URL( repoUrlAsString);
						} catch (Exception e) {
							result.getMessages().add( String.format( NO_VALID_URL_IN_REPOSITORY_OF_PROFILE, repoName, name, repoUrlAsString));
							result.setValidationState(false);		
							break;
						}
						String protocol = repoUrl.getProtocol();
						if (!protocol.equalsIgnoreCase(PROTOCOL_FILE) && !protocol.equalsIgnoreCase(PROTOCOL_HTTP) && !protocol.equalsIgnoreCase(PROTOCOL_HTTPS)) {
							result.getMessages().add( String.format( NO_SUPPORTED_PROTOCOL_IN_URL, repoUrlAsString, repoName, name));
							result.setValidationState(false);		
						}	
						// local file repos need no server, we don't need to check that id 
						if (!protocol.equalsIgnoreCase(PROTOCOL_FILE)) {
							repositoryIds.add( repoName);
						}
						// test policies
						if (repository.getSnapshots() == null && repository.getReleases() == null) {
							result.getMessages().add( String.format( NO_REPOSITORY_POLICY, repoName, name));
							result.setValidationState(false);	
						}
						
					}					
				}
			}
			// 			
			// check mirror / server connection??
			//
			List<String> mirrorIds = new ArrayList<String>();
			Settings settings = settingsReader.getCurrentSettings();			
			boolean reportedMirrorIdMissing = false;			
			List<Mirror> mirrorList = settings.getMirrors();			
			for (Mirror mirror : mirrorList) {
				String id = mirror.getId();
				if (id == null) {
					if (!reportedMirrorIdMissing) {
						result.getMessages().add( MIRROR_DETECTED_WITHOUT_ID_WHICH_IS_MANDATORY_FOR_MIRRORS);
						result.setValidationState(false);
					}
				}
				else {
					mirrorIds.add( mirror.getId());
				}
			}
			
			List<String> serverIds = new ArrayList<String>();
			boolean reportedServerIdMissing = false;			
			List<Server> serverList = settings.getServers();
			for (Server server : serverList) {
				String id = server.getId();
				if (id == null) {
					if (!reportedServerIdMissing) {
						result.getMessages().add( SERVER_DETECTED_WITHOUT_ID_WHICH_IS_MANDATORY_FOR_SERVERS);
						result.setValidationState(false);
					}
				}
				else {
					serverIds.add( id);
				}
			}				
			
			
			// any mirror must have a server connected 
			for (String id : mirrorIds) {
				if (!serverIds.contains(id)) {
					result.getMessages().add( String.format(MIRROR_S_HAS_NO_SERVER_ASSOCIATED,id));
					result.setValidationState(false);
				}
			}
			// any server must have a mirror OR a repository connected
			for (String id : serverIds) {
				if (!mirrorIds.contains(id) && !repositoryIds.contains( id)) {
					result.getMessages().add( String.format(SERVER_S_HAS_NO_MATCHING_MIRROR_NOR_MATCHING_REPOSITORY,id));				
				}
								
			}
						
			
		} catch (Exception e) {			
			
			result.getMessages().add( String.format(UNEXPECTED_EXCEPTION, e.getMessage()));
			result.setValidationState(false);		
		}
				
		return result;
	}
	

}
