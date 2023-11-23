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
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.artifact.processing.core.test.Commons;
import com.braintribe.artifact.processing.core.test.TestUtil;
import com.braintribe.artifact.processing.core.test.writer.ResolvedPlatformAssetWriter;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.console.ConsoleConfiguration;
import com.braintribe.console.PlainSysoutConsole;
import com.braintribe.model.artifact.processing.ArtifactIdentification;
import com.braintribe.model.artifact.processing.AssetFilterContext;
import com.braintribe.model.artifact.processing.HasArtifactIdentification;
import com.braintribe.model.artifact.processing.PlatformAssetResolution;
import com.braintribe.model.artifact.processing.ResolvedArtifact;
import com.braintribe.model.artifact.processing.ResolvedPlatformAsset;
import com.braintribe.model.artifact.processing.cfg.repository.RepositoryConfiguration;
import com.braintribe.model.artifact.processing.service.data.ResolvedPlatformAssets;
import com.braintribe.model.artifact.processing.service.request.ResolvePlatformAssets;
import com.braintribe.testing.category.KnownIssue;

public class AssetResolvingLab extends AbstractDependenciesLab {
	private static final String grpId = "com.braintribe.devrock.test.scopes";
	private static final String artId = "ScopeTestTerminal";
	private static final String version = "1.0";
	private static boolean verbose = true;
	
	ResolvedArtifact expectedTerminal;
	List<ResolvedArtifact> expectedDependencyList;
	
	
	private Map<String, RepoType> launcherMap;
	
	{
		launcherMap = new HashMap<>();		
		launcherMap.put( "archive," + new File( testSetup, "archive.zip").getAbsolutePath(), RepoType.singleZip);
		
		expectedTerminal = Commons.createResolvedArtifact( grpId + ":" + artId + "#" + version);
		
		
	}
		
	@Before
	public void before() {
		runBefore(launcherMap);
	}
	
	@After
	public void after() {
		runAfter();
	}
	



