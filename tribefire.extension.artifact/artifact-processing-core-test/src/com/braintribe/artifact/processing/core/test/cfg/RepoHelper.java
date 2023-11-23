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
package com.braintribe.artifact.processing.core.test.cfg;


import org.junit.Assert;

import com.braintribe.artifact.processing.backend.ArtifactProcessingSettingsPersistenceExpert;
import com.braintribe.model.artifact.processing.cfg.repository.details.ChecksumPolicy;
import com.braintribe.model.artifact.processing.cfg.repository.details.Repository;
import com.braintribe.model.artifact.processing.cfg.repository.details.RepositoryPolicy;
import com.braintribe.model.artifact.processing.cfg.repository.details.UpdatePolicy;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.model.maven.settings.Property;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.maven.settings.Settings;

public class RepoHelper {

	public static Repository createRepoOne() {
		Repository r = Repository.T.create();
		r.setName( "repoOne");
		
		r.setAllowsIndexing(true);
		r.setIsWeaklyCertified(false);
		r.setRemoteIndexCanBeTrusted(true);
		
		r.setUser("one-user");
		r.setPassword("one-password");
		r.setUrl("https://artifactory.server/artifactory/repoOne");
		
		RepositoryPolicy releasePolicy = RepositoryPolicy.T.create();
		releasePolicy.setEnabled(true);
		releasePolicy.setUpdatePolicy( UpdatePolicy.dynamic);
		releasePolicy.setUpdatePolicyParameter("https://artifactory.server/Ravenhurst/rest/repoOne");
		releasePolicy.setCheckSumPolicy( ChecksumPolicy.ignore);
		
		r.setRepositoryPolicyForReleases(releasePolicy);
		return r;
	}
	
	public static Repository createRepoTwo() {
		Repository rTwo = Repository.T.create();
		rTwo.setName( "repoTwo");
		
		rTwo.setAllowsIndexing(true);
		rTwo.setIsWeaklyCertified(false);
		rTwo.setRemoteIndexCanBeTrusted(true);
		
		rTwo.setUser("two-user");
		rTwo.setPassword("two-password");
		rTwo.setUrl("https://artifactory.server/artifactory/repoTwo");
		
		RepositoryPolicy coreDev_releasePolicy = RepositoryPolicy.T.create();
		coreDev_releasePolicy.setEnabled(true);
		coreDev_releasePolicy.setUpdatePolicy( UpdatePolicy.dynamic);
		coreDev_releasePolicy.setUpdatePolicyParameter("https://artifactory.server/Ravenhurst/rest/repoTwo");
		coreDev_releasePolicy.setCheckSumPolicy( ChecksumPolicy.ignore);
		
		rTwo.setRepositoryPolicyForReleases(coreDev_releasePolicy);
		return rTwo;
	}
	
