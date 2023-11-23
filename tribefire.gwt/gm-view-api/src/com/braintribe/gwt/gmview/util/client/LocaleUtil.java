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
package com.braintribe.gwt.gmview.util.client;

import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Logger;
import com.sencha.gxt.core.shared.FastMap;

/**
 * Class containing util locale for dates and numbers for our needed known locales.
 * The supported locales are: en, de, es, hr
 * If we need new locales, support must be added here.
 * @author michel.docouto
 *
 */
public class LocaleUtil {
	
	private static final Logger logger = new Logger(LocaleUtil.class);
	private static final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
	private static final String DEFAULT_DATE_TIME_FORMAT = "MM/dd/yyyy HH:mm";
	private static final String DEFAULT_DAY_MONTH_FORMAT = "MM/dd";
	private static final String DEFAULT_MONTH_YEAR_FORMAT = "MM/yyyy";
	private static final String DEFAULT_DECIMAL_SEPARATOR = ".";
	
	private static Supplier<String> localeSupplier;
	private static Map<String, String> dateFormatMap;
	private static Map<String, String> dateTimeFormatMap;
	private static Map<String, String> dayMonthFormatMap;
	private static Map<String, String> monthYearFormatMap;
	private static Map<String, String> decimalSeparatorMap;
	private static String dateSeparator = "/";
	private static String timeSeparator = ":";
	
	static {
		prepareDateFormatMap();
		prepareDateAndTimeFormatMap();
		prepareDayMonthFormatMap();
		prepareMonthYearFormatMap();
		prepareDecimalSeparatorMap();
	}
	
	protected LocaleUtil() {
	}
	
	/**
	 * Configures the required provider for the current locale.
	 */
	@Required
	public static void setLocaleProvider(Supplier<String> localeSupplier) {
		LocaleUtil.localeSupplier = localeSupplier;
	}
	
	public static void configureDateSeparator(String dateSeparator) {
		LocaleUtil.dateSeparator = dateSeparator;
	}
	
	public static void configureTimeSeparator(String timeSeparator) {
		LocaleUtil.timeSeparator = timeSeparator;
	}
	
	/**
	 * Returns the date format for day, month and year.
	 */
	public static String getDateFormat() {
		String locale = localeSupplier.get();
		String dateFormat = dateFormatMap.get(locale);
		if (dateFormat == null) {
			dateFormat = DEFAULT_DATE_FORMAT;
			logger.info("There is no date format set for the current locale: '" + locale + "'");
		}
		
		if (!dateSeparator.equals("/"))
			dateFormat = dateFormat.replaceAll("/", dateSeparator);
		
		return dateFormat;
	}
	
	/**
	 * Returns the date format, for the day, month, year, hour and minute.
	 */
	public static String getDateTimeFormat() {
		String locale = localeSupplier.get();
		String dateTimeFormat = dateTimeFormatMap.get(locale);
		if (dateTimeFormat == null) {
			dateTimeFormat = DEFAULT_DATE_TIME_FORMAT;
			logger.info("There is no datetime format set for the current locale: '" + locale + "'");
		}
		
		if (!dateSeparator.equals("/"))
			dateTimeFormat = dateTimeFormat.replaceAll("/", dateSeparator);
		if (!timeSeparator.equals(":"))
			dateTimeFormat = dateTimeFormat.replaceAll(":", timeSeparator);
		
		return dateTimeFormat;
	}

	/**
	 * Returns the date format, for the day, month, year, hour, minute and second.
	 */
	public static String getDateTimeSecondFormat() {
		return getDateTimeFormat() + timeSeparator + "ss";
	}
	
	/**
	 * Returns the date format, for the day, month, year, hour, minute, second and millisecond.
	 */
	public static String getDateTimeSecondMilisecondFormat() {
		return getDateTimeSecondFormat() + timeSeparator + "SSS";
	}

	/**
	 * Returns the date format, for the day and month.
	 */
	public static String getDayMonthFormat() {
		String locale = localeSupplier.get();
		String dayMonthFormat = dayMonthFormatMap.get(locale);
		if (dayMonthFormat == null) {
			dayMonthFormat = DEFAULT_DAY_MONTH_FORMAT;
			logger.info("There is no day month format set for the current locale: '" + locale + "'");
		}
		
		if (!dateSeparator.equals("/"))
			dayMonthFormat = dayMonthFormat.replaceAll("/", dateSeparator);
		
		return dayMonthFormat;
	}
	
	/**
	 * Returns the date format, for the month and year.
	 */
	public static String getMonthYearFormat() {
		String locale = localeSupplier.get();
		String monthYearFormat = monthYearFormatMap.get(locale);
		if (monthYearFormat == null) {
			monthYearFormat = DEFAULT_MONTH_YEAR_FORMAT;
			logger.info("There is no month year format set for the current locale: '" + locale + "'");
		}
		
		if (!dateSeparator.equals("/"))
			monthYearFormat = monthYearFormat.replaceAll("/", dateSeparator);
		
		return monthYearFormat;
	}
	
	/**
	 * Returns the decimal separator.
	 */
	public static String getDecimalSeparator() {
		String locale = localeSupplier.get();
		String decimalSeparator = decimalSeparatorMap.get(locale);
		if (decimalSeparator == null) {
			decimalSeparator = DEFAULT_DECIMAL_SEPARATOR;
			logger.info("There is no decimal separator set for the current locale: '" + locale + "'");
		}
		
		return decimalSeparator;
	}
	
	private static void prepareDateFormatMap() {
		dateFormatMap = new FastMap<>();
		dateFormatMap.put("en", DEFAULT_DATE_FORMAT);
		String format = "dd/MM/yyyy";
		dateFormatMap.put("de", format);
		dateFormatMap.put("es", format);
		dateFormatMap.put("hr", format);
	}
	
	private static void prepareDateAndTimeFormatMap() {
		dateTimeFormatMap = new FastMap<>();
		dateTimeFormatMap.put("en", DEFAULT_DATE_TIME_FORMAT);
		String format = "dd/MM/yyyy HH:mm";
		dateTimeFormatMap.put("de", format);
		dateTimeFormatMap.put("es", format);
		dateTimeFormatMap.put("hr", format);
	}
	
	private static void prepareDayMonthFormatMap() {
		dayMonthFormatMap = new FastMap<>();
		dayMonthFormatMap.put("en", DEFAULT_DAY_MONTH_FORMAT);
		String format = "dd/MM";
		dayMonthFormatMap.put("de", format);
		dayMonthFormatMap.put("es", format);
		dayMonthFormatMap.put("hr", format);
	}
	
	private static void prepareMonthYearFormatMap() {
		monthYearFormatMap = new FastMap<>();
		dayMonthFormatMap.put("en", DEFAULT_MONTH_YEAR_FORMAT);
		dayMonthFormatMap.put("de", DEFAULT_MONTH_YEAR_FORMAT);
		dayMonthFormatMap.put("es", DEFAULT_MONTH_YEAR_FORMAT);
		dayMonthFormatMap.put("hr", DEFAULT_MONTH_YEAR_FORMAT);
	}
	
	private static void prepareDecimalSeparatorMap() {
		decimalSeparatorMap = new FastMap<>();
		decimalSeparatorMap.put("en", DEFAULT_DECIMAL_SEPARATOR);
		String separator = ",";
		decimalSeparatorMap.put("de", separator);
		decimalSeparatorMap.put("es", separator);
		decimalSeparatorMap.put("hr", separator);
	}

}
