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
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.devrock.zarathud.runner.commons.ClasspathResolvingUtil;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.collection.impl.HashListMap;
@Category(KnownIssue.class)
public class MultiFoldersExtractorLab extends AbstractClassesRunnerLab {

	private static final String home = "f:/works/dev-envs/standard/git";
	private static final String localPathToArtifacts = home + "/" + "com.braintribe.devrock";
	private static final File localDirectory = new File( localPathToArtifacts);
	
	
	@Test
	public void test__analysis_artifact_model() {
		Map<String,AnalysisArtifact> nonPackaged = new HashMap<>();		

		File essentialFolder = new File( localDirectory, "essential-artifact-model/build");
		AnalysisArtifact essentialArtifact = identifyActualArtifact( "com.braintribe.devrock:essential-artifact-model#[,]");
		nonPackaged.put( essentialFolder.getAbsolutePath(), essentialArtifact);

		File declaredFolder = new File( localDirectory, "declared-artifact-model/build");
		AnalysisArtifact declaredArtifact = identifyActualArtifact( "com.braintribe.devrock:declared-artifact-model#[,]");
		nonPackaged.put( declaredFolder.getAbsolutePath(), declaredArtifact);
		
		File compiledFolder = new File( localDirectory, "compiled-artifact-model/build");
		AnalysisArtifact compiledArtifact = identifyActualArtifact( "com.braintribe.devrock:compiled-artifact-model#[,]");
		nonPackaged.put( compiledFolder.getAbsolutePath(), compiledArtifact);

		File analysisfolder = new File( localDirectory, "analysis-artifact-model/build");										
		
		test( analysisfolder, nonPackaged, "com.braintribe.devrock:analysis-artifact-model#[,]");
	}

	@Test
	public void test__analysis_artifact_model_as_in_Eclipse() {
		Map<String,AnalysisArtifact> nonPackaged = new HashMap<>();		
		
		File consumeableFolder = new File( localDirectory, "consumable-artifact-model/build");
		AnalysisArtifact essentialArtifact = identifyActualArtifact( "com.braintribe.devrock:consumable-artifact-model#[,]");
		nonPackaged.put( consumeableFolder.getAbsolutePath(), essentialArtifact);
		
		File analysisfolder = new File( localDirectory, "analysis-artifact-model/build");										
		
		test( analysisfolder, nonPackaged, "com.braintribe.devrock:analysis-artifact-model#[,]");
	}


	private AnalysisArtifact identifyActualArtifact(String terminal) {
		CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse(terminal);
		
		Maybe<CompiledArtifactIdentification> caiMaybe = ClasspathResolvingUtil.resolve(cdi, null);
		
		if ( caiMaybe.isUnsatisfied()) {
			throw new IllegalStateException( caiMaybe.whyUnsatisfied().stringify());
		}
		
		CompiledArtifactIdentification cai = caiMaybe.get();
		
		AnalysisArtifact aa = AnalysisArtifact.T.create();
		aa.setGroupId( cai.getGroupId());
		aa.setArtifactId(cai.getArtifactId());
		aa.setVersion( cai.getVersion().asString());
		
		return aa;
	}
	
	
	

	

}

