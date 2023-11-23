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
package com.braintribe.artifact.processing.core.test.walk;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.artifact.processing.core.test.Commons;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.model.artifact.processing.ArtifactResolution;
import com.braintribe.model.artifact.processing.HasArtifactIdentification;
import com.braintribe.model.artifact.processing.ResolvedArtifact;
import com.braintribe.model.artifact.processing.cfg.repository.RepositoryConfiguration;
import com.braintribe.model.artifact.processing.cfg.resolution.FilterScope;
import com.braintribe.model.artifact.processing.cfg.resolution.ResolutionConfiguration;
import com.braintribe.model.artifact.processing.cfg.resolution.ResolutionScope;


/**
 * tests the parameterization of the walk context when it comes to scopes (inclusive passthrough)
 * 
 * @author pit
 *
 */
public class ResolutionScopeLab extends AbstractDependenciesLab {
	private static final String grpId = "com.braintribe.devrock.test.scopes";
	private static final String artId = "ScopeTestTerminal";
	private static final String version = "1.0";
	
	ResolvedArtifact expectedTerminal;
	List<ResolvedArtifact> expectedDependencyList;
	private ResolvedArtifact expectedA;
	private ResolvedArtifact expectedB;
	private ResolvedArtifact expectedC;
	private ResolvedArtifact expectedD;
	private ResolvedArtifact expectedT;
	private ResolvedArtifact expectedP;

	
	private Map<String, RepoType> launcherMap;
	
	{
		launcherMap = new HashMap<>();		
		launcherMap.put( "archive," + new File( testSetup, "archive.zip").getAbsolutePath(), RepoType.singleZip);
		
		expectedTerminal = Commons.createResolvedArtifact( grpId + ":" + artId + "#" + version);
		
		expectedA = Commons.createResolvedArtifact( grpId + ":" + "A" + "#" + version);
		expectedB = Commons.createResolvedArtifact( grpId + ":" + "B" + "#" + version);
		expectedC = Commons.createResolvedArtifact( grpId + ":" + "C" + "#" + version);
		expectedT = Commons.createResolvedArtifact( grpId + ":" + "T" + "#" + version);
		expectedD = Commons.createResolvedArtifact( grpId + ":" + "D" + "#" + version);			
		expectedP = Commons.createResolvedArtifact( grpId + ":" + "P" + "#" + version);
		
	}
		
	@Before
	public void before() {
		runBefore(launcherMap);
	}
	
	@After
	public void after() {
		runAfter();
	}
	

	
	@Test
	public void compileWithOptionals() {
		// A,B.D,P
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, version);
				
		ResolutionScope resolutionScope = ResolutionScope.compile;
		ResolutionConfiguration walkConfiguration = buildWalkConfiguration( resolutionScope, false);
		ArtifactResolution resolved = resolvedDependencies(  repo,hasArtifactIdentification, scopeConfiguration, walkConfiguration);
		
		// validate 
		validateResult( resolved.getDependencies(), Arrays.asList( expectedA, expectedB, expectedD, expectedP));
	}
	
	@Test
	public void runtimeWithOptionals() {
		// A,B,D
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, version);
				
		ResolutionScope resolutionScope = ResolutionScope.runtime;
		ResolutionConfiguration walkConfiguration = buildWalkConfiguration( resolutionScope, false);
		ArtifactResolution resolved = resolvedDependencies(  repo,hasArtifactIdentification, scopeConfiguration, walkConfiguration);
		// validate 
		validateResult( resolved.getDependencies(), Arrays.asList( expectedA, expectedB, expectedD));
	}
	
	@Test
	public void compileWithoutOptionals() {
		// A,B,P
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, version);
				
		ResolutionScope resolutionScope = ResolutionScope.compile;
		ResolutionConfiguration walkConfiguration = buildWalkConfiguration( resolutionScope, true);
				
		ArtifactResolution resolved = resolvedDependencies( repo, hasArtifactIdentification, scopeConfiguration, walkConfiguration);
		
		// validate 
		validateResult( resolved.getDependencies(), Arrays.asList( expectedA, expectedB, expectedP));
	}
	
	@Test
	public void runtimeWithoutOptionals() {		
		// A,B
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, version);
				
		ResolutionScope resolutionScope = ResolutionScope.runtime;
		ResolutionConfiguration walkConfiguration = buildWalkConfiguration( resolutionScope, true);
		ArtifactResolution resolved = resolvedDependencies( repo, hasArtifactIdentification, scopeConfiguration, walkConfiguration);
		
		// validate 
		validateResult( resolved.getDependencies(), Arrays.asList( expectedA, expectedB));
	}
	
	@Test
	public void compileWithTest() {
		// A,B,T,P
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, version);
		
		ResolutionScope resolutionScope = ResolutionScope.test;
		ResolutionConfiguration walkConfiguration = buildWalkConfiguration( resolutionScope, true);
				
		ArtifactResolution resolved = resolvedDependencies( repo, hasArtifactIdentification, scopeConfiguration, walkConfiguration);
		
		// validate 
		validateResult( resolved.getDependencies(), Arrays.asList( expectedA, expectedB, expectedT, expectedP));
		
	}
	
	@Test
	public void filtering() {
		// A,B,T
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, version);
		
		ResolutionConfiguration walkConfiguration = buildWalkConfiguration( null, true, FilterScope.compile, FilterScope.runtime, FilterScope.test);		
		ArtifactResolution resolved = resolvedDependencies( repo, hasArtifactIdentification, scopeConfiguration, walkConfiguration);
		
		// validate 
		validateResult( resolved.getDependencies(), Arrays.asList( expectedA, expectedB, expectedT));
		
	}
	@Test
	public void filtering2() {
		// A,B,T
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, version);
		
		ResolutionConfiguration walkConfiguration = buildWalkConfiguration( null, true, FilterScope.provided, FilterScope.runtime);		
		ArtifactResolution resolvedArtifacts = resolvedDependencies( repo, hasArtifactIdentification, scopeConfiguration, walkConfiguration);
		
		// validate 
		validateResult( resolvedArtifacts.getDependencies(), Arrays.asList( expectedP));
		
	}
	
	@Test
	public void passThroughTest() {
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, version);
				
		ResolutionConfiguration walkConfiguration = buildWalkConfiguration( null, true);
		
		ArtifactResolution resolved = resolvedDependencies( repo, hasArtifactIdentification, scopeConfiguration, walkConfiguration);
		
		// validate 
		validateResult( resolved.getDependencies(), Arrays.asList( expectedA, expectedB, expectedC, expectedD, expectedP, expectedT));
		
	}

	
		
}
