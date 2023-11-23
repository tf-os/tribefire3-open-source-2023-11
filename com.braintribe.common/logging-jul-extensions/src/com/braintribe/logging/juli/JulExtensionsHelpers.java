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
package com.braintribe.logging.juli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.LogManager;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides simple helpers and convenience methods used by the other classes of this package and the sub packages.
 *
 * @author michael.lafite
 */
public class JulExtensionsHelpers {

	private static boolean debugEnabled = false;

	private JulExtensionsHelpers() {
		// no instantiation required
	}

	public static boolean debuggingEnabled() {
		return debugEnabled;
	}

	public static void debug(final Class<?> clazz, final String methodName, final String message) {
		System.out.println(clazz.getName() + "." + methodName + "(): " + message);
	}

	public static void debugIfEnabled(final Class<?> clazz, final String methodName, final String message) {
		if (debugEnabled) {
			debug(clazz, methodName, message);
		}
	}

	/**
	 * {@link LogManager#getProperty(String) Gets} the value of the specified property, {@link String#trim() trims} it and then casts it to specified
	 * type.
	 *
	 * @param configuredObjectClass
	 *            the class of the object for which to get the property. (The property name is expected to be [class
	 *            name].[propertyNameWithoutPrefix].)
	 * @param propertyNameWithoutClassNamePrefix
	 *            the property name without the class name prefix (see above).
	 * @param valueIsMandatory
	 *            whether the value is mandatory or not. If the value is not set (i.e. <code>null</code>), the default value is returned. If a
	 *            mandatory value is not set and there is no default value, an exception is thrown.
	 * @param defaultValue
	 *            an optional default value.
	 * @param expectedValueType
	 *            the class of the expected type of the value.
	 * @return the (trimmed) property value or <code>null</code>, if not set.
	 * @throws IllegalArgumentException
	 *             if a mandatory argument is not provided (<code>configuredObjectClass</code>, <code>propertyNameWithoutClassNamePrefix</code>
	 *             ,<code>expectedValueType</code>).
	 * @throws ConfigurationException
	 *             if anything else goes wrong.
	 */
	public static <T> T getProperty(final Class<?> configuredObjectClass, final String propertyNameWithoutClassNamePrefix,
			final boolean valueIsMandatory, final T defaultValue, final Class<T> expectedValueType)
			throws ConfigurationException, IllegalArgumentException {

		if (configuredObjectClass == null) {
			throw new IllegalArgumentException("ConfiguredObjectClass must not be null!");
		}

		if (expectedValueType == null) {
			throw new IllegalArgumentException("ExpectedValueType must not be null!");
		}

		if (propertyNameWithoutClassNamePrefix == null) {
			throw new IllegalArgumentException("PropertyName must not be null!");
		}

		final String errorMessagePrefix = "Error while getting value of configuration property '" + propertyNameWithoutClassNamePrefix
				+ "' for class " + configuredObjectClass.getName() + ": ";

		final String propertyName = configuredObjectClass.getName() + "." + propertyNameWithoutClassNamePrefix;
		final String valueAsString = LogManager.getLogManager().getProperty(propertyName);
		if (valueAsString == null) {
			if (defaultValue == null && valueIsMandatory) {
				throw new ConfigurationException(errorMessagePrefix + "property is mandatory, but not set!");
			}
			return defaultValue;
		}

		final String valueAsTrimmedString = valueAsString.trim();

		Object parsedValue;
		try {
			if (expectedValueType == String.class) {
				parsedValue = valueAsTrimmedString;
			} else if (expectedValueType == Boolean.class) {
				if (valueAsTrimmedString.equalsIgnoreCase("true")) {
					parsedValue = Boolean.TRUE;
				} else if (valueAsTrimmedString.equalsIgnoreCase("false")) {
					parsedValue = Boolean.FALSE;
				} else {
					throw new Exception("String '" + valueAsTrimmedString + "' is not a boolean value!");
				}
			} else if (expectedValueType == Integer.class) {
				parsedValue = Integer.parseInt(valueAsTrimmedString);
			} else if (expectedValueType == Long.class) {
				parsedValue = Long.parseLong(valueAsTrimmedString);
			} else if (expectedValueType == Double.class) {
				parsedValue = Double.parseDouble(valueAsTrimmedString);
			} else if (expectedValueType == Float.class) {
				parsedValue = Float.parseFloat(valueAsTrimmedString);
			} else {
				throw new Exception("Expected value type " + expectedValueType + " is not supported by this method!");
			}
		} catch (final Exception e) {
			throw new ConfigurationException(errorMessagePrefix + "Couldn't parse " + expectedValueType.getClass().getSimpleName()
					+ " value from String '" + valueAsTrimmedString + "'!", e);
		}

		try {
			// now we can perform the cast and return the result
			return expectedValueType.cast(parsedValue);
		} catch (final ClassCastException e) {
			// this code should be unreachable
			throw new ConfigurationException(errorMessagePrefix + "Error while casting value!", e);
		}
	}

