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
package com.braintribe.devrock.zarathud.test.extraction.resolving;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.KnownIssue;
@Category(KnownIssue.class)
public class RelevantDevrockExtractorLab extends AbstractResolvingRunnerLab {

	
	@Test
	public void test__version_model() {
		test( "com.braintribe.gm:version-model#[,]");
	}
	
	@Test
	public void test__essential_artifact_model() {
		test( "com.braintribe.devrock:essential-artifact-model#[,]");
	}


	
	@Test
	public void test__declared_artifact_model() {
		test( "com.braintribe.devrock:declared-artifact-model#[,]");
	}
	
	@Test
	public void test__compiled_artifact_model() {
		test( "com.braintribe.devrock:compiled-artifact-model#[,]");
	}
	
	@Test
	public void test__consumable_artifact_model() {
		test( "com.braintribe.devrock:consumable-artifact-model#[,]");
	}
	
	@Test
	public void test__analysis_artifact_model() {
		test( "com.braintribe.devrock:analysis-artifact-model#[,]");
	}
	
	@Test
	public void test__mc_core() {
		test( "com.braintribe.devrock:mc-core#[,]");
	}


	

}

