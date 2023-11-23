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
package com.braintribe.spring.support.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

public class DateConverter implements Converter<String, Date> {

	public Date convert(String source) {
	
		if (source == null || source.trim().length() == 0) {
			return null;
		} else {
			
			if (source.equals("now()")) {
				return new Date();
			}
			
			String [] data = source.split( "\\|");
			SimpleDateFormat format = null;
			if (data.length == 1) {
				format = new SimpleDateFormat( "dd.MM.yyyy");				
			} else {
				format = new SimpleDateFormat( data[0]);
				source = data[1];
			}
			try {			
				Date date = format.parse( source);
				return date;
			}
			catch (ParseException e) {
				throw new IllegalArgumentException("[" + source + "] is not a valid format for a data, [" + format.toPattern() + "] expected", e);
			}
			 
		}
	}
	
	

}
