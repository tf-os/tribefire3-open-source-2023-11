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
package com.braintribe.gwt.browserfeatures.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;

/**
 * This Util class is used for providing access to the meta tags for gwt:property
 * Example: <meta name="gwt:property" content="locale=de">
 * @author michel.docouto
 *
 */
public class GWTMetaPropertiesUtil {
	private static final String ICON_SET_PROPERTY = "iconSet";
	private static final String COLOURED_ICON_SET = "coloured";
	
	public enum IconSet {
		coloured, bw;
	}
	
	private static Map<String, String> metaProperties = null;
	
	/**
	 * Returns all the properties defined  in gwt:property meta tags.
	 */
	public static Map<String, String> getMetaProperties() {
		if (metaProperties == null) {
			metaProperties = new HashMap<String, String>();

			NodeList<Element> metaElements = Document.get().getElementsByTagName("meta");
			
			for (int i = 0; i < metaElements.getLength(); i++) {
				Element metaElement = metaElements.getItem(i);
				if ("gwt:property".equals(metaElement.getAttribute("name"))) {
					String content = metaElement.getAttribute("content");
					int index = content.indexOf("=");
					String key = content;
					String value = "";
					
					if (index != -1) {
						key = content.substring(0, index); 
						value = content.substring(index + 1); 
					}
					
					metaProperties.put(key, value);
				}
			}
		}

		return metaProperties;
	}
	
	public static String getMetaPropertyValue(String propertyName, String defaultValue) {
		Map<String, String> properties = getMetaProperties();
		String value = properties.get(propertyName);
		return value != null ? value : defaultValue;
	}
	
	/**
	 * Returns the IconSet currently configured using the iconSet gwt:property
	 */
	public static IconSet getIconSet() {
		return COLOURED_ICON_SET.equals(getMetaProperties().get(ICON_SET_PROPERTY)) ? IconSet.coloured : IconSet.bw;
	}

}
