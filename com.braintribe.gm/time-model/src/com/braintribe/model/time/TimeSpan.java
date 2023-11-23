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
package com.braintribe.model.time;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.GwtIncompatible;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Represents a time period - an amount of time.
 */
public interface TimeSpan extends GenericEntity, Comparable<TimeSpan> {

	EntityType<TimeSpan> T = EntityTypes.T(TimeSpan.class);

	double getValue();
	void setValue(double value);

	TimeUnit getUnit();
	void setUnit(TimeUnit unit);

	/**
	 * creates a {@link TimeSpan} from a standard {@link Duration}
	 */
	@GwtIncompatible
	static TimeSpan fromDuration(Duration duration) {
		return create(duration.getNano(), TimeUnit.second)
				.add(create(duration.getSeconds(), TimeUnit.nanoSecond));
	}

	/**
	 * creates a {@link TimeSpan} from a amount of milliseconds
	 */
	static TimeSpan fromMillies(double value) {
		return create(value, TimeUnit.milliSecond);
	}

	/**
	 * creates a {@link TimeSpan} with a given amount and unit
	 */
	static TimeSpan create(double value, TimeUnit unit) {
		TimeSpan timeSpan = T.create();
		timeSpan.setValue(value);
		timeSpan.setUnit(unit);
		return timeSpan;
	}

	/**
	 * Parses a {@link TimeSpan} in the syntax that {@link #asString()} uses for its output.
	 * 
	 * Examples:
	 * <ul>
	 * 	<li>TimeSpan.parse("1m") -> <b>TimeSpan.create(1, TimeUnit.minute)</b>
	 * 	<li>TimeSpan.parse("60000ms") -> <b>TimeSpan.create(60000.0, TimeUnit.milliSecond)</b>
	 * </ul>
	 */
	static TimeSpan parse(String stringValue) {
		if (stringValue == null) {
			throw new IllegalArgumentException("encode argument cannot be null");
		}

		int i = stringValue.length() - 1;
		
		for (; i >= 0; i--) {
			char c = stringValue.charAt(i);
			if (Character.isDigit(c))
				break;
		}
		
		if (i == -1)
			throw new IllegalArgumentException("Given value [ " + stringValue + " ] doesn't match expected format");

		Double value = Double.parseDouble(stringValue.substring(0, i + 1));
		TimeUnit timeUnit = TimeUnit.parseSymbol(stringValue.substring(i + 1));
		
		return create(value, timeUnit);
	}
	
	/**
	 * Converts this {@link TimeSpan} into a {@link TimeSpan} with another unit
	 */
	default TimeSpan convertTo(TimeUnit unit) {
		double factor = getUnit().getScaleFactor(unit);
		return TimeSpan.create(getValue() * factor, unit);
	}

	/**
	 * Return the time span of this {@link TimeSpan} as milliseconds
	 */
	default double toMillies() {
		if (getUnit() == TimeUnit.milliSecond)
			return getValue();
		else
			return convertTo(TimeUnit.milliSecond).getValue();
	}
	
	/**
	 * Return the time span of this {@link TimeSpan} as integer (long) milliseconds 
	 */
	default long toLongMillies() {
		return (long)toMillies();
	}

	/**
	 * Returns this {@link TimeSpan} as a standard {@link Duration}
	 */
	@GwtIncompatible
	default Duration toDuration() {
		double secondsWithFraction = convertTo(TimeUnit.second).getValue();
		
		long seconds = (long)secondsWithFraction;
		double secondFraction = secondsWithFraction - seconds;
		long nanos = (long)(secondFraction / 1_000_000_000);
		
		return Duration.ofSeconds(seconds, nanos);
	}

	/**
	 * Adds the given {@link TimeSpan} to this {@link TimeSpan} and returns the resulting {@link TimeSpan}
	 * based on the greater of both given {@link TimeUnit}s.
	 */
	default TimeSpan add(TimeSpan o) {
		TimeUnit u1 = getUnit();
		TimeUnit u2 = o.getUnit();
		
		final double v1, v2;
		final TimeUnit unit;
		
		if (u1 == u2) {
			v1 = getValue();
			v2 = o.getValue();
			unit = u1;
		}
		else if (u1.ordinal() > u2.ordinal()) {
			unit = u1;
			v1 = getValue();
			v2 = o.convertTo(unit).getValue();
		}
		else {
			unit = u2;
			v1 = convertTo(unit).getValue();
			v2 = o.getValue();
		}
		
		return create(v1 + v2, unit);
	}

