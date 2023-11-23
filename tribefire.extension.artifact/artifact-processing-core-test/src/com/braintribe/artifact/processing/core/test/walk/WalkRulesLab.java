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
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.artifact.processing.core.test.Commons;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.model.artifact.processing.ArtifactResolution;
import com.braintribe.model.artifact.processing.HasArtifactIdentification;
import com.braintribe.model.artifact.processing.ResolvedArtifact;
import com.braintribe.model.artifact.processing.cfg.repository.RepositoryConfiguration;
import com.braintribe.model.artifact.processing.cfg.resolution.ResolutionConfiguration;

/**
 * test cases for tag- & type rules (transposed from MC's test suite) 
 * 
 * @author pit
 *
 */
public class WalkRulesLab extends AbstractDependenciesLab {
	
	private static final String tags_grpId = "com.braintribe.devrock.test.tags";
	private static final String tags_artId = "tags-terminal";
	private static final String tags_version = "1.0.1";
	
	private static final String types_grpId = "com.braintribe.devrock.test.types";
	private static final String types_artId = "types-terminal";
	private static final String types_version = "1.0.1";
	
	private static final String filter_grpId = "com.braintribe.devrock.test.filter";
	private static final String filter_artId = "filter-terminal";
	private static final String filter_version = "1.0.1";
	
	
	ResolvedArtifact expectedTagsTerminal;
	List<ResolvedArtifact> expectedTagsDependencyList;
	
	ResolvedArtifact expectedTypesTerminal;
	List<ResolvedArtifact> expectedTypesDependencyList;
		
	private Map<String, RepoType> launcherMap;
	
	{
		launcherMap = new HashMap<>();		
		launcherMap.put( "archive," + new File( testSetup, "archive.zip").getAbsolutePath(), RepoType.singleZip);
		
		expectedTagsTerminal = Commons.createResolvedArtifact( tags_grpId + ":" + tags_artId + "#" + tags_version);
		expectedTypesTerminal = Commons.createResolvedArtifact( types_grpId + ":" + types_artId + "#" + types_version);
				
	}
		
	@Before
	public void before() {
		runBefore(launcherMap);
	}
	
	@After
	public void after() {
		runAfter();
	}
	
	
	private void runTypeRuleTest(String typeRule, String ... expectedNames) {
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(types_grpId, types_artId, types_version);
				
		ResolutionConfiguration walkConfiguration = ResolutionConfiguration.T.create();				
		walkConfiguration.setTypeRule(typeRule);
		
		ArtifactResolution resolved = resolvedDependencies( repo, hasArtifactIdentification, scopeConfiguration, walkConfiguration);
		
		validate( resolved.getDependencies(), Arrays.asList(expectedNames));
	}
	
	private void runTypeRuleFilterTest(String typeRule, String ... expectedNames) {
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(filter_grpId, filter_artId, filter_version);
				
		ResolutionConfiguration walkConfiguration = ResolutionConfiguration.T.create();				
		walkConfiguration.setTypeRule(typeRule);
		
		ArtifactResolution resolved = resolvedDependencies( repo, hasArtifactIdentification, scopeConfiguration, walkConfiguration);
		
		validate( resolved.getDependencies(), Arrays.asList(expectedNames));
	}

	
	private void runTagRuleTest(String tagRule, String ... expectedNames) {
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(tags_grpId, tags_artId, tags_version);
				
		ResolutionConfiguration walkConfiguration = ResolutionConfiguration.T.create();				
		walkConfiguration.setTagRule(tagRule);
		
		ArtifactResolution resolved = resolvedDependencies( repo, hasArtifactIdentification, scopeConfiguration, walkConfiguration);
		
		validate( resolved.getDependencies(), Arrays.asList(expectedNames));
	}
	
	private List<ResolvedArtifact> transpose( List<String> condensed){
		return condensed.stream().map( c -> {
			return Commons.createResolvedArtifact(c);
		}).collect( Collectors.toList());
	}

	private void validate(List<ResolvedArtifact> resolvedArtifacts, List<String> expectedNames) {
		List<ResolvedArtifact> expected = transpose(expectedNames);
		validateResult(resolvedArtifacts, expected);				
	}
	

	//
	// tags 
	//
	@Test
	public void tag_standard() {
		String[] expectedNames = new String [] {					
				"com.braintribe.devrock.test.tags:none#1.0.1",		
				"com.braintribe.devrock.test.tags:classpath#1.0.1",
				"com.braintribe.devrock.test.tags:standard#1.0.1",
				"com.braintribe.devrock.test.tags:one#1.0.1",
				"com.braintribe.devrock.test.tags:one-and-two#1.0.1",
		};
		runTagRuleTest(null, expectedNames);
	}
	
	@Test
	public void tag_allOut() {		
		String[] expectedNames = new String [] {					
				"com.braintribe.devrock.test.tags:none#1.0.1",
				"com.braintribe.devrock.test.tags:classpath#1.0.1",
		};		
		runTagRuleTest( "!*", expectedNames);
	}

	
	@Test
	public void tag_allin() {
		String[] expectedNames = new String [] {					
				"com.braintribe.devrock.test.tags:standard#1.0.1",
				"com.braintribe.devrock.test.tags:one#1.0.1",
				"com.braintribe.devrock.test.tags:one-and-two#1.0.1",
				"com.braintribe.devrock.test.tags:classpath#1.0.1",
		};
		runTagRuleTest( "*", expectedNames);		
	}
	
	
	@Test
	public void tag_one() {		
		String[] expectedNames = new String [] {								
				"com.braintribe.devrock.test.tags:one#1.0.1",
				"com.braintribe.devrock.test.tags:one-and-two#1.0.1",
		};
		runTagRuleTest( "one", expectedNames);		
	}
	
