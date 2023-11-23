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
package com.braintribe.common.lcd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.utils.MathTools;

public class NumbersTest {

	public final static long NANOS_PER_MILLISECOND = 1_000_000L;
	public final static long NANOS_PER_SECOND = NANOS_PER_MILLISECOND * 1_000L;
	public final static long NANOS_PER_MINUTE = NANOS_PER_SECOND * 60;
	public final static long NANOS_PER_HOUR = NANOS_PER_MINUTE * 60;
	public final static long NANOS_PER_DAY = NANOS_PER_HOUR * 24;

	@Test
	public void testNumbersValidity() {

		// This is necessary as nobody realized that these values were defined as integer and therefore became negative

		assertThat(Numbers.NANOSECONDS_PER_MILLISECOND).isEqualTo(NANOS_PER_MILLISECOND);
		assertThat(Numbers.NANOSECONDS_PER_SECOND).isEqualTo(NANOS_PER_SECOND);
		assertThat(Numbers.NANOSECONDS_PER_MINUTE).isEqualTo(NANOS_PER_MINUTE);
		assertThat(Numbers.NANOSECONDS_PER_HOUR).isEqualTo(NANOS_PER_HOUR);
		assertThat(Numbers.NANOSECONDS_PER_DAY).isEqualTo(NANOS_PER_DAY);

		assertThat(Numbers.NANOSECONDS_PER_DAY).isGreaterThan(Numbers.NANOSECONDS_PER_HOUR);
		assertThat(Numbers.NANOSECONDS_PER_HOUR).isGreaterThan(Numbers.NANOSECONDS_PER_MINUTE);
		assertThat(Numbers.NANOSECONDS_PER_MINUTE).isGreaterThan(Numbers.NANOSECONDS_PER_SECOND);
		assertThat(Numbers.NANOSECONDS_PER_SECOND).isGreaterThan(Numbers.NANOSECONDS_PER_MILLISECOND);

		assertThat(Numbers.NANOSECONDS_PER_DAY).isGreaterThan(0L);
		assertThat(Numbers.NANOSECONDS_PER_DAY).isLessThan(Long.MAX_VALUE); // Dumb test? Yes, it is but what the heck

		assertThat(Numbers.BYTE).isEqualTo(1);
		assertThat(Numbers.KIBIBYTE).isEqualTo(1024L);
		assertThat(Numbers.MEBIBYTE).isEqualTo(MathTools.power(Numbers.KIBIBYTE, 2));
		assertThat(Numbers.GIBIBYTE).isEqualTo(MathTools.power(Numbers.KIBIBYTE, 3));
		assertThat(Numbers.TEBIBYTE).isEqualTo(MathTools.power(Numbers.KIBIBYTE, 4));
		assertThat(Numbers.PEBIBYTE).isEqualTo(MathTools.power(Numbers.KIBIBYTE, 5));
		assertThat(Numbers.EXBIBYTE).isEqualTo(MathTools.power(Numbers.KIBIBYTE, 6));

		assertThat(Numbers.KILOBYTE).isEqualTo(1000L);
		assertThat(Numbers.MEGABYTE).isEqualTo(MathTools.power(Numbers.KILOBYTE, 2));
		assertThat(Numbers.GIGABYTE).isEqualTo(MathTools.power(Numbers.KILOBYTE, 3));
		assertThat(Numbers.TERABYTE).isEqualTo(MathTools.power(Numbers.KILOBYTE, 4));
		assertThat(Numbers.PETABYTE).isEqualTo(MathTools.power(Numbers.KILOBYTE, 5));
		assertThat(Numbers.EXABYTE).isEqualTo(MathTools.power(Numbers.KILOBYTE, 6));
	}

}
