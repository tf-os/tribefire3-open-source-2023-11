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
package com.braintribe.velocity;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.context.Context;

public class EscapeReferenceInserter implements ReferenceInsertionEventHandler {

	private String templatePath;
	private String type = "";
	private Function<String, String> escaper = Function.identity();
	private static Pattern typePattern = Pattern.compile(".*\\.(.*)\\.vm");
	
	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;

		Matcher matcher = typePattern.matcher(templatePath);
		
		if (matcher.matches()) {
			type = matcher.group(1);
			
			switch (type) {
				case "html":
					escaper = EscapeReferenceInserter::escapeHtml;
					break;
				case "xml":
					escaper = EscapeReferenceInserter::escapeXml;
					break;
			}
		}
	}
	
	public String getTemplatePath() {
		return templatePath;
	}
	
	public String getType() {
		return type;
	}
	

	@Override
	public Object referenceInsert(Context context, String reference, Object value) {
		if (value == null)
			return null;
		
		if (reference.endsWith("_noesc"))
			return value;
		
		return escaper.apply(value.toString());
	}

	public Function<String, String> getEscaper() {
		return escaper;
	}
	
	private static String escapeHtml(String s) {
		return escapeXml(s);
	}
	
	private static String escapeXml(String s) {
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
	
	public static void main(String[] args) {
		Matcher matcher = typePattern.matcher("test.xml.vm");
		
		if (matcher.matches()) {
			System.out.println(matcher.group(1));
		}
	}

}