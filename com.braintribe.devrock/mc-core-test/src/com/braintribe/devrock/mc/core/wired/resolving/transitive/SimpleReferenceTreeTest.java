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
package com.braintribe.devrock.mc.core.wired.resolving.transitive;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

public class SimpleReferenceTreeTest extends AbstractTransitiveResolverTest {

	@Override
	protected File archiveInput() {
		return new File( input, "simpleReferenceTree.definition.flow.yaml");
	}
	
	
	@Test
	public void runWithoutParents() {
		AnalysisArtifactResolution resolution = run( "com.braintribe.devrock.test:a#1.0.1", standardResolutionContext);
		Assert.assertTrue("didn't expect a NULL return value, but got one", resolution != null);
		Validator validator = new Validator();
		validator.validateYaml( new File ( input, "simpleReferenceTree.validation.flow.yaml"), resolution);
		validator.assertResults();
	}

	@Test
	public void runWithParents() {
		TransitiveResolutionContext resolutionContext = TransitiveResolutionContext.build()
				.includeParentDependencies(true)
				.done();
		AnalysisArtifactResolution resolution = run( "com.braintribe.devrock.test:a#1.0.1", resolutionContext);
		Assert.assertTrue("didn't expect a NULL return value, but got one", resolution != null);
		Validator validator = new Validator();
		validator.validateYaml( new File ( input, "simpleReferenceTree.parents.validation.flow.yaml"), resolution);
		validator.assertResults();
	}
}
