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
package tribefire.extension.artifact.management_processing.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.console.ConsoleConfiguration;
import com.braintribe.console.PrintStreamConsole;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.util.Lists;

import tribefire.extension.artifact.management.api.model.data.ArtifactVersions;
import tribefire.extension.artifact.management.api.model.request.GetArtifactVersions;
import tribefire.extension.artifact.management.api.model.request.GetArtifactsVersions;
import tribefire.extension.artifact.management.api.model.request.UploadArtifacts;
import tribefire.extension.artifact.management_processing.test.wire.contract.ArtifactManagementProcessingTestConfigurationContract;

/**
 * 
 * @author pit
 *
 */
public class ArtifactManagementTests extends ArtifactManagementProcessingTestBase {
	private File contents = new File("res/upload");
	private File workingDirectory = new File( contents, "working");
	private File uploadDirectory = new File( contents, "uploads");
	private File initialsDirectory = new File( contents, "initial");
	private File contentsDirectoryA = new File( initialsDirectory, "remoteRepoA");
	private File contentsDirectoryB = new File( initialsDirectory, "remoteRepoB");
	private File contentsDirectoryC = new File( initialsDirectory, "remoteRepoC");
	
	
	private File repoDirectory = new File( contents, "repo");
	private File settingsDirectory = new File( contents, "settings");
	private String repositoryId = "archiveA";
	private Launcher launcher;


	@Override
	protected Launcher launcher() {
		launcher = Launcher.build()
			.repolet()
				.name("archiveA")
				.uploadFilesystem()
					.filesystem(uploadDirectory)
				.close()
				.filesystem()
					.filesystem(contentsDirectoryA)
				.close()
			.close()
			.repolet()
				.name("archiveB")
				.filesystem()
					.filesystem(contentsDirectoryB)
				.close()
			.close()
			.repolet()
				.name("archiveC")
				.filesystem()
					.filesystem(contentsDirectoryC)
				.close()
			.close()
		.done();
		
		return launcher;
	}
	
	
	@Override
	protected void beforeTest() {
		ConsoleConfiguration.install( new PrintStreamConsole( System.out, true));
		TestUtils.ensure(uploadDirectory);
		TestUtils.ensure(repoDirectory);
	}
	
	private Map<String, List<String>> uploadNonOverwriteExpectedFiles = new HashMap<>();
	{
		List<String> expectedFiles = new ArrayList<>();
		expectedFiles.add( "artifactA-1.0.1-pc.jar");
		expectedFiles.add( "artifactA-1.0.1-pc-sources.jar");		
		uploadNonOverwriteExpectedFiles.put( "artifactA-1.0.1-pc", expectedFiles);
		
		expectedFiles = new ArrayList<>();
		expectedFiles.add( "artifactB-1.0.1-pc.jar");
		expectedFiles.add( "artifactB-1.0.1-pc-sources.jar");
		expectedFiles.add( "artifactB-1.0.1-pc.pom");
		uploadNonOverwriteExpectedFiles.put( "artifactB-1.0.1-pc", expectedFiles);
		
		expectedFiles = new ArrayList<>();
				
		expectedFiles.add( "parent-1.0.1-pc.pom");
		uploadNonOverwriteExpectedFiles.put( "parent-1.0.1-pc", expectedFiles);
						
	}
	
	private Map<String, List<String>> uploadOverwriteExpectedFiles = new HashMap<>();
	{
		List<String> expectedFiles = new ArrayList<>();
		expectedFiles.add( "artifactA-1.0.1-pc.jar");
		expectedFiles.add( "artifactA-1.0.1-pc-sources.jar");
		expectedFiles.add( "artifactA-1.0.1-pc.pom");
		uploadOverwriteExpectedFiles.put( "artifactA-1.0.1-pc", expectedFiles);
		
		expectedFiles = new ArrayList<>();
		expectedFiles.add( "artifactB-1.0.1-pc.jar");
		expectedFiles.add( "artifactB-1.0.1-pc-sources.jar");
		expectedFiles.add( "artifactB-1.0.1-pc.pom");
		uploadOverwriteExpectedFiles.put( "artifactB-1.0.1-pc", expectedFiles);
		
		expectedFiles = new ArrayList<>();
				
		expectedFiles.add( "parent-1.0.1-pc.pom");
		uploadOverwriteExpectedFiles.put( "parent-1.0.1-pc", expectedFiles);
						
	}
	

