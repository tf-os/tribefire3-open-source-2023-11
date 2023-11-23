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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Condition;
import com.braintribe.common.lcd.Empty;

/**
 * Provides convenience methods used to avoid <code>null</code> checks. All methods get passed an argument that may be <code>null</code>, i.e. no
 * <code>NullPointerException</code> is thrown. Most methods just return the passed object unless it is <code>null</code> in which case a
 * default/empty substitute is returned. Other methods provide information about the passed object, e.g. {@link NullSafe#size(Collection)} returns the
 * {@link Collection#size() size} of the passed collection or <code>0</code>, if no collection is passed.
 *
 * @author michael.lafite
 */
public final class NullSafe {

	private NullSafe() {
		// no instantiation required
	}

	/**
	 * Either returns the passed <code>object</code> (if it isn't <code>null</code>) or the specified <code>defaultObject</code> (which must not be
	 * <code>null</code>).
	 */
	public static <T> T get(final T object, final T defaultObject) {
		return object == null ? defaultObject : object;
	}

	public static <T> T provide(T object, Supplier<T> defaultObjectSupplier) {
		return object == null ? defaultObjectSupplier.get() : object;
	}

	public static <T> T nonNull(T obj, String name) {
		return Objects.requireNonNull(obj, () -> name + " cannot be null");
	}

	/**
	 * Returns the {@link Class} of the passed <code>object</code>, or <code>null</code>, if no <code>object</code> is passed.
	 */
	public static <T> Class<T> clazz(final T object) {

		if (object == null) {
			return null;
		}

		final Class<T> clazz = (Class<T>) object.getClass();
		return clazz;
	}

	/**
	 * Same as {@link #clazz(Object)}.
	 */
	public static <T> Class<T> getClass(final T object) {
		return clazz(object);
	}

	/**
	 * Returns the name of the passed <code>clazz</code>, or <code>null</code>, if no <code>clazz</code> is passed.
	 */
	public static String className(final Class<?> clazz) {

		if (clazz == null) {
			return null;
		}

		return clazz.getName();
	}

	/**
	 * Returns the name of the <code>Class</code> of the passed <code>object</code>, or <code>null</code>, if no <code>object</code> is passed.
	 */
	public static String className(final Object object) {

		if (object == null) {
			return null;
		}

		return object.getClass().getName();
	}

	/**
	 * Returns the simple name of the passed <code>clazz</code>, or <code>null</code>, if no <code>clazz</code> is passed.
	 */
	public static String simpleClassName(final Class<?> clazz) {

		if (clazz == null) {
			return null;
		}

		// Class.getSimpleName() is not supported by GWT
		return ReflectionTools.getSimpleName(clazz);
	}

	/**
	 * Returns the simple name of the <code>Class</code> of the passed <code>object</code>, or <code>null</code>, if no <code>object</code> is passed.
	 */
	public static String simpleClassName(final Object object) {

		if (object == null) {
			return null;
		}

		// Class.getSimpleName() is not supported by GWT
		return simpleClassName(object.getClass());
	}

	/**
	 * Either returns the passed <code>string</code> or the empty string, if the passed <code>string</code> is <code>null</code>.
	 */
	public static String string(final String string) {
		return get(string, Empty.string());
	}

	/**
	 * Returns the {@link Object#toString() string representation} of the passed <code>object</code>, or <code>null</code>, if the passed
	 * <code>object</code> is <code>null</code>.
	 */
	public static String toString(final Object object) {
		if (object == null) {
			return null;
		}
		return object.toString();
	}

	/**
	 * Returns the {@link Enum#name() name} of the passed <code>enumObject</code>, or <code>null</code>, if the passed <code>enumObject</code> is
	 * <code>null</code>.
	 */
	public static <E extends Enum<?>> String enumName(final E enumObject) {
		if (enumObject == null) {
			return null;
		}
		return enumObject.name();
	}

	/**
	 * Returns the length of the passed <code>string</code> or <code>0</code>, if the <code>string</code> is <code>null</code>.
	 */
	public static int length(final String string) {
		return string == null ? 0 : string.length();
	}

