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
package com.braintribe.artifact.processing.core.test.enrich;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
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
import com.braintribe.artifact.processing.core.test.Commons;
import com.braintribe.artifact.processing.core.test.TestUtil;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.processing.QualifiedPartIdentification;
import com.braintribe.model.artifact.processing.cfg.repository.RepositoryConfiguration;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.service.data.ArtifactPartData;
import com.braintribe.model.artifact.processing.service.request.GetArtifactPartData;
import com.braintribe.model.resource.Resource;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.IOTools;


/**
 * test to check the information retrieval feature,
 * 
 * @author pit
 *
 */

@Category(KnownIssue.class)
public class EnrichingTest extends AbstractArtifactProcessingLab {
	
	private static final String grpId = "com.braintribe.devrock.test.ape";
	private static final String artId = "ape-terminal";
	private static final String version = "1.0.1-pc";
	private static final String artifact = grpId + ":" + artId + "#" + version;	
	private Map<String, RepoType> map;
	private File tmp = new File( contents, "tmp");
	
	private List<String> strictRequirements;
	private List<String> lenientRequirements;
			
	{
		// setup for the repolet lauchner
		map = new HashMap<>();		
		map.put( "archive," + new File( testSetup, "archive.zip").getAbsolutePath(), RepoType.singleZip);
		
		strictRequirements = new ArrayList<>();
		strictRequirements.add( "jar");
		strictRequirements.add( "pom");
		
		
		lenientRequirements = new ArrayList<>();
		lenientRequirements.add( "sources:jar");
		lenientRequirements.add( "asset:man");
		
		
	}
	
	@Before
	public void before() {
		int port = runBefore(map);		
		TestUtil.ensure(tmp);
	}
	
	@After
	public void after() {
		runAfter();
	}
	

	private ArtifactPartData test(Map<String,List<PartTuple>> data, RepositoryConfiguration scopeConfiguration) {
		GetArtifactPartData getArtifactPartData = GetArtifactPartData.T.create();
		
		for (Entry<String, List<PartTuple>> entry : data.entrySet()) {
						
			List<PartTuple> tuples = entry.getValue();
			for (PartTuple tuple : tuples) {
				QualifiedPartIdentification qpi = Commons.parameterize( QualifiedPartIdentification.T, entry.getKey());
				qpi.setClassifier( tuple.getClassifier());
				qpi.setType( tuple.getType());
				
				if (strictRequirements.contains( PartTupleProcessor.toString(tuple))) {
					getArtifactPartData.getParts().add(qpi);			
				}
				else {
					getArtifactPartData.getOptionalParts().add(qpi);
				}
			}
		}
				
		ArtifactPartData artifactPartData = ArtifactProcessingCoreExpert.getArtifactPartData(repo, getArtifactPartData, scopeConfiguration);
		
		return artifactPartData;		
	}

	
	/**
	 * test on retrieval : downloaded from repolet
	 */
	@Test
	public void testSimple() {
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		Map<String, List<PartTuple>> data = new HashMap<>();
		data.put(artifact, Collections.singletonList(PartTupleProcessor.createPomPartTuple()));

		
		runTest(scopeConfiguration, data);				 		
	}
	
	/**
	 * test on retrieval : downloaded from repolet
	 */
	@Test
	public void testMore() {
		RepositoryConfiguration scopeConfiguration = generateResourceScopeConfiguration( new File( testSetup, "settings.xml"), overridesMap);
		Map<String, List<PartTuple>> data = new HashMap<>();
		List<PartTuple> tuples = new ArrayList<>();
		tuples.add( PartTupleProcessor.createPomPartTuple());
		tuples.add( PartTupleProcessor.createJarPartTuple());
		tuples.add( PartTupleProcessor.fromString("sources", "jar"));
		tuples.add( PartTupleProcessor.fromString("asset", "man"));
		data.put(artifact, tuples);
		
		runTest(scopeConfiguration, data);				 		
	}


	private String determinePartName( QualifiedPartIdentification qpi) {
		StringBuilder sb = new StringBuilder();
		sb.append( qpi.getArtifactId());
		sb.append( "-");
		sb.append( qpi.getVersion());
		if (qpi.getClassifier() != null && qpi.getClassifier().length() > 0) {
			sb.append( "-");
			sb.append( qpi.getClassifier());
		}
		sb.append( ".");
		sb.append( qpi.getType());
		return sb.toString();
	}
	
	private void runTest(RepositoryConfiguration scopeConfiguration, Map<String, List<PartTuple>> data) {
		ArtifactPartData result = test( data, scopeConfiguration);
		Assert.assertTrue("no data returned", result != null);
				
		 
		Map<QualifiedPartIdentification, Resource> response = result.getData();
		Assert.assertTrue("no result map in response", response != null);
		
		for (Entry<QualifiedPartIdentification, Resource> entry : response.entrySet()) {
			Resource resource = entry.getValue();
			if (resource == null) {				
				QualifiedPartIdentification qpi = entry.getKey();
				String key;
				if (qpi.getClassifier() == null) {
					key = qpi.getClassifier() + ":" + qpi.getType();
				}
				else {
					key = qpi.getType();							
				}
				if (strictRequirements.contains( key)) {
					Assert.fail( "no file found for [" + determinePartName( qpi) + "]");
				}
				continue;
			}
			
			File outFile = new File( tmp, determinePartName( entry.getKey()));
			try (
					InputStream in = resource.openStream();
					OutputStream out = new FileOutputStream( outFile)
				) {
				IOTools.pump(in, out, IOTools.SIZE_64K);
			}
			catch (IOException e) {
				Assert.fail("cannot process offered stream for file [" + outFile.getName() + "]");
			}
			
		}
	}
		

	
}
