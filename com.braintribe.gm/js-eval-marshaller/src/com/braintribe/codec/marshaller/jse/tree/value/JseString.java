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
package com.braintribe.codec.marshaller.jse.tree.value;

import java.io.IOException;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.jse.CountingWriter;
import com.braintribe.codec.marshaller.jse.tree.JseNode;

public class JseString extends JseNode {
	private String value;

	public JseString(String value) {
		super();
		this.value = value;
	}

    private final static char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
    private static final char[][] ESCAPES = new char[128][];
    
    static {
    	ESCAPES['"'] = "\\\"".toCharArray();
    	ESCAPES['\\'] = "\\\\".toCharArray();
    	ESCAPES['\t'] = "\\t".toCharArray();
    	ESCAPES['\f'] = "\\f".toCharArray();
    	ESCAPES['\n'] = "\\n".toCharArray();
    	ESCAPES['\r'] = "\\r".toCharArray();
    	
    	for (int i = 0; i < 32; i++) {
    		if (ESCAPES[i] == null)
    			ESCAPES[i] = ("\\u00" + HEX_CHARS[i >> 4] + HEX_CHARS[i & 0xF]).toCharArray();
    	}
    }

	@Override
	public void write(CountingWriter writer) throws MarshallException, IOException {
		writer.write('"');
		String string = value;
    	int len = string.length();
    	int s = 0;
    	int i = 0;
    	char esc[] = null;
    	for (; i < len; i++) {
    		char c = string.charAt(i);
    		
    		if (c < 128) {
    			esc = ESCAPES[c];
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
    	writer.write('"');
    }


}