	@Test
	public void tag_oneAndTwo() {
		String[] expectedNames = new String [] {												
				"com.braintribe.devrock.test.tags:one-and-two#1.0.1",				
		};
		runTagRuleTest( "one,two", expectedNames);		
	}
	
	@Test
	public void tag_oneNotTwo() {		
		String[] expectedNames = new String [] {								
				"com.braintribe.devrock.test.tags:one#1.0.1",			
		};
		runTagRuleTest( "one,!two", expectedNames);		
	}
	@Test
	public void tag_neitherTwoNorStandard() {		
		String[] expectedNames = new String [] {								
				"com.braintribe.devrock.test.tags:none#1.0.1",
				"com.braintribe.devrock.test.tags:one#1.0.1",
				"com.braintribe.devrock.test.tags:classpath#1.0.1",
		};
		runTagRuleTest( "!two,!standard", expectedNames);			
	}
	
	//
	// types
	//
	
	/**
	 * no (or null) rule: MC means no rule at all. Here it means rule "jar" .. should it stay that way?
	 */
	@Test
	public void testNullRule() {
		String[] expectedNames = new String [] {						
				types_grpId + ":" + "none" + "#" + types_version, // no packaging -> jar
				types_grpId + ":" + "standard" + "#" + types_version, // jar packaging
				//types_grpId + ":" + "war" + "#" + types_version, // war packaging
				//types_grpId + ":" + "zip" + "#" + types_version, // zip packaging
				//types_grpId + ":" + "asset-man" + "#" + types_version, // man packaging
				//types_grpId + ":" + "noasset-man" + "#" + types_version, // man packaging
				//types_grpId + ":" + "asset-other" + "#" + types_version, // other packaging										
		};
		runTypeRuleTest( null, expectedNames);
	}
	@Test
	public void testPassthruRule() {
		String[] expectedNames = new String [] {						
				types_grpId + ":" + "none" + "#" + types_version, // no packaging -> jar
				types_grpId + ":" + "standard" + "#" + types_version, // jar packaging
				types_grpId + ":" + "war" + "#" + types_version, // war packaging
				types_grpId + ":" + "zip" + "#" + types_version, // zip packaging
				types_grpId + ":" + "asset-man" + "#" + types_version, // man packaging
				types_grpId + ":" + "noasset-man" + "#" + types_version, // man packaging
				types_grpId + ":" + "asset-other" + "#" + types_version, // other packaging										
		};
		runTypeRuleTest( "*", expectedNames);
	}
	
	
	@Test
	public void testJarRule() {
		String[] expectedNames = new String [] {						
				types_grpId + ":" + "none" + "#" + types_version, // no packaging -> jar
				types_grpId + ":" + "standard" + "#" + types_version, // jar packaging
												
		};
		runTypeRuleTest( "jar", expectedNames);
	}
	
	@Test
	public void testClassifierRule() {
		String[] expectedNames = new String [] {										
				types_grpId + ":" + "asset-man" + "#" + types_version, // man packaging				
				types_grpId + ":" + "asset-other" + "#" + types_version, // other packaging										
		};
		runTypeRuleTest( "asset:", expectedNames);		
	}
	
	@Test
	public void testManRule() {
		String[] expectedNames = new String [] {										
				types_grpId + ":" + "asset-man" + "#" + types_version, // man packaging
				types_grpId + ":" + "noasset-man" + "#" + types_version, // man packaging														
		};
		runTypeRuleTest( "man", expectedNames);		
	}
	
	
	@Test
	public void testClassifierAndTypeRule() {
		String[] expectedNames = new String [] {						
				types_grpId + ":" + "asset-man" + "#" + types_version, // man packaging														
		};
		runTypeRuleTest( "asset:man", expectedNames);		
	}
	
	@Test
	public void testCombinedRule() {
		String[] expectedNames = new String [] {						
				
				types_grpId + ":" + "asset-man" + "#" + types_version, // man packaging
				types_grpId + ":" + "noasset-man" + "#" + types_version, // man packaging
				types_grpId + ":" + "asset-other" + "#" + types_version, // other packaging										
		};
		runTypeRuleTest( "asset:,asset:man,man", expectedNames);		
	}
	
	@Test
	public void testDenotingRule() {
		String[] expectedNames = new String [] {						
				types_grpId + ":" + "war" + "#" + types_version, // war packaging
				types_grpId + ":" + "zip" + "#" + types_version, // zip packaging
				types_grpId + ":" + "asset-man" + "#" + types_version, // man packaging
				types_grpId + ":" + "noasset-man" + "#" + types_version, // man packaging
														
		};
		runTypeRuleTest( "war,zip,man", expectedNames);		
	}
	
	@Test
	public void testClasspathExplicitRuleFilter() {
		String[] expectedNames = new String [] {						
				filter_grpId + ":" + "filter-aggregator" + "#" + filter_version, // no packaging -> jar
				filter_grpId + ":" + "filter-jar" + "#" + filter_version, // jar packaging												
		};
		runTypeRuleFilterTest( "jar,pom", expectedNames);		
	}
	
	@Test
	public void testClasspathStandardRuleFilter() {
		String[] expectedNames = new String [] {						
				filter_grpId + ":" + "filter-aggregator" + "#" + filter_version, // no packaging -> jar
				filter_grpId + ":" + "filter-jar" + "#" + filter_version, // jar packaging												
		};
		runTypeRuleFilterTest( null, expectedNames);		
	}
}
