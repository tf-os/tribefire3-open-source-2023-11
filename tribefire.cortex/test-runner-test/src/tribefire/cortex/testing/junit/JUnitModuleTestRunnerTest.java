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
package tribefire.cortex.testing.junit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static tribefire.cortex.testing.junit.tests.NotAnActualTest.CRASHING;
import static tribefire.cortex.testing.junit.tests.NotAnActualTest.FAILING;
import static tribefire.cortex.testing.junit.tests.NotAnActualTest.SLOW;
import static tribefire.cortex.testing.junit.tests.NotAnActualTest.SUCCEEDING;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.Slow;
import com.braintribe.testing.test.AbstractTest;

import tribefire.cortex.testing.junit.impl.JUnitModuleTestRunner;
import tribefire.cortex.testing.junit.tests.NotAnActualTest;
import tribefire.cortex.testrunner.api.RunTests;

@Category(ActualTest.class)
public class JUnitModuleTestRunnerTest extends AbstractTest {

	private static final RunTests request = RunTests.T.create();

	@Before
	public void prepareTests() {
		NotAnActualTest.runTests.clear();
	}

	@Test
	public void testCategories() {
		JUnitModuleTestRunner testRunner = createTestRunner();
		testRunner.setExcludedCategories(new Class[] { ActualTest.class, Slow.class });

		runTests(testRunner);

		assertThat(NotAnActualTest.runTests).containsExactlyInAnyOrder(CRASHING, FAILING, SUCCEEDING);
	}

	@Test
	public void testAll() {
		JUnitModuleTestRunner testRunner = createTestRunner();
		testRunner.setExcludedCategories(new Class[] { ActualTest.class });

		runTests(testRunner);

		assertThat(NotAnActualTest.runTests).containsExactlyInAnyOrder(CRASHING, FAILING, SUCCEEDING, SLOW);
	}

	@Test
	public void testTimeoutFailed() {
		JUnitModuleTestRunner testRunner = createTestRunner();
		testRunner.setExcludedCategories(new Class[] { ActualTest.class });
		testRunner.setTimeoutInMs(100);

		assertThatThrownBy(() -> runTests(testRunner)).isInstanceOf(RuntimeException.class);
	}

	@Test
	public void testTimeoutSucceeded() {
		JUnitModuleTestRunner testRunner = createTestRunner();
		testRunner.setExcludedCategories(new Class[] { ActualTest.class });
		testRunner.setTimeoutInMs(10000);

		runTests(testRunner);

		assertThat(NotAnActualTest.runTests).containsExactlyInAnyOrder(CRASHING, FAILING, SUCCEEDING, SLOW);
	}

	private JUnitModuleTestRunner createTestRunner() {
		JUnitModuleTestRunner jUnitTestRunner = new JUnitModuleTestRunner();
		jUnitTestRunner.setTestClassesSupplier(() -> new Class<?>[] { NotAnActualTest.class });
		return jUnitTestRunner;
	}

	private void runTests(JUnitModuleTestRunner testRunner) {
		assertThat(NotAnActualTest.runTests).isEmpty();

		NotAnActualTest.active = true;
		File reportRootDir = new File("reports");
		reportRootDir.mkdirs();
		testRunner.runTests(request, reportRootDir);
		NotAnActualTest.active = false;

	}
}
