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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import com.braintribe.testing.category.SpecialEnvironment;

import tribefire.cortex.testing.junit.ActualTest;
import tribefire.cortex.testing.junit.runner.AuthorizingParallelParameterized;

/**
 * This unit test only tests the parallelization but not the authorization of the corresponding thread. There is a
 * dedicated integration test for that.
 *
 * @author Neidhart.Orlich
 *
 */
@Category({SpecialEnvironment.class, ActualTest.class}) // does not work with BtAntTasks as it doesn't support custom runners
@RunWith(AuthorizingParallelParameterized.class)
public class ParallelParameterizedAuthorizationTest {
	private static final Set<Thread> prevoiusThreads = new HashSet<>();
	private static final Object[][] mockParameters = { { 12, "777" }, { 23, "777" }, { 34, "777" } };
	private int a;
	private String b;

	@Parameters(name = "{index}: {0},{1}")
	public static Iterable<Object[]> data() throws Exception {
		return Arrays.asList(mockParameters);
	}

	public ParallelParameterizedAuthorizationTest(int a, String b) {
		this.a = a;
		this.b = b;

	}

	@Test
	public void test1() throws Exception {
		// As opposed to the AuthorizingParallelRunner, the AuthorizingParallelParameterized will only parallelize
		// groups of test methods
		// So all tests with the same parameters run in the same thread.
		assertThat(prevoiusThreads.add(Thread.currentThread()))
				.as("Executed several tests in the same thread even though they had different parameters: " + Thread.currentThread()).isTrue();
		Thread.sleep(10);
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

	private void test() throws Exception {
		assertThat(a).isIn(12, 23, 34);
		assertThat(b).isEqualTo("777");
	}

}
