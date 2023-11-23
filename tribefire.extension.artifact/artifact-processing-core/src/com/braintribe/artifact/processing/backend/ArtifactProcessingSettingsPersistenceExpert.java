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
package com.braintribe.artifact.processing.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.marshaller.MavenSettingsMarshaller;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpertImpl;
import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.processing.cfg.repository.MavenRepositoryConfiguration;
import com.braintribe.model.artifact.processing.cfg.repository.RepositoryConfiguration;
import com.braintribe.model.artifact.processing.cfg.repository.SimplifiedRepositoryConfiguration;
import com.braintribe.model.artifact.processing.cfg.repository.details.ChecksumPolicy;
import com.braintribe.model.artifact.processing.cfg.repository.details.RepositoryPolicy;
import com.braintribe.model.artifact.processing.cfg.repository.details.UpdatePolicy;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.model.maven.settings.Property;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.maven.settings.Settings;
import com.braintribe.model.resource.Resource;

/**
 * produces the {@link Settings} assembly as required by MC <br/>
 * 
 * @author pit
 *
 */
public class ArtifactProcessingSettingsPersistenceExpert extends MavenSettingsPersistenceExpertImpl implements MavenSettingsPersistenceExpert {
	public static final String PROPERTY_LISTING_LENIENT_REPOSITORIES = "listingLenientRepositories";
	public static final String PROPERTY_TRUSTWORTHY_REPOSITORIES = "trustworthyRepositories";
	public static final String PROPERTY_UPDATE_REFLECTING_REPOSITORIES = "updateReflectingRepositories";
	public static final String PROPERTY_WEAK_CERTIFIED_REPOS = "weaklyCertifiedRepositories";
	private static final String PROPERTY_PREFIX_RAVENHURST_URL = "ravenhurst-url-";
	private static final String PROPERTY_PREFIX_RAVENHURST_CONTEXT = "ravenhurst-context-";
	private static Logger log = Logger.getLogger(ArtifactProcessingSettingsPersistenceExpert.class);
	private MavenSettingsMarshaller marshaller = new MavenSettingsMarshaller();
	private Settings settings;
	private RepositoryConfiguration scopeConfiguration;
	
	@Configurable
	public void setScopeConfiguration(RepositoryConfiguration scopeConfiguration) {
		this.scopeConfiguration = scopeConfiguration;
	}
	
	@Override
	public Settings loadSettings() throws RepresentationException {
		if (settings != null) {
			return settings;
		}
		
		if (scopeConfiguration != null) {						
			//
			// transpose 
			//		
			if (scopeConfiguration instanceof MavenRepositoryConfiguration) {
				// resource scope configuration 
				MavenRepositoryConfiguration resourceScopeConfiguration = (MavenRepositoryConfiguration) scopeConfiguration;
				Resource resource = resourceScopeConfiguration.getSettingsAsResource();
				try {
					settings = marshaller.unmarshall( resource.openStream());
					return settings;
				} catch (XMLStreamException e) {
					// panic and default
					String msg = "cannot unmarshall resource of configuration [" + scopeConfiguration.getGlobalId() + "]";
					log.error(msg, e);
				}
			}      
			else if (scopeConfiguration instanceof SimplifiedRepositoryConfiguration) {
				// modelled scope configuration 
				settings = transpose( (SimplifiedRepositoryConfiguration) scopeConfiguration);
				if (settings != null) {
					return settings;
				}
				// panic and default
				String msg = "unable to transpose scope configuration's assembly [" + scopeConfiguration.getGlobalId() + "]";
				log.error(msg);						
			}
			else {
				// can't handle that, so panic and default 
				String msg = "unsupported type [" + scopeConfiguration.entityType().getTypeSignature() + "]";
				log.error(msg);			
			}
		}
		
		// no settings configured, use Maven and Braintribe standards
		return super.loadSettings();
	}

