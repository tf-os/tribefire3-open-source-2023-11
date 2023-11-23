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
package com.braintribe.utils.lcd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This class provides utility methods that can be used to conveniently check method arguments. Unless otherwise mentioned the methods throw an
 * {@link IllegalArgumentException}, if a check fails.
 *
 * @author michael.lafite
 */
public final class Arguments {

	private Arguments() {
		// no instantiation required
	}

	public static void arrayNotEmpty(final String... array) {
		arrayNotEmptyWithName("strings", (Object[]) array);
	}

	public static void arrayNotEmpty(final Object... array) {
		arrayNotEmptyWithName("objects", array);
	}

	private static void arrayNotEmptyWithName(final String elementsName, final Object... array) {
		if (CommonTools.isEmpty(array)) {
			throw new IllegalArgumentException("No " + elementsName + " specified!");
		}
	}

	/**
	 * See {@link #notNull(Object, String)}.
	 */
	public static <T> T notNull(final T object) throws IllegalArgumentException {
		return notNull(object, "The passed argument must not be null!");
	}

	/**
	 * See {@link #notEmpty(String, String)}.
	 */
	public static String notEmpty(final String string) throws IllegalArgumentException {
		return notEmpty(string, "The passed string argument must not be null or empty!");
	}

	/**
	 * See {@link #notEmpty(Collection, String)}.
	 */
	public static <E> Collection<E> notEmpty(final Collection<E> collection) throws IllegalArgumentException {
		return notEmpty(collection, "The passed collection argument must not be null or empty!");
	}

	/**
	 * See {@link #notEmpty(Map, String)}.
	 */
	public static <K, V> Map<K, V> notEmpty(final Map<K, V> map) throws IllegalArgumentException {
		return notEmpty(map, "The passed map argument must not be null or empty!");
	}

	/**
	 * Returns the passed <code>object</code> or throws an {@link IllegalArgumentException} with the passed <code>errorMessage</code>, if it is
	 * <code>null</code>.
	 */
	public static <T> T notNull(final T object, final String errorMessage) throws IllegalArgumentException {
		if (object == null) {
			if (errorMessage == null) {
				throw new IllegalArgumentException();
			} else {
				throw new IllegalArgumentException(errorMessage);
			}
		}
		return object;
	}

	/**
	 * Returns the passed <code>string</code> or throws an {@link IllegalArgumentException} with the passed <code>errorMessage</code>, if it is
	 * <code>null</code> or empty.
	 */
	public static String notEmpty(final String string, final String errorMessage) throws IllegalArgumentException {
		if (string == null || string.length() == 0) {
			if (errorMessage == null) {
				throw new IllegalArgumentException();
			} else {
				throw new IllegalArgumentException(errorMessage);
			}
		}
		return string;
	}

	/**
	 * Returns the passed <code>collection</code> or throws an {@link IllegalArgumentException} with the passed <code>errorMessage</code>, if it is
	 * <code>null</code> or empty.
	 */
	public static <E> Collection<E> notEmpty(final Collection<E> collection, final String errorMessage) throws IllegalArgumentException {
		if (collection == null || collection.isEmpty()) {
			if (errorMessage == null) {
				throw new IllegalArgumentException();
			} else {
				throw new IllegalArgumentException(errorMessage);
			}
		}
		return collection;
	}

	/**
	 * Returns the passed <code>map</code> or throws an {@link IllegalArgumentException} with the passed <code>errorMessage</code>, if it is
	 * <code>null</code> or empty.
	 */
	public static <K, V> Map<K, V> notEmpty(final Map<K, V> map, final String errorMessage) throws IllegalArgumentException {
		if (map == null || map.size() == 0) {
			if (errorMessage == null) {
				throw new IllegalArgumentException();
			} else {
				throw new IllegalArgumentException(errorMessage);
			}
		}
		return map;
	}

	/**
	 * Makes sure the specified argument is not <code>null</code>. If it is, an exception is thrown (and the exception message will include the name
	 * of the argument).
	 *
	 * @see #notNullWithNames(Object...)
	 */
	public static void notNullWithName(final String argumentName, final Object argumentValue) {
		if (argumentValue == null) {
			throw new IllegalArgumentException("Argument '" + argumentName + "' must not be null!");
		}
	}

	/**
	 * Makes sure the specified arguments are not <code>null</code>. If they are, an exception is thrown (and the exception message will include the
	 * names of the all arguments that are null).
	 *
	 * @param argumentNamesAndValues
	 *            pairs of argument name/description and argument value.
	 *
	 * @see #notNullWithName(String, Object)
	 */
	public static void notNullWithNames(final Object... argumentNamesAndValues) {
		if (!CommonTools.isEven(argumentNamesAndValues.length)) {
			throw new IllegalArgumentException("Each argument must have a value! " + Arrays.asList(argumentNamesAndValues));
		}

		boolean success = true;
		for (int i = 0; i < argumentNamesAndValues.length - 1; i = i + 2) {
			if (argumentNamesAndValues[i + 1] == null) {
				success = false;
				break;
			}
		}

		if (success) {
			return;
		}

		final List<String> namesOfArgumentsThatAreNull = new ArrayList<>();
		for (int i = 0; i < (argumentNamesAndValues.length - 1); i = i + 2) {
			if (argumentNamesAndValues[i + 1] == null) {
				namesOfArgumentsThatAreNull.add((String) argumentNamesAndValues[i]);
			}
		}
		throw new IllegalArgumentException("The following arguments must not be null: " + namesOfArgumentsThatAreNull);
	}

	/**
	 * Makes sure the specified string arguments are not <code>null</code> or empty. If they are, an exception is thrown (and the exception message
	 * will include the names of the all arguments that are <code>null</code>/empty).
	 *
	 * @param argumentNamesAndValues
	 *            pairs of argument name/description and argument value.
	 */
	public static void notEmptyWithNames(final String... argumentNamesAndValues) {
		if (!CommonTools.isEven(argumentNamesAndValues.length)) {
			throw new IllegalArgumentException("Each argument must have a value! " + Arrays.asList(argumentNamesAndValues));
		}

		boolean success = true;
		for (int i = 0; i < argumentNamesAndValues.length - 1; i = i + 2) {
			final String value = argumentNamesAndValues[i + 1];
			if (value == null || value.length() == 0) {
				success = false;
				break;
			}
		}

		if (success) {
			return;
		}

		final List<String> namesOfArgumentsThatAreNull = new ArrayList<>();
		for (int i = 0; i < (argumentNamesAndValues.length - 1); i = i + 2) {
			final String value = argumentNamesAndValues[i + 1];
			if (value == null || value.length() == 0) {
				namesOfArgumentsThatAreNull.add(argumentNamesAndValues[i]);
			}
		}
		throw new IllegalArgumentException("The following string arguments must not be null or empty: " + namesOfArgumentsThatAreNull);
	}

}
