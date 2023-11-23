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
package com.braintribe.xml.parser.sax.test.framework;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.braintribe.utils.xml.parser.sax.SaxParserContentHandler;

public class TestContentHandler extends SaxParserContentHandler {

	Stack<String> stack = new Stack<String>();
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		StringBuffer buffer = new StringBuffer();
		if (atts != null) {
			for (int i = 0; i < atts.getLength(); i++) {
				if (buffer.length() > 0)
					buffer.append(";");
				buffer.append( atts.getQName(i) + "=" + atts.getValue(i));
			}		
		}
		System.out.println("Element [" + qName + "] starts, attrs [" + buffer.toString() + "]");
		stack.push(qName);
		super.startElement(uri, localName, qName, atts);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		System.out.println("Element [" + qName + "] ends");
		stack.pop();
		super.endElement(uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String str = new String( ch, start, length);		
		
		String qname = stack.peek();
		System.out.println( "value for [" + qname + "] is [" + str + "]");
		super.characters(ch, start, length);
	}

	
}
