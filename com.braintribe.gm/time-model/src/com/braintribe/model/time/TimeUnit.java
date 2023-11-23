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

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.braintribe.model.generic.annotation.GwtIncompatible;
import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * Units of measurement for time.
 *
 */
public enum TimeUnit implements EnumBase {
	planckTime, nanoSecond, microSecond, milliSecond, second, minute, hour, day;

	public static final EnumType T = EnumTypes.T(TimeUnit.class);
	
	@Override
	public EnumType type() {
		return T;
	}
	
	public static TimeUnit parseSymbol(String s) {
		switch (s) {
			case "d": return day;
			case "h": return hour;
			case "\u00b5s": return microSecond; 
			case "ms": return milliSecond;
			case "m": return minute;
			case "ns": return nanoSecond;
			case "pt": return planckTime;
			case "s": return second;
			default:
				throw new IllegalArgumentException("no unit registered for symbol: " + s);
		}
	}
	
	@GwtIncompatible
	public TimeUnit fromJavaUnit(java.util.concurrent.TimeUnit unit) {
		switch (unit) {
			case NANOSECONDS: return nanoSecond;
			case MICROSECONDS: return microSecond;
			case MILLISECONDS: return milliSecond;
			case SECONDS: return second;
			case MINUTES: return minute;
			case HOURS: return hour;
			case DAYS: return day;
			default:
				throw new IllegalStateException("Unsupported Java time unit '" + unit + "'");
		}
	}
	
	@GwtIncompatible
	public java.util.concurrent.TimeUnit toJavaUnit() {
		switch (this) {
			case nanoSecond: return java.util.concurrent.TimeUnit.NANOSECONDS;
			case microSecond: return java.util.concurrent.TimeUnit.MICROSECONDS;
			case milliSecond: return java.util.concurrent.TimeUnit.MILLISECONDS;
			case second: return java.util.concurrent.TimeUnit.SECONDS;
			case minute: return java.util.concurrent.TimeUnit.MINUTES;
			case hour: return java.util.concurrent.TimeUnit.HOURS;
			case day: return java.util.concurrent.TimeUnit.DAYS;
			default:
				throw new IllegalStateException("Unsupported time unit '" + this + "'");
		}
	}
	
	public String toSymbol() {
		switch (this) {
			case day: return "d";
			case hour: return "h";
			case microSecond: return "\u00b5s"; 
			case milliSecond: return "ms";
			case minute: return "m";
			case nanoSecond: return "ns";
			case planckTime: return "pt";
			case second: return "s";
			default:
				throw new IllegalStateException("no symbol registered for: " + this);
			
		}
	}
	
	public double getScaleFactor(TimeUnit unit) {
		double factor = getWeight() / unit.getWeight();
		return factor;
	}
	
	private double getWeight() {
		switch (this) {
			case planckTime: return 5.39056e-44; // don't rely on that too much ;-)
			case nanoSecond: return 1D;
			case microSecond: return 1E3D;
			case milliSecond: return 1E6D;
			case second: return 1E9D;
			case minute: return 60 * 1E9D;
			case hour: return 60 * 60 * 1E9D;
			case day: return 60 * 60 * 24 * 1E9D;
			default:
				throw new UnsupportedOperationException("unsupported time unit " + this);
		}
	}
	
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
	
	
	public static TimeUnit getFloorUnit(double milliSeconds) {
		Entry<Double, TimeUnit> floorEntry = unitMap.floorEntry(milliSeconds);
		return floorEntry != null? floorEntry.getValue(): nanoSecond;
	}

}
