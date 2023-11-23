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
package com.braintribe.utils.format.api;

import java.util.Date;
import java.util.List;

import com.braintribe.model.time.CalendarOffset;
import com.braintribe.model.time.DateOffset;
import com.braintribe.model.time.TimeZoneOffset;

// All dates are handled for UTC timezone
public interface CustomDateFormat {

	String formatDate(Date date, String pattern);
	
	Date parseDate(String dateString, String pattern);
	
	int getYear(Date date);
	
	int getMonth(Date date);
	
	int getDay(Date date);
	
	int getHour(Date date);
	
	int getMinute(Date date);
	
	int getSecond(Date date);
	
	int getMilliSecond(Date date);
	
	int getDayMax(Date date);
	
	Date setYear(Date date, int value);
	
	Date setMonth(Date date, int value);
	
	Date setDay(Date date, int value);
	
	Date setHour(Date date, int value);
	
	Date setMinute(Date date, int value);
	
	Date setSecond(Date date, int value);
	
	Date setMilliSecond(Date date, int value);
	
	Date addYear(Date date, int value);
	
	Date addMonth(Date date, int value);
	
	Date addDay(Date date, int value);
	
	Date addHour(Date date, int value);
	
	Date addMinute(Date date, int value);
	
	Date addSecond(Date date, int value);
	
	Date addMilliSecond(Date date, int value);
	
	Date addMicroSecond(Date date, int value);
	
	Date addNanoSecond(Date date, int value);
	
	Date shiftForTimeZone(Date date, int value);
	
	Date createDateFromOffsetList(List<CalendarOffset> offsetList);
	
	Date addCalendarOffsetToDate(Date date, CalendarOffset offset);
	
	Date addDateOffsetToDate(Date date, DateOffset offset);
	
	Date addTimeZoneOffsetToDate(Date date, TimeZoneOffset offset);
	
}
