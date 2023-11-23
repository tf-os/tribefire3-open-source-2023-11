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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Constants;
import com.braintribe.common.lcd.Empty;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.common.lcd.UnreachableCodeException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class provides basic utility methods that can be used in other utility classes.
 *
 * @author michael.lafite
 */
public class CommonTools {

	public static final String LINE_SEPARATOR;
	public static final String FILE_SEPARATOR;

	static {
		LINE_SEPARATOR = Constants.LINE_SEPARATOR;
		FILE_SEPARATOR = Constants.FILE_SEPARATOR;
	}

	protected CommonTools() {
		// no instantiation required
	}

	public static List<Byte> toList(final byte... array) {
		final List<Byte> list = new ArrayList<>();
		for (final Byte element : array) {
			list.add(element);
		}
		return list;
	}

	public static List<Boolean> toList(final boolean... array) {
		final List<Boolean> list = new ArrayList<>();
		for (final Boolean element : array) {
			list.add(element);
		}
		return list;
	}

	public static List<Character> toList(final char... array) {
		final List<Character> list = new ArrayList<>();
		for (final Character element : array) {
			list.add(element);
		}
		return list;
	}

	public static List<Double> toList(final double... array) {
		final List<Double> list = new ArrayList<>();
		for (final Double element : array) {
			list.add(element);
		}
		return list;
	}

	public static List<Float> toList(final float... array) {
		final List<Float> list = new ArrayList<>();
		for (final Float element : array) {
			list.add(element);
		}
		return list;
	}

	public static List<Integer> toList(final int... array) {
		final List<Integer> list = new ArrayList<>();
		for (final Integer element : array) {
			list.add(element);
		}
		return list;
	}

	public static List<Long> toList(final long... array) {
		final List<Long> list = new ArrayList<>();
		for (final Long element : array) {
			list.add(element);
		}
		return list;
	}

	// (short type is okay here)
	public static List<Short> toList(final short... array) {
		final List<Short> list = new ArrayList<>();
		for (final Short element : array) {
			list.add(element);
		}
		return list;
	}

	public static <E> List<E> toList(final E... array) {
		final List<E> list = new ArrayList<>();
		@SuppressWarnings("unchecked")
		final List<E> arrayAsList = Arrays.asList(NullSafe.array(array));
		list.addAll(arrayAsList);
		return list;
	}

	/**
	 * Creates a set, adds the passed elements and returns the set.
	 */
	public static <E> Set<E> toSet(final E... array) {
		final Set<E> set = new HashSet<>();
		@SuppressWarnings("unchecked")
		final List<E> arrayAsList = Arrays.asList(NullSafe.array(array));
		set.addAll(arrayAsList);
		return set;
	}

	public static boolean isEmpty(final boolean... array) {
		return (array == null || array.length == 0);
	}

	public static boolean isEmpty(final byte... array) {
		return (array == null || array.length == 0);
	}

	public static boolean isEmpty(final char... array) {
		return (array == null || array.length == 0);
	}

	public static boolean isEmpty(final double... array) {
		return (array == null || array.length == 0);
	}

	public static boolean isEmpty(final float... array) {
		return (array == null || array.length == 0);
	}

	public static boolean isEmpty(final int... array) {
		return (array == null || array.length == 0);
	}

	public static boolean isEmpty(final long... array) {
		return (array == null || array.length == 0);
	}

	// (short type is okay here)
	public static boolean isEmpty(final short... array) {
		return (array == null || array.length == 0);
	}

	public static <E> boolean isEmpty(final E... array) {
		return (array == null || array.length == 0);
	}

