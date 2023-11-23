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
package com.braintribe.utils.format.gwt;

import java.util.Date;
import java.util.List;

import com.braintribe.model.time.CalendarOffset;
import com.braintribe.model.time.DateOffset;
import com.braintribe.model.time.TimeZoneOffset;
import com.braintribe.utils.format.api.CustomDateFormat;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public class GwtDateFormat implements CustomDateFormat {

	final static long MILLISECONDS = 1;
	final static long SECONDS = 1000 * MILLISECONDS;
	final static long MINUTES = 60 * SECONDS;
	final static long HOURS = 60 * MINUTES;
	//private static final int MILLIS_PER_MINUTE = 60 * 1000;

	@Override
	public String formatDate(Date date, String pattern) {
		// TODO cache for patterns
		DateTimeFormat format = DateTimeFormat.getFormat(pattern);
		String encodedDate = format.format(date);

		return encodedDate;
	}

	@Override
	public Date parseDate(String dateString, String pattern) {

		DateTimeFormat format = DateTimeFormat.getFormat(pattern);
		Date dateValue = format.parse(dateString);

		return dateValue;
	}

	@Override
	public int getYear(Date date) {
		return Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(date));
	}

	@Override
	public int getMonth(Date date) {
		return Integer.parseInt(DateTimeFormat.getFormat("MM").format(date));
	}

	@Override
	public int getDay(Date date) {
		return Integer.parseInt(DateTimeFormat.getFormat("dd").format(date));
	}

	@Override
	public int getHour(Date date) {
		return Integer.parseInt(DateTimeFormat.getFormat("HH").format(date));
	}

	@Override
	public int getMinute(Date date) {
		return Integer.parseInt(DateTimeFormat.getFormat("mm").format(date));
	}

	@Override
	public int getSecond(Date date) {
		return Integer.parseInt(DateTimeFormat.getFormat("ss").format(date));
	}

	@Override
	public int getMilliSecond(Date date) {
		return Integer.parseInt(DateTimeFormat.getFormat("SSS").format(date));
	}

	@Override
	public int getDayMax(Date date) {
		CalendarUtil.setToFirstDayOfMonth(date);
		CalendarUtil.addMonthsToDate(date, 1);
		CalendarUtil.addDaysToDate(date, -1);
		return getDay(date);
	}

	@SuppressWarnings("deprecation")
	@Override
	public Date setYear(Date date, int value) {
		date.setYear(value - 1900);
		return date;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Date setMonth(Date date, int value) {
		date.setMonth(value);
		return date;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Date setDay(Date date, int value) {
		date.setDate(value);
		return date;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Date setHour(Date date, int value) {
		date.setHours(value);
		return date;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Date setMinute(Date date, int value) {
		date.setMinutes(value);
		return date;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Date setSecond(Date date, int value) {
		date.setSeconds(value);
		return date;
	}

	@Override
	public Date setMilliSecond(Date date, int value) {

		long currentDateTime = date.getTime();
		long milliSecondValue = getMilliSecond(date);
		long dateWithZeroMilliseconds = currentDateTime - milliSecondValue;

		Date resultDate = new Date(dateWithZeroMilliseconds + value);

		return resultDate;
	}

	@Override
	public Date addYear(Date date, int value) {

		CalendarUtil.addMonthsToDate(date, value * 12);
		return date;
	}

	@Override
	public Date addMonth(Date date, int value) {
		CalendarUtil.addMonthsToDate(date, value);
		return date;
	}

	@Override
	public Date addDay(Date date, int value) {
		CalendarUtil.addDaysToDate(date, value);
		return date;
	}

	@Override
	public Date addHour(Date date, int value) {
		date.setTime(date.getTime() + value * HOURS);
		return date;
	}

	@Override
	public Date addMinute(Date date, int value) {
		date.setTime(date.getTime() + value * MINUTES);
		return date;
	}

	@Override
	public Date addSecond(Date date, int value) {
		date.setTime(date.getTime() + value * SECONDS);
		return date;
	}

	@Override
	public Date addMilliSecond(Date date, int value) {
		date.setTime(date.getTime() + value * MILLISECONDS);
		return date;
	}

	@Override
	public Date addMicroSecond(Date date, int value) {
		date.setTime((long) (date.getTime() + value * MILLISECONDS * 0.001));
		return date;
	}

	@Override
	public Date addNanoSecond(Date date, int value) {
		date.setTime((long) (date.getTime() + value * MILLISECONDS * 0.000001));
		return date;
	}

	@Override
	public Date shiftForTimeZone(Date date, int value) {
		return addMinute(date, value);
	}

	@Override
	public Date addCalendarOffsetToDate(Date date, CalendarOffset offset) {
		if (offset instanceof DateOffset) {
			return addDateOffsetToDate(date, (DateOffset) offset);
		} else {
			return addTimeZoneOffsetToDate(date, (TimeZoneOffset) offset);
		}
	}

	@Override
	public Date addDateOffsetToDate(Date date, DateOffset offset) {
		int value = offset.getValue();
		Date result = date;
		switch (offset.getOffset()) {
		case year:
			result = addYear(date, value);
			break;
		case month:
			result = addMonth(date, value);
			break;
		case day:
			result = addDay(date, value);
			break;
		case hour:
			result = addHour(date, value);
			break;
		case minute:
			result = addMinute(date, value);
			break;
		case second:
			result = addSecond(date, value);
			break;
		case millisecond:
			result = addMilliSecond(date, value);
			break;
		}
		return result;
	}

	@Override
	public Date addTimeZoneOffsetToDate(Date date, TimeZoneOffset offset) {
		return shiftForTimeZone(date, offset.getMinutes());
	}

	@SuppressWarnings("deprecation")
	@Override
	public Date createDateFromOffsetList(List<CalendarOffset> offsetList) {
		int year = 0;
		int month = 0;
		int day = 1;
		int hour = 0;
		int minute = 0;
		int second = 0;
		int milliSecond = 0;
		int timeZone = 0;

		for (CalendarOffset offset : offsetList) {
			if (offset instanceof DateOffset) {
				DateOffset dateOffset = (DateOffset) offset;
				int value = dateOffset.getValue();
				switch (dateOffset.getOffset()) {
				case year:
					year = value;
					break;
				case month:
					month = value;
					break;
				case day:
					day = value;
					break;
				case hour:
					hour = value;
					break;
				case minute:
					minute = value;
					break;
				case second:
					second = value;
					break;
				case millisecond:
					milliSecond = value;
					break;
				}
			} else {
				TimeZoneOffset timeZoneOffset = (TimeZoneOffset) offset;
				timeZone = timeZoneOffset.getMinutes();
			}
		}

		if (timeZone != 0) { // this is not UTC format
			minute += -1.0 * timeZone;
		}

		Date result = new Date(Date.UTC(year - 1900, month, day, hour, minute, second));
		result = setMilliSecond(result, milliSecond);

		return result;
	}
}