	private void validate( Map<String, List<String>> expected) {
		File folder = new File( uploadDirectory, "tribefire/extension/artifact/test");

		Map<String, Pair<List<String>, List<String>>> artifactToMatchingAndExcessPairMap = new HashMap<>();
		for (Map.Entry< String, List<String>> entry : expected.entrySet()) {
			List<String> matching = new ArrayList<>();
			List<String> excess = new ArrayList<>();
			String artifactName = entry.getKey();	
			int d = artifactName.indexOf("-");
			String name = artifactName.substring(0, d);
			String version = artifactName.substring( d+1);
			File artifactFolder = new File( folder, name + "/" + version);
			File [] files = artifactFolder.listFiles();
			if (files == null) {
				Assert.fail("no files found in folder [" + artifactFolder.getAbsolutePath() + "]");
				return;
			}
			List<String> found = Arrays.asList(files).stream().map( File::getName).collect( Collectors.toList()); 
			for (String fileName : entry.getValue()) {
				if (found.contains(fileName)) {
					matching.add( fileName);
				}
				else {
					excess.add( fileName);
				}
			}
			Pair<List<String>, List<String>> pair = Pair.of( matching, excess);
			artifactToMatchingAndExcessPairMap.put( entry.getKey(), pair);																			
		}
		
		boolean pass = true;
		// assert 
		for (Map.Entry<String, Pair<List<String>, List<String>>> entry : artifactToMatchingAndExcessPairMap.entrySet()) {
			List<String> missing = new ArrayList<>( expected.get(entry.getKey()));
			missing.removeAll( entry.getValue().first);
			
			if (missing.size() != 0) {
				pass = false;
				System.out.println("missing files for [" + entry.getKey() + "] are [" + missing.stream().collect(Collectors.joining(",")));
			}
			if (entry.getValue().second.size() != 0) {
				pass = false;
				System.out.println("unexpected files for [" + entry.getKey() + "] are [" + missing.stream().collect(Collectors.joining(",")));
			}
		}
		
		if (!pass) {
			Assert.fail("expectations do not match found data");
		}
	}

	@Override
	protected ArtifactManagementProcessingTestConfigurationContract cfg() {
		ArtifactManagementProcessingTestConfigurationSpace cfg = new ArtifactManagementProcessingTestConfigurationSpace();
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		cfg.setVirtualEnvironment( ove);
		 
		ove.setEnv("repo", repoDirectory.getAbsolutePath());
		ove.setEnv("port", Integer.toString( launcher.getAssignedPort()));
		
		File settingsFile = new File( settingsDirectory, "basic-settings.xml");
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settingsFile.getAbsolutePath());
		
