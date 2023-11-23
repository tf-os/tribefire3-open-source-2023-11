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
package com.braintribe.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.common.lcd.Condition;

/**
 * Provides tests for {@link CommonTools}.
 *
 * @author michael.lafite
 */

public class CommonToolsTest {

	@Test
	public void testWait() {
		final long timeToWait = 500;
		long time = System.currentTimeMillis();
		CommonTools.wait(timeToWait, null, null);
		assertThat(System.currentTimeMillis()).isGreaterThanOrEqualTo(time + timeToWait);

		final Condition breakCondition = new Condition() {
			@Override
			public boolean evaluate() {
				return true;
			}
		};
		time = System.currentTimeMillis();
		CommonTools.wait(timeToWait, breakCondition, 10);
		assertThat(System.currentTimeMillis()).isLessThan(time + timeToWait);
	}

}
