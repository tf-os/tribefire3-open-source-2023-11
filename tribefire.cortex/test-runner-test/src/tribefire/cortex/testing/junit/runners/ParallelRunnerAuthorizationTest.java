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
package tribefire.cortex.testing.junit.runners;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.braintribe.testing.category.SpecialEnvironment;

import tribefire.cortex.testing.junit.ActualTest;
import tribefire.cortex.testing.junit.runner.AuthorizingParallelRunner;

/**
 * This unit test only tests the parallelization but not the authorization of the corresponding thread. There is a
 * dedicated integration test for that.
 *
 * @author Neidhart.Orlich
 */
@Category({SpecialEnvironment.class, ActualTest.class}) // does not work with BtAntTasks as it doesn't support custom runners
@RunWith(AuthorizingParallelRunner.class)
public class ParallelRunnerAuthorizationTest {
	private static final Set<Thread> previousThreads = new HashSet<>();

	@Test
	public void test1() throws Exception {
		test();
	}

	@Test
	public void test2() throws Exception {
		test();
	}

	@Test
	public void test3() throws Exception {
		test();
	}

	@Test
	public void test4() throws Exception {
		test();
	}

	private void test() throws Exception {
		assertThat(previousThreads.add(Thread.currentThread())).as("Executed several tests in the same thread").isTrue();

		Thread.sleep(10);
	}

}