	private static Server getServer( Settings s, String name) {
		return s.getServers().stream().filter( r -> {
			return name.equalsIgnoreCase( r.getId());
		}).findFirst().orElse(null);		
	}
	private static com.braintribe.model.maven.settings.Repository getRepo( Settings s, String name) {
		if (s.getActiveProfiles().size() == 1) {
			Profile profile = s.getActiveProfiles().get(0);
			return profile.getRepositories().stream().filter( r -> {
				return name.equalsIgnoreCase(r.getName());
			}).findFirst().orElse(null);		
		}
		return null;
	}
	private static com.braintribe.model.maven.settings.Property getProperty( Settings s, String name) {
		if (s.getActiveProfiles().size() == 1) {
			Profile profile = s.getActiveProfiles().get(0);
			return profile.getProperties().stream().filter( r -> {
				return name.equalsIgnoreCase(r.getName());
			}).findFirst().orElse(null);		
		}
		return null;
	}

	
	private static void validateAccessSettings( Settings settings, Repository repo) {
		String name = repo.getName();
		String user = repo.getUser();		
		String password = repo.getPassword();
		if (user != null && password != null) {
			
			Server server = getServer(settings, name);
			Assert.assertTrue( "server [" + name + "] not found", server != null);		
			Assert.assertTrue("user doesn't match", user.equalsIgnoreCase( server.getUsername()));
			Assert.assertTrue("password doesn't match", password.equalsIgnoreCase( server.getPassword()));
		}		 			
	}
	
	
	private static void validateFlags( Repository repo, Settings settings) {
		String name = repo.getName();
		
		Property indexingProperty = getProperty(settings, ArtifactProcessingSettingsPersistenceExpert.PROPERTY_LISTING_LENIENT_REPOSITORIES);
		if (repo.getAllowsIndexing()) {
			Assert.assertTrue("no property found for indexing", indexingProperty != null);
			Assert.assertTrue("no indexing entry found for repo [" + name + "]", indexingProperty.getValue().contains( repo.getName()));					
		}
		else {
			if (indexingProperty != null) {
				Assert.assertTrue("entry found for repo [" + name + "] allowing indexing", !indexingProperty.getValue().contains( repo.getName()));
			}
		}
		Property trustworthyProperty = getProperty(settings, ArtifactProcessingSettingsPersistenceExpert.PROPERTY_TRUSTWORTHY_REPOSITORIES);
		if (repo.getRemoteIndexCanBeTrusted()) {
			Assert.assertTrue("no property found for trust", trustworthyProperty != null);
			Assert.assertTrue("no trusting entry found for repo [" + name + "]", trustworthyProperty.getValue().contains( repo.getName()));
		}
		else {
			if (trustworthyProperty != null) {
				Assert.assertTrue("trusting entry found for repo [" + name + "]", !trustworthyProperty.getValue().contains( repo.getName()));
			}
		}
		Property isWeaklyProperty = getProperty(settings, ArtifactProcessingSettingsPersistenceExpert.PROPERTY_WEAK_CERTIFIED_REPOS);
		if (repo.getIsWeaklyCertified()) {
			Assert.assertTrue("no property found for certificate", isWeaklyProperty != null);
			Assert.assertTrue("no certificate entry found for repo", isWeaklyProperty.getValue().contains( repo.getName()));
		}
		else {
			if (isWeaklyProperty != null) {
				Assert.assertTrue("certificate entry found for repo [" + name + "]", !isWeaklyProperty.getValue().contains( repo.getName()));
			}
		}
		
		Property dynamicProperty = getProperty(settings, ArtifactProcessingSettingsPersistenceExpert.PROPERTY_UPDATE_REFLECTING_REPOSITORIES);
		if (
			( 	repo.getRepositoryPolicyForReleases() != null && 
				repo.getRepositoryPolicyForReleases().getUpdatePolicy() != null && 
				repo.getRepositoryPolicyForReleases().getUpdatePolicy() == UpdatePolicy.dynamic)
			|| 
			(	repo.getRepositoryPolicyForSnapshots() != null && 
				repo.getRepositoryPolicyForSnapshots().getUpdatePolicy() != null && 
				repo.getRepositoryPolicyForSnapshots().getUpdatePolicy() == UpdatePolicy.dynamic )
		) {
			Assert.assertTrue("no property found for update reflection", dynamicProperty != null);
			Assert.assertTrue("no update reflection entry found for repo [" + name + "]", dynamicProperty.getValue().contains( repo.getName()));
		}
		else {
			if (isWeaklyProperty != null) {
				Assert.assertTrue("update reflecting entry found for repo [" + name + "]", !dynamicProperty.getValue().contains( repo.getName()));
			}
		}								
		
	}
	
	
	private static void validatePolicy( RepositoryPolicy rPoli, com.braintribe.model.maven.settings.RepositoryPolicy sPoli) {
		if (rPoli == null) {
			Assert.assertTrue( "enablement doesn't match via default", !sPoli.getEnabled());
			return;
		}
		else {
			Assert.assertTrue("enablement doesn't match", rPoli.getEnabled() == sPoli.getEnabled());
		}
		
		ChecksumPolicy checkSumPolicy = rPoli.getCheckSumPolicy();
		String value = sPoli.getChecksumPolicy();
		
		switch (checkSumPolicy) {
			case fail:
				 Assert.assertTrue("fail not set", value != null && value.equalsIgnoreCase("fail")); 
				break;
			case warn:
				Assert.assertTrue("warn not set", value != null && value.equalsIgnoreCase("warn"));
				break;
			default:
			case ignore:
				Assert.assertTrue( "ignore not set", value == null);
				break;
			
			}
		
		UpdatePolicy rUpdatePolicy = rPoli.getUpdatePolicy();
		String sUpdatePolicy = sPoli.getUpdatePolicy();
		
		switch( rUpdatePolicy) {
			case always:
				Assert.assertTrue("no match on always", sUpdatePolicy != null && sUpdatePolicy.equalsIgnoreCase("always"));
				break;
			case daily:
				Assert.assertTrue("no match on daily", sUpdatePolicy != null && sUpdatePolicy.equalsIgnoreCase("daily"));
				break;
			case dynamic:
				Assert.assertTrue("no match on dynamic (never)", sUpdatePolicy != null && sUpdatePolicy.equalsIgnoreCase("never"));
				break;
			case interval:
				Assert.assertTrue( "no set for interval", sUpdatePolicy != null && sUpdatePolicy.startsWith("interval:"));
				int i = sUpdatePolicy.indexOf(':');
				String remainder = sUpdatePolicy.substring( i+1);
				Assert.assertTrue( "interval no match", remainder.equalsIgnoreCase(rPoli.getUpdatePolicyParameter()));
				break;
			case never:
				Assert.assertTrue("no match on never", sUpdatePolicy != null && sUpdatePolicy.equalsIgnoreCase("never"));
				break;
			default:
				break;		
		}
		
		
	}
	
	private static void validatePolicies( Repository repo, Settings settings) {
		String name = repo.getName();
		com.braintribe.model.maven.settings.Repository rp = getRepo(settings,  name);
		
		RepositoryPolicy rReleases = repo.getRepositoryPolicyForReleases();
		com.braintribe.model.maven.settings.RepositoryPolicy sReleases = rp.getReleases();
		validatePolicy(rReleases, sReleases);
		
		RepositoryPolicy rSnapshots = repo.getRepositoryPolicyForSnapshots();
		com.braintribe.model.maven.settings.RepositoryPolicy sSnapshots = rp.getSnapshots();
		validatePolicy( rSnapshots, sSnapshots);
		
		
	}
	
	public static boolean validateRepo( Repository repo, Settings settings) {
		String name = repo.getName();
		com.braintribe.model.maven.settings.Repository rp = getRepo(settings,  name);
		Assert.assertTrue( "repository [" + name + "] not found", rp != null);
		
		// access : server 
		validateAccessSettings(settings, repo);
		
		// url
		String url = repo.getUrl();
		Assert.assertTrue( "url doesn't match", url.equalsIgnoreCase( rp.getUrl()));
		
		// policies
		validatePolicies(repo, settings);
		
		// properties  
		validateFlags(repo, settings);
		
		return true;
	}
	
}