	/**
	 * Returns the {@link #trim(String) trimmed} version of the passed <code>string</code> or the empty string, if the passed <code>string</code> is
	 * <code>null</code>.
	 */
	public static String trim(final String string) {
		final String result = string(string).trim();
		return result;
	}

	/**
	 * Null-safe version of {@link String#startsWith(String)}.
	 */
	public static boolean startsWith(final String string, final String prefix) {
		return string != null && string.startsWith(prefix);
	}

	/**
	 * Works like {@link #startsWith(String, String)}, but uses {@link String#charAt(int)} (which is less expensive).
	 */
	public static boolean startsWith(final String string, final char prefix) {
		return string != null && prefix == string.charAt(0);
	}

	/**
	 * {@link Collection#clear() Clears} the passed <code>collection</code>, unless it is <code>null</code>.
	 */
	public static void clear(final Collection<?> collection) {
		if (collection != null) {
			collection.clear();
		}
	}

	/**
	 * Returns an <code>Iterable</code> that is either the passed <code>iterable</code> or an empty (unmodifiable) collection, if the passed
	 * <code>iterable</code> is <code>null</code>. This method can be used to safely iterate through the elements of an <code>Iterable</code> without
	 * the initial <code>null</code> check.
	 */
	public static <E> Iterable<E> iterable(final Iterable<E> iterable) {
		final Iterable<E> defaultIterable = Empty.collection();
		return get(iterable, defaultIterable);
	}

	/**
	 * Returns an <code>Iterator</code> for the passed <code>iterable</code> or an iterator for an empty (unmodifiable) collection, if the passed
	 * <code>iterable</code> is <code>null</code>.
	 */
	public static <E> Iterator<E> iterator(final Iterable<E> iterable) {
		final Iterator<E> result = iterable(iterable).iterator();
		return result;
	}

	/**
	 * Returns a <code>Collection</code> that is either the passed <code>collection</code> or an empty (unmodifiable) collection, if the passed
	 * <code>collection</code> is <code>null</code>.
	 */
	public static <E> Collection<E> collection(final Collection<E> collection) {
		final Collection<E> defaultCollection = Empty.collection();
		return get(collection, defaultCollection);
	}

	/**
	 * Returns a <code>Set</code> that is either the passed <code>set</code> or an empty (unmodifiable) set, if the passed <code>set</code> is
	 * <code>null</code>.
	 */
	public static <E> Set<E> set(final Set<E> set) {
		return set != null ? set : Collections.emptySet();
	}

	/**
	 * Returns a <code>List</code> that is either the passed <code>list</code> or an empty (unmodifiable) list, if the passed <code>list</code> is
	 * <code>null</code>.
	 */
	public static <E> List<E> list(final List<E> list) {
		return list != null ? list : Collections.emptyList();
	}

	/**
	 * Returns either the passed <code>map</code> or an empty (unmodifiable) map, if the passed <code>map</code> is <code>null</code>. This method can
	 * be used to safely invoke (read-only) methods without the initial <code>null</code> check.
	 */
	public static <K, V> Map<K, V> map(final Map<K, V> map) {
		final Map<K, V> defaultMap = Empty.map();
		return get(map, defaultMap);
	}

	/**
	 * Returns either the {@link Map#keySet()} for the passed <code>map</code> or an empty (unmodifiable) key set.
	 */
	public static <K, V> Set<K> keySet(final Map<K, V> map) {
		return map != null ? map.keySet() : Collections.emptySet();
	}

	/**
	 * Invokes {@link #entrySet(Map)} on the passed <code>map</code> or on an empty map, if the passed <code>map</code> is <code>null</code> (see
	 * {@link #map(Map)}. This method can be used to safely iterate through the entries of a <code>Map</code> without the initial <code>null</code>
	 * check.
	 */
	public static <K, V> Set<Entry<K, V>> entrySet(final Map<K, V> map) {
		return map(map).entrySet();
	}

	/**
	 * Checks if the <code>searchElement</code> is part of the passed <code>collection</code>, or returns <code>false</code>, if the
	 * <code>collection</code> is <code>null</code>.
	 */
	public static boolean contains(final Collection<?> collection, final Object searchedElement) {
		return collection != null && collection.contains(searchedElement);
	}