	/**
	 * Returns a new instance of the type specified via <code>className</code> or the <code>defaultInstance</code>, if no class name is specified.
	 *
	 * @throws ConfigurationException
	 *             if any error occurs.
	 */
	public static <T> T createInstance(final String className, final Class<T> expectedType, final T defaultInstance, final String errorMessagePrefix)
			throws ConfigurationException {

		if (className != null) {
			final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

			Class<?> clazz;
			try {
				clazz = classLoader.loadClass(className);
			} catch (final ClassNotFoundException e) {
				throw new ConfigurationException(errorMessagePrefix + "Class doesn't exist!", e);
			}

			Object instance;
			try {
				instance = clazz.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new ConfigurationException(errorMessagePrefix + "Couldn't instantiate class " + clazz.getName() + "!", e);
			}

			try {
				return expectedType.cast(instance);
			} catch (final ClassCastException e) { // NOSONAR: no need to propagate exception
				throw new ConfigurationException(
						errorMessagePrefix + "Couldn't cast " + instance.getClass().getName() + " to " + expectedType.getClass().getName() + "!");

			}
		}
		return defaultInstance;
	}

	public static String getSystemPropertiesInfo(final String linePrefix) {
		return getPropertiesInfo(System.getProperties(), linePrefix);
	}

	public static String getPropertiesInfo(final Properties properties, final String linePrefix) {
		@SuppressWarnings("unchecked")
		final Set<String> propertyNames = (Set<String>) (Object) properties.keySet();

		if (propertyNames.isEmpty()) {
			return linePrefix + "[no properties]";
		}

		final List<String> sortedPropertyNames = new ArrayList<>(propertyNames);
		Collections.sort(sortedPropertyNames);

		final StringBuilder builder = new StringBuilder();
		for (final String propertyName : sortedPropertyNames) {
			builder.append(linePrefix + propertyName + " = '" + properties.getProperty(propertyName) + "'\n");
		}
		return builder.toString();
	}

	public static Object invokeNoArgumentMethod(final Object object, final String methodName) {
		final Method method = getMethod(object, methodName);
		return invokeMethod(object, method);
	}

	public static Method getMethod(final Object object, final String methodName, final Class<?>... parameterTypes) {
		try {
			return object.getClass().getMethod(methodName, parameterTypes);
		} catch (final NoSuchMethodException e) {
			throw new IllegalArgumentException(
					"Error while searching method '" + methodName + "' for type " + object.getClass().getName() + ": method doesn't exist!", e);
		} catch (final SecurityException e) {
			throw new IllegalArgumentException("Not allowed to get method '" + methodName + "' for type " + object.getClass().getName() + "!", e);
		}
	}

	public static Object invokeMethod(final Object object, final Method method, final Object... args) {
		try {
			final Object result = method.invoke(object, args);
			return result;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalArgumentException("Error while invoking method " + method + " on object " + object + "!", e);

		}
	}

	public static boolean fileUrlAvailable(final String urlString) {
		return new File(toURI(urlString)).exists();
	}

	public static void assertFileUrlAvailable(final String urlString) {
		if (!fileUrlAvailable(urlString)) {
			throw new IllegalArgumentException("File specified by URL '" + urlString + "' not available!");
		}
	}

	public static boolean fileAvailable(final String filePath) {
		return new File(filePath).exists();
	}

	public static void assertFileAvailable(final String filePath) {
		if (!fileAvailable(filePath)) {
			throw new IllegalArgumentException("File with path '" + filePath + "' not available!");
		}
	}

	public static URI toURI(final URL url) {
		try {
			return url.toURI();
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException("Error while converting URL '" + url + "' to URI!", e);
		}
	}

	public static URI toURI(final String urlString) {
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (final MalformedURLException e) {
			throw new IllegalArgumentException("Error while creating URL using string '" + urlString + "'!", e);
		}
		return toURI(url);
	}

	public static String toURLString(final File file) {
		try {
			return file.toURI().toURL().toString();
		} catch (final MalformedURLException e) {
			throw new IllegalArgumentException("Error while getting URL for file '" + file + "'!", e);
		}
	}

	public static boolean isEmpty(final String value) {
		return value == null || value.length() == 0;
	}

	public static boolean isNull(final String value) {
		return value == null;
	}

	public static void assertNotEmpty(final String value, final String errorMessage) {
		if (value == null || value.length() == 0) {
			throw new IllegalArgumentException(errorMessage);
		}
	}

	public static void assertNotNull(final String value, final String errorMessage) {
		if (value == null) {
			throw new IllegalArgumentException(errorMessage);
		}
	}

