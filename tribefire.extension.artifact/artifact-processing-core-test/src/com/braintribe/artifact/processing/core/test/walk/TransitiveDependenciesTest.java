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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.artifact.processing.core.test.Commons;
import com.braintribe.artifact.processing.core.test.writer.ResolvedArtifactWriter;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.model.artifact.processing.ArtifactResolution;
import com.braintribe.model.artifact.processing.HasArtifactIdentification;
import com.braintribe.model.artifact.processing.ResolvedArtifact;
import com.braintribe.model.artifact.processing.cfg.repository.RepositoryConfiguration;

/**
 * test for transitive dependency feature 
 * 
 *  
 * @author pit
 *
 */

public class TransitiveDependenciesTest extends AbstractDependenciesLab {
	private static final String grpId = "com.braintribe.devrock.test.ape";
	private static final String artId = "ape-terminal";
	private static final String version = "1.0.1-pc";
	private static boolean dumpIt = true;	
	
	private int port;
	
	ResolvedArtifact expectedTerminal;
	List<ResolvedArtifact> expectedDependencyList;
	
	
	
	private Map<String, RepoType> launcherMap;
	
	{
		launcherMap = new HashMap<>();		
		launcherMap.put( "archive," + new File( testSetup, "archive.zip").getAbsolutePath(), RepoType.singleZip);
		
		expectedTerminal = Commons.createResolvedArtifact( grpId + ":" + artId + "#" + version);
		ResolvedArtifact expectedA = Commons.createResolvedArtifact( grpId + ":" + "a" + "#" + version);
		ResolvedArtifact expectedB = Commons.createResolvedArtifact( grpId + ":" + "b" + "#" + version);
		ResolvedArtifact expectedC = Commons.createResolvedArtifact( grpId + ":" + "c" + "#" + version);
		ResolvedArtifact expectedCommons = Commons.createResolvedArtifact( grpId + ":" + "ape-commons" + "#" + version);
		
		expectedA.getDependencies().add(expectedB);
		expectedA.getDependencies().add(expectedCommons);
		
		expectedB.getDependencies().add( expectedCommons);
		
		expectedTerminal.getDependencies().add( expectedA);
		expectedTerminal.getDependencies().add( expectedCommons);
		
		expectedDependencyList = new ArrayList<>();
		expectedDependencyList.add(expectedA);
		expectedDependencyList.add(expectedB);
		expectedDependencyList.add(expectedCommons);
		expectedDependencyList.add(expectedC);
	
	}
		
	@Before
	public void before() {
		port = runBefore(launcherMap);
	}
	
	@After
	public void after() {
		runAfter();
	}

	
	@Test
	public void testResourceBased_Dependencies() {		
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, version);
		ArtifactResolution resolved = resolvedDependencies( repo, hasArtifactIdentification, scopeConfiguration, null);		
		if (dumpIt) {
			StringWriter swriter = new StringWriter();
			ResolvedArtifactWriter writer = new ResolvedArtifactWriter(swriter);
			writer.dump(resolved.getDependencies());
			System.out.println( swriter.toString());		
		}
		//
		validateResolvedArtifacts(resolved.getDependencies(), expectedDependencyList);
	}
	
	@Test
	public void testSimplifiedBased_Dependencies() {		
		RepositoryConfiguration scopeConfiguration = generateModelledScopeConfiguration("braintribe.Base", "http://localhost:"+ port + "/archive/", "builder", "operating2005", null, overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, version);
		ArtifactResolution resolved = resolvedDependencies( repo, hasArtifactIdentification, scopeConfiguration, null);		
		if (dumpIt) {
			StringWriter swriter = new StringWriter();
			ResolvedArtifactWriter writer = new ResolvedArtifactWriter(swriter);
			writer.dump(resolved.getDependencies());
			System.out.println( swriter.toString());		
		}
		//
		validateResolvedArtifacts(resolved.getDependencies(), expectedDependencyList);
	}

	@Test
	public void testResourceBased_ResolvedArtifact() {
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, version);
		ArtifactResolution resolved = resolvedDependencies( repo, hasArtifactIdentification, scopeConfiguration, null);		
		if (dumpIt) {
			StringWriter swriter = new StringWriter();
			ResolvedArtifactWriter writer = new ResolvedArtifactWriter(swriter);
			writer.dump( Collections.singletonList( resolved.getResolvedArtifact()));
			System.out.println( swriter.toString());
			
			validateResolvedArtifact( resolved.getResolvedArtifact(), expectedTerminal);
		}
	}
	
	@Test
	public void testSimplifiedBased_ResolvedArtifact() {
		RepositoryConfiguration scopeConfiguration = generateModelledScopeConfiguration("braintribe.Base", "http://localhost:"+ port + "/archive/", "builder", "operating2005", null, overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, version);
		ArtifactResolution resolved = resolvedDependencies( repo, hasArtifactIdentification, scopeConfiguration, null);		
		if (dumpIt) {
			StringWriter swriter = new StringWriter();
			ResolvedArtifactWriter writer = new ResolvedArtifactWriter(swriter);
			writer.dump( Collections.singletonList( resolved.getResolvedArtifact()));
			System.out.println( swriter.toString());
			
			validateResolvedArtifact( resolved.getResolvedArtifact(), expectedTerminal);
		}
	}

	
	
	public static void main( String [] args) {
		for (String arg : args) {
			String [] splitted = arg.split( "[:#]");
			System.out.println( splitted.length);
		}
	}
	
}