	/**
	 * Subtracts the given {@link TimeSpan} from this {@link TimeSpan} and returns the resulting {@link TimeSpan}
	 */
	default TimeSpan sub(TimeSpan o) {
		TimeUnit u1 = getUnit();
		TimeUnit u2 = o.getUnit();
		
		final double v1, v2;
		final TimeUnit unit;
		
		if (u1 == u2) {
			v1 = getValue();
			v2 = o.getValue();
			unit = u1;
		}
		else if (u1.ordinal() > u2.ordinal()) {
			unit = u1;
			v1 = getValue();
			v2 = o.convertTo(unit).getValue();
		}
		else {
			unit = u2;
			v1 = convertTo(unit).getValue();
			v2 = o.getValue();
		}
		
		return create(v1 - v2, unit);
	}
	
	/**
	 * Returns a string representation of this {@link TimeSpan} using a number part which can be rendered as integer or floating point notation followed by
	 * the symbol of the unit given by {@link TimeUnit#toSymbol()}
	 * 
	 * Examples:
	 * <ul>
	 * 	<li>TimeSpan.create(1, TimeUnit.minute).asString() -> <b>"1m"</b>
	 * 	<li>TimeSpan.create(60000.0, TimeUnit.milliSecond) -> <b>"60000ms"</b>
	 * </ul>
	 */
	default String asString() {
		double value = getValue();
		long intPart = (long)value;
		boolean isInteger = value - intPart == 0;
		return isInteger? 
			intPart + getUnit().toSymbol(): 
			value + getUnit().toSymbol();
	}

	/**
	 * Returns a string representation of this {@link TimeSpan} that finds the floor unit via {@link #floorUnit()} and adds one further sub-unit
	 * if the value of this {@link TimeSpan} has fractions of the floor unit on the sub-unit level. 
	 * 
	 * <p>
	 * Examples:
	 * <ul>
	 * 	<li>TimeSpan.create(3, TimeUnit.min).formatWithFloorUnitAndSubUnit() -> <b>"3m"</b>
	 * 	<li>TimeSpan.create(1.5, TimeUnit.hour).formatWithFloorUnitAndSubUnit() -> <b>"1h 30m"</b>
	 * 	<li>TimeSpan.create(1.5002, TimeUnit.second).formatWithFloorUnitAndSubUnit() -> <b>"1s 500ms"</b>
	 * </ul>
	 * 
	 * <p>
	 * This method is equivalent to {@link #formatWithFloorUnitAndSubUnits(int) formatWithFloorUnitAndSubUnits(1)}.
	 */
	default String formatWithFloorUnitAndSubUnit() {
		return asString(unitAnalysis(1, TimeUnit.day, TimeUnit.microSecond));
	}
	
	/**
	 * Returns a string representation of this {@link TimeSpan} that finds the floor unit via {@link #floorUnit()} and adds {@code n} further sub-units
	 * if the value of this {@link TimeSpan} has fractions of the floor unit on the sub-unit levels. 
	 * 
	 * Examples:
	 * <ul>
	 * 	<li>TimeSpan.create(3, TimeUnit.minute).formatWithFloorUnitAndSubUnits(1) -> <b>"3m"</b>
	 * 	<li>TimeSpan.create(1.5002, TimeUnit.milliSecond).formatWithFloorUnitAndSubUnits(1) -> <b>"1s 500ms"</b>
	 * 	<li>TimeSpan.create(1.5002, TimeUnit.minute).formatWithFloorUnitAndSubUnits(2) -> <b>"1m 30s 12ms"</b>
	 * </ul>
	 */
	default String formatWithFloorUnitAndSubUnits(int n) {
		return asString(unitAnalysis(n, TimeUnit.day, TimeUnit.microSecond));
	}
	
	/**
	 * Returns a string representation of the given {@code spans}.
	 * 
	 * Examples:
	 * <ul>
	 * 	<li>Arrays.asList(TimeSpan.create(100, TimeUnit.hour)) -> <b>100h</b>
	 * 	<li>Arrays.asList(TimeSpan.create(3, TimeUnit.minute), TimeSpan.create(10.5, TimeUnit.second)) -> <b>3m 10.5s</b>
	 * 	<li>Arrays.asList(TimeSpan.create(15, TimeUnit.second), TimeSpan.create(3, TimeUnit.hour), TimeSpan.create(200, TimeUnit.milliSecond)) -> <b>15s 3h 200ms</b>
	 * </ul>
	 */
	static String asString(List<TimeSpan> spans) {
		return spans.stream().map(TimeSpan::asString).collect(Collectors.joining(" "));
	}
	
