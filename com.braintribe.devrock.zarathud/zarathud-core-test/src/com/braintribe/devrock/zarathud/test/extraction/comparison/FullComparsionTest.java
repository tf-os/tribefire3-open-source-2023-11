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
package com.braintribe.devrock.zarathud.test.extraction.comparison;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zarathud.test.common.HasCommonFilesystemNode;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.zarathud.model.data.Artifact;

/**
 * 
 * @author pit
 */
public class FullComparsionTest extends AbstractComparisonTest implements HasCommonFilesystemNode {
	
	private File output;

	{	
		Pair<File,File> pair = filesystemRoots("extraction");	
		output = pair.second;				
	}

	private void runTest( String baseTerminal, String otherTerminal, File output) {
		Maybe<Artifact> maybe = extract(baseTerminal);
		if (maybe.isEmpty()) {
			Assert.fail( maybe.whyUnsatisfied().stringify());
		}

		Artifact baseArtifact = maybe.value();
		
		maybe = extract( otherTerminal);
		if (maybe.isEmpty()) {
			Assert.fail( maybe.whyUnsatisfied().stringify());
		}

		Artifact otherArtifact = maybe.value();
		
		compare(baseArtifact, otherArtifact, output);		
	}
	
	@Test
	public void testAnalysisArtifact() {
		runTest( "com.braintribe.devrock:analysis-artifact-model#2.0.22", "com.braintribe.devrock:analysis-artifact-model#2.0.18", new File( output, "analyis-artifact-result.yaml"));
	}

	@Test
	public void testMcCore() {
		runTest( "com.braintribe.devrock:mc-core#2.0.55", "com.braintribe.devrock:mc-core#2.0.54", new File( output, "mc-core-result.yaml"));
	}
	
	@Test
	public void testLibrary() {
		runTest( "com.braintribe.devrock.zarathud.test:library-base#1.0.1-pc", "com.braintribe.devrock.zarathud.test:library-other#1.0.1-pc", new File( output, "test-library"));
	}
}
