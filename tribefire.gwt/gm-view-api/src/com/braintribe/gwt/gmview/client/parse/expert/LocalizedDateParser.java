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
package com.braintribe.gwt.gmview.client.parse.expert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.codec.CodecException;
import com.braintribe.gwt.codec.string.client.GwtDateCodec;
import com.braintribe.gwt.gmview.client.parse.SimpleTypeParser;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.sencha.gxt.core.shared.FastMap;

/**
 * 
 */
public class LocalizedDateParser implements Function<String, Date> {

	private SimpleTypeParser owner;

	private static Map<String, List<DateTimeFormat>> localizedFormats = new FastMap<List<DateTimeFormat>>();
	private GwtDateCodec dateCodec = new GwtDateCodec();

	private static final String[] days = { "d", "dd" };
	private static final String[] months = { "M", "MM" };
	private static final String[] years = { "yy", "yyyy" };

	static {
		localizedFormats.put("en", new ArrayList<DateTimeFormat>());
		localizedFormats.put("de", new ArrayList<DateTimeFormat>());

		addFormatForPermutationss("en", months, days, years, "/");
		addFormatForPermutationss("de", days, months, years, ".");

		addFormats("en", "yyyy-MM-dd", "MM/dd", "MMdd", "MMddyy", "MMddyyyy");
		addFormats("de", "yyyy-MM-dd", "dd.MM", "ddMM", "ddMMyy", "ddMMyyyy");
	}

	private static void addFormatForPermutationss(String locale, String[] ss1, String[] ss2, String[] ss3,
			String delimiter) {
		for (String s1 : ss1) {
			for (String s2 : ss2) {
				for (String s3 : ss3) {
					String pattern = s1 + delimiter + s2 + delimiter + s3;
					addFormat(locale, pattern);
				}
			}
		}

	}

	private static void addFormats(String locale, String... patterns) {
		for (String pattern : patterns) {
			addFormat(locale, pattern);
		}
	}

	private static void addFormat(String locale, String pattern) {
		localizedFormats.get(locale).add(DateTimeFormat.getFormat(pattern));
	}

	public LocalizedDateParser(SimpleTypeParser owner) {
		this.owner = owner;
	}

	@Override
	public Date apply(String value) throws RuntimeException {
		for (DateTimeFormat format : getFormats()) {
			Date date = safeParse(format, value);

			if (date != null) {
				return date;
			}
		}

		return null;
	}

	private Date safeParse(DateTimeFormat format, String value) {
		dateCodec.setFormat(format);

		try {
			Date result = dateCodec.decode(value);

			return processValue(result, format);

		} catch (CodecException e) {
			return null;
		}
	}

	/**
	 * If the pattern doesn't contain a year, we set it to current year.
	 */
	@SuppressWarnings("deprecation")
	private Date processValue(Date date, DateTimeFormat format) {
		if (!format.getPattern().contains("y")) {
			// PGA: I know it's deprecated, but there doesn't seem to be any nice way to do this in GWT
			date.setYear(new Date().getYear());
		}

		return date;
	}

	private List<DateTimeFormat> getFormats() {
		List<DateTimeFormat> formats = localizedFormats.get(owner.locale());

		if (formats == null) {
			formats = localizedFormats.get(SimpleTypeParser.DEFAULT_LOCALE);
		}

		return formats;
	}

}
