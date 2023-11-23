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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * <p>
 * A helper class that's used to get string representations of objects where implementing the {@link Object#toString()} method in their respective
 * classes is not feasible. The <code>CustomToStringBuilder</code> is useful especially when working with container objects (i.e. collections, maps,
 * arrays), because it relies on its own <code>toString(Object)</code> method to print the contents of these container classes. Furthermore it sorts
 * maps and sets, shows the contents of arrays (instead of just the arry object), etc.
 * </p>
 * <p>
 * Support for custom string representations can easily be added by passing a {@link CustomStringRepresentationProvider} to
 * {@link #CustomToStringBuilder(CustomStringRepresentationProvider)}. For objects for which the provider returns no result (i.e. <code>null</code>),
 * the {@link Object#toString()} default string representation will be returned.<br/>
 * Note that one can easily further customize the string representations by overriding any of the <code>protected</code> methods of this class, e.g.
 * {@link #nullToString()}, {@link #getCollectionElementSeparator()} , {@link #getMapEntryKeyValueSeparator()}, etc. For example, arrays are just
 * converted to lists by default. This can be changed by overriding {@link #arrayToStringAfterNullCheck(Object)}.
 * </p>
 *
 * @author michael.lafite
 */
public class CustomToStringBuilder {

	private static final CustomToStringBuilder DEFAULT_INSTANCE = new CustomToStringBuilder();

	private final CustomStringRepresentationProvider customStringRepresentationProvider;

	public CustomToStringBuilder() {
		this(null);
	}

	public static CustomToStringBuilder instance() {

		final CustomToStringBuilder result = DEFAULT_INSTANCE;
		return result;
	}

	public CustomToStringBuilder(final CustomStringRepresentationProvider customStringRepresentationProvider) {
		this.customStringRepresentationProvider = customStringRepresentationProvider;
	}

	public String toString(final Object object) {
		if (object == null) {
			return nullToString();
		}

		return toStringAfterNullCheck(object);
	}

	public String toStringVarArgs(final Object... objects) {
		return toString(objects);
	}

	protected String nullToString() {
		return "null";
	}

	protected String toStringAfterNullCheck(final Object object) {

		if (object.getClass().isArray()) {
			return arrayToStringAfterNullCheck(object);
		}

		if (object instanceof SortedMap) {
			return sortedMapToStringAfterNullCheck((SortedMap<?, ?>) object);
		}

		if (object instanceof Map) {
			return mapToStringAfterNullCheck((Map<?, ?>) object);
		}

		if (object instanceof SortedSet) {
			return sortedSetToStringAfterNullCheck((SortedSet<?>) object);
		}

		if (object instanceof Set) {
			return setToStringAfterNullCheck((Set<?>) object);
		}

		if (object instanceof Collection) {
			return collectionToStringAfterNullCheck((Collection<?>) object);
		}

		if (this.customStringRepresentationProvider != null) {
			final String result = this.customStringRepresentationProvider.toString(object);
			if (result != null) {
				return result;
			}
		}

		return unknownTypeToStringAfterNullCheck(object);
	}

	protected String unknownTypeToStringAfterNullCheck(final Object object) {
		// we assume that object.toString always returns a non-null string
		return object.toString();
	}

	protected String arrayToStringAfterNullCheck(final Object array) {
		return array.toString();
	}

	protected <K, V> String mapToStringAfterNullCheck(final Map<K, V> map) {
		// create SortedMap whose entries are sorted by the string representations of their keys
		final SortedMap<String, V> sortedMap = new TreeMap<>();
		for (final Entry<K, V> entry : map.entrySet()) {
			final String keyAsString = mapEntryKeyToString(entry.getKey());
			sortedMap.put(keyAsString, entry.getValue());
		}
		return sortedMapToStringAfterNullCheck(sortedMap);
	}

	protected <K, V> String sortedMapToStringAfterNullCheck(final SortedMap<K, V> map) {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getMapPrefix(map));
		if (!map.isEmpty()) {
			final String entrySeparator = getMapEntrySeparator();
			for (final Entry<K, V> entry : map.entrySet()) {
				StringTools.append(stringBuilder, mapEntryKeyToString(entry.getKey()), "=", mapEntryValueToString(entry.getValue()), entrySeparator);
			}
			stringBuilder.delete(stringBuilder.length() - entrySeparator.length(), stringBuilder.length());
		}
		stringBuilder.append(getMapSuffix(map));

		return stringBuilder.toString();
	}

	protected <E> String setToStringAfterNullCheck(final Set<E> set) {
		// get string representations of elements and put in sorted set
		final SortedSet<String> sortedSet = new TreeSet<>();
		for (final E element : set) {
			final String elementAsString = containerElementToString(element);
			sortedSet.add(elementAsString);
		}
		return sortedSetToStringAfterNullCheck(sortedSet);
	}

	protected <E> String sortedSetToStringAfterNullCheck(final SortedSet<E> set) {
		return collectionToStringAfterNullCheck(set);
	}

	protected <E> String collectionToStringAfterNullCheck(final Collection<E> collection) {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getCollectionPrefix(collection));
		if (!collection.isEmpty()) {
			final String elementSeparator = getCollectionElementSeparator();
			for (final E element : collection) {
				stringBuilder.append(collectionElementToString(element));
				stringBuilder.append(elementSeparator);
			}
			stringBuilder.delete(stringBuilder.length() - elementSeparator.length(), stringBuilder.length());
		}
		stringBuilder.append(getCollectionSuffix(collection));

		return stringBuilder.toString();
	}

	protected String containerElementToString(final Object element) {
		return toString(element);
	}

	protected String collectionElementToString(final Object element) {
		return containerElementToString(element);
	}

	protected String mapEntryKeyToString(final Object key) {
		return containerElementToString(key);
	}

	protected String mapEntryValueToString(final Object value) {
		return containerElementToString(value);
	}

	protected String getDefaultSeparator() {
		return ", ";
	}

	protected String getCollectionElementSeparator() {
		return getDefaultSeparator();
	}

	protected String getMapEntrySeparator() {
		return getDefaultSeparator();
	}

	protected String getMapEntryKeyValueSeparator() {
		return "=";
	}

	@SuppressWarnings("unused")
	protected <K, V> String getMapPrefix(final Map<K, V> map) {
		return "{";
	}

	@SuppressWarnings("unused")
	protected <K, V> String getMapSuffix(final Map<K, V> map) {
		return "}";
	}

	@SuppressWarnings("unused")
	protected <E> String getCollectionPrefix(final Collection<E> collection) {
		return "[";
	}

	@SuppressWarnings("unused")
	protected <E> String getCollectionSuffix(final Collection<E> collection) {
		return "]";
	}

	/**
	 * A simple interface to get custom string representations for one or more types, i.e. representations different than the ones returned by
	 * {@link Object#toString()}.
	 *
	 * @author michael.lafite
	 */
	public interface CustomStringRepresentationProvider {

		/**
		 * Returns a (custom) string representation for the passed <code>object</code>.
		 *
		 * @param object
		 *            the object for which to return the string representation. Must not be <code>null</code>!
		 * @return either the custom string representation or <code>null</code>, if the type of the passed <code>object</code> is not supported.
		 */

		String toString(Object object);
	}
}
