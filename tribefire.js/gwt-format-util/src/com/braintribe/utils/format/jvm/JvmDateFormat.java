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
package com.braintribe.utils.format.jvm;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.braintribe.model.time.CalendarOffset;
import com.braintribe.model.time.DateOffset;
import com.braintribe.model.time.TimeZoneOffset;
import com.braintribe.utils.format.api.CustomDateFormat;

public class JvmDateFormat implements CustomDateFormat {

	private static final int MILLIS_PER_MINUTE = 60 * 1000;

	@Override
	public String formatDate(Date date, String pattern) {
		DateTimeFormatter dateFormat = DateTimeFormat.forPattern(pattern);
		DateTime dateTime = createDateTime(date);
		String encodedDate = dateFormat.print(dateTime);
		return encodedDate;
	}

	@Override
	public Date parseDate(String dateString, String pattern) {
		DateTimeFormatter dateFormat = DateTimeFormat.forPattern(pattern);
		DateTime dateTime = dateFormat.parseDateTime(dateString);
		Date date = dateTime.toDate();
		return date;
	}

	@Override
	public int getYear(Date date) {
		DateTime dateTime = createDateTime(date);
		return dateTime.getYear();
	}

	@Override
	public int getMonth(Date date) {
		DateTime dateTime = createDateTime(date);
		return dateTime.getMonthOfYear();
	}

	@Override
	public int getDay(Date date) {
		DateTime dateTime = createDateTime(date);
		return dateTime.getDayOfMonth();
	}

	@Override
	public int getHour(Date date) {
		DateTime dateTime = createDateTime(date);
		return dateTime.getHourOfDay();
	}

	@Override
	public int getMinute(Date date) {
		DateTime dateTime = createDateTime(date);
		return dateTime.getMinuteOfHour();
	}

	@Override
	public int getSecond(Date date) {
		DateTime dateTime = createDateTime(date);
		return dateTime.getSecondOfMinute();
	}

	@Override
	public int getMilliSecond(Date date) {
		DateTime dateTime = createDateTime(date);
		return dateTime.getMillisOfSecond();
	}

	@Override
	public int getDayMax(Date date) {
		DateTime dateTime = createDateTime(date);
		return dateTime.dayOfMonth().getMaximumValue();
	}

	@Override
	public Date setYear(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.setYear(value);
		return dateTime.toDate();
	}

	@Override
	public Date setMonth(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.setMonthOfYear(value);
		return dateTime.toDate();
	}

	@Override
	public Date setDay(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.setDayOfMonth(value);
		return dateTime.toDate();
	}

	@Override
	public Date setHour(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.setHourOfDay(value);
		return dateTime.toDate();
	}

	@Override
	public Date setMinute(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.setMinuteOfHour(value);
		return dateTime.toDate();
	}

	@Override
	public Date setSecond(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.setSecondOfMinute(value);
		return dateTime.toDate();
	}

	@Override
	public Date setMilliSecond(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.setMillisOfSecond(value);
		return dateTime.toDate();
	}

	@Override
	public Date addYear(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.addYears(value);
		return dateTime.toDate();
	}

	@Override
	public Date addMonth(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.addMonths(value);
		return dateTime.toDate();
	}

	@Override
	public Date addDay(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.addDays(value);
		return dateTime.toDate();
	}

	@Override
	public Date addHour(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.addHours(value);
		return dateTime.toDate();
	}

	@Override
	public Date addMinute(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.addMinutes(value);
		return dateTime.toDate();
	}

	@Override
	public Date addSecond(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.addSeconds(value);
		return dateTime.toDate();
	}

	@Override
	public Date addMilliSecond(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.addMillis(value);
		return dateTime.toDate();
	}

	@Override
	public Date addMicroSecond(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.addMillis((int) (value * 0.001));
		return dateTime.toDate();
	}

	@Override
	public Date addNanoSecond(Date date, int value) {
		MutableDateTime dateTime = createMutableDateTime(date);
		dateTime.addMillis((int) (value * 0.000001));
		return dateTime.toDate();
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

	@Override
	public Date createDateFromOffsetList(List<CalendarOffset> offsetList) {
		int year = 0;
		int month = 0;
		int day = 1;
		int hour = 0;
		int minute = 0;
		int second = 0;
		int milliSecond = 0;
		Calendar cal = Calendar.getInstance();
		cal.clear();
		// Timezone should be set prior to the actual date content, otherwise
		// for some reason it is not registered
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));

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
				// Logic for setting timeZone in calendar is borrowed from
				// SimpleDateFormat
				// subParseNumericZone(String text, int start, int sign, int
				// count, boolean colon, CalendarBuilder calb)
				TimeZoneOffset timeZoneOffset = (TimeZoneOffset) offset;
				cal.set(Calendar.ZONE_OFFSET, timeZoneOffset.getMinutes() * MILLIS_PER_MINUTE);
				cal.set(Calendar.DST_OFFSET, 0);

			}
		}

		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, milliSecond);

		return cal.getTime();
	}

	private MutableDateTime createMutableDateTime(Date date) {
		MutableDateTime dateTime = new MutableDateTime(date);
		return dateTime;
	}

	private DateTime createDateTime(Date date) {
		DateTime dateTime = new DateTime(date);
		return dateTime;
	}
}