	/**
	 * Returns the size of the passed <code>collection</code> or <code>0</code>, if the <code>collection</code> is <code>null</code>.
	 */
	public static int size(final Collection<?> collection) {
		return collection == null ? 0 : collection.size();
	}

	/**
	 * Returns the size of the passed <code>map</code> or <code>0</code>, if the <code>map</code> is <code>null</code>.
	 */
	public static int size(final Map<?, ?> map) {
		return map == null ? 0 : map.size();
	}

	/**
	 * Returns the length of the passed <code>array</code> or <code>0</code>, if the <code>array</code> is <code>null</code>.
	 */
	public static <E> int length(final E... array) {
		return array == null ? 0 : array.length;
	}

	/**
	 * Returns <code>false</code>, if the passed <code>condition</code> is <code>null</code>, otherwise the result of {@link Condition#evaluate()}.
	 */
	public static boolean evaluate(final Condition condition) {
		return condition != null && condition.evaluate();
	}

	/**
	 * {@link Comparable#compareTo(Object) Compares} the two {@link Comparable}s. Any object is considered to be less than <code>null</code>.
	 * Therefore, when this method is used for sorting, <code>null</code> elements will be at the end of the list.
	 *
	 * @return <code>0</code> if <code>comparable1</code> and <code>comparable2</code> are both <code>null</code>, <code>-1</code> if
	 *         <code>comparable2</code> is <code>null</code>, <code>1</code> if <code>comparable1</code> is <code>null</code>, otherwise the result of
	 *         <code>comparable1.compareTo(comparable2)</code>.
	 */
	public static <C extends Comparable<C>> int compare(final C comparable1, final C comparable2) {
		if (comparable1 == comparable2) {
			return 0;
		}

		if (comparable1 == null) {
			return -1;
		}

		if (comparable2 == null) {
			return 1;
		}

		return comparable1.compareTo(comparable2);
	}

	/**
	 * Same as {@link #compare(Comparable, Comparable)}.
	 */
	public static <C extends Comparable<C>> int cmp(C comparable1, C comparable2) {
		return compare(comparable1, comparable2);
	}

	/**
	 * Returns an array that is either the passed <code>array</code> or an {@link Empty empty array} (i.e. size <code>0</code>), if the passed
	 * <code>array</code> is <code>null</code>.
	 */
	public static <T> T[] array(final T... array) {
		return array == null ? (T[]) Empty.objectArray() : array;
	}

	/**
	 * See {@link #array(Object[])}.
	 */
	public static String[] array(final String... array) {
		return array == null ? Empty.stringArray() : array;
	}

	/**
	 * See {@link #array(Object[])}.
	 */
	public static boolean[] array(final boolean... array) {
		return array == null ? Empty.booleanArray() : array;
	}

	/**
	 * See {@link #array(Object[])}.
	 */
	public static byte[] array(final byte... array) {
		return array == null ? Empty.byteArray() : array;
	}

	/**
	 * See {@link #array(Object[])}.
	 */
	public static char[] array(final char... array) {
		return array == null ? Empty.charArray() : array;
	}

	/**
	 * See {@link #array(Object[])}.
	 */
	public static double[] array(final double... array) {
		return array == null ? Empty.doubleArray() : array;
	}

	/**
	 * See {@link #array(Object[])}.
	 */
	public static float[] array(final float... array) {
		return array == null ? Empty.floatArray() : array;
	}

	/**
	 * See {@link #array(Object[])}.
	 */
	public static int[] array(final int... array) {
		return array == null ? Empty.intArray() : array;
	}

	/**
	 * See {@link #array(Object[])}.
	 */
	public static long[] array(final long... array) {
		return array == null ? Empty.longArray() : array;
	}

	/**
	 * See {@link #array(Object[])}.
	 */
	// (short type is okay here)
	public static short[] array(final short... array) {
		return array == null ? Empty.shortArray() : array;
	}

	/**
	 * Returns whether the passed <code>string</code> {@link String#matches(String) matches} the specified <code>regex</code> or <code>false</code>,
	 * if any of those arguments are <code>null</code>.
	 */
	public static boolean matches(String string, String regex) {
		if (string == null || regex == null) {
			return false;
		}
		return string.matches(regex);
	}
}
