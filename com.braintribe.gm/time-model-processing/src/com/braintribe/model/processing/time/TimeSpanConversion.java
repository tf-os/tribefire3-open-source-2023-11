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
package com.braintribe.model.processing.time;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;

public class TimeSpanConversion {
	private static NavigableMap<Double, TimeUnit> unitMap = new TreeMap<>();
	
	static {
		unitMap.put(0.000001, TimeUnit.nanoSecond);
		unitMap.put(0.001, TimeUnit.microSecond);
		unitMap.put(1.0, TimeUnit.milliSecond);
		unitMap.put(1000.0, TimeUnit.second);
		unitMap.put(60*1000.0, TimeUnit.minute);
		unitMap.put(60*60*1000.0, TimeUnit.hour);
		unitMap.put(24*60*60*1000.0, TimeUnit.day);
	}
	
	/**
	 * Determines the integer timespan fragment for each unit from {@code maxUnit} to {@code minUnit} and collects it if it is greater than or equal to 1.
	 * 
	 * @return the collected integer timespan fragments
	 */
	public static List<TimeSpan> unitAnalysis(TimeSpan ts, TimeUnit maxUnit, TimeUnit minUnit) {
		int maxOrdinal = maxUnit.ordinal();
		int minOrdinal = minUnit.ordinal();
		
		List<TimeSpan> parts = new ArrayList<>();
		
		for (int ordinal = maxOrdinal; ordinal >= minOrdinal; ordinal--) {
			TimeUnit unit = TimeUnit.values()[ordinal];
			double value = fromTimeSpan(ts).unit(unit).toValue();
			
			double integerPart = Math.floor(value);
			double fractionPart = value - integerPart;
			
			if ((1 - fractionPart) < 0.00001) {
				integerPart++;
				fractionPart = value - integerPart;
			}
			
			if (integerPart > 0) {
				TimeSpan span = TimeSpan.T.create();
				span.setValue(integerPart);
				span.setUnit(unit);
				parts.add(span);
			}
			
			ts = TimeSpan.T.create();
			ts.setValue(fractionPart);
			ts.setUnit(unit);
		}
		
		return parts;
	}
	
	/**
	 * Uses {@link #findUnitBoundaries(TimeSpan, int, TimeUnit, TimeUnit)} to determine upper and lower unit boundary and delegates to {@link #unitAnalysis(TimeSpan, TimeUnit, TimeUnit)} 
	 */
	public static List<TimeSpan> unitAnalysis(TimeSpan ts, int subUnits, TimeUnit maxUnit, TimeUnit minUnit) {
		Pair<TimeUnit, TimeUnit> boundaries = findUnitBoundaries(ts, subUnits, maxUnit, minUnit);
		return unitAnalysis(ts, boundaries.first(), boundaries.second());
	}
	
	/**
	 * Calculates upper and lower unit boundaries for a given timespan. The algorithm takes the {@link #getMaxUnit(TimeSpan)} for the timespan and limits it with the given {@code maxUnit} 
	 * to get the upper boundary. Based on the given {@code subUnits} and starting from the upper boundary the lower boundary candidate is determined and 
	 * then limited with the given {@code minUnit} to get the actual lower boundary.
	 * 
	 * @return a pair of the calculated upper and lower unit boundaries.
	 */
	public static Pair<TimeUnit, TimeUnit> findUnitBoundaries(TimeSpan ts, int subUnits, TimeUnit maxUnit, TimeUnit minUnit) {
		TimeUnit unit = Stream.of(maxUnit, getMaxUnit(ts)).min(TimeUnit::compareTo).get();
		
		int ordinal = Math.max(0, unit.ordinal() - subUnits);
		
		TimeUnit subUnit = TimeUnit.values()[ordinal];
		
		return Pair.of(unit, Stream.of(subUnit, minUnit).max(TimeUnit::compareTo).get());
	}
	
	
	/**
	 * Returns the highest unit where the given timespan has a value greater than or equal to 1
	 */
	public static TimeUnit getMaxUnit(TimeSpan ts) {
		Double milliSeconds = fromTimeSpan(ts).unit(TimeUnit.milliSecond).toValue();
		return unitMap.floorEntry(milliSeconds).getValue();
	}
	
