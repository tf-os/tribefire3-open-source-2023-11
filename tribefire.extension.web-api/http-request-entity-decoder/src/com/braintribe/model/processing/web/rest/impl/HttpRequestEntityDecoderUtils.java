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
package com.braintribe.model.processing.web.rest.impl;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;

public class HttpRequestEntityDecoderUtils {

	private static final Set<Property> INVALID_HEADER_PROPERTIES = new HashSet<>(GenericEntity.T.getDeclaredProperties());

	// TODO move this somewhere?
	private static final DateTimeFormatter DATETIME_FORMATTER = 
			new DateTimeFormatterBuilder().optionalStart().appendPattern("yyyy-MM-dd['T'HH[:mm[:ss[.SSS]]]][Z]").optionalEnd()
			.optionalStart().appendPattern("yyyyMMdd['T'HH[mm[ss[SSS]]]][Z]").optionalEnd()
		            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
		            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
		            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
		            .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
		            .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
		            .toFormatter();

	/**
	 * Parse the given date.
	 * 
	 * @param date the string to parse, must not be {@code null}
	 * @return the parsed date
	 * @throws DateTimeParseException if the string has a wrong format
	 * 
	 * @see ZonedDateTime#parse
	 */
	public static Date parseDate(String date) {
		ZonedDateTime result = ZonedDateTime.parse(date, DATETIME_FORMATTER);
		return Date.from(result.toInstant());
	}
	
	public static String serializeDate(Date date) {
		
		Instant instant = date.toInstant();
		// TODO: I don't get why this returns an empty string:
		//		return DATETIME_FORMATTER.format(instant);
		throw new NotImplementedException("Date encoding not supported yet.");
	}
	
	public static List<Property> filterOutInvalidHeaderProperties(List<Property> properties) {
		return properties.stream().filter(property -> !INVALID_HEADER_PROPERTIES.contains(property)).collect(Collectors.toList());
	}
	
	public static void illegalArgument(String message, Object ...values) {
		throw new IllegalArgumentException(String.format(message, values));
	}

	public static void checkArgumentNotNull(Object arg, String name) {
		if(arg == null) {
			illegalArgument("The %s must not be null", name);
		}
	}
}
