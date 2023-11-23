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
package com.braintribe.utils;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

import com.braintribe.common.lcd.Numbers;

public class DateToolsTest {

	@Test
	public void testISO8601() {
		String dateString = "2018-03-28T17:16:56+0200";

		ZonedDateTime dt = ZonedDateTime.parse(dateString, DateTools.ISO8601_DATE_FORMAT);
		Date actual = Date.from(dt.toInstant());

		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(actual);
		gc.setTimeZone(TimeZone.getTimeZone("GMT"));

		assertThat(gc.get(Calendar.DAY_OF_MONTH)).isEqualTo(28);
		assertThat(gc.get(Calendar.MONTH)).isEqualTo(2);
		assertThat(gc.get(Calendar.YEAR)).isEqualTo(2018);
		assertThat(gc.get(Calendar.HOUR_OF_DAY)).isEqualTo(15); // timezone is set to GMT; hence 2 less
		assertThat(gc.get(Calendar.MINUTE)).isEqualTo(16);
		assertThat(gc.get(Calendar.SECOND)).isEqualTo(56);
		assertThat(gc.get(Calendar.MILLISECOND)).isEqualTo(0);
		assertThat(gc.get(Calendar.ZONE_OFFSET)).isEqualTo(0);
	}

	@Test
	public void testISO8601WithMs() {
		String dateString = "2018-03-28T17:16:56.048+0200";

		ZonedDateTime dt = ZonedDateTime.parse(dateString, DateTools.ISO8601_DATE_WITH_MS_FORMAT);
		Date actual = Date.from(dt.toInstant());

		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(actual);
		gc.setTimeZone(TimeZone.getTimeZone("GMT"));

		assertThat(gc.get(Calendar.DAY_OF_MONTH)).isEqualTo(28);
		assertThat(gc.get(Calendar.MONTH)).isEqualTo(2);
		assertThat(gc.get(Calendar.YEAR)).isEqualTo(2018);
		assertThat(gc.get(Calendar.HOUR_OF_DAY)).isEqualTo(15); // timezone is set to GMT; hence 2 less
		assertThat(gc.get(Calendar.MINUTE)).isEqualTo(16);
		assertThat(gc.get(Calendar.SECOND)).isEqualTo(56);
		assertThat(gc.get(Calendar.MILLISECOND)).isEqualTo(48);
		assertThat(gc.get(Calendar.ZONE_OFFSET)).isEqualTo(0);

		gc.set(Calendar.MILLISECOND, 12);
		System.out.println("Calendar: " + gc.toString());
		String encoded = DateTools.encode(gc, DateTools.ISO8601_DATE_WITH_MS_FORMAT);
		assertThat(encoded).isEqualTo("2018-03-28T15:16:56.012+0000");
	}

	@Test
	public void testCharsInDate() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMM/ddHH/mmss").withLocale(Locale.US);

		GregorianCalendar gc = new GregorianCalendar();
		gc.set(Calendar.DAY_OF_MONTH, 1);
		gc.set(Calendar.MONTH, 2);
		gc.set(Calendar.YEAR, 2018);
		gc.set(Calendar.HOUR_OF_DAY, 3);
		gc.set(Calendar.MINUTE, 4);
		gc.set(Calendar.SECOND, 5);

		String encoded = DateTools.encode(gc.getTime(), formatter);

		assertThat(encoded).isEqualTo("1803/0103/0405");
	}

	@Test
	public void testLocalDateTime() {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").withLocale(Locale.US);
		LocalDateTime now = LocalDateTime.now();
		String nowString = dateFormatter.format(now);
		System.out.println("nowString: " + nowString);
		assertThat(nowString.length()).isEqualTo(19);
	}

	@Test
	public void testDateOnly() throws Exception {
		Date parseDate = DateTools.parseDate("2018.10.09");
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(parseDate);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		assertThat(day).isEqualTo(9);
	}

	@Test
	public void testParseTimeSpan() {
		assertThat(DateTools.parseTimeSpan("1 s", null)).isEqualTo(Numbers.MILLISECONDS_PER_SECOND);
		assertThat(DateTools.parseTimeSpan("1s", null)).isEqualTo(Numbers.MILLISECONDS_PER_SECOND);
		assertThat(DateTools.parseTimeSpan(" 1 s ", null)).isEqualTo(Numbers.MILLISECONDS_PER_SECOND);
		assertThat(DateTools.parseTimeSpan("1 s 1 s", null)).isEqualTo((long) Numbers.MILLISECONDS_PER_SECOND * 2);
		assertThat(DateTools.parseTimeSpan("1min", null)).isEqualTo(Numbers.MILLISECONDS_PER_MINUTE);
		assertThat(DateTools.parseTimeSpan("1 h", null)).isEqualTo(Numbers.MILLISECONDS_PER_HOUR);
		assertThat(DateTools.parseTimeSpan("1 d", null)).isEqualTo(Numbers.MILLISECONDS_PER_DAY);
		assertThat(DateTools.parseTimeSpan("1 m", null)).isEqualTo(((long) Numbers.MILLISECONDS_PER_DAY) * 30);
		assertThat(DateTools.parseTimeSpan("1 y", null)).isEqualTo(((long) Numbers.MILLISECONDS_PER_DAY) * 365);
		assertThat(DateTools.parseTimeSpan("3 y", null)).isEqualTo(((long) Numbers.MILLISECONDS_PER_DAY) * 365 * 3);
		assertThat(DateTools.parseTimeSpan("3y", null)).isEqualTo(((long) Numbers.MILLISECONDS_PER_DAY) * 365 * 3);
		assertThat(DateTools.parseTimeSpan("3 m", null)).isEqualTo(((long) Numbers.MILLISECONDS_PER_DAY) * 30 * 3);
		assertThat(DateTools.parseTimeSpan("3 y 3 m", null))
				.isEqualTo(((long) Numbers.MILLISECONDS_PER_DAY) * 365 * 3 + ((long) Numbers.MILLISECONDS_PER_DAY) * 30 * 3);

		assertThat(DateTools.parseTimeSpan("1 s", asSet("s"))).isEqualTo(Numbers.MILLISECONDS_PER_SECOND);
		assertThatCode(() -> DateTools.parseTimeSpan("1 s", asSet("m"))).isInstanceOf(IllegalArgumentException.class);
		assertThatCode(() -> DateTools.parseTimeSpan("1", null)).isInstanceOf(IllegalArgumentException.class);
		assertThatCode(() -> DateTools.parseTimeSpan("s", null)).isInstanceOf(IllegalArgumentException.class);
		assertThatCode(() -> DateTools.parseTimeSpan(null, null)).isInstanceOf(IllegalArgumentException.class);

		assertThat(DateTools.parseTimeSpan("1 s", asSet())).isEqualTo(Numbers.MILLISECONDS_PER_SECOND);
	}

	@Test
	public void testIsoDateTime() {
		// Throws an exception if not parseable
		DateTools.parseDate("2023-09-21T08:56:36.97Z");
		DateTools.parseDate("2023-09-21T08:56:36.9Z");
	}
}