	public static double factorToSeconds(TimeUnit timeUnit) {
		if (timeUnit == null)
			timeUnit = TimeUnit.milliSecond;

		switch (timeUnit) {
			case planckTime:
				return 5.39056e-44; // don't rely on that too much ;-)
			case nanoSecond:
				return 1e-9;
			case microSecond:
				return 1e-6;
			case milliSecond:
				return 1e-3;
			case second:
				return 1;
			case minute:
				return 60;
			case hour:
				return 60 * 60;
			case day:
				return 60 * 60 * 24;
			default:
				throw new UnsupportedOperationException("unsupported time unit " + timeUnit);
		}
	}

	public static TimeSpanBuilder fromTimeSpan(TimeSpan span) {
		return fromValue(span.getValue(), span.getUnit());
	}

	@SuppressWarnings("hiding")
	public static TimeSpanBuilder fromValue(final double value, final TimeUnit timeUnit) {
		return new TimeSpanBuilder() {
			private TimeUnit unit = TimeUnit.second;
			private GmSession session;

			@Override
			public TimeSpanBuilder unit(TimeUnit timeUnit) {
				this.unit = timeUnit;
				return this;
			}

			@Override
			public TimeSpanBuilder session(GmSession session) {
				this.session = session;
				return this;
			}

			@Override
			public double toValue() {
				double factor = factorToSeconds(timeUnit) / factorToSeconds(unit);
				double result = factor * value;
				return result;
			}

			@Override
			public TimeSpan toTimeSpan() {
				double value = toValue();
				TimeSpan span = session != null ? session.create(TimeSpan.T) : TimeSpan.T.create();
				span.setUnit(unit);
				span.setValue(value);
				return span;
			}
		};
	}

	/**
	 * Gives a {@link java.util.concurrent.TimeUnit} for a given {@link com.braintribe.model.time.TimeUnit}.
	 * {@link com.braintribe.model.time.TimeUnit#planckTime} is not supported and throws
	 * {@link UnsupportedOperationException}
	 * 
	 * @param timeUnit
	 *            {@link com.braintribe.model.time.TimeUnit} to be converted
	 * @return {@link java.util.concurrent.TimeUnit} based on given timeUnit
	 */
	public static java.util.concurrent.TimeUnit getJavaTimeUnit(TimeUnit timeUnit) {
		if (timeUnit == null) {
			throw new IllegalArgumentException("Not 'com.braintribe.model.time.TimeUnit' specified!");
		}

		switch (timeUnit) {
			case nanoSecond:
				return java.util.concurrent.TimeUnit.NANOSECONDS;
			case microSecond:
				return java.util.concurrent.TimeUnit.MICROSECONDS;
			case milliSecond:
				return java.util.concurrent.TimeUnit.MILLISECONDS;
			case second:
				return java.util.concurrent.TimeUnit.SECONDS;
			case minute:
				return java.util.concurrent.TimeUnit.MINUTES;
			case hour:
				return java.util.concurrent.TimeUnit.HOURS;
			case day:
				return java.util.concurrent.TimeUnit.DAYS;
			default:
				throw new UnsupportedOperationException("Unsupported time unit '" + timeUnit + "'");
		}
	}

	/**
	 * Gives a {@link Duration} for a given {@link TimeSpan}.<br/>
	 * Attention: this method is potentially rounding and should only be used where this is acceptable!
	 * 
	 * @param timeSpan
	 *            {@link TimeSpan} to be converted
	 * @return {@link Duration} based on given {@link TimeSpan}
	 */
	public static Duration getDuration(TimeSpan timeSpan) {
		TimeUnit timeUnit = timeSpan.getUnit();

		double doubleValue = TimeSpanConversion.fromTimeSpan(timeSpan).unit(timeUnit).toValue();
		long longValue = Double.valueOf(doubleValue).longValue();

		switch (timeUnit) {
			case nanoSecond:
				return Duration.ofNanos(longValue);
			case microSecond:
				return Duration.ofNanos(longValue * 1000);
			case milliSecond:
				return Duration.ofMillis(longValue);
			case second:
				return Duration.ofSeconds(longValue);
			case minute:
				return Duration.ofMinutes(longValue);
			case hour:
				return Duration.ofHours(longValue);
			case day:
				return Duration.ofDays(longValue);
			default:
				throw new UnsupportedOperationException("Unsupported time unit '" + timeUnit + "'");
		}
	}

}
