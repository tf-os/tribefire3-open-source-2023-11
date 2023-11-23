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
package com.braintribe.artifact.processing.core.test.retrieval;


import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.artifact.processing.ArtifactProcessingCoreExpert;
import com.braintribe.artifact.processing.core.test.AbstractArtifactProcessingLab;
import com.braintribe.artifact.processing.core.test.ArtifactInformationWriter;
import com.braintribe.artifact.processing.core.test.Commons;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.info.ArtifactInformation;
import com.braintribe.model.artifact.info.PartInformation;
import com.braintribe.model.artifact.info.RemoteRepositoryInformation;
import com.braintribe.model.artifact.processing.HasArtifactIdentification;
import com.braintribe.model.artifact.processing.cfg.repository.RepositoryConfiguration;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.testing.category.KnownIssue;


/**
 * test to check the information retrieval feature,
 * 
 * @author pit
 *
 */

@Category(KnownIssue.class)
public class InformationRetrievalTest extends AbstractArtifactProcessingLab {
	
	private static boolean dumpIt = true;
	
	private static final String grpId = "com.braintribe.devrock.test.ape";
	private static final String artId = "ape-terminal";
	private static final String version = "1.0.1-pc";
	private static final String artifact = grpId + ":" + artId + "#" + version;
	private Map<String, String> expectedLocalParts;
	private Map<String, String> expectedRemoteParts;
	private Map<String, RepoType> map;
	
	private Map<String,Map<String,String>> remoteRepoToParts;
	private Map<String,String> remoteRepoToUrlMap;
		
	
	{
		// setup for the repolet lauchner
		map = new HashMap<>();		
		map.put( "archive," + new File( testSetup, "archive.zip").getAbsolutePath(), RepoType.singleZip);
		
		// local parts 
		expectedLocalParts = new HashMap<>();
		
		expectedLocalParts.put("pom", "file:/" + repo.getAbsolutePath().replace("\\", "/") + "/com/braintribe/devrock/test/ape/ape-terminal/1.0.1-pc/ape-terminal-1.0.1-pc.pom");
		
		
		
	}
	
	@Before
	public void before() {
		int port = runBefore(map);		
		//TODO: port isn't always 8080 !!
		// remote parts
		expectedRemoteParts = new HashMap<>();
		expectedRemoteParts.put("sources:jar", "http://localhost:" + port + "/archive/com/braintribe/devrock/test/ape/ape-terminal/1.0.1-pc/ape-terminal-1.0.1-pc-sources.jar");
		expectedRemoteParts.put("sources:md5", "http://localhost:" + port + "/archive/com/braintribe/devrock/test/ape/ape-terminal/1.0.1-pc/ape-terminal-1.0.1-pc-sources.jar.md5");
		expectedRemoteParts.put("sources:sha1", "http://localhost:" + port + "/archive/com/braintribe/devrock/test/ape/ape-terminal/1.0.1-pc/ape-terminal-1.0.1-pc-sources.jar.sha1");
		expectedRemoteParts.put("jar","http://localhost:"+ port + "/archive/com/braintribe/devrock/test/ape/ape-terminal/1.0.1-pc/ape-terminal-1.0.1-pc.jar");
		expectedRemoteParts.put("md5","http://localhost:"+ port + "/archive/com/braintribe/devrock/test/ape/ape-terminal/1.0.1-pc/ape-terminal-1.0.1-pc.jar.md5");
		expectedRemoteParts.put("sha1","http://localhost:" + port + "/archive/com/braintribe/devrock/test/ape/ape-terminal/1.0.1-pc/ape-terminal-1.0.1-pc.jar.sha1");
		expectedRemoteParts.put("pom","http://localhost:" + port + "/archive/com/braintribe/devrock/test/ape/ape-terminal/1.0.1-pc/ape-terminal-1.0.1-pc.pom");
		expectedRemoteParts.put("md5","http://localhost:" + port + "/archive/com/braintribe/devrock/test/ape/ape-terminal/1.0.1-pc/ape-terminal-1.0.1-pc.pom.md5");
		expectedRemoteParts.put("sha1","http://localhost:" + port + "/archive/com/braintribe/devrock/test/ape/ape-terminal/1.0.1-pc/ape-terminal-1.0.1-pc.pom.sha1");
		
		// remote repos to URL map
		remoteRepoToUrlMap = new HashMap<>();
		remoteRepoToUrlMap.put( "braintribe.Base", "http://localhost:" + port + "/archive/");
		
		// remote repos to part list map
		remoteRepoToParts = new HashMap<>();
		remoteRepoToParts.put( "braintribe.Base", expectedRemoteParts);
	}
	
	@After
	public void after() {
		runAfter();
	}
	

