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

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.model.user.Group;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;

/**
 * Similar to {@link ParallelTestA}.
 */
public class ParallelTestB extends AbstractParallelExecutionTest {

	@Test
	public void testUsers() {
		assertExists(User.T, "auth");
	}

	// This test may fail in a standard tribefire demo (since it won't have any groups)
	@Ignore
	@Test
	public void testGroups() {
		assertExists(Group.T, "auth");
	}

	@Test
	public void testRoles() {
		assertExists(Role.T, "auth");
	}
}
