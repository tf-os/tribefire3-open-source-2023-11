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
package com.braintribe.artifact.processing.core.test.versions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.artifact.processing.ArtifactProcessingCoreExpert;
import com.braintribe.artifact.processing.core.test.AbstractArtifactProcessingLab;
import com.braintribe.artifact.processing.core.test.Commons;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.model.artifact.info.RepositoryOrigin;
import com.braintribe.model.artifact.info.VersionInfo;
import com.braintribe.model.artifact.processing.ArtifactIdentification;
import com.braintribe.model.artifact.processing.HasArtifactIdentification;
import com.braintribe.model.artifact.processing.cfg.repository.RepositoryConfiguration;

/**
 * TestCase for the GetArtifactVersionInfo feature 
 * 
 * 
 * @author pit
 *
 */

public class GetArtifactVersionInfosTest extends AbstractArtifactProcessingLab{
	
	private static final String grpId = "com.braintribe.devrock.test.ranges";
	private static final String artId = "RangeTestTerminal";
	private static final String version = "1.0.1";
	private Map<String, RepoType> map;
	private File configuration = new File( testSetup, "settings.xml");
	
	private List<VersionInfo> qualifiedResult;
	private List<VersionInfo> filteredQualifiedResult;
	
	private List<VersionInfo> unqualifiedResult;
	private List<VersionInfo> filteredUnqualifiedResult;
	
	{
		map = new HashMap<>();		
		map.put( "archive," + new File( testSetup, "archive.zip").getAbsolutePath(), RepoType.singleZip);
		
		
	}
	
	private List<VersionInfo> createVersionInfo( int port, String ... versions) {
		List<VersionInfo> vis = new ArrayList<>(versions.length);
		for (String version : versions) {
			vis.add( createVersionInfo(port, version));
		}
		return vis;
	}
	
	private VersionInfo createVersionInfo( int port, String version) {
		VersionInfo vi = VersionInfo.T.create();
		vi.setVersion(version);

		RepositoryOrigin rO = RepositoryOrigin.T.create();
		rO.setUrl( "http://localhost:" + port + "/archive/");
		rO.setName( "braintribe.Base");
		vi.getRepositoryOrigins().add(rO);		
		return vi;
	}
	
	@Before
	public void before() {
		int port = runBefore(map);
		
		// requires the port being set here 
		String [] expectedVersionsOf_1_0_1 = new String[] {"1.0.1", "1.0.2", "1.0.3", "1.0.4"};
		qualifiedResult = createVersionInfo( port, expectedVersionsOf_1_0_1);
		
		String [] expectedFilteredVersionsOf_1_0_1 = new String[] {"1.0"};
		filteredQualifiedResult = createVersionInfo( port, expectedFilteredVersionsOf_1_0_1);
		
		
		
		String [] expectedVersionsOf_all = new String[] {"1.0.1", "1.0.2", "1.0.3", "1.0.4","1.1.1","1.1.2", "1.2.1-pc"};
		unqualifiedResult = createVersionInfo( port, expectedVersionsOf_all);
		
		String [] expectedFilteredVersions_all =  new String[] {"1.0","1.1", "1.2"};
		filteredUnqualifiedResult = createVersionInfo( port, expectedFilteredVersions_all);				
	}
	
	@After
	public void after() {
		runAfter();
	}
	
	/**
	 * @param is
	 * @param version
	 * @return
	 */
	private VersionInfo findVersionInfo( List<VersionInfo> is, String version) {
		return is.stream().filter( i -> {
			if (i.getVersion().equalsIgnoreCase(version)) {
				return true;
			}
			else 
				return false;
		}).findFirst().orElse(null);
	}	
	
	/**
	 * @param origins
	 * @param name
	 * @return
	 */
	private RepositoryOrigin findRepositoryOrigin( List<RepositoryOrigin> origins, String name) {
		return origins.stream().filter( o -> {
			if (o.getName().equalsIgnoreCase(name))
				return true;
			else
				return false;
		}).findFirst().orElse(null);
	}
	
