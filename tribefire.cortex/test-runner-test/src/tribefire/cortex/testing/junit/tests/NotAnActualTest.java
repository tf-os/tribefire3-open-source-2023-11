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
package tribefire.cortex.testing.junit.tests;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.testing.category.VerySlow;

/**
 * This is not an actual test and should never be executed as test. This just simulates a test for the test runners in
 * the actual tests.
 * <p>
 * The {@link SpecialEnvironment} category set on this test prevents this test to be executed in the CI. However it
 * can't be prevented to be executed from other test runners like in eclipse. So please just ignore its failure.
 *
 * @author Neidhart.Orlich
 *
 */
@Category(SpecialEnvironment.class)
public class NotAnActualTest {
	public static final String SUCCEEDING = "succeedingTest";
	public static final String SLOW = "slowTest";
	public static final String FAILING = "failingTest";
	public static final String CRASHING = "crashingTest";

	public static final Collection<String> runTests = new ArrayList<String>();

	public static boolean active;

	@Test
	public void failingTest() {
		if (active) {
			runTests.add(FAILING);
			assertTrue(false);
		}
	}

	@Test
	public void crashingTest() {
		if (active) {
			runTests.add(CRASHING);
			throw new RuntimeException("Intended Exception to simulate a crashing test. Please ignore!");
		}
	}

	@Test
	public void succeedingTest() {
		if (active) {
			runTests.add(SUCCEEDING);
		}
	}

	@Test
	@Category(VerySlow.class)
	public void slowTest() throws Exception {
		if (active) {
			Thread.sleep(500);
			runTests.add(SLOW);
		}
	}
}
