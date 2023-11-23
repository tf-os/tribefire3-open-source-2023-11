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
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

/**
 * Provides tests for {@link MathTools}.
 *
 * @author michael.lafite
 */

public class MathToolsTest {

	@Test
	public void testNormalizeAndParseDecimalString() {
		assertEquals(new BigDecimal("123"), MathTools.normalizeAndParseDecimalString("123"));
		assertEquals(new BigDecimal("123.45"), MathTools.normalizeAndParseDecimalString("123.45"));
		assertEquals(new BigDecimal("123.45"), MathTools.normalizeAndParseDecimalString("123,45"));
		assertEquals(new BigDecimal("123.45"), MathTools.normalizeAndParseDecimalString("1,2,3,45"));
		assertEquals(new BigDecimal("123.45"), MathTools.normalizeAndParseDecimalString("1.2.3,45"));
		assertEquals(new BigDecimal(".45"), MathTools.normalizeAndParseDecimalString(",45"));
		assertEquals(new BigDecimal("1234.567"), MathTools.normalizeAndParseDecimalString("1.234,567"));

		assertEquals(new BigDecimal("12345.67"), MathTools.normalizeAndParseDecimalString("12.345,67"));
		assertEquals(new BigDecimal("12345.67"), MathTools.normalizeAndParseDecimalString("12345,67"));
		assertEquals(new BigDecimal("12345.67890"), MathTools.normalizeAndParseDecimalString("1.2.3.4.5.67890"));

		assertEquals("" + 12345.6789f, "" + MathTools.normalizeAndParseDecimalString("1.2.3.4.5.67890").floatValue());
	}

	@Test
	public void testRemoveTrailingZeros() {
		final BigDecimal decimal = new BigDecimal(0);
		assertEquals(decimal.toString(), MathTools.removeTrailingZeros(decimal).toString());
	}

	@Test
	public void testGetValueOrMinimumOrMaximum() {
		assertThat(MathTools.getValueOrMinimumOrMaximum(1, null, null)).isEqualTo(1);
		assertThat(MathTools.getValueOrMinimumOrMaximum(1, 0, null)).isEqualTo(1);
		assertThat(MathTools.getValueOrMinimumOrMaximum(1, null, 2)).isEqualTo(1);
		assertThat(MathTools.getValueOrMinimumOrMaximum(1, 2, 3)).isEqualTo(2);
		assertThat(MathTools.getValueOrMinimumOrMaximum(1, 0, 0)).isEqualTo(0);
	}

	@Test
	public void testClipping() {
		assertThat(MathTools.clip(0, 0, 0)).isEqualTo(0);
		assertThat(MathTools.clip(-1, 0, 10)).isEqualTo(0);
		assertThat(MathTools.clip(11, 0, 10)).isEqualTo(10);
		assertThat(MathTools.clip(5, 0, 10)).isEqualTo(5);
		assertThat(MathTools.clip(-2, -1, 1)).isEqualTo(-1);
		assertThat(MathTools.clip(0, 0, 10)).isEqualTo(0);
		assertThat(MathTools.clip(10, 0, 10)).isEqualTo(10);

		assertThat(MathTools.clip(0L, 0L, 0L)).isEqualTo(0L);
		assertThat(MathTools.clip(-1L, 0L, 10L)).isEqualTo(0L);
		assertThat(MathTools.clip(11L, 0L, 10L)).isEqualTo(10L);
		assertThat(MathTools.clip(5L, 0L, 10L)).isEqualTo(5L);
		assertThat(MathTools.clip(-2L, -1L, 1L)).isEqualTo(-1L);
		assertThat(MathTools.clip(0L, 0L, 10L)).isEqualTo(0L);
		assertThat(MathTools.clip(10L, 0L, 10L)).isEqualTo(10L);

		try {
			assertThat(MathTools.clip(0, 1, -1)).isEqualTo(0);
			throw new AssertionError("This should have thrown an exception.");
		} catch (IllegalArgumentException expected) {
			// OK
		}
	}
}