	/**
	 * transpose a modelled 
	 * @param scopeConfiguration
	 * @return
	 */
	private Settings transpose(SimplifiedRepositoryConfiguration scopeConfiguration) {
		if (scopeConfiguration == null)
			return null;
		Settings settings = Settings.T.create();

		// transfer local repository
		settings.setLocalRepository( scopeConfiguration.getLocalRepositoryExpression());

		// come up with a profile
		Profile profile = Profile.T.create();
		profile.setId( "injectedProfile");
		settings.getProfiles().add(profile);
		settings.setActiveProfiles( Collections.singletonList( profile));
		
		List<String> dynamicRepositories = new ArrayList<>();
		List<String> indexableRepositories = new ArrayList<>();
		List<String> trustworthyRepositories = new ArrayList<>();
		List<String> weaklyCertifiedRepositories = new ArrayList<>();
		
		// create repositories
		scopeConfiguration.getRepositories().stream().forEach( r -> {
			
			// create a server if user/pwd is given 
			String usr = r.getUser();
			String pwd = r.getPassword();
			if (usr != null && pwd != null) {
				Server server = Server.T.create();
				server.setUsername(usr);
				server.setPassword(pwd);
				server.setId( r.getName());
				settings.getServers().add(server);
			}
		
			// create the repo
			com.braintribe.model.maven.settings.Repository repository = com.braintribe.model.maven.settings.Repository.T.create();
			repository.setName( r.getName());
			repository.setId( r.getName());
			repository.setUrl( r.getUrl());
			
			//
			// policies
			//
			
			// release
			RepositoryPolicy policyForReleases = r.getRepositoryPolicyForReleases();
			if (policyForReleases == null) {
				policyForReleases = RepositoryPolicy.T.create();
				policyForReleases.setEnabled( false);
			}
			repository.setReleases( transpose( policyForReleases));
			
			// snap shot
			RepositoryPolicy policyForSnapshots = r.getRepositoryPolicyForSnapshots();
			if (policyForSnapshots == null) {
				policyForSnapshots = RepositoryPolicy.T.create();
				policyForSnapshots.setEnabled(false);
			}			
			repository.setSnapshots( transpose( policyForSnapshots));
			
			Property contextProperty = Property.T.create();
			contextProperty.setName(PROPERTY_PREFIX_RAVENHURST_CONTEXT + r.getName());
			contextProperty.setValue( "/");
			profile.getProperties().add(contextProperty);
			
			// RH property for the repository.. only use one, i.e. release has it, fine, otherwise test snapshot
			if ( policyForReleases.getEnabled() && policyForReleases.getUpdatePolicy() == UpdatePolicy.dynamic) {
				String parameter = policyForReleases.getUpdatePolicyParameter();
				if (parameter != null) {
					// add variable 
					
					Property property = Property.T.create();
					property.setName(PROPERTY_PREFIX_RAVENHURST_URL + r.getName());
					property.setValue( parameter);
					profile.getProperties().add(property);
					
					dynamicRepositories.add( r.getName());
				}
			}
			else if (policyForSnapshots.getEnabled() && policyForSnapshots.getUpdatePolicy() == UpdatePolicy.dynamic) {
				String parameter = policyForSnapshots.getUpdatePolicyParameter();
				if (parameter != null) {
					// add variable 
					Property property = Property.T.create();
					property.setName(PROPERTY_PREFIX_RAVENHURST_URL + r.getName());
					property.setValue( parameter);
					profile.getProperties().add(property);
					dynamicRepositories.add( r.getName());
				}
			}
			
			// trustworthy? 
			if (r.getRemoteIndexCanBeTrusted()) {
				trustworthyRepositories.add( r.getName());
			}
			// indexing allowed?
			if (r.getAllowsIndexing()) {
				indexableRepositories.add( r.getName());
			}
			// weakly certified 				
			if (r.getIsWeaklyCertified()) {
				weaklyCertifiedRepositories.add( r.getName());
			}
			profile.getRepositories().add(repository);
			
		});
		//
		// tag the profile with the appropriate variables (dynamic update, iterable, trustworthy)
		//
		// dynamics 
		if (dynamicRepositories.size() > 0) {
			Property property = buildListingProperty( PROPERTY_UPDATE_REFLECTING_REPOSITORIES, dynamicRepositories);
			profile.getProperties().add( property);
		}
		// trustworthy
		if (trustworthyRepositories.size() > 0) {
			Property property = buildListingProperty( PROPERTY_TRUSTWORTHY_REPOSITORIES, trustworthyRepositories);
			profile.getProperties().add( property);			
		}
		// indexable, i.e. listing is allowed
		if (indexableRepositories.size() > 0) {			
			Property property = buildListingProperty( PROPERTY_LISTING_LENIENT_REPOSITORIES, indexableRepositories);
			profile.getProperties().add( property);			
		}
				
		// weakly certified
		if (weaklyCertifiedRepositories.size() > 0) {			
			Property property = buildListingProperty( PROPERTY_WEAK_CERTIFIED_REPOS, weaklyCertifiedRepositories);
			profile.getProperties().add( property);			
		}		
		return settings;
	}

	private Property buildListingProperty(String key, List<String> dynamicRepositories) {
		StringBuilder builder = new StringBuilder();
		dynamicRepositories.stream().forEach( n -> {
			if (builder.length() > 0) {
				builder.append( ",");					
			}
			builder.append( n);
		});
		Property property = Property.T.create();
		property.setName( key);
		property.setValue( builder.toString());
		return property;
	}
	
	
	com.braintribe.model.maven.settings.RepositoryPolicy transpose( RepositoryPolicy rolesettings) {
		com.braintribe.model.maven.settings.RepositoryPolicy repoPolicy = com.braintribe.model.maven.settings.RepositoryPolicy.T.create();
		
		repoPolicy.setEnabled( rolesettings.getEnabled());
		ChecksumPolicy checkSumPolicy = rolesettings.getCheckSumPolicy();
		if (checkSumPolicy == null) {
			checkSumPolicy = ChecksumPolicy.ignore;
		}
		switch( checkSumPolicy) {
			case fail:
				repoPolicy.setChecksumPolicy( "fail");			
				break;
			case warn:
				repoPolicy.setChecksumPolicy( "warn");
				break;
			default:
			case ignore:
				break;			
		}
		UpdatePolicy updatePolicy = rolesettings.getUpdatePolicy();
		if (updatePolicy == null) {
			updatePolicy = UpdatePolicy.never;
		}
		switch ( updatePolicy) {
			case always:
				repoPolicy.setUpdatePolicy( "always");
				break;
			case daily:
				repoPolicy.setUpdatePolicy( "daily");
				break;
			case interval:
				repoPolicy.setUpdatePolicy( "interval:" + rolesettings.getUpdatePolicyParameter());
				break;
			case dynamic:
			case never:			
			default:
				repoPolicy.setUpdatePolicy( "never");
				break;		
		}
		return repoPolicy;
			
	}

}
