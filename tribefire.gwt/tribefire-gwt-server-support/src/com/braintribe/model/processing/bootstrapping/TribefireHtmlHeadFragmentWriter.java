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
package com.braintribe.model.processing.bootstrapping;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class TribefireHtmlHeadFragmentWriter {
	
	public static void write(Writer writer, Map<String, String> metaMap) throws IOException {
		for (Map.Entry<String, String> entry: metaMap.entrySet()) {
			String name = "tf:" + entry.getKey();
			String content = entry.getValue();
			writer.write("<meta name=\"");
			writeEscaped(writer, name != null ? name : "");
			writer.write("\" content=\"");
			writeEscaped(writer, content != null ? content : "");
			writer.write("\">\n");
		}
	}
	
	private static void writeEscaped(Writer writer, String text) throws IOException {
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			
			switch (c) {
			case '<':
				writer.write("&lt;");
				break;
			case '>':
				writer.write("&gt;");
				break;
			case '&':
				writer.write("&amp;");
				break;
			case '\'':
				writer.write("&#39;");
				break;
			case '"':
				writer.write("&quot;");
				break;
			default:
				if (Character.isWhitespace(c)) {
					writer.write("&#" + (int)c + ";");
				}
				else {
					writer.write(c);
				}
			}
		}
	}
}

