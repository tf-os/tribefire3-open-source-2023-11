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
package com.braintribe.utils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.braintribe.utils.lcd.Arguments;

/**
 * This class may contain Java-only (i.e. GWT incompatible) code. For further information please see {@link com.braintribe.utils.lcd.MapTools}.
 *
 * @author michael.lafite
 */
public final class MapTools extends com.braintribe.utils.lcd.MapTools {

	private MapTools() {
		// no instantiation required
	}

	/**
	 * Creates a parameterized map with the passed key/value pairs.
	 *
	 * @param keyAndValuePairs
	 *            keys/value pairs (i.e. key, value, key, value, ...)
	 * @return the map.
	 *
	 * @see MapTools#getParameterizedMap(Class, Class, Map)
	 * @see MapTools#getMap(Object...)
	 */
	public static <K, V> Map<K, V> getParameterizedMap(final Class<K> keyClass, final Class<V> valueClass, final Object... keyAndValuePairs) {
		final Map<?, ?> map = getMap(keyAndValuePairs);
		final Map<K, V> parameterizedMap = getParameterizedMap(keyClass, valueClass, map);
		return parameterizedMap;
	}

	/**
	 * Creates a parameterized map with the passed key/value pairs.
	 *
	 * @return the parameterized map.
	 *
	 * @throws ClassCastException
	 *             if some key/value has the wrong type.
	 */
	public static <K, V> Map<K, V> getParameterizedMap(final Class<K> keyClass, final Class<V> valueClass, final Map<?, ?> map) {
		final Map<K, V> result = new HashMap<>();

		for (final Entry<?, ?> entry : map.entrySet()) {
			final K key = keyClass.cast(entry.getKey());
			final V value = valueClass.cast(entry.getValue());
			result.put(key, value);
		}
		return result;
	}

	/**
	 * Creates a string map (i.e. keys and values are strings) with the passed key/value pairs.
	 *
	 * @see #getParameterizedMap(Class, Class, Object...)
	 */
	public static Map<String, String> getStringMap(final String... keyAndValuePairs) {
		return getParameterizedMap(String.class, String.class, (Object[]) keyAndValuePairs);
	}

	/**
	 * Creates a string-to-object map (i.e. keys are strings, values are objects) with the passed key/value pairs.
	 *
	 * @see #getParameterizedMap(Class, Class, Object...)
	 */
	public static Map<String, Object> getStringToObjectMap(final Object... keyAndValuePairs) {
		return getParameterizedMap(String.class, Object.class, keyAndValuePairs);
	}

	/**
	 * Adds the passed <code>properties</code> to the <code>map</code>.
	 */
	public static void addPropertiesToMap(final Map<String, String> map, final Properties properties) {
		Arguments.notNullWithNames("map", map, "properties", properties);
		for (final Map.Entry<?, ?> entry : properties.entrySet()) {
			final String key = (String) entry.getKey();
			final String value = (String) entry.getValue();
			map.put(key, value);
		}
	}

	/**
	 * Reads from the specified {@link Properties} file and returns the properties in a <code>Map</code>.
	 */
	public static Map<String, String> readPropertiesFile(final String filePath) {
		final File file = new File(filePath);
		if (!file.exists()) {
			throw new IllegalArgumentException("File '" + filePath + "' doesn't exist!");
		}

		final Map<String, String> result = new HashMap<>();

		final Properties properties = new Properties();
		try {
			properties.load(FileTools.newInputStream(file));
		} catch (final IOException e) {
			throw new UncheckedIOException("Error while loading properties from file '" + file + "'!", e);
		}
		result.putAll(MapTools.getParameterizedMap(String.class, String.class, properties));

		return result;
	}
}
