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
package org.apache.catalina.loader;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Set of little helpers, e.g. reading from a file.
 */
public class Helpers {

	private Helpers() {
		// no instantiation required
	}

	static List<String> readFileLines(File file) {
		try {
			return Files.readAllLines(file.toPath());
		} catch (IOException e) {
			throw new UncheckedIOException("Error while reading from file " + file.getAbsolutePath() + ".", e);
		}
	}

	static String readFileContent(File file) {
		return readFileLines(file).stream().collect(Collectors.joining("\n"));
	}

	static String getSubstringBetween(final String string, final String leftSearchString, final String rightSearchString) {

		final int leftSearchStringIndex = string.indexOf(leftSearchString);
		final int rightSearchStringIndex = string.indexOf(rightSearchString);

		if (leftSearchStringIndex < 0) {
			throw new IllegalArgumentException("String '" + leftSearchString + "' not contained in string '" + string + "'!");
		}
		if (rightSearchStringIndex < 0) {
			throw new IllegalArgumentException("String '" + rightSearchString + "' not contained in string '" + string + "'!");
		}
		if (leftSearchStringIndex > rightSearchStringIndex) {
			throw new IllegalArgumentException(
					"String '" + rightSearchString + "' (unexpectedly) found before '" + leftSearchString + "' in string '" + string + "'!");
		}

		String result = string.substring(leftSearchStringIndex + leftSearchString.length(), rightSearchStringIndex);
		return result;
	}

	static <E> List<E> list(E... elements) {
		List<E> list = new ArrayList<>();
		if (elements != null) {
			list.addAll(Arrays.asList(elements));
		}
		return list;
	}

	static boolean getBooleanSystemPropertyValue(String propertyKey, boolean defaultValue) {
		String value = System.getProperty(propertyKey);
		boolean booleanValue;
		if (value != null) {
			if (value.equalsIgnoreCase("true")) {
				booleanValue = true;
			} else if (value.equalsIgnoreCase("false")) {
				booleanValue = false;
			} else {
				throw new RuntimeException("Value of system property '" + propertyKey + "' is '" + value + "', which is not a valid boolean value!");
			}
		} else {
			booleanValue = defaultValue;
		}
		return booleanValue;
	}

	static String normalizePath(File file) {
		return normalizePath(file.getAbsolutePath());
	}

	static String normalizePath(String path) {
		return path.replace('\\', '/');
	}
}
