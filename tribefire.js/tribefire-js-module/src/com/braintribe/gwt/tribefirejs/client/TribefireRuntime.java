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
package com.braintribe.gwt.tribefirejs.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.NodeList;

public class TribefireRuntime {
	private static final String TF_PREFIX = "tf:";
	private static Map<String, String> envProps = new HashMap<String, String>();
	
	static {
		Document document = Document.get();
		NodeList<Element> metaElements = document.getHead().getElementsByTagName("meta");
		
		for (int i = 0; i < metaElements.getLength(); i++) {
			MetaElement metaElement = metaElements.getItem(i).cast();
			String name = metaElement.getName();
			if (name.startsWith(TF_PREFIX)) {
				name = name.substring(TF_PREFIX.length());
				envProps.put(name, metaElement.getContent());
			}
		}
	}
	
	public static String getProperty(String name, String def) {
		String value = envProps.get(name);
		return value != null? value: def;
	}
	
	public static String getProperty(String name) {
		return envProps.get(name);
	}
}