	/**
	 * Returns <code>true</code> if the passed <code>collection</code> is <code>null</code> or empty.
	 */
	public static boolean isEmpty(final Collection<?> collection) {
		if (collection == null || collection.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if the passed <code>enumeration</code> is <code>null</code> or is empty (i.e. has no more elements).
	 */
	public static boolean isEmpty(final Enumeration<?> enumeration) {
		if (enumeration == null || !enumeration.hasMoreElements()) {
			return true;
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if the passed <code>map</code> is <code>null</code> or empty.
	 */
	public static boolean isEmpty(final Map<?, ?> map) {
		if (map == null || map.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether the passed <code>string</code> is empty.
	 *
	 * @return <code>true</code> if the passed <code>string</code> is <code>null</code> or {@link String#isEmpty() empty}.
	 */
	public static boolean isEmpty(final String string) {
		return string == null || string.isEmpty();
	}

	public static String requireNonEmpty(String s, String message) {
		if (isEmpty(s)) {
			throw new IllegalArgumentException(message);
		}
		return s;
	}

	public static String requireNonEmpty(String s, Supplier<String> messageSupplier) {
		if (isEmpty(s)) {
			throw new IllegalArgumentException(messageSupplier.get());
		}
		return s;
	}

	/**
	 * Checks whether the passed <code>string</code> is empty or only contains white space characters.
	 *
	 * @return <code>true</code> if the passed <code>string</code> is <code>null</code> or {@link String#isEmpty() empty} after {@link String#trim()
	 *         trimming}.
	 */
	public static boolean isBlank(final String string) {
		/* NOTE: trimming is less efficient than checking the string with Character.isWhiteSpace(). However, that's not GWT compatible. */
		return NullSafe.trim(string).length() == 0;
	}

	/**
	 * Returns <code>true</code>, if at least one object is <code>null</code>.
	 */
	public static boolean isAnyNull(final Object... objects) {
		Arguments.notNull(objects, "No objects specified!");
		for (final Object object : objects) {
			if (object == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code>, if at least one object is not <code>null</code>.
	 */
	public static boolean isAnyNotNull(final Object... objects) {
		Arguments.notNull(objects, "No objects specified!");
		for (final Object object : objects) {
			if (object != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if all objects are <code>null</code>.
	 */
	public static boolean isAllNull(final Object... objects) {
		Arguments.notNull(objects, "No objects specified!");
		for (final Object object : objects) {
			if (object != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns <code>true</code> if all of the passed <code>strings</code> are {@link #isEmpty(String) empty}.
	 */
	public static boolean isAllEmpty(final String... strings) {
		Arguments.arrayNotEmpty(strings);
		for (final String string : strings) {
			if (!isEmpty(string)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns <code>true</code> if at least one of the passed <code>strings</code> is {@link #isEmpty(String) empty}.
	 */
	public static boolean isAnyEmpty(final String... strings) {
		Arguments.arrayNotEmpty(strings);
		for (final String string : strings) {
			if (isEmpty(string)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if all of the passed <code>strings</code> are {@link #isBlank(String) blank}.
	 */
	public static boolean isAllBlank(final String... strings) {
		Arguments.arrayNotEmpty(strings);
		for (final String string : strings) {
			if (!isBlank(string)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns <code>true</code> if at least one of the passed <code>strings</code> is {@link #isBlank(String) blank}.
	 */
	public static boolean isAnyBlank(final String... strings) {
		Arguments.arrayNotEmpty(strings);
		for (final String string : strings) {
			if (isBlank(string)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the number of null pointers.
	 *
	 * @see #getNonNullPointerCount(Object...)
	 */
	public static int getNullPointerCount(final Object... objects) {
		Arguments.notNull(objects, "No objects specified!");

		int nullCount = 0;
		for (final Object object : objects) {
			if (object == null) {
				nullCount++;
			}
		}
		return nullCount;
	}

	/**
	 * Returns the number of objects that are not <code>null</code>.
	 *
	 * @see #getNullPointerCount(Object...)
	 */
	public static int getNonNullPointerCount(final Object... objects) {
		Arguments.notNull(objects, "No objects specified!");
		return objects.length - getNullPointerCount(objects);
	}

	/**
	 * Returns the first reference from the passed <code>objects</code> array that is not a null pointer (or <code>null</code> if the
	 * <code>objects</code> array is <code>null</code>, empty or contains only null pointers).
	 */

	public static Object getFirstNonNullPointer(final Object... objects) {
		for (final Object object : NullSafe.array(objects)) {
			if (object != null) {
				return object;
			}
		}
		return null;
	}

	/**
	 * Returns <code>true</code> if the passed object equals at least one element in the passed collection (i.e. if the collection
	 * {@link Collection#contains(Object) contains} the element). This method just returns <code>false</code> if the collection is <code>null</code>.
	 */
	public static boolean equalsAny(final Object object, final Collection<?> collection) {
		if (collection == null) {
			return false;
		}
		return collection.contains(object);
	}

	/**
	 * Returns <code>true</code> if the passed object equals at least one of the passed <code>objectsToCompareTo</code> ). This method just returns
	 * <code>false</code> if <code>objectsToCompareTo</code> is <code>null</code>.
	 */
	public static boolean equalsAny(final Object object, final Object... objectsToCompareTo) {
		if (objectsToCompareTo == null) {
			return false;
		}
		return toCollection(objectsToCompareTo).contains(object);
	}

	/**
	 * Returns 's' if the <code>count</code> is <code>!= 1</code>, otherwise an empty string.
	 */
	public static String getPluralS(final int count) {
		return getSingularOrPlural("", count);
	}

	/**
	 * Returns the <code>singular</code> if the specified <code>count</code> is <code>1</code>, otherwise it returns <code>singular</code> +
	 * <code>s</code>.
	 */
	public static String getSingularOrPlural(final String singular, final int count) {
		return getSingularOrPlural(singular, singular + "s", count);
	}

	/**
	 * Returns the <code>singular</code> if the specified <code>count</code> is <code>1</code>, otherwise the <code>plural</code>.
	 */
	public static String getSingularOrPlural(final String singular, final String plural, final int count) {
		if (count == Numbers.ONE) {
			return singular;
		}
		return plural;
	}

	/**
	 * Works like {@link #getSingularOrPlural(String, int)} except that it adds <code>count</code> (+ space) as prefix.
	 */
	public static String getCountAndSingularOrPlural(final int count, final String singular) {
		return count + " " + getSingularOrPlural(singular, count);
	}

	/**
	 * Works like {@link #getSingularOrPlural(String, String, int)} except that it adds <code>count</code> (+ space) as prefix.
	 */
	public static String getCountAndSingularOrPlural(final int count, final String singular, final String plural) {
		return count + " " + getSingularOrPlural(singular, plural, count);
	}

	/**
	 * Invokes {@link #getSingularOrPlural(String, int)} with the size of the <code>collection</code>.
	 */
	public static String getSingularOrPlural(final String singular, final Collection<?> collection) {
		return getSingularOrPlural(singular, NullSafe.size(collection));
	}

	/**
	 * Invokes {@link #getSingularOrPlural(String, String, int)} with the size of the <code>collection</code>.
	 */
	public static String getSingularOrPlural(final String singular, final String plural, final Collection<?> collection) {
		return getSingularOrPlural(singular, plural, NullSafe.size(collection));
	}

	/**
	 * Invokes {@link #getCountAndSingularOrPlural(int, String)} with the size of the <code>collection</code>.
	 */
	public static String getCountAndSingularOrPlural(final Collection<?> collection, final String singular) {
		return getCountAndSingularOrPlural(NullSafe.size(collection), singular);
	}

	/**
	 * Invokes {@link #getCountAndSingularOrPlural(int, String, String)} with the size of the <code>collection</code>.
	 */
	public static String getCountAndSingularOrPlural(final Collection<?> collection, final String singular, final String plural) {
		return getCountAndSingularOrPlural(NullSafe.size(collection), singular, plural);
	}

	/**
	 * Returns the passed <code>value</code> or the <code>defaultValue</code> if the <code>value</code> is <code>null</code>. Works like
	 * {@link NullSafe#get(Object, Object)}.
	 */
	public static <T> T getValueOrDefault(final T value, final T defaultValue) {
		return (value == null) ? defaultValue : value;
	}

	public static <T> T getValueOrSupplyDefault(T value, Supplier<T> valueSupplier) {
		return value == null ? valueSupplier.get() : value;
	}

	/**
	 * Parses the passed string as a <code>boolean</code>. In contrast to {@link Boolean#parseBoolean(String)} a string that doesn't equal "true" or
	 * "false" (ignoring the case) is not parsed as <code>false</code> but an exception is thrown.
	 *
	 * @param booleanString
	 *            the string to parse.
	 * @return <code>true</code> or <code>false</code>.
	 * @throws IllegalArgumentException
	 *             if the passed string couldn't be parsed.
	 */
	public static boolean parseBooleanOrThrowException(final String booleanString) throws IllegalArgumentException {

		final String booleanStringNonNull = Arguments.notNull(booleanString);

		if (booleanStringNonNull.equalsIgnoreCase("true")) {
			return true;
		}
		if (booleanStringNonNull.equalsIgnoreCase("false")) {
			return false;
		}
		throw new IllegalArgumentException(
				"Can't parse boolean value from string because string is not valid boolean: " + getStringRepresentation(booleanStringNonNull));
	}

	/**
	 * Parses the passed string as a <code>boolean</code>. If the string is <code>null</code> or empty, the default value is returned, otherwise the
	 * result of {@link #parseBooleanOrThrowException(String)} is returned.
	 */
	public static boolean parseBooleanOrThrowException(final String booleanString, final boolean defaultValue) throws IllegalArgumentException {
		if (isEmpty(booleanString)) {
			return defaultValue;
		}

		return parseBooleanOrThrowException(booleanString);
	}

	/**
	 * Parses the passed string as a <code>boolean</code>. This method accepts "true", "yes" and "1" as <code>true</code> and "false", "no" and "0" as
	 * <code>false</code>. Furthermore the case is ignored. If the value cannot be parsed, an exception is thrown.
	 *
	 * @param booleanString
	 *            the string to parse.
	 * @return <code>true</code> or <code>false</code>.
	 * @throws IllegalArgumentException
	 *             if the passed string couldn't be parsed.
	 * @see #parseBooleanOrThrowException(String)
	 */
	public static boolean parseOldBooleanOrThrowException(final String booleanString) throws IllegalArgumentException {

		final String lowerCaseBooleanString = Arguments.notNull(booleanString).toLowerCase();

		if ("true".equals(lowerCaseBooleanString) || "yes".equals(lowerCaseBooleanString) || "1".equals(lowerCaseBooleanString)) {
			return true;
		}
		if ("false".equals(lowerCaseBooleanString) || "no".equals(lowerCaseBooleanString) || "0".equals(lowerCaseBooleanString)) {
			return false;
		}
		throw new IllegalArgumentException(
				"Can't parse boolean value from string because it's not a valid boolean: " + getStringRepresentation(booleanString));
	}

	/**
	 * Returns <code>true</code>, if and only if <code>object1</code> and <code>object2</code> are not <code>null</code> and <code>object1</code>
	 * {@link Object#equals(Object) equals} <code>object2</code>, otherwise <code>false</code>.
	 */
	public static boolean equalsAndNotNull(final Object object1, final Object object2) {
		if (object1 == null || object2 == null) {
			return false;
		}
		return object1.equals(object2);
	}

	/**
	 * Checks if the passed objects are equal. The method works like {@link #equalsOrBothNull(Object, Object)}. The method returns <code>true</code>,
	 * if both objects are <code>null</code>, because that's also how it's implemented in collections.
	 */
	public static boolean equals(final Object object1, final Object object2) {
		return equalsOrBothNull(object1, object2);
	}

	/**
	 * Returns <code>true</code>, if and only if both objects are <code>null</code> or if <code>object1</code> {@link Object#equals(Object) equals}
	 * <code>object2</code>, otherwise <code>false</code>.
	 */
	public static boolean equalsOrBothNull(final Object object1, final Object object2) {
		if (object1 == null) {
			return (object2 == null);
		}
		return object1.equals(object2);
	}

	/**
	 * Returns <code>true</code> if one or both objects are <code>null</code> or if <code>object1</code> {@link Object#equals(Object) equals}
	 * <code>object2</code>, otherwise <code>false</code>.
	 */
	public static boolean equalsOrAtLeastOneNull(final Object object1, final Object object2) {
		if (object1 == null || object2 == null) {
			return true;
		}
		return object1.equals(object2);
	}

	/**
	 * Returns <code>true</code> if (only) one of the passed objects is <code>null</code> or if <code>object1</code> {@link Object#equals(Object)
	 * equals} <code>object2</code>, otherwise <code>false</code>.
	 */
	public static boolean equalsOrOneNull(final Object object1, final Object object2) {
		final int nullPointerCount = getNullPointerCount(object1, object2);

		switch (nullPointerCount) {
			case 0:
				return object1.equals(object2);
			case 1:
				return true;
			case 2:
				return false;
			default:
				throw new UnreachableCodeException("Unexpected null pointer count: " + nullPointerCount);
		}
	}

	/**
	 * Gets the string representation of the passed <code>object</code> which is determined in the follwing way: <br/>
	 * If the passed <code>object</code> is null, "null" is returned (without quotes).<br>
	 * If the passed <code>object</code> is a <code>String</code>, quotes are added to the <code>String</code> (--&gt; 'string'). <br>
	 * Otherwise the result of {@link Object#toString()} is returned.<br>
	 * This method can be used in log lines where one wants to distinguish between a <code>null</code> string, an empty string (or even the string
	 * "null") and between numbers or booleans and strings.
	 *
	 * @param object
	 *            the <code>object</code> whose string representation will be returned.
	 * @return the string representation of the passed <code>object</code> as described above.
	 */
	public static String getStringRepresentation(final Object object) {
		if (object == null) {
			return "null";
		}
		if (object instanceof String) {
			return "'" + ((String) object) + "'";
		}
		return object.toString();
	}

	public static String getParametersString(final Object... parametersAndValues) {
		return getParametersStringWithOptionalParentheses(true, parametersAndValues);
	}

	public static String getParametersStringWithoutParentheses(final Object... parametersAndValues) {
		return getParametersStringWithOptionalParentheses(false, parametersAndValues);
	}

	/**
	 * Helper method to get a string that lists method parameters and their actual values (e.g. for error messages). For example, if a method has two
	 * parameters "String someString" and "Integer someNumber" and their values are <code>"test"</code> and <code>123</code>, one can use this method
	 * to get the string "(someString='someValue',someNumber=123)".
	 *
	 * @param addParentheses
	 *            whether or not to add parentheses to the returned string.
	 * @param parametersAndValues
	 *            1..n pairs of parameter names and values, e.g. "param1, "value1", "param2", "value2", ... , "paramN ", "valueN". Alternatively one
	 *            may also pass a single {@link Map}. In that case the map is converted to an object array using {@link MapTools#toArray(Map, Class)}.
	 * @return the parameter description string.
	 */
	private static String getParametersStringWithOptionalParentheses(final boolean addParentheses, Object... parametersAndValues) {
		Arguments.notNull(parametersAndValues, "No parameters&values specified!");
		if (!isEven(parametersAndValues.length)) {
			if (parametersAndValues.length == 1 && parametersAndValues[0] instanceof Map) {
				Map<?, ?> parametersAndValuesAsMap = (Map<?, ?>) parametersAndValues[0];
				parametersAndValues = MapTools.toArray(parametersAndValuesAsMap, Object.class);
			} else {
				throw new IllegalArgumentException("Each parameter must have a value! " + Arrays.asList(parametersAndValues));
			}
		}
		final StringBuilder stringBuilder = new StringBuilder();
		if (addParentheses) {
			stringBuilder.append('(');
		}
		for (int i = 0; i < parametersAndValues.length - 1; i = i + 2) {
			if (i > Numbers.ZERO) {
				stringBuilder.append(',');
			}
			if (parametersAndValues[i] == null) {
				throw new IllegalArgumentException("A parameter name must not be null! Parameters and values: " + Arrays.asList(parametersAndValues));
			}
			final String parameter = parametersAndValues[i].toString();
			final String parameterValue = getStringRepresentation(parametersAndValues[i + 1]);
			StringTools.append(stringBuilder, parameter, "=", parameterValue);
		}
		if (addParentheses) {
			stringBuilder.append(')');
		}
		return stringBuilder.toString();
	}

	/**
	 * Returns the hash code of the object or <code>0</code> if the passed object is <code>null</code>.
	 *
	 * @see Object#hashCode()
	 */
	public static int getHashCode(final Object object) {
		if (object != null) {
			return object.hashCode();
		}
		return 0;
	}

	/**
	 * Checks if the passed <code>number</code> is even.
	 */
	public static boolean isEven(final long number) {
		return ((number % 2) == 0);
	}

	/**
	 * Creates a new collection, adds the passed elements and returns the collection.
	 */
	public static <E> Collection<E> toCollection(final E... elements) {
		final List<E> collection = new ArrayList<>();
		if (elements != null) {
			collection.addAll(Arrays.asList(elements));
		}
		return collection;
	}

	/**
	 * Returns the defaultString if the passed <code>object</code> is <code>null</code>, otherwise the result of {@link Object#toString()}.
	 */
	public static String toStringOrDefault(final Object object, final String defaultString) {
		return (object == null) ? defaultString : object.toString();
	}

	/**
	 * Returns <code>null</code> if the passed object is <code>null</code>, otherwise the result of {@link Object#toString()}.
	 */

	public static String toStringOrNull(final Object object) {
		String result = null;
		if (object != null) {
			result = object.toString();
		}
		return result;
	}

	/**
	 * Returns an empty string if the passed object is <code>null</code>, otherwise the result of {@link Object#toString()}.
	 */
	public static String toStringOrEmptyString(final Object object) {
		return toStringOrDefault(object, Empty.string());
	}

	/**
	 * Checks if the passed <code>value</code> is in the specified range.
	 */
	public static boolean isInRange(final double value, final double min, final double max) {
		return (value >= min) && (value <= max);
	}

	/**
	 * Checks if the passed <code>value</code> is in the specified range.
	 */
	// false warning in eclipse

	public static boolean isInRange(final double value, final Double min, final Double max) {
		if (min == null && max == null) {
			return true;
		}
		if (min == null) {
			return (value <= max);
		}
		if (max == null) {
			return (value >= min);
		}
		return (value >= min) && (value <= max);
	}

	/**
	 * Checks if the passed double <code>value</code> is an integral number or not.
	 *
	 * @param value
	 *            the double value to check.
	 * @return <code>true</code> if the passed value is an integral number, otherwise <code>false</code>.
	 */
	// equality check is intended
	@SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
	public static boolean isIntegralNumber(final double value) {
		return (((long) value) == value);
	}

	/**
	 * Returns the enum value for the specified <code>enumName</code>. The method at first compares the specified <code>enumName</code> to all the
	 * {@link Class#getEnumConstants() enum constants} of the specified <code>enumClass</code> using the <code>==</code> operator. If the enum
	 * constant is not found that way, the comparison is performed again using {@link String#equalsIgnoreCase(String)}. Please note that this
	 * algorithm is a bit slower than using {@link Enum#valueOf(Class, String)} if the enum name is (usually) specified correctly (i.e. case
	 * sensitive), but it's faster otherwise. Furthermore the <code>valueOf</code> method would force a stronger generic restriction.
	 *
	 * @param enumClass
	 *            The Enum class. Must not be <code>null</code>.
	 * @param enumName
	 *            The name of the searched enum value. Must not be <code>null</code> or empty.
	 * @return the enum value.
	 * @throws IllegalArgumentException
	 *             if the specified enum value doesn't exist.
	 */
	@SuppressFBWarnings("ES_COMPARING_PARAMETER_STRING_WITH_EQ")
	public static <E extends Enum<?>> E parseEnum(final Class<E> enumClass, final String enumName) {

		Arguments.notNullWithNames("enumClass", enumClass, "enumName", enumName);

		final E[] enumConstants = enumClass.getEnumConstants();

		for (final E enumConstant : enumConstants) {
			if (enumConstant.name() == enumName) {
				return enumConstant;
			}
		}

		for (final E enumConstant : enumConstants) {
			if (enumConstant.name().equalsIgnoreCase(enumName)) {
				return enumConstant;
			}
		}

		throw new IllegalArgumentException("Couldn't find enum constant " + getStringRepresentation(enumName) + " for enum class "
				+ enumClass.getName() + ". Valid constants are: " + Arrays.asList(enumConstants));
	}

	/**
	 * Busy-waits for the specified amount of milliseconds.
	 *
	 * @param milliseconds
	 *            the time to wait.
	 */
	public static void busyWait(final long milliseconds) {
		final long endTime = System.currentTimeMillis() + milliseconds;
		while (true) {
			if (System.currentTimeMillis() > endTime) {
				break;
			}
		}
	}

	/**
	 * Returns the passed <code>object</code> or throws a {@link NullPointerException} if it is <code>null</code>.
	 *
	 * @see #objNotNull(Object, String)
	 */
	public static <T> T objNotNull(final T object) throws NullPointerException {
		return objNotNull(object, null);
	}

	/**
	 * Returns the passed <code>object</code> or throws a {@link NullPointerException} with the passed <code>errorMessage</code> if it is
	 * <code>null</code>.
	 *
	 * @see #objNotNull(Object)
	 */
	public static <T> T objNotNull(final T object, final String errorMessage) throws NullPointerException {
		if (object == null) {
			if (errorMessage == null) {
				throw new NullPointerException();
			} else {
				throw new NullPointerException(errorMessage);
			}
		}
		return object;
	}

	/**
	 * Verifies that all passed objects are not <code>null</code>.
	 *
	 * @see #objsNotNullWithMsg(String, Object...)
	 */
	public static void objsNotNull(final Object... objectNamesAndObjects) throws NullPointerException {
		objsNotNullWithMsg(null, objectNamesAndObjects);
	}

	/**
	 * Verifies that none of the passed objects is <code>null</code>.
	 *
	 * @param errorMessage
	 *            an optional error message.
	 * @param objectNamesAndObjects
	 *            pairs of object names and objects.
	 * @throws NullPointerException
	 *             if any of the passed objects is <code>null</code>. The exception message includes the passed <code>errorMessage</code> (if set) and
	 *             the names of objects that are null.
	 * @see Arguments#notNullWithNames(Object...)
	 */
	public static void objsNotNullWithMsg(final String errorMessage, final Object... objectNamesAndObjects) throws NullPointerException {
		if (isEmpty(objectNamesAndObjects)) {
			return;
		}

		if (!isEven(objectNamesAndObjects.length)) {
			throw new IllegalArgumentException("The passed object names and objects are not valid: " + Arrays.asList(objectNamesAndObjects));
		}

		final List<String> namesOfObjectsThatAreNull = new ArrayList<>();

		for (int i = 0; i < objectNamesAndObjects.length - 1; i += 2) {
			if (isEmpty((String) objectNamesAndObjects[i])) {
				throw new NullPointerException("Object names must not be empty! Object names and objects: " + Arrays.asList(objectNamesAndObjects));
			}

			if (objectNamesAndObjects[i + 1] == null) {
				namesOfObjectsThatAreNull.add((String) objectNamesAndObjects[i]);
			}
		}

		if (namesOfObjectsThatAreNull.isEmpty()) {
			return;
		}

		throw new NullPointerException((errorMessage == null ? "" : errorMessage + " ") + "Found unexpected null pointer"
				+ getPluralS(namesOfObjectsThatAreNull.size()) + " for: " + namesOfObjectsThatAreNull);
	}

	/**
	 * Converts the first letter of the passed <code>string</code> to upper case. If the string is {@link #isEmpty(String) empty}, the string is not
	 * changed.
	 */
	public static String capitalize(final String string) {
		if (isEmpty(string)) {
			return string;
		}
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	/**
	 * Converts the first letter of the passed <code>string</code> to lower case. If the string is {@link #isEmpty(String) empty}, the string is not
	 * changed.
	 */
	public static String uncapitalize(final String string) {
		if (isEmpty(string)) {
			return string;
		}
		return string.substring(0, 1).toLowerCase() + string.substring(1);
	}

	/**
	 * Returns the typical getter method name for the specified <code>property</code> (i.e. field name).
	 */
	public static String getGetterMethodName(final String property) {
		return "get" + capitalize(property);
	}

	/**
	 * Returns the typical setter method name for the specified <code>property</code> (i.e. field name).
	 */
	public static String getSetterMethodName(final String propertyName) {
		return "set" + capitalize(propertyName);
	}

	/**
	 * Creates a list, adds the passed elements and returns the list.
	 */
	public static <E> List<E> getList(final E... elements) {
		return toList(elements);
	}

	/**
	 * Creates a set, adds the passed elements and returns the set.
	 */
	public static <E> Set<E> getSet(final E... elements) {
		return toSet(elements);
	}

	/**
	 * Creates a list of <code>Object</code>s, adds the passed elements and returns the list.
	 */
	public static List<Object> getObjectList(final Object... elements) {
		return getList(elements);
	}

	/**
	 * Creates a map where the keys are the elements of the passed <code>list</code> and the values are the indexes of the elements in the
	 * <code>list</code>.
	 */
	public static <E> Map<E, Integer> getElementToIndexMap(final List<E> list) {
		final Map<E, Integer> map = new HashMap<>();
		int index = 0;
		for (final E element : NullSafe.iterable(list)) {
			map.put(element, index);
			index++;
		}
		return map;
	}

	/**
	 * Returns the simple name part of the passed <code>fullyQualifiedClassName</code>, i.e. the sub string after the last '.'.
	 */
	public static String getSimpleNameFromFullyQualifiedClassName(final String fullyQualifiedClassName) {
		final int index = fullyQualifiedClassName.lastIndexOf('.');
		if (index < Numbers.ZERO) {
			// likely an error, but could be the default package ...
			return fullyQualifiedClassName;
		}

		final String result = fullyQualifiedClassName.substring(index + 1);
		return result;
	}

	/**
	 * Returns the package name part of the passed <code>fullyQualifiedClassName</code>, i.e. the sub string before the last '.'.
	 */
	public static String getPackageNameFromFullyQualifiedClassName(final String fullyQualifiedClassName) {
		final int index = fullyQualifiedClassName.lastIndexOf('.');
		if (index < Numbers.ZERO) {
			// could e.g. be the default package ...
			return "";
		}

		final String result = fullyQualifiedClassName.substring(0, index);
		return result;
	}

	/**
	 * Returns the class name part of the passed <code>fullyQualifiedClassName</code>, i.e. the sub string after the last '.' (or the whole string, if
	 * it doesn't contain ".").
	 */
	public static String getClassNameFromFullyQualifiedClassName(final String fullyQualifiedClassName) {
		final int index = fullyQualifiedClassName.lastIndexOf('.');
		if (index < Numbers.ZERO) {
			return fullyQualifiedClassName;
		}

		final String result = fullyQualifiedClassName.substring(index + 1);
		return result;
	}

	/**
	 * Returns the element with the specified <code>index</code> in the passed <code>array</code>. The <code>array</code> may be <code>null</code> and
	 * the <code>index</code> may be out of bounds. In both cases, the method returns <code>null</code>.
	 */

	public static <E> E getElementOrNull(final E[] array, final int index) {
		E result = null;
		if (array != null) {
			try {
				result = array[index];
			} catch (final ArrayIndexOutOfBoundsException e) {
				// ignore
			}
		}
		return result;
	}

	/**
	 * Returns the element with the specified <code>index</code> in the passed <code>array</code>. The <code>collection</code> may be
	 * <code>null</code> and the <code>index</code> may be out of bounds. In both cases, the method returns <code>null</code>.
	 */

	public static <E> E getElementOrNull(final List<E> collection, final int index) {
		E result = null;
		if (collection != null) {
			try {
				result = collection.get(index);
			} catch (final IndexOutOfBoundsException e) {
				// ignore
			}
		}
		return result;
	}

	/**
	 * Throws an {@link IllegalArgumentException}, if the passed <code>object</code> is not an array.
	 */
	public static void assertIsArray(final Object object) {
		if (object == null) {
			throw new IllegalArgumentException("The passed object is not an array (it is null)!");
		}

		if (!object.getClass().isArray()) {
			throw new IllegalArgumentException(
					"The passed object is not an array! " + CommonTools.getParametersString("object", object, "object class", object.getClass()));
		}
	}

	/**
	 * Making equivalent function exist in Java 8 (java.util.Objects.isNull)
	 */
	public static boolean isNull(Object o) {
		return null == o;
	}

	/**
	 * Making equivalent function exist in Java 8 (java.util.Objects.nonNull)
	 */
	public static boolean nonNull(Object o) {
		return null != o;
	}

	/**
	 * Returns the same string that the {@link Object#toString() default toString implementation} would return or "null", if the passed object is
	 * <code>null</code>.
	 *
	 * @see #defaultToStringWithIdentityHashCode(Object)
	 */
	public static String defaultToString(Object object) {
		if (object == null) {
			return "null";
		}
		String result = object.getClass().getName() + '@' + Integer.toHexString(object.hashCode());
		return result;
	}

	/**
	 * Similar to {@link #defaultToString(Object)}, but uses the {@link System#identityHashCode(Object) identity hash code}.
	 */
	public String defaultToStringWithIdentityHashCode(Object object) {
		if (object == null) {
			return "null";
		}
		String result = object.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(object));
		return result;
	}

	/** Returns given object casted to whatever was inferred by java compiler. */
	@SuppressWarnings("unchecked")
	public static <T> T cast(Object o) {
		return (T) o;
	}
}
