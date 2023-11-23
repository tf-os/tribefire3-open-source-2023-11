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
package com.braintribe.artifact.processing.core.test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.build.artifact.test.repolet.Repolet;
import com.braintribe.model.artifact.processing.ResolvedArtifact;
import com.braintribe.model.artifact.processing.cfg.repository.MavenRepositoryConfiguration;
import com.braintribe.model.artifact.processing.cfg.repository.RepositoryConfiguration;
import com.braintribe.model.artifact.processing.cfg.repository.SimplifiedRepositoryConfiguration;
import com.braintribe.model.artifact.processing.cfg.repository.details.Repository;
import com.braintribe.model.artifact.processing.cfg.repository.details.RepositoryPolicy;
import com.braintribe.model.artifact.processing.cfg.repository.details.UpdatePolicy;
import com.braintribe.model.resource.Resource;
import com.braintribe.util.network.NetworkTools;

public abstract class AbstractArtifactProcessingLab {
	protected File res = new File( "res");
	protected File testSetup = new File( res, "test-setup");
	protected File contents = new File( res, "contents");
	protected File repo = new File( contents, "repo");
	protected LauncherShell launcherShell;	
	private Map<String, Repolet> launchedRepolets;
	private ResourceProvidingSession session;
	protected int port;
	
	protected Map<String,String> overridesMap = new HashMap<>();
	
	
	protected int runBefore(Map<String, RepoType> map) {
		
		// 
		TestUtil.ensure( repo);
		session = new ResourceProvidingSession();
	
		port = NetworkTools.getUnusedPortInRange(8080, 8100);
		
		overridesMap = new HashMap<>();
		overridesMap.put("port", Integer.toString(port));
		overridesMap.put( "M2_REPO", repo.getAbsolutePath());
		
		launcherShell = new LauncherShell( port);
		launchedRepolets = launcherShell.launch( map);
		
		protocolLaunch( "launching repolets:");
		
		return port;
	}

	private void protocolLaunch(String prefix) {
		StringBuilder builder = new StringBuilder();
		launchedRepolets.keySet().stream().forEach( n -> {
			if (builder.length() > 0) {
				builder.append( ",");
			}
			builder.append( n);
		});
		System.out.println( prefix + ":" + builder.toString());
	}
	
	
	protected void runAfter() {
		protocolLaunch("shutting down repolets");
		
		launcherShell.shutdown();		
	}
	
	/**
	 * TODO : make it work.. 
	 * @return
	 */
	protected RepositoryConfiguration generateModelledScopeConfiguration(String repoName, String url, String user, String pwd, String rhUrl, Map<String,String> overrides) {
		SimplifiedRepositoryConfiguration modelledScopeConfiguration = SimplifiedRepositoryConfiguration.T.create();
		
		Repository repository = Repository.T.create();
		repository.setName(repoName);
		repository.setId(repoName);
		repository.setUrl(url);
		repository.setUser(user);
		repository.setPassword(pwd);
		
		repository.setAllowsIndexing(true);
		repository.setRemoteIndexCanBeTrusted(true);
		
		RepositoryPolicy policy = RepositoryPolicy.T.create();
		policy.setEnabled(true);		
		policy.setUpdatePolicy( UpdatePolicy.always);				
		policy.setUpdatePolicyParameter(rhUrl);
		repository.setRepositoryPolicyForReleases( policy);
		
		modelledScopeConfiguration.getRepositories().add( repository);
		
		Commons.attachVirtualEnvironment( modelledScopeConfiguration, overrides);
		
		return modelledScopeConfiguration;
	}
	