	//@Test
	public void test() {
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, version);
		AssetFilterContext context = null;
		PlatformAssetResolution resolvedAssets = resolvedAssets( repo,hasArtifactIdentification, scopeConfiguration, context);
		validate( resolvedAssets.getResolvedPlatformAsset(), resolvedAssets.getDependencies());		
		dump(resolvedAssets);		
	}
	

	@Test
	@Category(KnownIssue.class)
	public void testRealLife() {
		Map<String,String> empty_overridesMap = new HashMap<>();
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.real_life.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation("tribefire.extension.demo", "tribefire-demo-setup", "2.0");
		AssetFilterContext context = null;
		PlatformAssetResolution assetResolution = resolvedAssets( null,hasArtifactIdentification, scopeConfiguration, context);
		
		dump(assetResolution);		
	}
	
	@Test
	@Category(KnownIssue.class)
	public void testJinniModuleIssue() {	
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.real_life.adx.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation("tribefire.adx.phoenix", "adx-standard-module-setup", "2.0.712");
		AssetFilterContext context = null;
		PlatformAssetResolution assetResolution = resolvedAssets( null,hasArtifactIdentification, scopeConfiguration, context);
		
		dump(assetResolution);		
	}
	
	@Test
	@Category(KnownIssue.class)
	public void testTribefireDemo() {
		Map<String,String> empty_overridesMap = new HashMap<>();
		RepositoryConfiguration scopeConfiguration = generateRealLifeModelledScopeConfiguration( repo, empty_overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation("tribefire.extension.demo", "tribefire-demo-setup", "2.0");
		AssetFilterContext context = null;
		PlatformAssetResolution assetResolution = resolvedAssets( null,hasArtifactIdentification, scopeConfiguration, context);
		
		dump(assetResolution);		
	}
	

	@Test
	@Category(KnownIssue.class)
	public void testRealLifeDocuments() {
		Map<String,String> empty_overridesMap = new HashMap<>();
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.real_life.xml"), empty_overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation("tribefire.cortex.assets", "tribefire-standard-aggregator", "2.0");
		AssetFilterContext context = null;
		PlatformAssetResolution assetResolution = resolvedAssets( null,hasArtifactIdentification, scopeConfiguration, context);
		
		dump(assetResolution);		
	}
	
	//@Test
	@Category(KnownIssue.class)
	public void testRealLifeBug() {
		ConsoleConfiguration.install(PlainSysoutConsole.INSTANCE);
		Map<String,String> empty_overridesMap = new HashMap<>();
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.concurrency.bug.enabled.xml"), empty_overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation("platform.demo", "platform-demo-setup", "1.0");
		AssetFilterContext context = null;
		
		//
		//File localRepo = new File( contents, "bug-repo");
		// 
		
		PlatformAssetResolution assetResolution = resolvedAssets( repo,hasArtifactIdentification, scopeConfiguration, context);
		
		dump(assetResolution);		
	}
	
	@Test
	@Category(KnownIssue.class)
	public void testRealLifeAdx() {
		ConsoleConfiguration.install(PlainSysoutConsole.INSTANCE);		
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.real_life.adx.xml"), overridesMap);
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation( "tribefire.adx.phoenix", "adx-standard-setup", "2.0");
		AssetFilterContext context = null;
		
		//
		//File localRepo = new File( contents, "bug-repo");
		// 
		
		PlatformAssetResolution assetResolution = resolvedAssets( repo,hasArtifactIdentification, scopeConfiguration, context);
		
		dump(assetResolution);		
	}
	
	@Test
	@Category(KnownIssue.class)
	public void testMultiResolution() {
		ConsoleConfiguration.install(PlainSysoutConsole.INSTANCE);		
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.real_life.xml"), overridesMap);
		
		AssetFilterContext context = null;
		ResolvePlatformAssets request = ResolvePlatformAssets.T.create();
		
		ArtifactIdentification artifactIdentification = Commons.generate( "tribefire.extension.simple", "simple-cartridge-setup", "2.0");
		request.getAssets().add(artifactIdentification);
		
		// the demo aggregator has been removed. when this test gets fixed, we must also switch to another artifact.
		artifactIdentification = Commons.generate( "tribefire.cortex.assets", "tribefire-demo-aggregator", "2.0");
		request.getAssets().add(artifactIdentification);
		/*
		artifactIdentification = Commons.generate( "tribefire.extension.modelling", "model-designer-setup", "2.0");
		request.getAssets().add(artifactIdentification);
		*/
		
		//File localRepo = new File( contents, "bug-repo");
		// 
		
		ResolvedPlatformAssets resolvedAssets = resolvedAssets( repo, request, scopeConfiguration, context);
		
		dump(resolvedAssets);		
	}
	
	
	
	//@Test
	@Category(KnownIssue.class)
	public void testConcurrency() {
		for (int i = 0; i < 20; i++) {
			TestUtil.ensure(repo);
			testRealLifeBug();
		}
	}
	
	
	private void dump(PlatformAssetResolution assetResolution) {
		if (!verbose)
			return;
		try {
			StringWriter swriter = new StringWriter();
			ResolvedPlatformAssetWriter writer = new ResolvedPlatformAssetWriter(swriter);
			writer.dump( assetResolution.getResolvedPlatformAsset());
			writer.dump( assetResolution.getDependencies());
			System.out.println( swriter.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void dump(ResolvedPlatformAssets assets) {
		if (!verbose)
			return;
		try {
			StringWriter swriter = new StringWriter();
			ResolvedPlatformAssetWriter writer = new ResolvedPlatformAssetWriter(swriter);
			for (ResolvedPlatformAsset asset : assets.getTerminalAssets()) {
			writer.dump( asset);
			System.out.println( swriter.toString());
			}
			writer.dump( assets.getResolvedAssets());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void validate(ResolvedPlatformAsset resolvedPlatformAsset, List<ResolvedPlatformAsset> dependencies) {
		// TODO Auto-generated method stub
		
	}

}
