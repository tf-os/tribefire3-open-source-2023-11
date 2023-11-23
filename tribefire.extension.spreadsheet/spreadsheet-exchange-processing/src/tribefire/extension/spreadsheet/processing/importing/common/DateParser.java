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
package tribefire.extension.spreadsheet.processing.importing.common;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Date;
import java.util.Locale;
import java.util.function.Function;

import com.braintribe.model.meta.data.constraint.DateClipping;

public class DateParser extends DateClipper implements Function<String, Date> {

	private DateTimeFormatter formatter;
	private String pattern;
	private Locale locale;
	private boolean emptyStringsAreNullDates;

	public DateParser(String pattern, ZoneId defaultZone, Locale locale, DateClipping dateClipping, boolean emptyStringsAreNullDates) {
		super(dateClipping, defaultZone);
		this.pattern = pattern;
		this.locale = locale;
		this.emptyStringsAreNullDates = emptyStringsAreNullDates;
		formatter = DateTimeFormatter.ofPattern(pattern) //
				.withZone(defaultZone).withLocale(locale);
	}

	@Override
	public Date apply(String s) {
		if (emptyStringsAreNullDates && s.isEmpty())
			return null;

		try {
			TemporalAccessor accessor = formatter.parse(s);
			LocalTime time = accessor.query(TemporalQueries.localTime());
			LocalDate date = accessor.query(TemporalQueries.localDate());
			ZoneId zone = accessor.query(TemporalQueries.zone());
			if (time == null)
				time = LocalTime.of(0, 0);
			if (date == null)
				date = LocalDate.of(0, 1, 1);
			Date result = Date.from(ZonedDateTime.of(date, time, zone).toInstant());
			result = clip(result);
			return result;
		} catch (Exception e) {
			throw new IllegalArgumentException("Date [" + s + "] could not be parsed with the meta-data mapped format: pattern [" + pattern
					+ "], defaultTimeZone [" + getZoneId() + "], locale [" + locale.toLanguageTag() + "]", e);
		}
	}
}
