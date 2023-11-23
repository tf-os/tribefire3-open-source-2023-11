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
package com.braintribe.gwt.customizationui.client.startup;

import java.util.Map;

import com.braintribe.gwt.utils.client.FastMap;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.dom.client.NodeList;

public class TribefireRuntime {
	private static final String TF_PREFIX = "tf:";
	private static Map<String, String> envProps = new FastMap<>();
	
	static {
		loadConfiguration(null);
	}
	
	public static String getProperty(String name, String def) {
		String value = envProps.get(name);
		return value != null? value: def;
	}
	
	public static String getProperty(String name, String def, boolean force) {
		String value = getProperty(name, null);
		if (value != null)
			return value;
		
		if (!force)
			return def;
		
		loadConfiguration(name);
		return getProperty(name, def);
	}
	
	public static String getProperty(String name) {
		return envProps.get(name);
	}
	
	public static Map<String, String> getEnvProps() {
		return envProps;
	}
	
	private static void loadConfiguration(String propertyName) {
		Document document = Document.get();
		NodeList<Element> metaElements = document.getHead().getElementsByTagName("meta");
		
		for (int i = 0; i < metaElements.getLength(); i++) {
			MetaElement metaElement = metaElements.getItem(i).cast();
			String metaName = metaElement.getName();
			if (metaName.startsWith(TF_PREFIX)) {
				metaName = metaName.substring(TF_PREFIX.length());
				if (propertyName == null || metaName.equals(propertyName)) {
					envProps.put(metaName, metaElement.getContent());
					if (propertyName != null)
						break;
				}
			}
		}
	}
}

