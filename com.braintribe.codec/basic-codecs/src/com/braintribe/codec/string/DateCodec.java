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
/**
 * 
 */
package com.braintribe.codec.string;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.utils.DateTools;

public class DateCodec implements Codec<Date, String> {

	private DateFormat javaFormat;
	private DateTimeFormatter jodaFormat;
	private boolean lenient;
	
	private static final byte MODE_JAVA = 0;
	private static final byte MODE_JAVA8 = 1;
	private static final byte MODE_AUTO = 2;
	
	private int mode;
	
	public static DateTimeFormatter[] ALL_FORMATTERS = new DateTimeFormatter[] {
			DateTools.TERSE_DATE_FORMAT,
			DateTools.TERSE_DATETIME_FORMAT,
			DateTools.ISO8601_DATE_FORMAT,
			DateTools.RFC822_DATE_FORMAT,
			DateTools.IPROCESS_DATE_FORMAT,
			DateTools.IPROCESS_DATE_FORMAT_2,
			DateTools.IPROCESS_DATETIME_FORMAT_2,
			DateTools.LEGACY_DATETIME_FORMAT,
			DateTools.ISO8601_DATE_WITH_MS_FORMAT,
			DateTools.LEGACY_DATETIME_WITH_MS_FORMAT,
			DateTools.TERSE_DATETIME_FORMAT_2,
			DateTools.TERSE_DATETIME_WITH_MS_FORMAT
	};
	

	public DateCodec() {
		this.mode = MODE_AUTO;
	}

	public DateCodec(String format) {
		this.jodaFormat = DateTimeFormatter.ofPattern(format);
		this.mode = MODE_JAVA8;
	}

	public DateCodec(DateFormat format) {
		this.javaFormat = format;
		this.mode = MODE_JAVA;
	}

	public DateCodec(String format, boolean lenient) {
		this.jodaFormat = DateTimeFormatter.ofPattern(format);
		this.lenient = lenient;
		this.mode = MODE_JAVA8;
	}

	public DateCodec(DateFormat format, boolean lenient) {
		this.javaFormat = format;
		this.mode = MODE_JAVA;
		this.lenient = lenient;
	}
		
	public void setFormat(String format) {
		this.jodaFormat = DateTimeFormatter.ofPattern(format);
		this.mode = MODE_JAVA8;
	}

	@Override
	public Date decode(String strValue) throws CodecException {
		if (strValue == null || strValue.trim().length() == 0)
			return null;

		try {
			switch (mode) {
			case MODE_AUTO:
				return failsafeDateParse(strValue);
				
			case MODE_JAVA:
				try {
					return javaFormat.parse(strValue);
				} catch (ParseException ex) {
					if (lenient)
						return failsafeDateParse(strValue);
					else
						throw new CodecException(ex);
				}
			
			case MODE_JAVA8:
				try {
					ZonedDateTime dt = ZonedDateTime.parse(strValue, jodaFormat);
					return Date.from(dt.toInstant());
				} catch (Exception ex) {
					if (lenient)
						return failsafeDateParse(strValue);
					else
						throw new CodecException(ex);
				}
			default:
				throw new CodecException("DateCodec is in an invalid mode " + mode);
			}
		} 
		catch (CodecException e) {
			throw e;
		}
		catch (Exception e) {
			throw new CodecException("error while decoding date from string " + strValue, e);
		}
	}

	/**
	 * Converts a {@link Date} object into a String using the configured format.
	 * Please note that the resulting String is depending on the local timezone and thus
	 * may vary.
	 */
	@Override
	public String encode(Date obj) throws CodecException {
		if (obj == null)
			return "";

		try {
			switch (mode) {
			case MODE_AUTO:
				return String.valueOf(obj.getTime());
			case MODE_JAVA:
				return javaFormat.format(obj);
			case MODE_JAVA8:
				ZonedDateTime dateTime = ZonedDateTime.ofInstant(obj.toInstant(), ZoneOffset.systemDefault());
				return jodaFormat.format(dateTime);
			default:
				throw new CodecException("DateCodec is in an invalid mode " + mode);
			}
		}
		catch (CodecException e) {
			throw e;
		}
		catch (Exception e) {
			throw new CodecException("error while encoding date " + obj + " to string", e);
		}	
	}

	@Override
	public Class<Date> getValueClass() {
		return Date.class;
	}

	protected static Date failsafeDateParse(String s) {
		s = s.trim();

		try {
			long t = Long.parseLong(s);
			if (t > 299999999999L && t < 9999999999999L)
				return new Date(t); // note: take care not to confuse with
									// yyyyMMddhhmm; allowed range is Thu Jul 05
									// 06:19:59 CET 1979 to Sat Nov 20 18:46:39
									// CET 2286
		} catch (NumberFormatException ex) {
			//Ignore
		}

		for (int i=0; i<ALL_FORMATTERS.length; ++i) {
			try {
				DateTimeFormatter formatter = ALL_FORMATTERS[i];
				ZonedDateTime dt = ZonedDateTime.parse(s, formatter);
				if (dt != null) {
					return Date.from(dt.toInstant());
				}
			} catch (Exception e) {
				//Ignore
			}
		}

		return null;
	}

}
