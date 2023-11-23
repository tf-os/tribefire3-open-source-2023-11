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
package com.braintribe.gwt.codec.date.client;

import java.util.Date;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;

public class ZonelessDateCodec implements Codec<Date, Date> {
	/*private static final String datePatternWithZone = "yyyy-MM-dd HH:mm:ss SSS Z";
	private static final String datePatternWithoutZone = "yyyy-MM-dd HH:mm:ss SSS";
	private DateTimeFormat formatWithZone = DateTimeFormat.getFormat(datePatternWithZone);
	private DateTimeFormat formatWithoutZone = DateTimeFormat.getFormat(datePatternWithoutZone);*/
	
	public static ZonelessDateCodec INSTANCE = new ZonelessDateCodec();

	/**
	 * convert from zoneless (UTC) to local zone
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Date encode(Date zonedDate) throws CodecException {
		if (zonedDate == null)
			return null;
		
		int minutesOffset = zonedDate.getTimezoneOffset();
		
		long shiftedTime = zonedDate.getTime() - minutesOffset * 60_000;
		
		Date zonelessDate = new Date(shiftedTime);
		return zonelessDate;
		
		/*String formattedZonedDate = formatWithoutZone.format(zonedDate);
		formattedZonedDate += " +0000"; 
		
		Date zonelessDate = formatWithZone.parse(formattedZonedDate);
		return zonelessDate;*/
	}

	/**
	 * convert from local zone to zoneless (UTC)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Date decode(Date zonelessDate) throws CodecException {
		if (zonelessDate == null)
			return null;
		
		int minutesOffset = zonelessDate.getTimezoneOffset();
		
		long shiftedTime = zonelessDate.getTime() + minutesOffset * 60_000;
		
		Date zonedDate = new Date(shiftedTime);
		return zonedDate;
		
		/*String formattedZonelessDate = formatWithZone.format(zonelessDate, TimeZone.createTimeZone(0));
		formattedZonelessDate = formattedZonelessDate.substring(0, datePatternWithoutZone.length());
		
		Date zonedDate = formatWithoutZone.parse(formattedZonelessDate);
		return zonedDate;*/
	}

	@Override
	public Class<Date> getValueClass() {
		return Date.class;
	}
	
}