	/**
	 * Determines the integer timespan fragment for each unit from {@code maxUnit} to {@code minUnit} and collects it if it is greater than or equal to 1.
	 * If no fragments could be determined, a {@link TimeSpan} having value {@code 0.0} and unit {@code minUnit} is collected instead.
	 * 
	 * @return a list of collected integer timespan fragments 
	 */
	default List<TimeSpan> unitAnalysis(TimeUnit maxUnit, TimeUnit minUnit) {
		int maxOrdinal = maxUnit.ordinal();
		int minOrdinal = minUnit.ordinal();
		
		List<TimeSpan> parts = new ArrayList<>();
		
		TimeSpan ts = this;
		
		for (int ordinal = maxOrdinal; ordinal >= minOrdinal; ordinal--) {
			TimeUnit unit = TimeUnit.values()[ordinal];
			double value = ts.convertTo(unit).getValue();
			
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
		
		if (parts.isEmpty()) {
			TimeSpan span = TimeSpan.T.create();
			span.setValue(0);
			span.setUnit(minUnit);
			parts.add(span);
		}
		
		return parts;
	}
	
	/**
	 * Uses {@link #findUnitBoundaries(int, TimeUnit, TimeUnit)} to determine upper and lower unit boundary and delegates to {@link #unitAnalysis(TimeUnit, TimeUnit)} 
	 */
	default List<TimeSpan> unitAnalysis(int subUnits, TimeUnit maxUnit, TimeUnit minUnit) {
		TimeUnit[] boundaries = findUnitBoundaries(subUnits, maxUnit, minUnit);
		return unitAnalysis(boundaries[0], boundaries[1]);
	}
	
	/**
	 * Calculates upper and lower unit boundaries for a given timespan. The algorithm takes the {@link #floorUnit()} for the timespan and limits it with the given {@code maxUnit} 
	 * to get the upper boundary. Based on the given {@code subUnits} and starting from the upper boundary, the lower boundary candidate is determined and 
	 * then limited with the given {@code minUnit} to get the actual lower boundary.
	 * 
	 * @return a pair of the calculated upper and lower unit boundaries.
	 */
	default TimeUnit[] findUnitBoundaries(int subUnits, TimeUnit maxUnit, TimeUnit minUnit) {
		TimeUnit unit = Stream.of(maxUnit, floorUnit()).min(TimeUnit::compareTo).get();
		
		int ordinal = Math.max(0, unit.ordinal() - subUnits);
		
		TimeUnit subUnit = TimeUnit.values()[ordinal];
		
		if (minUnit != null)
			minUnit = Stream.of(subUnit, minUnit).max(TimeUnit::compareTo).get();
		else
			minUnit = subUnit;
			
		return new TimeUnit[] {unit, minUnit};
	}
	
	/**
	 * Calculates upper and lower unit boundaries for a given timespan. The algorithm takes the {@link #floorUnit()} for the timespan and limits it with the given {@code maxUnit} 
	 * to get the upper boundary. Based on the given {@code subUnits} and starting from the upper boundary, the lower boundary candidate is determined.
	 * 
	 * @return a pair of the calculated upper and lower unit boundaries.
	 */
	default TimeUnit[] findUnitBoundaries(int subUnits, TimeUnit maxUnit) {
		return findUnitBoundaries(subUnits, maxUnit, null);
	}
	
	/**
	 * Returns the highest unit where the given timespan has a value greater than or equal to 1
	 */
	default TimeUnit floorUnit() {
		return TimeUnit.getFloorUnit(toMillies());
	}
	
	@Override
	default int compareTo(TimeSpan o) {
		TimeUnit u1 = getUnit();
		TimeUnit u2 = o.getUnit();
		double v1 = getValue();
		final double v2;
		
		if (u1 != u2) {
			v2 = o.getValue();
		}
		else {
			v2 = o.convertTo(u1).getValue();
		}
			
		return new Double(v1).compareTo(v2);
	}
}
