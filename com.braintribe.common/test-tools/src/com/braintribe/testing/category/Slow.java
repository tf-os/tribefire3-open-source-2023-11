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
package com.braintribe.testing.category;

/**
 * Signals that the respective test is expected to take a relatively long time to execute (e.g. more than 10 seconds). This information can, for
 * example, be used by continuous integration pipelines to exclude these tests to process a pull request more quickly. The respective test will then
 * only be run in periodic builds where developers don't have to wait for the pipeline to finish.
 *
 * Whether or not to mark a test as <code>Slow</code> not only depends on the (expected) test execution time though, but also on the importance of the
 * test and on the total amount of tests in the respective artifact and artifact group. For example, if there 1000s tests, then even even an expected
 * execution time of a single second is quite much. On the other hand, if a developer decided to implement one big test that runs for a minute but
 * covers everything, then that test is a slow but also very important one. Furthermore total test execution time is still resonably fast, since it's
 * just one test.
 *
 * By marking a test as <code>Slow</code> one basically signals that the test is slow AND that it thus should be skipped when testing time is limited.
 *
 * @author michael.lafite
 *
 * @see VerySlow
 */
public interface Slow {
	// no methods required
}
