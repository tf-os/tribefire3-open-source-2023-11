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
package com.braintribe.devrock.zarathud.test.extraction.classes;

import java.io.File;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.KnownIssue;
@Category(KnownIssue.class)
public class RelevantDevrockExtractorLab extends AbstractClassesRunnerLab {

	private static final String home = "f:/sde/env/devrock/git";
	private static final String localPathToArtifacts = home + "/" + "com.braintribe.devrock.zarathud.test";
	private static final File localDirectory = new File( localPathToArtifacts);
	
	//@Test
	public void test__version_model() {
		File folder = null;
		test( folder, "com.braintribe.gm:version-model#[,]");
	}
	
	@Test
	public void test__essential_artifact_model() {
		File folder = new File( localDirectory, "essential-artifact-model/build");
		test( folder, "com.braintribe.devrock:essential-artifact-model#[,]");
	}

	
	@Test
	public void test__declared_artifact_model() {
		File folder = new File( localDirectory, "declared-artifact-model/build");
		test( folder, "com.braintribe.devrock:declared-artifact-model#[,]");
	}
	
	@Test
	public void test__compiled_artifact_model() {
		File folder = new File( localDirectory, "compiled-artifact-model/build");
		test( folder, "com.braintribe.devrock:compiled-artifact-model#[,]");
	}
	
	@Test
	public void test__analysis_artifact_model() {
		File folder = new File( localDirectory, "analysis-artifact-model/build");
		test( folder, "com.braintribe.devrock:analysis-artifact-model#[,]");
	}
	
	@Test
	public void test__mc_core() {
		File folder = new File( localDirectory, "mc-core/build");
		test( folder, "com.braintribe.devrock:mc-core#[,]");
	}
	

	@Test
	public void test__libary_parameters() {
		File folder = new File( localDirectory, "libray-parameters/build");
		test( folder, "com.braintribe.devrock.zarathud.test:libray-parameters#1.0.1-pc");
	}



	

}

