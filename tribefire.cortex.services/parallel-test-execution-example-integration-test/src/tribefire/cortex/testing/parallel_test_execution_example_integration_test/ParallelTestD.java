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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.googlecode.junittoolbox.ParallelParameterized;

/**
 * Similar to {@link ParallelTestA}, but uses parameterized tests.
 */
@RunWith(ParallelParameterized.class)
public class ParallelTestD extends AbstractParallelExecutionTest {

	EntityType<?> entityType;
	String accessId;

	public ParallelTestD(EntityType<?> entityType, String accessId) {
		this.entityType = entityType;
		this.accessId = accessId;
	}

	@Test
	public void test() {
		assertExists(entityType, accessId);
	}

	@Parameters(name = "Test {index}: Type {0} on access {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ Deployable.T, "cortex" },
			{ IncrementalAccess.T, "cortex" },
			{ GmMetaModel.T, "cortex" },
			{ GmEntityType.T, "cortex" },
			{ GmEnumType.T, "cortex" },
			{ GmBaseType.T, "cortex" },
			{ GmProperty.T, "cortex" }
		});
	}

}