	public static File returnFirstExistingFile(final String... fileUrls) {
		if (fileUrls != null) {
			for (final String fileUrl : fileUrls) {
				final File file = new File(JulExtensionsHelpers.toURI(fileUrl));
				if (file.exists()) {
					return file;
				}
			}
		}
		return null;
	}

	public static String removeTrailingFileSeparators(String path) {
		while (path.endsWith("/") || path.endsWith("\\")) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	/**
	 * Reads the specified <code>file</code> and returns its content as a <code>String</code>.
	 *
	 * @throws IOException
	 *             if any error occurs
	 */
	public static String readStringFromFile(final File file, final String encoding) throws IOException {

		BufferedReader reader = null;
		IOException exceptionWhileReading = null;

		try {
			final InputStream inputStream = new FileInputStream(file);
			final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, encoding);
			reader = new BufferedReader(inputStreamReader);
			final int bufferSize = 8192;
			final StringBuilder stringBuilder = new StringBuilder(bufferSize);

			while (true) {
				final char[] buffer = new char[bufferSize];
				final int charactersRead = reader.read(buffer);
				if (charactersRead == -1) {
					break;
				}
				stringBuilder.append(String.valueOf(buffer, 0, charactersRead));
			}
			return stringBuilder.toString();
		} catch (final IOException e) {
			exceptionWhileReading = e;
			throw new IOException("Error while reading from file " + file + "!", e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (final IOException exceptionWileClosingStream) {
				if (exceptionWhileReading == null) {
					throw new IOException("Error while closing stream after successfully reading from " + file + "!", exceptionWileClosingStream);
				} else {
					// ignore this exception
				}
			}
		}
	}

	/**
	 * Parses the passed <code>xmlString</code> and returns a {@link Document}.
	 */
	public static Document parseXmlString(final String xmlString) {
		DocumentBuilder documentBuilder;
		try {
			final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			throw new RuntimeException("Error while creating new " + DocumentBuilder.class.getSimpleName() + "!", e);
		}

		try {
			final Document document = documentBuilder.parse(new InputSource(new StringReader(xmlString)));
			return document;
		} catch (final SAXException | IOException e) {
			throw new IllegalArgumentException("Error while parsing document from string '" + xmlString + "'!", e);
		}
	}

	/**
	 * Converts the passed <code>node</code> to a formatted string.
	 */
	public static String toFormattedString(final Node node) {
		DOMImplementationLS impl;
		try {
			impl = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
			throw new RuntimeException("Error while getting DOMImplementationLS from DOMImplementationRegistry!", e);
		}
		final LSSerializer writer = impl.createLSSerializer();
		writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
		String result = writer.writeToString(node);
		result = result.replaceFirst("<\\?xml version=[^>]*\\?>", "").trim();
		return result;
	}

	public static List<Element> getChildElements(final Element element) {
		final List<Element> childElements = new ArrayList<>();

		Node childNode = element.getFirstChild();
		while (childNode != null) {
			if (childNode instanceof Element) {
				childElements.add((Element) childNode);
			}
			childNode = childNode.getNextSibling();
		}
		return childElements;
	}

	public static String getWorkingDirectoryPath() {
		try {
			return new File(".").getCanonicalFile().getAbsolutePath();
		} catch (final IOException e) {
			throw new RuntimeException("Error while getting canonical path of working directory!", e);
		}
	}

	public static void setDebugEnabled(boolean debugEnabled) {
		JulExtensionsHelpers.debugEnabled = debugEnabled;
	}

	public static File createLoggingConfigFromTemplate(String webappName, String confPath, String templateFilename) throws Exception {
		File confDir = new File(JulExtensionsHelpers.toURI(confPath));
		File templateFile = new File(confDir, templateFilename);
		if (!templateFile.exists()) {
			throw new Exception("Could not find the template logging configuration file at: " + templateFile.getAbsolutePath());
		}
		File targetFile = new File(confDir, webappName + "_logging.properties");
		if (targetFile.exists()) {
			System.out.println("The target file exists already: " + templateFile.getAbsolutePath());
			return targetFile;
		}
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(templateFile), StandardCharsets.UTF_8));
				PrintWriter out = new PrintWriter(
						new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8)))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.contains("[WebappName]")) {
					line = line.replace("[WebappName]", webappName);
				}
				out.println(line);
			}
		} catch (Exception e) {
			if (targetFile.exists()) {
				try {
					targetFile.delete();
				} catch (Exception ignore) {
					// well...
				}
			}
			throw new Exception("Could not create the configuration file " + targetFile.getAbsolutePath() + " from the template "
					+ templateFile.getAbsolutePath() + " for the webapp " + webappName, e);
		}
		return targetFile;
	}

}
