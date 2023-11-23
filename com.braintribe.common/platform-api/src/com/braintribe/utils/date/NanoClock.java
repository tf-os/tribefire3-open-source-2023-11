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
package com.braintribe.utils.date;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Provides a nanosecond-precision clock implementation. This could be used to do time measurement ({@link Clock#instant()}) and printing a duration
 * between two instants with {@link com.braintribe.utils.StringTools#prettyPrintDuration(java.time.Duration, boolean, java.time.temporal.ChronoUnit)}.
 */
public class NanoClock extends Clock {

	private final Clock clock;
	private final long initialNanos;
	private final Instant initialInstant;

	public final static Clock INSTANCE = new NanoClock();

	public NanoClock() {
		this(Clock.systemUTC());
	}

	public NanoClock(final Clock clock) {
		this.clock = clock;
		initialInstant = clock.instant();
		initialNanos = System.nanoTime();
	}

	@Override
	public ZoneId getZone() {
		return clock.getZone();
	}

	@Override
	public Instant instant() {
		return initialInstant.plusNanos(System.nanoTime() - initialNanos);
	}

	@Override
	public Clock withZone(final ZoneId zone) {
		return new NanoClock(clock.withZone(zone));
	}

}