		return cfg;
	}


	/**
	 * upload tests 
	 */
	@Test
	public void testUpload() {
		
		UploadArtifacts ua = UploadArtifacts.T.create();
		ua.setPath( workingDirectory.getAbsolutePath());
		ua.setRepoId( repositoryId);
		ua.setUpdate(false);
		
		Neutral neutral = ua.eval( evaluator).get();
		
		Assert.assertTrue("", neutral == Neutral.NEUTRAL);
		validate( uploadNonOverwriteExpectedFiles);
	}
	
	@Test
	public void testUploadWithUpdate() {		
		UploadArtifacts ua = UploadArtifacts.T.create();
		ua.setPath( workingDirectory.getAbsolutePath());
		ua.setRepoId( repositoryId);
		ua.setUpdate( true);
		
		Neutral neutral = ua.eval( evaluator).get();
		
		Assert.assertTrue("", neutral == Neutral.NEUTRAL);
		validate( uploadOverwriteExpectedFiles);
	}
	
	private Map<String, List<String>> unversionedExpectations;
	{
		unversionedExpectations = new HashMap<>();
		List<String> versionsForA = Lists.list("1.0.1-pc", "1.0.1", "1.0.2-pc");
		unversionedExpectations.put("tribefire.extension.artifact.test.v:a", versionsForA);
		List<String> versionsForB = Lists.list("1.0.1-pc", "1.0.1");
		unversionedExpectations.put("tribefire.extension.artifact.test.v:b", versionsForB);
		List<String> versionsForC = Lists.list("1.0.1-pc", "1.0.1", "1.0.2-pc");
		unversionedExpectations.put("tribefire.extension.artifact.test.v:c", versionsForC);
	}
	
	private Map<String, List<String>> versionedExpectations;
	{
		versionedExpectations = new HashMap<>();
		List<String> versionsForA = Lists.list("1.0.1-pc", "1.0.1");
		versionedExpectations.put("tribefire.extension.artifact.test.v:a#[1.0,1.0.1]", versionsForA);
		List<String> versionsForB = Lists.list("1.0.1-pc", "1.0.1");
		versionedExpectations.put("tribefire.extension.artifact.test.v:b#[1.0,1.0.1]", versionsForB);
		List<String> versionsForC = Lists.list("1.0.1-pc");
		versionedExpectations.put("tribefire.extension.artifact.test.v:c#[1.0,1.0.1)", versionsForC);
	}
	
	
	private String collate(Collection<String> strings) {
		return strings.stream().collect( Collectors.joining(","));
	}
	/*
	 * version retrieval tests
	 */
	private void validate(String expectedArtifact, List<String> expectedVersions, ArtifactVersions found) {
		List<String> matching = new ArrayList<>();
		List<String> missing = new ArrayList<>();
		for (String versionAsString : expectedVersions) {
			if (found.getVersionsAsStrings().contains(versionAsString)) {
				matching.add( versionAsString);
			}
			else {
				missing.add( versionAsString);
			}
		}
		List<String> excess = new ArrayList<>( found.getVersionsAsStrings());
		excess.removeAll( matching);
		
		Assert.assertTrue("expected for [" + expectedArtifact + "] : [" + collate( expectedVersions) + "], but found [" + collate( found.getVersionsAsStrings()) + "], missing [" + collate( missing) + "]", missing.size() == 0);
		Assert.assertTrue("expected for [" + expectedArtifact + "] : [" + collate( expectedVersions) + "], but found [" + collate( found.getVersionsAsStrings()) + "], excess [" + collate( excess) + "]", excess.size() == 0);
	}
	
	@Test
	public void testSingleUnversionedArtifactVersions() {
		for (Map.Entry<String, List<String>> expectation : unversionedExpectations.entrySet()) {
			GetArtifactVersions gav = GetArtifactVersions.T.create();		
			gav.setArtifact( expectation.getKey());			
			ArtifactVersions retval = gav.eval(evaluator).get();			
			validate(expectation.getKey(), expectation.getValue(), retval);						
		}				
	}

	@Test
	public void testSingleVersionedArtifactVersions() {
		for (Map.Entry<String, List<String>> expectation : versionedExpectations.entrySet()) {
			GetArtifactVersions gav = GetArtifactVersions.T.create();		
			gav.setArtifact( expectation.getKey());			
			ArtifactVersions retval = gav.eval(evaluator).get();			
			validate(expectation.getKey(), expectation.getValue(), retval);						
		}				
	}

	@Test
	public void testMultipleUnversionedArtifactVersions() {
		GetArtifactsVersions gav = GetArtifactsVersions.T.create();
		List<String> keys = new ArrayList<>( unversionedExpectations.keySet());
		gav.setArtifacts( keys);
		
		List<ArtifactVersions> retval = gav.eval(evaluator).get();
		for (ArtifactVersions suspect : retval) {
			String key = suspect.getGroupId() + ":" + suspect.getArtifactId();
			List<String> expectedVersions = unversionedExpectations.get(key);
			validate( key, expectedVersions, suspect);
		}	
	}

	@Test
	public void testMultipleVersionedArtifactVersions() {
		GetArtifactsVersions gav = GetArtifactsVersions.T.create();
		List<String> keys = new ArrayList<>( versionedExpectations.keySet());
		gav.setArtifacts( keys);
		
		List<ArtifactVersions> retval = gav.eval(evaluator).get();
		for (ArtifactVersions suspect : retval) {
			String key = suspect.getGroupId() + ":" + suspect.getArtifactId();
			String actualKey = versionedExpectations.keySet().stream().filter( k -> k.startsWith(key)).findFirst().orElse(null);
			Assert.assertTrue( "no expression found for [" + key + "]", actualKey != null);
			List<String> expectedVersions = versionedExpectations.get( actualKey);
			validate( key, expectedVersions, suspect);
		}	
	}

	
}