	private ArtifactInformation test(String solutionAsString) {
		Solution solution = NameParser.parseCondensedSolutionName(solutionAsString);
		
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation( solution.getGroupId(), solution.getArtifactId(), VersionProcessor.toString(solution.getVersion()));
		
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		
		
		ArtifactInformation artifactInformation = ArtifactProcessingCoreExpert.getArtifactInformation( repo, hasArtifactIdentification, scopeConfiguration);		
		if (dumpIt) {
			Writer writer = new StringWriter();
			ArtifactInformationWriter infoWriter = new ArtifactInformationWriter(writer);  
			try {
				infoWriter.dump(artifactInformation);
				System.out.println(writer.toString());
			} catch (IOException e) {
				Assert.fail("cannot dump retrieved data as " + e);
			}
		}		
		return artifactInformation;
	}

	
	/**
	 * test on retrieval : downloaded from repolet
	 */
	@Test
	public void testEager() {
		ArtifactInformation result = test( artifact);
		Assert.assertTrue( "eager has found not data when it should've", result != null);
		// expected : 
		String grpId = "com.braintribe.devrock.test.ape";
		String artId = "ape-terminal";
		String vers = "1.0.1-pc";
	
		String lrepo = repo.getAbsolutePath().replace('\\', '/');
				
		// main
		Assert.assertTrue( "groupid [" + grpId + "] expected, found [" + result.getGroupId() +"]", grpId.equalsIgnoreCase( result.getGroupId()));
		Assert.assertTrue( "artifactid [" + artId + "] expected, found [" + result.getArtifactId() +"]", artId.equalsIgnoreCase( result.getArtifactId()));
		Assert.assertTrue( "vers [" + vers + "] expected, found [" + result.getVersion() +"]", vers.equalsIgnoreCase( result.getVersion()));
		
		String url = result.getLocalInformation().getUrl().replace('\\', '/');
		Assert.assertTrue( "repo [" + lrepo + "] expected, found [" + url +"]", lrepo.equalsIgnoreCase( url));
		
			
		validateParts( result.getLocalInformation().getPartInformation(), expectedLocalParts);
		List<RemoteRepositoryInformation> remoteInformation = result.getRemoteInformation();
		
		validateRemoteRepoInformation( remoteInformation, remoteRepoToUrlMap, remoteRepoToParts);
		// parts remote
		
	}
	
	/**
	 * validate remote repository data 
	 * @param remoteInformation - the {@link RemoteRepositoryInformation} as conveyed by the APE
	 * @param urlMap - the {@link Map} that links the repo name to its URL
	 * @param partMap - the {@link Map} that links the repo name to its parts 
	 */
	private void validateRemoteRepoInformation(List<RemoteRepositoryInformation> remoteInformation, Map<String, String> urlMap, Map<String, Map<String, String>> partMap) {
		remoteInformation.stream().forEach( r -> {
			String name = r.getName();
			String foundUrl = r.getUrl();
			
			String expectedUrl = urlMap.get(name);			
			Assert.assertTrue("unexpected remote repository [" + name + "] found", expectedUrl != null);
			
			Assert.assertTrue("expected [" + expectedUrl + "] as URL of remote repository [" + name + "], but found [" + foundUrl + "]", expectedUrl.equalsIgnoreCase( foundUrl));
			
			Map<String,String> expectedParts = partMap.get(name);
			Assert.assertTrue( "no part information found for remote repository [" + name + "] found", expectedParts != null);			
			validateParts( r.getPartInformation(), expectedParts);						 					
		});
		
	}

	/**
	 * validate parts 
	 * @param partInformations - the {@link List} of {@link PartInformation}
	 * @param expectedParts - the {@link Map} with the expected parts 
	 * @return - true if succeeded
	 */
	private boolean validateParts( List<PartInformation> partInformations, Map<String,String> expectedParts) {
		boolean allfound = true;
		for (Entry<String,String> entry : expectedParts.entrySet()) {			
			boolean found = false;
			
			String key = entry.getKey();
			int p = key.indexOf( ':');
			String classifier = null;
			String type;
			if (p > 0) {
				type = key.substring( p+1);
				classifier = key.substring(0, p);
			}
			else {
				type = key;
			}
			
			for (PartInformation partInformation : partInformations) {
				if (
						type.equalsIgnoreCase(partInformation.getType()) &&
						(classifier == null || classifier.equalsIgnoreCase( partInformation.getClassifier())) && 
						entry.getValue().equalsIgnoreCase( partInformation.getUrl())						
					) {
					found = true;
					break;
				}					
			}
			if (!found) {
				allfound = false;
				Assert.fail("no matching part found for type [" + key + "] and url [" + entry.getValue() + "]");			
			}						
		}
		return allfound;
	}
	
	

}
