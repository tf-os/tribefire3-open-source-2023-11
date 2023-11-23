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

import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zarathud.test.common.HasCommonFilesystemNode;

public class ComparisonTest extends AbstractComparisonTest implements HasCommonFilesystemNode{
	
	private File input;
	private File output;

	{	
		Pair<File,File> pair = filesystemRoots("extraction");
		input = pair.first;
		output = pair.second;				
	}
	
	@Test 
	public void compareIdenticalExtractionsOnAnalysisArtifactModel() {			
		File fileA = new File( input, "analysis-artifact-model-1.yaml");
		File fileB = new File( input, "analysis-artifact-model-2.yaml");		
		compare( fileA, fileB, new File( output, "analysis-artifact-identical.comparision.yaml"));				
	}
	
	@Test
	public void compareAnalysisArtifactModel() {
		File fileA = new File( input, "analysis-artifact-model-2.0.22.yaml");
		File fileB = new File( input, "analysis-artifact-model-2.0.18.yaml");		
		compare( fileA, fileB, new File( output, "analysis-artifact-model.comparision.yaml"));
	}
	
	@Test 
	public void compareIdenticalExtractionsOnMcCore() {
		File fileA = new File( input, "mc-core-1.yaml");
		File fileB = new File( input, "mc-core-2.yaml");
		File out = new File( output, "mc-core-identical.comparision.yaml");		
		compare( fileA, fileB, out);				
	}
	
	@Test 
	public void compareOnMcCore() {
		File fileA = new File( input, "mc-core-2.0.55.yaml");
		File fileB = new File( input, "mc-core-2.0.54.yaml");
		File out = new File( output, "mc-core.comparision.yaml");		
		compare( fileA, fileB, out);				
	}
	
	@Test
	public void compareLibraries() {
		File fileA = new File( input, "library-base.yaml");
		File fileB = new File( input, "library-other.yaml");		
		compare( fileA, fileB, new File( output, "library.comparision.yaml"));
	}

	
}
