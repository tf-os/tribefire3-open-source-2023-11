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
package com.braintribe.tomcat.extension;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tomcat.util.IntrospectionUtils.PropertySource;

/**
 */
public class DefaultAwareEnvironmentPropertySource implements PropertySource {

	private static final Logger logger = Logger.getLogger(DefaultAwareEnvironmentPropertySource.class.getName());

	@Override
	public String getProperty(String key) {

		logger.fine("getProperty called for key " + key);

		if (key != null && key.startsWith("?")) {

			logger.fine("Key seems to be an encoded specification");

			Map<String, String> spec = splitSpecification(key);

			logger.fine("Read from " + key + ": " + spec);

			String envName = spec.get("envName");
			if (envName != null) {
				String value = getEnvironmentVariable(envName);
				if (value != null) {
					return value;
				} else {
					String defaultValue = spec.get("default");
					return defaultValue;
				}
			}
		}
		return null;
	}

	public Map<String, String> splitSpecification(String input) {
		// Get rid of the leading ?
		input = input.substring(1);
		String[] split = input.split("&");
		Map<String, String> result = new LinkedHashMap<>();
		for (String entry : split) {
			final int idx = entry.indexOf("=");
			if (idx > 0 && idx < entry.length() - 1) {
				String key = entry.substring(0, idx).trim();
				String value = entry.substring(idx + 1).trim();

				try {
					result.put(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					logger.log(Level.SEVERE, "Error while trying to parse " + entry, e);
				}
			}

		}
		return result;
	}

	private String getEnvironmentVariable(String name) {
		Map<String, String> getenv = System.getenv();
		String value;

		value = getenv.get(name);
		if (value != null) {
			return value;
		}

		value = System.getProperty(name);
		return value;
	}
}