	protected RepositoryConfiguration generateRealLifeModelledScopeConfiguration(File repo, Map<String, String> overrides) {
		SimplifiedRepositoryConfiguration modelledScopeConfiguration = SimplifiedRepositoryConfiguration.T.create();
		
		Repository repository = Repository.T.create();
		repository.setName("third-party");
		repository.setId("third-party");
		repository.setUrl("https://artifactory.server/artifactory/third-party");
		repository.setUser("bt_developer");
		repository.setPassword("secret");
		
		repository.setAllowsIndexing(true);
		repository.setRemoteIndexCanBeTrusted(true);
		
		RepositoryPolicy policy = RepositoryPolicy.T.create();
		policy.setEnabled(true);		
		policy.setUpdatePolicy( UpdatePolicy.dynamic);				
		policy.setUpdatePolicyParameter("https://artifactory.server/Ravenhurst/rest/third-party");
		repository.setRepositoryPolicyForReleases( policy);
		
		modelledScopeConfiguration.getRepositories().add( repository);
		
		repository = Repository.T.create();
		repository.setName("core-dev");
		repository.setId("core-dev");
		repository.setUrl("https://artifactory.server/artifactory/core-dev");
		repository.setUser("bt_developer");
		repository.setPassword("secret");
		
		repository.setAllowsIndexing(true);
		repository.setRemoteIndexCanBeTrusted(true);
		
		policy = RepositoryPolicy.T.create();
		policy.setEnabled(true);		
		policy.setUpdatePolicy( UpdatePolicy.dynamic);				
		policy.setUpdatePolicyParameter("https://artifactory.server/Ravenhurst/rest/core-dev");
		repository.setRepositoryPolicyForReleases( policy);
		
		modelledScopeConfiguration.getRepositories().add( repository);
		
		modelledScopeConfiguration.setLocalRepositoryExpression( repo.getAbsolutePath());
			
		Commons.attachVirtualEnvironment( modelledScopeConfiguration, overrides);
		return modelledScopeConfiguration;
	}

	
	protected RepositoryConfiguration generateResourceScopeConfiguration( File configuration, Map<String,String> overrides) {
		MavenRepositoryConfiguration resourceScopeConfiguration = MavenRepositoryConfiguration.T.create();
		// 
		Resource resource = ResourceGenerator.filesystemResourceFromFile(session, configuration);
		resourceScopeConfiguration.setSettingsAsResource(resource);
		resourceScopeConfiguration.setName("test case");
		resourceScopeConfiguration.setName("test case with [" + configuration.getAbsolutePath() + "]");
		
		Commons.attachVirtualEnvironment(resourceScopeConfiguration, overrides);
		
		return resourceScopeConfiguration;		
	}
	
	protected void validateResolvedArtifact( ResolvedArtifact ra, ResolvedArtifact rb) {
		Assert.assertTrue( "expected [" + Commons.toString( ra) + "] but found [" + Commons.toString( rb) + "]", Commons.compare(ra, rb));
		List<ResolvedArtifact> depsA = ra.getDependencies();
		List<ResolvedArtifact> depsB = rb.getDependencies();
		
		Assert.assertTrue("expected [" + depsB.size() + "] dependencies, but found [" + depsA.size() + "]", depsA.size() == depsB.size());
		depsA.stream().forEach( rra -> {
			ResolvedArtifact suspect = depsB.stream().filter( rrb -> {
				return Commons.compare( rra, rrb);
			}).findFirst().orElse( null);
			Assert.assertTrue("unexpected dependency [" + Commons.toString( rra) + "] found", suspect != null);
		});		
		
	}
	
	protected static void validateResolvedArtifacts( List<ResolvedArtifact> depsA, List<ResolvedArtifact> depsB) {
		Assert.assertTrue("expected [" + depsB.size() + "] dependencies, but found [" + depsA.size() + "]", depsA.size() == depsB.size());
		depsA.stream().forEach( rra -> {
			ResolvedArtifact suspect = depsB.stream().filter( rrb -> {
				return Commons.compare( rra, rrb);
			}).findFirst().orElse( null);
			Assert.assertTrue("unexpected dependency [" + Commons.toString( rra) + "] found", suspect != null);
		});		
	}
	protected static void validateResolvedArtifactSortOrder( List<ResolvedArtifact> depsA, List<ResolvedArtifact> depsB) {
		Assert.assertTrue("expected [" + depsB.size() + "] dependencies, but found [" + depsA.size() + "]", depsA.size() == depsB.size());
		for (int i = 0; i < depsA.size(); i++) {
			ResolvedArtifact a = depsA.get(i);
			ResolvedArtifact b = depsB.get(i);
			String nA = Commons.toString(a);
			String nB = Commons.toString(b);
			Assert.assertTrue( "expected [" + nB + "], but found [" + nA + "]", nA.equalsIgnoreCase( nB)); 
		}		
	}
	
}