	private VersionInfo contains( List<VersionInfo> infos, VersionInfo info) {
		return infos.stream().filter( i -> {
			return i.getVersion().equalsIgnoreCase( info.getVersion());
		}).findFirst().orElse( null);
	}
	
	private void validate(List<VersionInfo> expectedVersions, List<VersionInfo> foundVersions) {
		List<VersionInfo> unexpected = new ArrayList<>();
		List<VersionInfo> matched = new ArrayList<>();
		List<VersionInfo> expectedMatchedVersions = new ArrayList<>();
		for (VersionInfo versionInfo : foundVersions) {
			VersionInfo expectedMatchedVersion = contains( expectedVersions, versionInfo);
			if (expectedMatchedVersion == null) {
				unexpected.add(versionInfo);
			}
			else {
				matched.add( versionInfo);
				expectedMatchedVersions.add(expectedMatchedVersion);
			}
		}
		List<VersionInfo> missing = new ArrayList<>( expectedVersions);
		missing.removeAll( expectedMatchedVersions);
		
		if (unexpected.size() != 0) {
			String msg = "Unexpected :" + unexpected.stream().map( vi -> {
				return vi.getVersion();
			}).collect( Collectors.joining( ","));			
			Assert.fail( msg);
		}
		
		if (missing.size() != 0) {
			String msg = "Missing :" + missing.stream().map( vi ->  {
				return vi.getVersion();}).collect( Collectors.joining( ","));
			Assert.fail( msg);
		}
	}

	/**
	 * tests with the {@link ArtifactIdentification} fully qualified per properties
	 */
	@Test
	public void testFullyQualified() {
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, version);		
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( configuration, overridesMap);
		List<VersionInfo> artifactVersions = ArtifactProcessingCoreExpert.getArtifactVersionInfo( repo, hasArtifactIdentification, scopeConfiguration, false);
		
		Assert.assertTrue( "result is unexpectedly empty", artifactVersions.size() > 0);
		
		validate(artifactVersions, qualifiedResult);		
	}

	/**
	 * tests with the {@link ArtifactIdentification} fully qualified per properties
	 */
	@Test
	public void testFullyQualifiedWithoutRevision() {
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, version);		
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( configuration, overridesMap);
		List<VersionInfo> artifactVersions = ArtifactProcessingCoreExpert.getArtifactVersionInfo( repo, hasArtifactIdentification, scopeConfiguration, true);
		
		Assert.assertTrue( "result is unexpectedly empty", artifactVersions.size() > 0);
		
		validate(artifactVersions, filteredQualifiedResult);		
	}

	
	/**
	 * tests with the {@link ArtifactIdentification} partially qualified (no version range) by properties
	 */
	@Test
	public void testPartiallyQualified() {
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, null);
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( configuration, overridesMap);
		List<VersionInfo> artifactVersions = ArtifactProcessingCoreExpert.getArtifactVersionInfo( repo, hasArtifactIdentification, scopeConfiguration, false);
		
		Assert.assertTrue( "result is unexpectedly empty", artifactVersions.size() > 0);
		validate( artifactVersions, unqualifiedResult);		
	}
	

	/**
	 * tests with the {@link ArtifactIdentification} partially qualified (no version range) by properties
	 */
	@Test
	public void testPartiallyQualifiedWithoutRevision() {
		HasArtifactIdentification hasArtifactIdentification = Commons.generateDenotation(grpId, artId, null);
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( configuration, overridesMap);
		List<VersionInfo> artifactVersions = ArtifactProcessingCoreExpert.getArtifactVersionInfo( repo, hasArtifactIdentification, scopeConfiguration, true);
		
		Assert.assertTrue( "result is unexpectedly empty", artifactVersions.size() > 0);
		validate( artifactVersions, filteredUnqualifiedResult);		
	}

}
