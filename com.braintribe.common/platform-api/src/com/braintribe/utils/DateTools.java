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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.utils.date.ExtSimpleDateFormat;
import com.braintribe.utils.lcd.Not;

/**
 * This class provides date (and time) related utility methods.
 *
 * @author michael.lafite
 */
public final class DateTools {

	private static Logger logger = Logger.getLogger(DateTools.class);

	public static final DateTimeFormatter RFC822_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z").withLocale(Locale.US);
	public static final DateTimeFormatter ISO8601_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ").withLocale(Locale.US);
	// Note: we deliberarely keep the wrong format here to maintain backward compatibility
	public static final DateTimeFormatter ISO8601_DATE_WITH_MS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
			.withLocale(Locale.US);
	// Note: this is the actual correct ISO8601 format (with the X in the specification) so that Strings that have a 'Z'
	// instead of a numeric offset
	// can be parsed
	public static final DateTimeFormatter ISO8601_DATE_WITH_MS_FORMAT_AND_Z = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
			.withLocale(Locale.US);
	public static final DateTimeFormatter ISO8601_DATE_WITH_MS_FORMAT_AND_Z_OPTIONAL_TIME = DateTimeFormatter
			.ofPattern("yyyy-MM-dd['T'HH:mm:ss[.SSS]][XXXXX][X]").withLocale(Locale.US);
	public static final DateTimeFormatter IPROCESS_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss zzz yyyy").withLocale(Locale.US); // Wed
																																					// Oct
																																					// 04
																																					// 00:00:00
																																					// MEST
																																					// 2006

	public static final DateTimeFormatter IPROCESS_DATE_FORMAT_2 = new DateTimeFormatterBuilder().optionalStart().appendPattern("dd/MM/yyyy")
			.optionalEnd().parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
			.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
			.parseDefaulting(ChronoField.NANO_OF_SECOND, 0).toFormatter().withZone(ZoneOffset.systemDefault()).withLocale(Locale.US);

	public static final DateTimeFormatter IPROCESS_DATETIME_FORMAT_2 = new DateTimeFormatterBuilder().optionalStart()
			.appendPattern("dd/MM/yyyy HH:mm").optionalEnd().parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
			.parseDefaulting(ChronoField.MILLI_OF_SECOND, 0).parseDefaulting(ChronoField.NANO_OF_SECOND, 0).toFormatter()
			.withZone(ZoneOffset.systemDefault()).withLocale(Locale.US);// 04/10/2006 12:23

	public static final DateTimeFormatter TERSE_DATE_FORMAT = new DateTimeFormatterBuilder().optionalStart().appendPattern("yyyyMMdd").optionalEnd()
			.parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
			.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
			.parseDefaulting(ChronoField.NANO_OF_SECOND, 0).toFormatter().withZone(ZoneOffset.systemDefault()).withLocale(Locale.US); // 20061130

