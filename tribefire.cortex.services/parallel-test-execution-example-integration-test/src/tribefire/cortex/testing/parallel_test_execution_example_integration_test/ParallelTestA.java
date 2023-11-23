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
package tribefire.cortex.testing.parallel_test_execution_example_integration_test;

import org.junit.Test;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.meta.GmMetaModel;

/**
 * Provides example tests which take a few seconds to execute. This can be used to test parallel test execution. For
 * more information see {@link AbstractParallelExecutionTest}.
 */
public class ParallelTestA extends AbstractParallelExecutionTest {

	@Test
	public void testDeployables() {
		assertExists(Deployable.T, "cortex");
	}

	@Test
	public void testIncrementAccesses() {
		assertExists(IncrementalAccess.T, "cortex");
	}

	@Test
	public void testGmMetaModels() {
		assertExists(GmMetaModel.T, "cortex");
	}
}
