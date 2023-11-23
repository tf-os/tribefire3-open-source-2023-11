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
package com.braintribe.gwt.codec.string.client;

import java.util.Date;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.ioc.client.Configurable;
import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * A date string {@link Codec} using {@link Date} as value class and an instance of {@link DateTimeFormat}
 * to parse and format {@link Date} values; 
 * @author Dirk
 *
 */
public class GwtDateCodec implements Codec<Date, String> {
	private DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy");
	
	/**
	 * Sets the {@link DateTimeFormat} used for decoding the String into Date
	 * and vice-versa.
	 */
	@Configurable
	public void setFormat(DateTimeFormat format) {
		this.format = format;
	}
	
	/**
	 * Sets the format used for preparing the {@link DateTimeFormat} used for
	 * decoding the String into Date and vice-versa.
	 */
	@Configurable
	public void setFormatByString(String format) {
		this.format = DateTimeFormat.getFormat(format);
	}
	
	@Override
	public Date decode(String encodedValue) throws CodecException {
		try {
			if (encodedValue == null || encodedValue.trim().length() == 0)
				return null;
			
			Date date = format.parse(encodedValue);
			return date;
		} catch (Exception e) {
			throw new CodecException("Failed decoding the date value: " + encodedValue, e);
		}
	}
	
	@Override
	public String encode(Date value) throws CodecException {
		if (value == null)
			return "";
		
		return format.format(value);
	}
	
	@Override
	public Class<Date> getValueClass() {
		return Date.class;
	}
}
