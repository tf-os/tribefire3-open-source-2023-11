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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.data.Offset;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.common.lcd.Numbers;

/**
 * Tests for {@link StopWatch}
 *
 *
 */
public class StopWatchTest {

	public static final int ALLOWED_MEASUREMENT_DELTA_IN_MS = 30;

	@Test
	public void basicTest() {
		final long start = System.currentTimeMillis();
		final StopWatch stopWatch = new StopWatch();

		final long elapsedTime = stopWatch.getElapsedTime();

		final long myCalc = System.currentTimeMillis() - start;

		assertThat(elapsedTime).isCloseTo(myCalc, Offset.offset((long) ALLOWED_MEASUREMENT_DELTA_IN_MS));
	}

	@Test
	public void keepsRunning() throws Exception {
		final long start = System.currentTimeMillis();
		final StopWatch stopWatch = new StopWatch();

		long elapsedTime = stopWatch.getElapsedTime();

		long myCalc = System.currentTimeMillis() - start;

		assertThat(elapsedTime).isCloseTo(myCalc, Offset.offset((long) ALLOWED_MEASUREMENT_DELTA_IN_MS));

		Thread.sleep(10);

		elapsedTime = stopWatch.getElapsedTime();
		myCalc = System.currentTimeMillis() - start;

		assertThat(elapsedTime).isCloseTo(myCalc, Offset.offset((long) ALLOWED_MEASUREMENT_DELTA_IN_MS));
	}

	@Test
	public void testIntermediates() throws Exception {
		StopWatch stopWatch = new StopWatch();

		Thread.sleep(10L);

		stopWatch.intermediate("X");

		Thread.sleep(10L);
		String toString = stopWatch.toString();

		assertThat(toString).contains("X:");
		assertThat(stopWatch.getElapsedTimesReport()).contains("X:");
	}

	@Test
	@Ignore("Runs too long")
	// TODO: long running tests
	public void testSeconds() throws Exception {
		final StopWatch stopWatch = new StopWatch();
		Thread.sleep(Numbers.MILLISECONDS_PER_SECOND);
		final long actualSeconds = stopWatch.getElapsedTimeInSeconds();
		assertThat(actualSeconds).isEqualTo(1);
	}

	@Test
	@Ignore("Runs too long")
	// TODO: long running tests
	public void testMinutes() throws Exception {
		final StopWatch stopWatch = new StopWatch();
		Thread.sleep(Numbers.MILLISECONDS_PER_MINUTE);
		final long actualMinutes = stopWatch.getElapsedTimeInMinutes();
		assertThat(actualMinutes).isEqualTo(1);
	}
}
