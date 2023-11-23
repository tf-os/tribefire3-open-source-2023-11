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
package com.braintribe.model.processing.vde.evaluator.impl.arithmetic;

import java.util.Date;

import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.arithmetic.ArithmeticEvalExpert;
import com.braintribe.model.time.DateOffset;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.model.time.TimeZoneOffset;
import com.braintribe.utils.format.api.CustomDateFormat;
import com.braintribe.utils.format.lcd.FormatTool;

/**
 * Util class that assists with evaluation, used by some implementations of
 * {@link ArithmeticEvalExpert}
 * 
 */
public class ArithmeticVdeUtil {

	/**
	 * identifies the lowest time unit between two TimeSpans
	 */
	public static TimeUnit getTargetTimeUnit(TimeSpan first, TimeSpan second) {

		int lowestValue = Math.min(first.getUnit().ordinal(), second.getUnit().ordinal());

		TimeUnit unit = null;
		if (first.getUnit().ordinal() == lowestValue) {
			unit = first.getUnit();
		} else {
			unit = second.getUnit();
		}

		return unit;
	}

	/**
	 * convert a time unit value to nanoseconds
	 */
	public static double convertNanoSeconds(TimeUnit unit, double value) throws VdeRuntimeException {

		double computationResult = 0;
		switch (unit) {
			case day:
				computationResult = value / (24.0 * 60.0 * 60.0 * 1000000000.0);
				break;
			case hour:
				computationResult = value / (60.0 * 60.0 * 1000000000.0);
				break;
			case minute:
				computationResult = value / (60.0 * 1000000000.0);
				break;
			case second:
				computationResult = value / (1000000000.0);
				break;
			case milliSecond:
				computationResult = value / (1000000.0);
				break;
			case microSecond:
				computationResult = value / (1000.0);
				break;
			case nanoSecond:
				computationResult = value;
				break;
			case planckTime:
				computationResult = 0; // planck time is just too small
				break;

			default:
				throw new VdeRuntimeException("Unknown TimeUnit provided");
		}
		return computationResult;

	}

	/**
	 * convert a time span to nanoseconds
	 */
	public static double getNanoSeconds(TimeSpan span) throws VdeRuntimeException {
		double value = span.getValue();

		double computationResult = 0;
		switch (span.getUnit()) {
			case day:
				computationResult = value * 24 * 60 * 60 * 1000000000;
				break;
			case hour:
				computationResult = value * 60 * 60 * 1000000000;
				break;
			case minute:
				computationResult = value * 60 * 1000000000;
				break;
			case second:
				computationResult = value * 1000000000;
				break;
			case milliSecond:
				computationResult = value * 1000000;
				break;
			case microSecond:
				computationResult = value * 1000;
				break;
			case nanoSecond:
				computationResult = value;
				break;
			case planckTime:
				computationResult = 0; // planck time is just too small
				break;

			default:
				throw new VdeRuntimeException("Unknown TimeUnit provided");
		}
		return computationResult;

	}

	/**
	 * add timeSpan to date
	 */
	public static Date addTimeSpanToDate(Date date, TimeSpan span, double sign) throws VdeRuntimeException {
		int value = (int) (span.getValue() * sign);
		CustomDateFormat dateFormat = FormatTool.getExpert().getDateFormat();
		switch (span.getUnit()) {
			case day:
				return dateFormat.addDay(date, value);
			case hour:
				return dateFormat.addHour(date, value);
			case minute:
				return dateFormat.addMinute(date, value);
			case second:
				return dateFormat.addSecond(date, value);
			case milliSecond:
				return dateFormat.addMilliSecond(date, value);
			case microSecond:
				return dateFormat.addMicroSecond(date, value);
			case nanoSecond:
				return dateFormat.addNanoSecond(date, value);
			case planckTime:
				// Do nothing
				return date;
			default:
				throw new VdeRuntimeException("Unknown TimeUnit provided");

		}
	}

	/**
	 * add dateoffset to date
	 */
	public static Date addDateOffsetToDate(Date date, DateOffset offset, double sign) throws VdeRuntimeException {
		offset.setValue(offset.getValue() * (int) sign);
		Date result = FormatTool.getExpert().getDateFormat().addDateOffsetToDate(date, offset);
		offset.setValue(offset.getValue() * (int) sign);
		return result;
	}

	/**
	 * add timezone offset to date
	 */
	public static Date addTimeZoneOffsetToDate(Date date, TimeZoneOffset offset, double sign) {
		offset.setMinutes(offset.getMinutes() * (int) sign);
		Date result = FormatTool.getExpert().getDateFormat().addTimeZoneOffsetToDate(date, offset);
		offset.setMinutes(offset.getMinutes() * (int) sign);
		return result;
	}

}
