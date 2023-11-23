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
package com.braintribe.codec.marshaller.json;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Date;
import java.util.Locale;

public class DateCoding {

	private DateTimeFormatter inFormatter;
	private DateTimeFormatter outFormatter;
	private String pattern;
	private ZoneId defaultZone;
	private Locale locale;

	public DateCoding(String pattern, ZoneId defaultZone, Locale locale) {
		this.pattern = pattern;
		this.defaultZone = defaultZone;
		this.locale = locale;
		inFormatter = outFormatter = DateTimeFormatter.ofPattern(pattern) //
				.withZone(defaultZone).withLocale(locale);
	}

	public DateCoding(DateTimeFormatter inFormatter, DateTimeFormatter outFormatter) {
		this.defaultZone = ZoneOffset.UTC;
		this.inFormatter = inFormatter;
		this.outFormatter = outFormatter;
	}

	public Date decode(String s) {
		try {
			TemporalAccessor accessor = inFormatter.parse(s);
			LocalTime time = accessor.query(TemporalQueries.localTime());
			LocalDate date = accessor.query(TemporalQueries.localDate());
			ZoneId zone = accessor.query(TemporalQueries.zone());
			if (time == null) {
				time = LocalTime.of(0, 0);
			}
			if (date == null) {
				date = LocalDate.of(0, 1, 1);
			}

			final Date result;
			if (zone != null) {
				result = Date.from(ZonedDateTime.of(date, time, zone).toInstant());
			} else {
				result = Date.from(LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).toInstant());
			}

			return result;
		} catch (Exception e) {
			throw new IllegalArgumentException("Date [" + s + "] could not be parsed with the meta-data mapped format: pattern [" + pattern
					+ "], defaultTimeZone [" + defaultZone + "], locale [" + (locale != null ? locale.toLanguageTag() : "null") + "]", e);
		}
	}

	public String encode(Date date) {
		ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), defaultZone);
		return outFormatter.format(dateTime);
	}

}