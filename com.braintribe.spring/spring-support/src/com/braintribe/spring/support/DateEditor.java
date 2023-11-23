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
package com.braintribe.spring.support;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * DateEditor : text can have two forms
 * 
 * a) "dd.MM.yyyy" 
 * b) formatPattern | date value as specified in format
 * 
 * @author pit
 *
 */
public class DateEditor extends PropertyEditorSupport {
	
	public String getAsText() {
		return "";
	}

	public void setAsText(String text) throws IllegalArgumentException {
		if (text == null || text.trim().length() == 0) {
			setValue(null);
		} else {
			String [] data = text.split( "\\|");
			SimpleDateFormat format = null;
			if (data.length == 1) {
				format = new SimpleDateFormat( "dd.MM.yyyy");				
			} else {
				format = new SimpleDateFormat( data[0]);
				text = data[1];
			}
			try {			
				Date date = format.parse( text);
				setValue( date);
			}
			catch (ParseException e) {
				throw new IllegalArgumentException("[" + text + "] is not a valid format for a data, [" + format.toPattern() + "] expected", e);
			}
			 
		}
	}

}
