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

import com.braintribe.model.time.DateOffsetUnit;

public class DateLab {
	private static String formatDate(Date now) {
		return formatDate(now, ZoneOffset.systemDefault());
	}

	private static String formatDate(Date now, ZoneId zone) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy h:mm a z").withLocale(Locale.getDefault()).withZone(zone);
		ZonedDateTime dateTime = ZonedDateTime.ofInstant(now.toInstant(), zone);
		String formattedDate = formatter.format(dateTime);

		return formattedDate;
	}

	public static void formats() {
		String d1 = "30/08/2018 0:00";
		String d2 = "30/08/2018 12:00:00 PM";

		String p1 = "d/M/yyyy H:m";
		String p2 = "d/M/yyyy hh:mm:ss a";
		String p3 = "[d/M/yyyy hh:mm:ss a][d/M/yyyy H:m]";

		DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(p1).withLocale(Locale.getDefault()).withZone(ZoneOffset.UTC);
		DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern(p2).withLocale(Locale.US).withZone(ZoneOffset.UTC);
		DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern(p3).withLocale(Locale.US).withZone(ZoneOffset.UTC);

		// DateTimeFormatter formatter3 = new DateTimeFormatterBuilder() //
		// .optionalStart().appendPattern(p2).optionalEnd() //
		// .optionalStart().appendPattern(p1).optionalEnd() //
		// .toFormatter().withLocale(Locale.US) //
		// .withZone(ZoneOffset.UTC);

		Date date1 = parse(formatter1, d1);
		Date date2 = parse(formatter2, d2);
		Date date4 = parse(formatter3, d2);
		Date date3 = parse(formatter3, d1);

		System.out.println(Locale.US.toLanguageTag());
		System.out.println(Locale.US.toString());

		System.out.println(date1);
		System.out.println(date2);
		System.out.println(date3);
		System.out.println(date4);

	}

	public static Date parse(DateTimeFormatter formatter, String dateAsStr) {
		TemporalAccessor accessor = formatter.parse(dateAsStr);
		LocalTime time = accessor.query(TemporalQueries.localTime());
		LocalDate date = accessor.query(TemporalQueries.localDate());
		ZoneId zone = accessor.query(TemporalQueries.zone());

		if (time == null)
			time = LocalTime.of(0, 0);

		if (date == null)
			date = LocalDate.of(0, 1, 1);

		Date result = Date.from(ZonedDateTime.of(date, time, zone).toInstant());
		return result;
	}

	public static void main(String[] args) {
		formats();
	}

	public static void foo() {
		ZoneId zone = ZoneId.systemDefault();
		LocalDateTime localDateTime = LocalDateTime.of(1992, 10, 10, 14, 30);

		class Clipper {
			DateOffsetUnit upperBound = DateOffsetUnit.month;
			DateOffsetUnit lowerBound = DateOffsetUnit.day;

			int clip(DateOffsetUnit unit, int value, int clippedValue) {
				return (unit.ordinal() >= lowerBound.ordinal() && unit.ordinal() <= upperBound.ordinal()) ? value : clippedValue;
			}
		}

		Clipper clipper = new Clipper();

		LocalDateTime clippedLocalDateTime = LocalDateTime.of(clipper.clip(DateOffsetUnit.year, localDateTime.getYear(), 0),
				clipper.clip(DateOffsetUnit.month, localDateTime.getMonthValue(), 1),
				clipper.clip(DateOffsetUnit.day, localDateTime.getDayOfMonth(), 1), clipper.clip(DateOffsetUnit.hour, localDateTime.getHour(), 0),
				clipper.clip(DateOffsetUnit.minute, localDateTime.getMinute(), 0), clipper.clip(DateOffsetUnit.second, localDateTime.getSecond(), 0),
				clipper.clip(DateOffsetUnit.millisecond, localDateTime.getNano() / 1_000_000, 0) * 1_000_000);

		System.out.println(clippedLocalDateTime);
	}

	public static void bar() {

		try {
			ZoneId zoneId = ZoneOffset.UTC;

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm") //
					.withZone(zoneId).withLocale(Locale.ENGLISH);

			TemporalAccessor accessor = formatter.parse("2017/05/09 00:00");
			LocalTime time = accessor.query(TemporalQueries.localTime());
			LocalDate date = accessor.query(TemporalQueries.localDate());
			ZoneId zone = accessor.query(TemporalQueries.zone());

			if (time == null)
				time = LocalTime.of(0, 0);

			if (date == null)
				date = LocalDate.of(0, 1, 1);

			Date result = Date.from(ZonedDateTime.of(date, time, zone).toInstant());
			System.out.println(formatDate(result, ZoneOffset.systemDefault()));

			//
			// accessor.get(ChronoField.);

			// LocalDate localDate = LocalDate.parse("2011/07/24 12:00", formatter);
			//
			// Instant instant = localDate.atStartOfDay(zoneId).toInstant();
			// System.out.println(Date.from(instant));
			//
			// ZonedDateTime dateTime = ZonedDateTime.parse("2011/07/24", formatter);
			// System.out.println(Date.from(dateTime.toInstant()));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
