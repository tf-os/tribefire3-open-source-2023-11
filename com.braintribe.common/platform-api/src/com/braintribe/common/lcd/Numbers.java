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

/**
 * Provides constants for frequently used numbers, e.g. {@link Numbers#MILLISECONDS_PER_HOUR} or {@link #MILLION}.
 *
 * @author michael.lafite
 *
 * @see Constants
 */
public final class Numbers {

	/* Constants for simple numbers such as -1, 0, 1, etc. are not really needed. The reason they have been added here were experiments with checks
	 * that complain about "magic numbers" (which makes sense in some cases, but not always) and the idea was to easily avoid those warnings by using
	 * the numbers here. However, the better approach probably is to (find a way to) disable the warnings for these simple numbers, since their
	 * meaning often is self-explaining (in the context they are used). */
	public static final int NEGATIVE_ONE = -1;
	public static final int ZERO = 0;
	public static final int ONE = 1;
	public static final int TWO = 2;
	public static final int TEN = 10;
	public static final int HUNDRED = 100;
	public static final int THOUSAND = 1000;
	public static final int MILLION = THOUSAND * THOUSAND;
	public static final int BILLION = MILLION * THOUSAND;
	public static final int TRILLION = BILLION * THOUSAND;

	public static final int HOURS_PER_DAY = 24;

	public static final int MINUTES_PER_HOUR = 60;
	public static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;

	public static final int SECONDS_PER_MINUTE = 60;
	public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
	public static final int SECONDS_PER_DAY = SECONDS_PER_MINUTE * MINUTES_PER_DAY;
	public static final int DAYS_PER_YEAR = 365;

	public static final int MICROSECONDS_PER_SECOND = MILLION;
	public static final int MICROSECONDS_PER_MILLISECOND = THOUSAND;

	public static final int MILLISECONDS_PER_SECOND = 1000;
	public static final int MILLISECONDS_PER_MINUTE = MILLISECONDS_PER_SECOND * SECONDS_PER_MINUTE;
	public static final int MILLISECONDS_PER_HOUR = MILLISECONDS_PER_SECOND * SECONDS_PER_HOUR;
	public static final int MILLISECONDS_PER_DAY = MILLISECONDS_PER_SECOND * SECONDS_PER_DAY;
	public static final long MILLISECONDS_PER_YEAR = ((long) MILLISECONDS_PER_DAY) * ((long) DAYS_PER_YEAR);

	public static final long NANOSECONDS_PER_MILLISECOND = MILLION;
	public static final long NANOSECONDS_PER_SECOND = NANOSECONDS_PER_MILLISECOND * MILLISECONDS_PER_SECOND;
	public static final long NANOSECONDS_PER_MINUTE = NANOSECONDS_PER_MILLISECOND * MILLISECONDS_PER_MINUTE;
	public static final long NANOSECONDS_PER_HOUR = NANOSECONDS_PER_MILLISECOND * MILLISECONDS_PER_HOUR;
	public static final long NANOSECONDS_PER_DAY = NANOSECONDS_PER_MILLISECOND * MILLISECONDS_PER_DAY;

	public static final int NO_OFFSET = ZERO;

	// see https://en.wikipedia.org/wiki/Kibibyte
	public static final long BYTE = 1L;
	public static final long KIBIBYTE = 1_024L;
	public static final long MEBIBYTE = 1_048_576L;
	public static final long GIBIBYTE = 1_073_741_824L;
	public static final long TEBIBYTE = 1_099_511_627_776L;
	public static final long PEBIBYTE = 1_125_899_906_842_624L;
	public static final long EXBIBYTE = 1_152_921_504_606_846_976L;

	public static final long KILOBYTE = 1_000L;
	public static final long MEGABYTE = 1_000_000L;
	public static final long GIGABYTE = 1_000_000_000L;
	public static final long TERABYTE = 1_000_000_000_000L;
	public static final long PETABYTE = 1_000_000_000_000_000L;
	public static final long EXABYTE = 1_000_000_000_000_000_000L;

	private Numbers() {
		// no instantiation required
	}
}
