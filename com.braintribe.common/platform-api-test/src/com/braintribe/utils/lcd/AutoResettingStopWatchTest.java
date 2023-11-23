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
package com.braintribe.utils.lcd;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.data.Offset;
import org.junit.Test;

/**
 * Tests for {@link AutoResettingStopWatch}
 *
 *
 */
public class AutoResettingStopWatchTest {

	@Test
	public void doesResetAutomatically() throws Exception {
		long start = System.currentTimeMillis();
		final AutoResettingStopWatch stopWatch = new AutoResettingStopWatch();

		long elapsedTime = stopWatch.getElapsedTime();
		start = System.currentTimeMillis();

		long myCalc = System.currentTimeMillis() - start;

		assertThat(elapsedTime).isCloseTo(myCalc, Offset.offset((long) StopWatchTest.ALLOWED_MEASUREMENT_DELTA_IN_MS));

		Thread.sleep(10);

		elapsedTime = stopWatch.getElapsedTime();
		myCalc = System.currentTimeMillis() - start;

		assertThat(elapsedTime).isCloseTo(myCalc, Offset.offset((long) StopWatchTest.ALLOWED_MEASUREMENT_DELTA_IN_MS));
	}

}
