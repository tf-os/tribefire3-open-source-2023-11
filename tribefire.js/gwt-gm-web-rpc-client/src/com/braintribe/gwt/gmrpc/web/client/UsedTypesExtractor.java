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
package com.braintribe.gwt.gmrpc.web.client;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.gwt.codec.dom.client.DomCodecUtil;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;

public class UsedTypesExtractor {
	private static final String startMarker = "//BEGIN_TYPES"; 
	private static final String endMarker = "//END_TYPES"; 
	public static Set<String> extractTypes(String jseSource) {
		Set<String> typeNames = new HashSet<String>();
		
		int stIndex = jseSource.indexOf(';');
		
		if (stIndex == -1)
			return typeNames;
		
		stIndex = jseSource.indexOf(';', stIndex + 1);
		
		if (stIndex == -1)
			return typeNames;
		
		int s = jseSource.substring(0, stIndex).indexOf(startMarker);
		
		if (s == -1)
			return typeNames;
		
		int e = jseSource.indexOf(endMarker);
		
		String typeNamesSection = jseSource.substring(s + startMarker.length(), e);
		String typeNamesStatements[] = typeNamesSection.split(";");
		
		for (String typeNameFragment: typeNamesStatements) {
			int startQuoteIndex = typeNameFragment.indexOf('"');
			int endQuoteIndex = typeNameFragment.lastIndexOf('"');
			
			if (startQuoteIndex != -1 && endQuoteIndex != -1) {
				String typeName = typeNameFragment.substring(startQuoteIndex + 1, endQuoteIndex);
				typeNames.add(typeName);
			}
		}
		
		return typeNames;
	}
	
	public static Set<String> extractTypes(Document document) {
		Set<String> typeNames = new HashSet<String>();
		Element documentElement = document.getDocumentElement();
		if (documentElement.getTagName().equals("gm-data")) {
			Element typesElement = DomCodecUtil.getFirstChildElement(documentElement, "required-types");
			
			if (typesElement != null) {
				Node node = typesElement.getFirstChild();
				
				while (node != null) {
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element)node;
						String typeSignature = DomCodecUtil.getFullElementText(element, false);
						typeNames.add(typeSignature);
					}
					node = node.getNextSibling();
				}
			}
			else {
				NodeList entityElements = documentElement.getElementsByTagName("entity");
				
				for (int i = 0; i < entityElements.getLength(); i++) {
					Element element = (Element)entityElements.item(i);
					String typeName = element.getAttribute("type");
					
					if (typeName != null && typeName.length() > 0) {
						typeNames.add(typeName);
					}
				}
			}
		}
		
		return typeNames;
		
	}
}
