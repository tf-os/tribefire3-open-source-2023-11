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
package com.braintribe.gwt.codec.dom.client;

import java.util.Arrays;

import com.braintribe.codec.CodecException;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

public class DomCodecUtil {
	private static Document document = XMLParser.createDocument();
	
	public static Element createElement(String tagName) {
		Element element = document.createElement(tagName);
		return element;
	}
	
	public static Text createTextElement(String data) {
		return document.createTextNode(data);
	}
	
	public static Element getElement(Element parent, String... tagNames) {
		Element element = parent;
		
		for (String tagName: tagNames) {
			element = getFirstChildElement(element, tagName);
			if (element == null) break;
		}
		
		return element;
	}
	
	public static Element requireElement(Element parent, String... tagNames) throws CodecException {
		Element element = getElement(parent, tagNames);
		
		if (element == null)
			throw new CodecException("element with path " + Arrays.asList(tagNames) + " not found");

		return element;
	}
	
	public static Element getFirstChildElement(Element parent, String tagName) {
		ElementIterator elementIterator = new ElementIterator(parent);
		
		for (Element element: elementIterator) {
			if (tagName == null) return element;
			else if (element.getTagName().equals(tagName)) return element; 
		}
		
		return null;
	}
	
	public static Element requireFirstChildElement(Element parent, String tagName) throws CodecException {
		Element element = getFirstChildElement(parent, tagName);
		if (element == null)
			throw new CodecException("tag with name " + tagName + " not found");
		return element;
	}
	
	public static Text getFirstText(Element parent) {
		TextIterator textIterator = new TextIterator(parent);
		if (textIterator.hasNext()) return textIterator.next();
		else return null;
	}
	
	public static String getFirstTextAsString(Element parent) {
		return getFirstTextAsString(parent, "");
	}
	
	public static String getFirstTextAsString(Element parent, String def) {
		Text text = getFirstText(parent);
		if (text == null) return def;
		else return text.getData();
	}
	
	public static String getFullElementText(Element element, boolean rekursive) {
		StringBuilder stringBuilder = new StringBuilder();
		getFullElementText(element, stringBuilder, rekursive);
		
		String text = stringBuilder.toString();
		return text;
	}
	
	public static void getFullElementText(Element element, StringBuilder builder, boolean rekursive) {
		Node childNode = element.getFirstChild();
		while (childNode != null) {
			if (childNode instanceof Text) {
				Text text = (Text)childNode;
				builder.append(text.getData());
			}
			else if (rekursive && childNode instanceof Element) {
				getFullElementText((Element)childNode, builder, true);
			}
			childNode = childNode.getNextSibling();
		}
	}
}
