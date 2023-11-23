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
package com.braintribe.codec.marshaller.stax.tree;

import java.io.IOException;
import java.io.Writer;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.PrettinessSupport;

public class StringNode extends ValueStaxNode {
	private static final char[] endElement = "</s>".toCharArray();
	private static final char[] startPropElementClose = "'>".toCharArray();
	private static final char[] startPropElement = "<s p='".toCharArray();
	private static final char[] startElement = "<s>".toCharArray();	

	private String string;
	
	public StringNode(String string) {
		super();
		this.string = string;
	}

	@Override
	public void write(Writer writer, PrettinessSupport prettinessSupport, int indent) throws IOException, MarshallException {
		writer.write(startElement);
		writeEscaped(writer, string);
		writer.write(endElement);
	}
	
	@Override
	public void write(Writer writer, PrettinessSupport prettinessSupport, String propertyName, int indent) throws IOException, MarshallException {
		writer.write(startPropElement);
		writer.write(propertyName);
		writer.write(startPropElementClose);
		writeEscaped(writer, string);
		writer.write(endElement);
	}
	
	private static final char[][] ESCAPES = new char[63][];
    
    static {
    	ESCAPES['<'] = "&lt;".toCharArray();
    	ESCAPES['>'] = "&gt;".toCharArray();
    	ESCAPES['&'] = "&amp;".toCharArray();
    	ESCAPES['\r'] = "&#13;".toCharArray();
    }
    
    private static final char[][] ATTRIBUTE_ESCAPES = new char[63][];
    
    static {
    	ATTRIBUTE_ESCAPES['<'] = "&lt;".toCharArray();
    	ATTRIBUTE_ESCAPES['>'] = "&gt;".toCharArray();
    	ATTRIBUTE_ESCAPES['&'] = "&amp;".toCharArray();
    	ATTRIBUTE_ESCAPES['\r'] = "&#13;".toCharArray();
    	ATTRIBUTE_ESCAPES['\''] = "&apos;".toCharArray();
    }

    public static void writeEscaped(Writer writer, String string) throws IOException {
    	writeEscaped(writer, string, ESCAPES);
    }
    
    public static void writeEscapedAttribute(Writer writer, String string) throws IOException {
    	writeEscaped(writer, string, ATTRIBUTE_ESCAPES);
    }
    
    public static void writeEscaped(Writer writer, String string, char[][] escapes) throws IOException {
    	int len = string.length();
    	int s = 0;
    	int i = 0;
    	char esc[] = null;
    	for (; i < len; i++) {
    		char c = string.charAt(i);
    		
    		if (c < 63) {
    			esc = escapes[c];
    			if (esc != null) {
    				writer.write(string, s, i - s);
    				writer.write(esc);
    				s = i + 1;
    			}
    		}
    	}
    	if (i > s) {
    		if (s == 0)
    			writer.write(string);
    		else
    			writer.write(string, s, i - s);
    	}
    }
}
