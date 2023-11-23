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
package com.braintribe.devrock.model.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.eclipse.model.resolution.AnalysisArtifactResolutionViewerModel;
import com.braintribe.devrock.eclipse.model.storage.TranspositionContext;
import com.braintribe.devrock.model.transposition.resolution.Transposer;
import com.braintribe.devrock.model.transposition.resolution.context.BasicTranspositionContext;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

public class TranspositionModelLab implements HasCommonFilesystemNode {
	
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("transpose");
		input = pair.first;
		output = pair.second;
					
	}

	private YamlMarshaller marshaller = new YamlMarshaller();
	
	
	
	protected AnalysisArtifactResolutionViewerModel testBuild(String resolutionFilename) {
		
		File resolutionInputFile = new File( input, resolutionFilename);
		AnalysisArtifactResolution resolution;
		
		try (InputStream in = new FileInputStream(resolutionInputFile)) {
			resolution = (AnalysisArtifactResolution) marshaller.unmarshall(in);
		}
		catch (Exception e) {
			throw new IllegalStateException("file [" + resolutionInputFile.getAbsolutePath() + "] can't be unmarshalled", e);	
		}
		
		Transposer transposer = new Transposer();
		TranspositionContext context = BasicTranspositionContext.build().includeDependencies(true).includeDependers(true).includeParents(true).done();
		Map<String, TranspositionContext> tcm = new HashMap<>();
		tcm.put( Transposer.CONTEXT_DEFAULT, context);
		
		AnalysisArtifactResolutionViewerModel vm = transposer.from( tcm, resolution);
		
		File resolutionOutputFile = new File( output, resolutionFilename);
		try (OutputStream out = new FileOutputStream( resolutionOutputFile)){
			marshaller.marshall(out, vm);
		}
		catch( Exception e) {
			throw new IllegalStateException("file [" + resolutionOutputFile.getAbsolutePath() + "] can't be marshalled", e);
		}		
		
		return vm;
		
	}
	//@Test
	public void test_unresolvedDependencies() {
		testBuild( "invalid/unresolved-dependency.dump.tdr.yaml");
	}
	//@Test
	public void test_unresolvedDependenciesMetadata() {
		testBuild( "invalid/unresolved-dependency-through-metadata-mismatch.dump.tdr.yaml");
	}
	//@Test
	public void test_unresolvedImport() {
		testBuild( "invalid/unresolved-import.dump.tdr.yaml");
	}
	//@Test
	public void test_unresolvedParent() {
		testBuild( "invalid/unresolved-parent.dump.tdr.yaml");
	}
	//@Test
	public void test_unresolvedRedirection() {
		testBuild( "invalid/unresolved-redirection.dump.tdr.yaml");
	}
	//@Test
	public void test_mc_core() {
		testBuild( "mc-core.cpr.dump.yaml");
	}
	@Test
	public void test_parents() {
		AnalysisArtifactResolutionViewerModel viewerModel = testBuild( "com.braintribe.devrock.test.t#1.0.1-enriched.yaml");
		System.out.println(viewerModel);
	}
	
}
