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
package tribefire.cortex.asset.resolving.test;

import java.io.File;

import org.junit.After;
import org.junit.Before;

import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

import tribefire.cortex.asset.resolving.ng.api.AssetDependencyResolver;
import tribefire.cortex.asset.resolving.ng.api.AssetResolutionContext;
import tribefire.cortex.asset.resolving.ng.api.PlatformAssetResolution;
import tribefire.cortex.asset.resolving.ng.impl.PlatformAssetSolution;
import tribefire.cortex.asset.resolving.test.wire.AssetResolvingTestWireModule;
import tribefire.cortex.asset.resolving.test.wire.contract.AssetResolvingTestContract;

public class AssetResolvingTest {
	private WireContext<AssetResolvingTestContract> context;
	private final File yaml = new File( "res/repo-src/repository-skeleton-validation.yml");

	@Before
	public void initialize() {
		context = Wire.context(AssetResolvingTestWireModule.INSTANCE);
	}
	
	@After
	public void dispose() {
		if (context != null)
			context.shutdown();
	}
	
	// This is failing.
	// Also why does a core test depend on an extension??? 
	//@Test
	public void simpleTest() throws Exception {
		AssetDependencyResolver assetResolver = context.contract().assetResolver();
		
		CompiledDependencyIdentification setupDependency = CompiledDependencyIdentification.parse("tribefire.extension.test:asset-test-aggregator#[1.0,1.1)");

		AssetResolutionContext resolutionContext = AssetResolutionContext.build().done();
		
		PlatformAssetResolution assetResolution = assetResolver.resolve(resolutionContext, setupDependency);

		for (PlatformAssetSolution solution: assetResolution.getSolutions()) {
			System.out.println(solution.solution.asString() + " -> " + solution.nature.entityType());
		}

		
		Validator.validate(yaml, assetResolution.getSolutions());
	}
	
}
