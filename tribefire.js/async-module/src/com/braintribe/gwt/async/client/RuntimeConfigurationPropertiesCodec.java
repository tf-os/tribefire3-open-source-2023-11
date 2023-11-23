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
package com.braintribe.gwt.async.client;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.codec.dom.client.DomCodecUtil;
import com.braintribe.gwt.codec.dom.client.ElementIterator;
import com.braintribe.gwt.logging.client.Logger;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

/**
 * Codec responsible for decoding the XML into a Map with the properties from the
 * runtime configuration file.
 * @author michel.docouto
 *
 */
public class RuntimeConfigurationPropertiesCodec implements Codec<Map<String, String>, String> {
	private Logger logger = new Logger(RuntimeConfigurationPropertiesCodec.class);
	@Override
	public String encode(Map<String, String> value) throws CodecException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Map<String, String> decode(String xml)
			throws CodecException {
		try {
			Map<String, String> properties = new HashMap<String, String>();
			// sanity check
			if (xml.trim().startsWith("<?xml")) {
				Document doc = XMLParser.parse(xml);
				ElementIterator it = new ElementIterator(doc.getDocumentElement(), "entry");
				for (Element element: it) {
					String key = element.getAttribute("key");
					String value = DomCodecUtil.getFirstTextAsString(element, "");
					properties.put(key, value);
				}
			}
			else {
				String prologCandidate = xml.trim();
				String prolog = prologCandidate.substring(0, Math.min(prologCandidate.length(), 55));
				logger.warn("runtime configuration starts with invalid " + prolog + " -> assuming configuration file is not existing");
			}
			return properties;
		}
		catch (Exception e) {
			throw new CodecException("error while parsing runtimeConfiguration.xml", e);
		}
		
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Class<Map<String, String>> getValueClass() {
		return (Class) Map.class;
	}

}
