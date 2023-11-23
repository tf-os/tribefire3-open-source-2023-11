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
package com.braintribe.web.servlet;

import java.util.Date;

import com.braintribe.utils.DateTools;
import com.braintribe.utils.StringTools;

public class VelocityTools {

	public static String escape(String s) {
		int c = s.length();
		if (c == 0)
			return s;

		char[] chars = new char[c];
		s.getChars(0, c, chars, 0);

		StringBuilder b = new StringBuilder(c * 2);

		for (int i = 0; i < chars.length; i++) {
			char ch = chars[i];

			switch (ch) {
			case '&':
				b.append("&amp;");
				break;
			case '<':
				b.append("&lt;");
				break;
			case '>':
				b.append("&gt;");
				break;
			case '"':
				b.append("&quot;");
				break;
			case '\'':
				b.append("&apos;");
				break;
			default:
				if (ch > 0x7F) {
					b.append("&#");
					b.append(Integer.toString(ch, 10));
					b.append(';');
				} else {
					b.append(ch);
				}
			}
		}
		
		return b.toString();
	}
	
	public static String displayUserName(String firstName, String lastName, String userName, String defaultText) {
		
		String displayName = userName;
		if (!StringTools.isEmpty(firstName)) {
			displayName = firstName;
		}
		if (!StringTools.isEmpty(lastName)) {
			displayName = (StringTools.isEmpty(displayName) ? lastName : displayName + " " + lastName);
		}
		if (StringTools.isEmpty(displayName)) {
			displayName = defaultText;
		}
		return displayName;
		
	}

	public static String getDateAsString(Date date) {
		return getDateAsString(date, "dd MMMM yyyy, HH:mm");
	}

	public static String getDateAsString(Date date, String format) {
		return DateTools.getDateString(date, format);
	}
	
}