	public static final DateTimeFormatter TERSE_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.systemDefault())
			.withLocale(Locale.US); // 20061130201833

	// This a workaround because SSS cannot be in this form directly in the pattern
	// (https://bugs.openjdk.java.net/browse/JDK-8031085)
	public static final DateTimeFormatter TERSE_DATETIME_WITH_MS_FORMAT = new DateTimeFormatterBuilder().appendPattern("yyyyMMddHHmmss")
			.appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter().withZone(ZoneOffset.systemDefault()).withLocale(Locale.US); // 20061130201833123

	public static final DateTimeFormatter LEGACY_DATETIME_FORMAT = new DateTimeFormatterBuilder().optionalStart().appendPattern("yyyy.MM.dd HH:mm:ss")
			.optionalEnd().parseDefaulting(ChronoField.MILLI_OF_SECOND, 0).parseDefaulting(ChronoField.NANO_OF_SECOND, 0).toFormatter()
			.withZone(ZoneOffset.systemDefault()).withLocale(Locale.US);// 04/10/2006 12:23

	public static final DateTimeFormatter LEGACY_DATETIME_WITH_MS_FORMAT = new DateTimeFormatterBuilder().optionalStart()
			.appendPattern("yyyy.MM.dd HH:mm:ss.SSS").optionalEnd().parseDefaulting(ChronoField.NANO_OF_SECOND, 0).toFormatter()
			.withZone(ZoneOffset.systemDefault()).withLocale(Locale.US);// 04/10/2006 12:23

	public static final DateTimeFormatter TERSE_DATETIME_FORMAT_2 = new DateTimeFormatterBuilder().optionalStart().appendPattern("yyyyMMdd-HHmmss")
			.optionalEnd().parseDefaulting(ChronoField.MILLI_OF_SECOND, 0).parseDefaulting(ChronoField.NANO_OF_SECOND, 0).toFormatter()
			.withZone(ZoneOffset.systemDefault()).withLocale(Locale.US);// 04/10/2006 12:23

	/**
	 * The default {@link java.text.SimpleDateFormat#SimpleDateFormat(String) date format pattern}: <code>yyyy.MM.dd HH:mm:ss.SSS</code>. This pattern
	 * was also used as default in the I2Z server code. Please note that the pattern also parses <code>yyyy.MM.dd HH:mm:ss.SS</code> and
	 * <code>yyyy.MM.dd HH:mm:ss.SSS</code> correctly.
	 */
	public static final String DEFAULT_DATEFORMAT_PATTERN = "yyyy.MM.dd HH:mm:ss.SSS";

	/**
	 * The supported date/time formats that are used by {@link #parseDate(String)} to parse a date string. The array is initialized in the static
	 * initializer block.
	 */
	private static final DateTimeFormatter[] dateTimeFormatParsers;
	/**
	 * The supported date formats that are used by {@link #parseDate(String)} to parse a date string. The array is initialized in the static
	 * initializer block.
	 */
	private static final DateTimeFormatter[] dateFormatParsers;

	private final static String timeSpanSpecificationRegex = "(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)";

	/**
	 * Initializes the supported date formats.
	 */
	static {

		// Please note that the order of the formats is important.
		// If e.g. "yyyy.MM.dd" comes before "yyyy.MM.dd hh:mm" information may get lost.
		// After adding new formats, please run the DateAndTimeToolsTest!
		dateTimeFormatParsers = new DateTimeFormatter[] { DateTimeFormatter.ofPattern("EEE MMM dd hh:mm:ss zzz yyyy").withLocale(Locale.GERMAN),
				DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy").withLocale(Locale.US),
				DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss Z yyyy").withLocale(Locale.US),
				DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z").withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSZ").withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSX").withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SZ").withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SX").withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ").withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX").withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss.SSS").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy.MM.dd-HH:mm:ss.SSS").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy.MM.dd-HH:mm:ss").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy.MM.dd-HH:mm").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss.SSS").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US) };
		dateFormatParsers = new DateTimeFormatter[] {
				DateTimeFormatter.ofPattern("yyyy.MM.dd").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("MM/dd/yyyy").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("HH:mm:ss.S").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneOffset.systemDefault()).withLocale(Locale.US),
				DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.US) };
	}

	/**
	 * Parses the passed <code>dateString</code> and returns the date. Parse exceptions are not logged, just ignored. The <code>DateFormat</code>
	 * instances are created during initialization of the class.
	 *
	 * @param dateString
	 *            the string to parse.
	 * @return the parsed {@code Date}.
	 * @throws IllegalArgumentException
	 *             if the passed <code>dateString</code> couldn't be parsed.
	 */
	public static Date parseDate(String dateString) throws IllegalArgumentException {
		for (DateTimeFormatter dateFormat : dateTimeFormatParsers) {
			try {
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("trying to parse date string: '%s' with format: '%s'", dateString, dateFormat.toString()));
				}

				Date result = decodeDateTime(dateString, dateFormat);

				if (logger.isTraceEnabled()) {
					logger.trace(String.format("successfully parsed date '%s' with format: '%s' to date : %s", dateString, dateFormat.toString(),
							encode(result, dateFormat)));
				}

				return result;
			} catch (Exception e) {
				// since this exception is expected and will be thrown quite often, only log on trace level and continue
				// with next dateformat
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("failed to parse date '%s' with format: '%s'", dateString, dateFormat.toString()));
				}
			}
		}
		for (DateTimeFormatter dateFormat : dateFormatParsers) {
			try {
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("trying to parse date string: '%s' with format: '%s'", dateString, dateFormat.toString()));
				}

				Date result = decodeDate(dateString, dateFormat);

				if (logger.isTraceEnabled()) {
					logger.trace(String.format("successfully parsed date '%s' with format: '%s' to date : %s", dateString, dateFormat.toString(),
							encode(result, dateFormat)));
				}

				return result;
			} catch (Exception e) {
				// since this exception is expected and will be thrown quite often, only log on trace level and continue
				// with next dateformat
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("failed to parse date '%s' with format: '%s'", dateString, dateFormat.toString()));
				}
			}
		}
		throw new IllegalArgumentException(String.format("Couldn't parse date '%s': please see %s.parseDate(String) for supported formats",
				StringTools.getStringRepresentation(dateString), DateTools.class.getName()));
	}

	/**
	 * The id of the GMT time zone.
	 */
	public static final String GMT_ID = "GMT";

	private DateTools() {
		// no instantiation required
	}

	/**
	 * Computes the time in another time zone. For more information on {@link TimeZone}s and the supported IDs please check
	 * http://java.sun.com/j2se/1.5.0/docs/api/java/util/TimeZone.html.
	 *
	 * @param localTime
	 *            the time to convert.
	 * @param localTimeZoneId
	 *            the id of local time zone.
	 * @param targetTimeZoneId
	 *            the id of the time zone to which to convert the passed time to.
	 * @return the converted time.
	 */
	public static long convertTimeToOtherTimeZone(final long localTime, final String localTimeZoneId, final String targetTimeZoneId) {
		final TimeZone localTimeZone = TimeZone.getTimeZone(localTimeZoneId);
		final TimeZone targetTimeZone = TimeZone.getTimeZone(targetTimeZoneId);

		final int localUTCOffset = localTimeZone.getOffset(localTime);
		final int targetUTCOffset = targetTimeZone.getOffset(localTime);

		final long utcTime = localTime - localUTCOffset;
		final long targetTime = utcTime + targetUTCOffset;

		return targetTime;
	}

	/**
	 * Gets the {@link #getCurrentDateString(String) current date string}
	 */
	public static String getCurrentDateString() {
		return encode(new Date(), LEGACY_DATETIME_WITH_MS_FORMAT);
	}

	/**
	 * Returns the current date in the specified format.
	 *
	 * @see #getDateString(Date, String)
	 */
	public static String getCurrentDateString(final String simpleDateFormatPattern) {
		return getDateString(new Date(), simpleDateFormatPattern);
	}

	/**
	 * Converts the passed <code>date</code> to a string using the specified <code>simpleDateFormatPattern</code>. Milliseconds are handled as
	 * decimals. See {@link #getDateString(Date, String, boolean)} for more information.
	 */
	public static String getDateString(final Date date, final String simpleDateFormatPattern) {
		return getDateString(date, simpleDateFormatPattern, true);
	}

	/**
	 * <p>
	 * Converts the passed <code>date</code> to a string using the specified <code>simpleDateFormatPattern</code>. If
	 * <code>millisecondsHandledAsDecimals</code> is set to <code>false</code>, the date is formatted as usual (using
	 * {@link java.text.SimpleDateFormat#format(Date)}), otherwise milliseconds are handled as decimals.
	 * </p>
	 * <p>
	 * Explanation:<br>
	 * <code>SimpleDateFormat</code> works as follows: if the date/time is "1 second and 12 milliseconds" and the {@link java.text.SimpleDateFormat}
	 * pattern is "s.SS", the result will be "1.12" (instead of "1.01"), because the number of "S" characters in the pattern only specifies the
	 * minimum number of milliseconds digits. This also means that for "1 second and 1 millisecond" the result would be "1.01". Furthermore, because
	 * it's just the MINIMUM number of digits, so for "1 second and 123 milliseconds" the result would be "1.123" instead of "1.12". These are not
	 * bugs but in many cases it's not what the programmer expects and wants.
	 * </p>
	 * <p>
	 * If <code>millisecondsHandledAsDecimals</code> is set to <code>true</code> the milliseconds characters ("S") in the pattern are handled as
	 * decimals, i.e. as a fraction of a second. This means that the method always returns the number of requested digits. For "1 second and 12
	 * milliseconds" and pattern "s.SS" the result is "1.01", for "s.SSS" it's "1.012" for "s.SSSSS" it's 1.01200". Please note that for "1 second and
	 * 19 milliseconds " and pattern "s.SS" the method returns "1.01" and not "1.02" (i.e. numbers are not rounded).
	 * </p>
	 *
	 * @param date
	 *            the date to convert to a string.
	 * @param simpleDateFormatPattern
	 *            the {@link java.text.SimpleDateFormat} pattern.
	 * @param millisecondsHandledAsDecimals
	 *            whether or not milliseconds shall be handled as decimals.
	 * @return the date string.
	 */
	public static String getDateString(final Date date, final String simpleDateFormatPattern, final boolean millisecondsHandledAsDecimals) {
		if (!millisecondsHandledAsDecimals) {
			/* NOTE: To improve performance date format instances could be cached. (see e.g. biz.i2z.utils.format.FormatProvider) */
			return Not.Null(new ExtSimpleDateFormat(simpleDateFormatPattern).format(date));
		}

		// this character is used as a substitute for the milliseconds character "S" in the pattern.
		final char substitutionCharacter = 26;

		// this shouldn't happen but check anyway because format patterns are usually not that long.
		if (simpleDateFormatPattern.contains(new String(new char[] { substitutionCharacter }))) {
			throw new RuntimeException("The SimpleDateFormat pattern " + CommonTools.getStringRepresentation(simpleDateFormatPattern)
					+ " contains the unicode character with decimal value " + ((int) substitutionCharacter)
					+ " which is not allowed since it's also used as substitution character!");
		}

		final char[] charArray = simpleDateFormatPattern.toCharArray();
		final char singleQuote = '\'';
		boolean outsideQuote = true;
		final char millisecondsChar = 'S';
		int indexOfFirstMillisecondsCharOutsideQuote = -1;
		int indexOfLastMillisecondsCharOutsideQuote = -1;
		for (int i = 0; i < simpleDateFormatPattern.length(); i++) {

			if (charArray[i] == singleQuote) {
				if ((i < (simpleDateFormatPattern.length() - 1)) && (charArray[i + 1] == singleQuote)) {
					// escaped quote
					i++; // SuppressCSWarning(ModifiedControlVariable) - otherwise loop would be even more complex
					continue;
				}

				// we entered or left a quote
				outsideQuote = !outsideQuote;
				continue;
			}

			if (outsideQuote && charArray[i] == millisecondsChar) {
				// we found a milliseconds char outside a quote!

				if (indexOfLastMillisecondsCharOutsideQuote > -1 && indexOfLastMillisecondsCharOutsideQuote < i - 1) {
					// we do not support multiple milliseconds parts in the pattern
					throw new IllegalArgumentException("The SimpleDateFormat pattern " + CommonTools.getStringRepresentation(simpleDateFormatPattern)
							+ " has a milliseconds char (" + com.braintribe.utils.lcd.CommonTools.getStringRepresentation(millisecondsChar)
							+ ") at index " + i + " but there is also one at " + indexOfLastMillisecondsCharOutsideQuote
							+ " which means there are multiple milliseconds parts in the pattern which is not supported!");
				}

				// update index information and substitute character
				if (indexOfFirstMillisecondsCharOutsideQuote == -1) {
					indexOfFirstMillisecondsCharOutsideQuote = i;
				}
				indexOfLastMillisecondsCharOutsideQuote = i;

				charArray[i] = substitutionCharacter;
			}
		}

		if (indexOfFirstMillisecondsCharOutsideQuote < Numbers.ZERO) {
			// there were no milliseconds characters so we can just use the standard method.
			return getDateString(date, simpleDateFormatPattern, false);
		}

		// format the date (without milliseconds)
		final String dateString = getDateString(date, new String(charArray), false);

		// get milliseconds date string (length is 1-3)
		String millisecondsDateString = getDateString(date, "S", false);

		// prepend zeros if necessary, i.e. instead of "12" we want "012"
		final int requestedMillisecondsDigitsCount = indexOfLastMillisecondsCharOutsideQuote - indexOfFirstMillisecondsCharOutsideQuote + 1;
		final int defaultNumberOfMillisecondsCharacters = 3;
		final int missingZerosToPreprend = defaultNumberOfMillisecondsCharacters - millisecondsDateString.length();
		millisecondsDateString = StringTools.concat(StringTools.getFilledString(missingZerosToPreprend, '0'), millisecondsDateString);

		if (millisecondsDateString.length() > requestedMillisecondsDigitsCount) {
			// we want less digits --> remove from the end of the string
			millisecondsDateString = com.braintribe.utils.lcd.StringTools.getFirstNCharacters(millisecondsDateString,
					requestedMillisecondsDigitsCount);

		} else if (millisecondsDateString.length() < requestedMillisecondsDigitsCount) {
			/* we need more digits! since we already prepended zeros to the string, there can only be further missing digits if the pattern is
			 * something like "s.SSSSS", i.e. more than 3 milliseconds digits. In this case we just append zeros to the string. */
			final int missingZerosToAppend = requestedMillisecondsDigitsCount - millisecondsDateString.length();
			millisecondsDateString = StringTools.concat(millisecondsDateString, StringTools.getFilledString(missingZerosToAppend, '0'));
		}

		// replace substitution characters with milliseconds string and then return date string.
		final int replacementIndex = dateString.indexOf(substitutionCharacter);
		final String result = StringTools.replaceRegion(dateString, Not.Null(millisecondsDateString), replacementIndex,
				replacementIndex + requestedMillisecondsDigitsCount);
		return result;
	}

	/**
	 * Simple helper method that returns a number based on the current date and time. It is NOT guaranteed that this number is unique!
	 */
	public static long getTimestampNumber() {
		return Long.parseLong(encode(new Date(), TERSE_DATETIME_WITH_MS_FORMAT));
	}

	public static String encode(GregorianCalendar gc, DateTimeFormatter formatter) {
		return formatter.format(gc.toZonedDateTime());
	}
	public static String encode(Date date, DateTimeFormatter formatter) {
		ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.systemDefault());
		return formatter.format(dateTime);
	}
	public static String encode(Instant instant, DateTimeFormatter formatter) {
		ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.systemDefault());
		return formatter.format(dateTime);
	}
	/**
	 * Parses the provided text and returns the resulting {@link Date} object.
	 *
	 * @param text
	 *            The text that should be decoded
	 * @param formatter
	 *            The format used to decode the text
	 * @return The parse Date object.
	 * @deprecated This method does not work for strings that contain dates only (i.e., no time information). So, either
	 *             {@link #decodeDate(String, DateTimeFormatter)} or {@link #decodeDateTime(String, DateTimeFormatter)} should be used.
	 */
	@Deprecated
	public static Date decode(String text, DateTimeFormatter formatter) {
		return decodeDateTime(text, formatter);
	}
	public static Date decodeDateTime(String text, DateTimeFormatter formatter) {
		ZonedDateTime dt = ZonedDateTime.parse(text, formatter);
		return Date.from(dt.toInstant());
	}
	public static Date decodeDate(String text, DateTimeFormatter formatter) {
		LocalDate localDate = LocalDate.parse(text, formatter);
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Utility method to parse time spans in a human-readable form and returns the corresponding time interval in milliseconds. The time span is
	 * specified in one or more groups of values and time units. Supported time units are: y (year), m (month), d (day), h (hour), min (minute), s
	 * (second) and ms (millisecond). Please note that this code does not prevent the resulting long value to become negative if the time span exceeds
	 * the maximum long value.
	 *
	 * @param timeSpanSpec
	 *            The string containing the time span specification
	 * @param allowedUnits
	 *            An optional set of time units that are allowed. If this is not provided, or empty, all time units will be used.
	 * @throws IllegalArgumentException
	 *             Thrown when the time span specification is null, or when it does not contain an even number of value/units pairs.
	 * @return The parsed time span in milliseconds.
	 */
	public static long parseTimeSpan(String timeSpanSpec, Set<String> allowedUnits) {
		if (timeSpanSpec == null) {
			throw new IllegalArgumentException("The time span specification is null");
		}
		timeSpanSpec = timeSpanSpec.trim();
		String[] parsedTtl = timeSpanSpec.split(timeSpanSpecificationRegex);

		if (parsedTtl == null || parsedTtl.length == 0) {
			throw new IllegalArgumentException("Could not parse the time span specification " + timeSpanSpec);
		}
		if ((parsedTtl.length % 2) == 1) {
			throw new IllegalArgumentException("The time span specification " + timeSpanSpec + " does not contain pairs of values and units.");
		}
		if (allowedUnits != null && allowedUnits.isEmpty()) {
			allowedUnits = null;
		}

		long totalMs = 0l;

		for (int i = 0; i < parsedTtl.length; i = i + 2) {
			int ttlValue = Integer.parseInt(parsedTtl[i]);
			String ttlUnit = parsedTtl[i + 1].toLowerCase().trim();

			if (allowedUnits != null && !allowedUnits.contains(ttlUnit)) {
				throw new IllegalArgumentException("The time unit " + ttlUnit + " is not in the set of allowed units: " + allowedUnits);
			}
			switch (ttlUnit) {
				case "y":
					totalMs += TimeUnit.DAYS.toMillis(ttlValue) * 365;
					break;
				case "m":
					totalMs += TimeUnit.DAYS.toMillis(ttlValue) * 30;
					break;
				case "d":
					totalMs += TimeUnit.DAYS.toMillis(ttlValue);
					break;
				case "h":
					totalMs += TimeUnit.HOURS.toMillis(ttlValue);
					break;
				case "min":
					totalMs += TimeUnit.MINUTES.toMillis(ttlValue);
					break;
				case "s":
					totalMs += TimeUnit.SECONDS.toMillis(ttlValue);
					break;
				case "ms":
					totalMs += ttlValue;
					break;
				default:
					throw new IllegalArgumentException("Invalid time unit: " + ttlUnit + ", allowed values: y, m, d, h, min, s, ms");
			}

		}

		return totalMs;
	}
}
