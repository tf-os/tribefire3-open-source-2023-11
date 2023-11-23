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
package com.braintribe.logging.jul;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Provides helper methods to set custom JUL configurations.
 *
 * @author michael.lafite
 */
public class JulConfigurationHelper {

	private static final String LOGGING_PROPERTIES_PACKAGEPREFIX = "/" + JulConfigurationHelper.class.getPackage().getName().replace('.', '/') + "/";
	public static final String SIMPLE_CONSOLE_ONLY_LOGGING_PROPERTIES = LOGGING_PROPERTIES_PACKAGEPREFIX + "simple-console-only-logging.properties";

	private JulConfigurationHelper() {
		// no instantiation required
	}

	/**
	 * Configures the JUL {@link LogManager} to use a simple, console-only logging configuration which logs single-line messages to
	 * <code>System.out</code>, instead of two-liners to <code>System.err</code> (which is the JUL default). <br>
	 * This delegates to {@link #setCustomConfiguration(InputStream)}.
	 */
	public static void setSimpleConsoleOnlyLoggingConfiguration() {
		try (InputStream inputStream = JulConfigurationHelper.class.getResourceAsStream(SIMPLE_CONSOLE_ONLY_LOGGING_PROPERTIES)) {
			setCustomConfiguration(inputStream);
		} catch (IOException e) {
			throw new RuntimeException("Error while reading logging configuration!", e);
		}
	}

	/**
	 * Same as {@link #setSimpleConsoleOnlyLoggingConfiguration()}, but only sets the new configuration if there is no
	 * {@link #isCustomJulConfiguration() custom configuration} yet.
	 */
	public static void setSimpleConsoleOnlyLoggingConfigurationUnlessAlreadyConfigured() {
		if (!isCustomJulConfiguration()) {
			setSimpleConsoleOnlyLoggingConfiguration();
		}
	}

	/**
	 * Configures the JUL {@link LogManager} to use the custom logging configuration read from the passed <code>inputStream</code>. This overrides
	 * previous configuration settings (if any). This method does NOT close the stream.
	 */
	public static void setCustomConfiguration(InputStream inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException("The passed input stream must not be null!");
		}
		try {
			LogManager.getLogManager().readConfiguration(inputStream);
		} catch (IOException e) {
			throw new RuntimeException("Error while reading logging confiuration from input stream!", e);
		}
	}

	/**
	 * Same as {@link #setCustomConfiguration(InputStream)}, but only sets the new configuration if there is no {@link #isCustomJulConfiguration()
	 * custom configuration} yet.
	 */
	public static void setCustomConfigurationUnlessAlreadyConfigured(InputStream inputStream) {
		if (!isCustomJulConfiguration()) {
			setCustomConfiguration(inputStream);
		}
	}

	/**
	 * Checks whether the current JUL configuration is a custom configuration (and not the default one).
	 */
	public static boolean isCustomJulConfiguration() {
		return !isDefaultJulConfiguration();
	}

	/**
	 * Checks whether the current JUL configuration is the default one.
	 */
	public static boolean isDefaultJulConfiguration() {
		boolean result = false;

		if (System.getProperty("java.util.logging.config.file") == null && System.getProperty("java.util.logging.config.class") == null) {

			LogManager manager = LogManager.getLogManager();

			// see $JAVA_HOME/conf/logging.properties
			if ("%h/java%u.log".equals(manager.getProperty("java.util.logging.FileHandler.pattern")) //
					&& "50000".equals(manager.getProperty("java.util.logging.FileHandler.limit")) //
					&& "1".equals(manager.getProperty("java.util.logging.FileHandler.count")) //
					&& "100".equals(manager.getProperty("java.util.logging.FileHandler.maxLocks"))) {
				// pretty safe to say this is the default JUL configuration
				result = true;

				// in Java 8 one could also check for "com.xyz.foo.level", but this (correctly) is commented out in more recent versions
			}
		}
		return result;
	}

}
