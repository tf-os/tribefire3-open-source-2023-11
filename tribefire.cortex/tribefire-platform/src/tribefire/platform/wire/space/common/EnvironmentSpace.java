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
package tribefire.platform.wire.space.common;

import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.map;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.function.Function;

import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.time.TimeSpanStringCodec;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.api.EnvironmentDenotations;
import tribefire.platform.impl.PlaceholderReplacer;
import tribefire.platform.impl.configuration.EnvironmentDenotationRegistry;

@Managed
public class EnvironmentSpace implements WireSpace {

	/**
	 * Resolves placeholders referencing system properties and environment variables in the given string.
	 * <p>
	 * The placeholder is guaranteed to be resolved through {@link TribefireRuntime#getProperty(String)}
	 * 
	 * <h2>Examples:</h2>
	 * 
	 * Having an environment variable named {@code MY_HOST} set to <em>"braintribe.com"</em>:
	 * 
	 * <pre>
	 * String result = environment.resolve("https://${MY_HOST}/mypath");
	 * // result is "https://braintribe.com/mypath"
	 * </pre>
	 * 
	 * Having a system property named {@code my.host} set to <em>"tribefire.com"</em>:
	 * 
	 * <pre>
	 * String result = environment.resolve("https://${my.host}/mypath");
	 * // result is "https://tribefire.com/mypath"
	 * </pre>
	 * 
	 * @param value
	 *            The string to be resolved.
	 * @return The resolved string.
	 */

	public String resolve(String value) {
		if (value == null) {
			return null;
		}
		String resolved = PlaceholderReplacer.resolve(value, TribefireRuntime::getProperty);
		return resolved;
	}

	/**
	 * Equivalent to {@code property(name, null)}
	 * 
	 * @see #property(String, String)
	 */

	public String property(String name) {
		requireNonNull(name, "name must not be null");
		String value = TribefireRuntime.getProperty(name);
		return value;
	}

	/**
	 * Returns the value of the tribefire runtime property (system properties or environment variables) with the given {@code name}, falling back the
	 * a given default value if the property is not set.
	 * <p>
	 * The property is guaranteed to be resolved through {@link TribefireRuntime#getProperty(String)}
	 * 
	 * @param name
	 *            The name of the tribefire runtime property to be returned.
	 * @param defaultValue
	 *            The default to be returned if the property is not set.
	 * @return The value of the tribefire runtime property.
	 * @throws NullPointerException
	 *             If the {@code name} parameter is {@code null}.
	 */

	public String property(String name, String defaultValue) {
		requireNonNull(name, "name must not be null");
		String value = TribefireRuntime.getProperty(name, defaultValue);
		return value;
	}

	/**
	 * Equivalent to {@code property(name, type, null)}
	 * 
	 * @see #property(String, Class, String)
	 */

	public <T> T property(String name, Class<T> type) {
		T value = property(name, type, null);
		return value;
	}

	/**
	 * Returns the value of the tribefire runtime property (system properties or environment variables) with the given {@code name}, converted to the
	 * given {@code type} and falling back the a given {@code defaultValue} if the property is not set.
	 * <p>
	 * The supported conversion types are:
	 * <ul>
	 * <li>{@link java.lang.Integer}</li>
	 * <li>{@link java.lang.Long}</li>
	 * <li>{@link java.lang.Float}</li>
	 * <li>{@link java.lang.Double}</li>
	 * <li>{@link java.lang.Character}</li>
	 * <li>{@link com.braintribe.model.time.TimeSpan}</li>
	 * </ul>
	 * 
	 * <p>
	 * The property is guaranteed to be resolved through {@link TribefireRuntime#getProperty(String)}
	 * 
	 * @param name
	 *            The name of the tribefire runtime property to be returned.
	 * @param type
	 *            The type of the tribefire runtime property to be returned.
	 * @param defaultValue
	 *            The default to be returned if the property is not set.
	 * @return The value of the tribefire runtime property.
	 * @throws NullPointerException
	 *             If {@code name} or {@code type} is {@code null}.
	 * @throws IllegalArgumentException
	 *             If the given {@code type} is not supported or not valid for converting the property value.
	 */

	public <T> T property(String name, Class<T> type, T defaultValue) {
		requireNonNull(name, "type must not be null");
		String string = property(name);
		T value = null;
		if (string == null) {
			value = defaultValue;
		} else {
			value = convert(name, string, type);
		}
		return value;
	}

	@Managed
	private Map<Class<?>, Function<String, ?>> converters() {
		// NOTE: Make sure to update the contract's javadoc when updating this map:
		// @formatter:off
		Map<Class<?>, Function<String, ?>> bean = 
				map(
					entry(Boolean.class		, v -> (v == null) ? null : Boolean.valueOf(v)),
					entry(Integer.class		, v -> (v == null) ? null : Integer.valueOf(v)),
					entry(Long.class		, v -> (v == null) ? null : Long.valueOf(v)),
					entry(Float.class		, v -> (v == null) ? null : Float.valueOf(v)),
					entry(Double.class		, v -> (v == null) ? null : Double.valueOf(v)),
					entry(Character.class	, v -> (v == null) ? null : Character.valueOf(v.charAt(0))),
					entry(TimeSpan.class	, this::convertToTimeSpan)
				);
		return bean;
		// @formatter:on
	}

	@Managed
	private TimeSpanStringCodec timeSpanStringCodec() {
		TimeSpanStringCodec bean = new TimeSpanStringCodec();
		return bean;
	}

	private <T> T convert(String propertyName, String propertyValue, Class<T> type) {
		try {
			Function<String, T> converter = (Function<String, T>) converters().get(type);
			if (converter == null)
				throw new IllegalArgumentException("No converter found for type " + type.getName());

			T convertedValue = converter.apply(propertyValue);
			return convertedValue;

		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot retrieve property [ " + propertyName + " ]. Value [ " + propertyValue
					+ " ] cannot be converted to " + type.getName() + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}
	}

	private TimeSpan convertToTimeSpan(String value) {
		try {
			return (timeSpanStringCodec()).decode(value);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to convert \"" + value + "\" into a " + TimeSpan.class.getName(), e);
		}
	}

	public EnvironmentDenotations environmentDenotations() {
		return EnvironmentDenotationRegistry.getInstance();
	}

}
