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

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import com.braintribe.common.lcd.GenericTaskWithContext;
import com.braintribe.common.lcd.GenericTaskWithNullableContext;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.common.lcd.Pair;

/**
 * This class provides utility methods related to {@link Map}s.
 *
 * @author michael.lafite
 */
public class MapTools {

	protected MapTools() {
		// nothing to do
	}

	/**
	 * Converts the passed map to a map with <code>Integer</code> keys. The keys are parsed using {@link Integer#parseInt(String)}.
	 *
	 * @param map
	 *            the map to convert.
	 * @return the converted map.
	 */
	public static Map<Integer, String> convertMapToMapWithIntegerKeys(final Map<String, String> map) {
		final Map<Integer, String> integerKeysMap = new HashMap<>();
		for (final Entry<String, String> entry : map.entrySet()) {
			final int integer = Integer.parseInt(entry.getKey());
			integerKeysMap.put(integer, entry.getValue());
		}
		return integerKeysMap;
	}

	/**
	 * Converts the passed map to a map with keys and values of type <code>String</code>. Keys and values are converted to <code>String</code>s using
	 * {@link CommonTools#toStringOrNull(Object)}.
	 *
	 * @param map
	 *            the map to convert.
	 * @return the converted map.
	 */
	public static Map<String, String> convertMapToMapWithStringKeysAndValues(final Map<?, ?> map) {
		final Map<String, String> stringMap = new HashMap<>();
		for (final Entry<?, ?> entry : map.entrySet()) {
			final String key = CommonTools.toStringOrNull(entry.getKey());
			final String value = CommonTools.toStringOrNull(entry.getValue());
			stringMap.put(key, value);
		}
		return stringMap;
	}

	/**
	 * Removes the specified prefix from the keys in the passed map.
	 *
	 * @throws IllegalArgumentException
	 *             if any of the keys does not start with the specified prefix.
	 */
	public static void removePrefixFromKeys(final Map<String, Object> map, final String prefix) throws IllegalArgumentException {
		final Map<String, Object> temp = new HashMap<>();
		final int prefixLengh = prefix.length();
		for (final Entry<String, Object> entry : map.entrySet()) {
			final String key = entry.getKey();
			if (CommonTools.isEmpty(key) || !key.startsWith(prefix)) {
				throw new IllegalArgumentException("Cannot remove prefix because key doesn't start with the specified prefix!"
						+ CommonTools.getParametersString("prefix", prefix, "key", key, "map", map));
			}

			temp.put(StringTools.removeFirstNCharacters(key, prefixLengh), entry.getValue());
		}

		map.clear();
		map.putAll(temp);
	}

	/**
	 * Searches the specified old values in the passed map and replaces them with the specified new ones.
	 *
	 * @param <K>
	 *            type of the keys in map.
	 * @param <V>
	 *            type of the values in the map.
	 * @param map
	 *            the map that contains the values to be replaced.
	 * @param oldAndNewValues
	 *            Pairs of old values and new values (old values will be searched in the map and then replaced with new values).
	 */
	public static <K, V> void replaceValuesInMap(final Map<K, V> map, final Map<V, V> oldAndNewValues) {

		final Map<K, V> newValues = new HashMap<>();

		for (final Entry<V, V> oldAndNewValue : oldAndNewValues.entrySet()) {
			if (map.containsValue(oldAndNewValue.getKey())) {
				for (final Entry<K, V> keyAndValue : map.entrySet()) {
					if (CommonTools.equalsOrBothNull(keyAndValue.getValue(), oldAndNewValue.getKey())) {
						newValues.put(keyAndValue.getKey(), oldAndNewValue.getValue());
					}
				}
			}
		}

		map.putAll(newValues);
	}

	/**
	 * This method checks for each entry of <code>mapToAdd</code> if the passed <code>map</code> already contains a an entry with the same key and
	 * only adds the entry if it does not.
	 *
	 * @param map
	 *            the map the entries will be added to.
	 * @param mapToAdd
	 *            the map containing the entries that are added to the other map.
	 */
	public static <K, V, V2 extends V> void addToMapButDoNotReplace(final Map<K, V> map, final Map<K, V2> mapToAdd) {
		for (final Entry<K, V2> entry : mapToAdd.entrySet()) {
			if (!map.containsKey(entry.getKey())) {
				map.put(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Returns a new map that only contains the mappings of the passed <code>map</code> where the keys match the specified regular expression.
	 */
	public static <V> Map<String, V> getMapWithMatchingKeys(final Map<String, V> map, final String regex) {

		final Map<String, V> result = new HashMap<>();

		for (final Entry<String, V> entry : map.entrySet()) {
			if (entry.getKey().matches(regex)) {
				result.put(entry.getKey(), entry.getValue());
			}
		}

		return result;
	}

	/**
	 * @see CommonTools#isEmpty(Map)
	 */
	public static boolean isEmpty(final Map<?, ?> map) {
		return CommonTools.isEmpty(map);
	}

	/**
	 * Returns a map containing all key/value pairs of <code>mapToCheck</code> where the key is contained in <code>mapToCheck</code> but missing in
	 * <code>mapToCompareTo</code>.
	 */
	public static <K, V> Map<K, V> getAdditionalEntries(final Map<K, V> mapToCheck, final Map<K, V> mapToCompareTo) {
		final Map<K, V> result = new HashMap<>();
		if (mapToCheck != null) {
			if (mapToCompareTo == null) {
				result.putAll(mapToCheck);
			} else {
				final List<K> additionalKeys = CollectionTools.getAdditionalElements(Not.Null(mapToCheck.keySet()),
						Not.Null(mapToCompareTo.keySet()));
				result.putAll(getMapWithSearchedKeys(mapToCheck, additionalKeys));
			}
		}
		return result;
	}

	/**
	 * Returns a map containing all key/value pairs of <code>mapToCompareTo</code> where the key is missing in <code>mapToCheck</code> but contained
	 * in <code>mapToCompareTo</code>.
	 */
	public static <K, V> Map<K, V> getMissingEntries(final Map<K, V> mapToCheck, final Map<K, V> mapToCompareTo) {
		return getAdditionalEntries(mapToCompareTo, mapToCheck);
	}

	/**
	 * Returns all key/value pairs from <code>mapToCheck</code> where the key exists in <code>mapToCompareTo</code> but the value is different.
	 */
	public static <K, V> Map<K, V> getModifiedEntries(final Map<K, V> mapToCheck, final Map<K, V> mapToCompareTo) {
		final Map<K, V> result = new HashMap<>();
		final List<K> keysContainedInBothMaps = CollectionTools.getIntersection(Not.Null(mapToCheck.keySet()), Not.Null(mapToCompareTo.keySet()));
		for (final K key : keysContainedInBothMaps) {
			final V value = mapToCheck.get(key);
			final V valueToCompareTo = mapToCompareTo.get(key);
			if (!CommonTools.equalsOrBothNull(value, valueToCompareTo)) {
				result.put(key, value);
			}
		}
		return result;
	}

	/**
	 * Returns a {@link SortedMap} containing all the entries of the passed <code>map</code>.
	 */
	public static <K, V> Map<K, V> getSortedMap(final Map<K, V> map) {
		final SortedMap<K, V> sortedMap = new TreeMap<>();
		putAll(sortedMap, map);
		return sortedMap;
	}

	/**
	 * Returns a map containing the key/value pairs whose keys are contained in <code>searchedKeys</code>.
	 */
	public static <K, V> Map<K, V> getMapWithSearchedKeys(final Map<K, V> map, final Collection<K> searchedKeys) {
		final Map<K, V> result = new HashMap<>();
		if (!isEmpty(map) && !CollectionTools.isEmpty(searchedKeys)) {
			for (final K key : searchedKeys) {
				result.put(key, map.get(key));
			}
		}
		return result;
	}

	/**
	 * Returns a map containing the passed <code>keys</code> (all mapping to the specified <code>value</code>).
	 */
	public static <K, V> Map<K, V> getMapWithKeys(final Set<K> keys, final V value) {
		final Map<K, V> result = new HashMap<>();
		if (!CollectionTools.isEmpty(keys)) {
			for (final K key : keys) {
				result.put(key, value);
			}
		}
		return result;
	}

	/**
	 * Returns a map containing with the passed <code>keys</code> (all mapping to <code>null</code>).
	 *
	 * @see #getMapWithKeys(Set, Object)
	 */
	public static <K> Map<K, Object> getMapWithKeys(final Set<K> keys) {
		return getMapWithKeys(keys, null);
	}

	/**
	 * Converts the passed <code>map</code> to a string array where each element has the format [entry key] + <code>keyValueSeparatory</code> + [entry
	 * value].
	 */
	public static String[] convertMapToArray(final Map<?, ?> map, final String keyValueSeparator) {
		final String[] array = new String[map.size()];
		int index = 0;
		for (final Entry<?, ?> entry : map.entrySet()) {
			array[index] = entry.getKey() + keyValueSeparator + entry.getValue();
			index++;
		}
		return array;
	}

	/**
	 * Adds the <code>entriesToAdd</code> to the <code>map</code>. <code>entriesToAdd</code> may be <code>null</code> (in which case no entries are
	 * added).
	 *
	 * @throws NullPointerException
	 *             if the <code>map</code> is <code>null</code>.
	 */
	public static <K, V> void putAll(final Map<K, V> map, final Map<K, V> entriesToAdd) {
		Arguments.notNull(map, "Cannot add entries to map because the map is null! The following entries would have been added: " + entriesToAdd);
		if (!isEmpty(entriesToAdd)) {
			map.putAll(entriesToAdd);
		}
	}

	/**
	 * Puts the passed <code>value</code> into the passed <code>map</code> unless the specified <code>key</code> is <code>null</code>.
	 */
	public static <K, V> void putIfKeyNotNull(final Map<K, V> map, final K key, final V value) {
		if (key != null) {
			map.put(key, value);
		}
	}

	/**
	 * Adds a new entry with the specified <code>key</code> to the passed <code>map</code> unless the String <code>value</code> is
	 * {@link StringTools#isEmpty(String) empty}.
	 */
	public static <K> void putIfValueNotEmpty(final Map<K, String> map, final K key, final String value) {
		if (!StringTools.isEmpty(value)) {
			map.put(key, value);
		}
	}

	/**
	 * Adds a new entry with the specified <code>key</code> to the passed <code>map</code> unless the <code>value</code> is <code>null</code>.
	 */
	public static <K, V> void putIfValueNotNull(final Map<K, V> map, final K key, final V value) {
		if (value != null) {
			map.put(key, value);
		}
	}

	/**
	 * Encodes the passed map as string.
	 *
	 * @throws IllegalArgumentException
	 *             if the map contains keys and/or values that are <code>null</code>, but no <code>nullString</code> is set.
	 * @see #decodeMap(String, String, String, String)
	 */
	public static String encodeMap(final Map<String, String> map, final String entrySeparator, final String keyAndValueSeparator,
			final String nullString, final boolean separatorCheckEnabled) throws IllegalArgumentException {
		final StringBuilder stringBuilder = new StringBuilder();
		if (!isEmpty(map)) {
			boolean firstEntry = true;

			for (final Entry<String, String> entry : map.entrySet()) {
				if (firstEntry) {
					firstEntry = false;
				} else {
					stringBuilder.append(entrySeparator);
				}

				String key = entry.getKey();
				if (key == null) {
					if (nullString == null) {
						throw new IllegalArgumentException(
								"Cannot encode null key because no nullString has been set! " + CommonTools.getParametersString("map", map));
					}

					key = nullString;
				}

				String value = entry.getValue();
				if (value == null) {
					if (nullString == null) {
						throw new IllegalArgumentException(
								"Cannot encode null value because no nullString has been set! " + CommonTools.getParametersString("map", map));
					}
					value = nullString;
				}

				if (separatorCheckEnabled) {

					if (key.contains(keyAndValueSeparator)) {
						throw new IllegalArgumentException("Error while encoding map! Key contains key/Value separator! "
								+ CommonTools.getParametersString("key", key, "separator", keyAndValueSeparator));
					}

					if (key.contains(entrySeparator)) {
						throw new IllegalArgumentException("Error while encoding map! Key contains entry separator! "
								+ CommonTools.getParametersString("key", key, "separator", entrySeparator));
					}

					if (value.contains(entrySeparator)) {
						throw new IllegalArgumentException("Error while encoding map! Value contains entry separator! "
								+ CommonTools.getParametersString("key", key, "value", value, "separator", entrySeparator));
					}
				}

				stringBuilder.append(key);
				stringBuilder.append(keyAndValueSeparator);
				stringBuilder.append(value);
			}
		}
		return stringBuilder.toString();
	}

	/**
	 * Decodes the passed <code>entries</code>, puts them into a map and returns it.
	 */
	public static Map<String, String> decodeEntries(final Collection<String> entries, final String keyAndValueSeparator) {

		final Map<String, String> result = new HashMap<>();
		for (final String entry : entries) {
			Pair<String, String> keyAndValue = null;

			try {
				keyAndValue = StringTools.splitStringOnce(entry, keyAndValueSeparator);
			} catch (final IllegalArgumentException e) {
				throw new IllegalArgumentException("Couldn't split key/value pair! " + CommonTools.getParametersString("erroneous key/value pair",
						entry, "entries", entries, "key/value separator", keyAndValueSeparator), e);
			}
			result.put(keyAndValue.getFirst(), keyAndValue.getSecond());
		}

		return result;
	}

	/**
	 * Decodes the passed string and returns the decoded map.
	 *
	 * @see #encodeMap(Map, String, String, String, boolean)
	 */
	public static Map<String, String> decodeMap(final String mapString, final String entrySeparator, final String keyAndValueSeparator,
			final String nullString) {

		final Map<String, String> map = new HashMap<>();

		boolean nullKeyFound = false;
		if (!StringTools.isEmpty(mapString)) {
			final List<String> entryList = CollectionTools.decodeCollection(mapString, entrySeparator, false, false, false, false);

			for (final String entry : entryList) {

				Pair<String, String> keyAndValue = null;

				try {
					keyAndValue = StringTools.splitStringOnce(Not.Null(entry), keyAndValueSeparator);
				} catch (final IllegalArgumentException e) {
					throw new IllegalArgumentException("Couldn't split key/value pair! " + CommonTools.getParametersString("erroneous key/value pair",
							entry, "mapString", mapString, "entry separator", entrySeparator, "key/value separator", keyAndValueSeparator), e);
				}

				String key = null;
				if (nullString == null || !nullString.equals(keyAndValue.getFirst())) {
					// key is not null
					key = keyAndValue.getFirst();
				} else {
					// key is null
					if (nullKeyFound) {
						throw new IllegalArgumentException("Found multiple null-keys in encoded map string! "
								+ CommonTools.getParametersString("mapString", mapString, "nullString", nullString));
					} else {
						nullKeyFound = true;
					}
				}

				String value = null;
				if (nullString == null || !nullString.equals(keyAndValue.getSecond())) {
					// value is not null;
					value = keyAndValue.getSecond();
				}

				map.put(key, value);
			}
		}
		return map;
	}

	/**
	 * Removes all entries with <code>null</code> values from the passed <code>map</code>.
	 */
	public static void removeEntriesWithNullValues(final Map<?, ?> map) {
		if (map != null) {
			final Iterator<?> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				final Entry<?, ?> entry = (Entry<?, ?>) iterator.next();
				if (entry.getValue() == null) {
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Parses the specified key/value map string and returns the resulting map.
	 *
	 * @param keyValueMapString
	 *            a string specifying key/value entries.
	 * @param entryDelimiter
	 *            the entry delimiter (a regular expression).
	 * @param keyAndValueDelimiter
	 *            the key/value delimiter (a regular expression).
	 * @param trimmingEnabled
	 *            if enabled, keys and values are trimmed.
	 * @return the map of key/value pairs.
	 * @throws IllegalArgumentException
	 *             if the specified delimiters are empty or the key/value string is invalid.
	 */
	public static Map<String, String> parseKeyValueMap(final String keyValueMapString, final String entryDelimiter, final String keyAndValueDelimiter,
			final boolean trimmingEnabled) throws IllegalArgumentException {
		final String firstPartOfExceptionMessage = "Couldn't parse key/value map from string "
				+ CommonTools.getStringRepresentation(keyValueMapString) + " (entry delimiter=" + CommonTools.getStringRepresentation(entryDelimiter)
				+ ", key/value delimiter=" + CommonTools.getStringRepresentation(keyAndValueDelimiter) + ")!";

		if (CommonTools.isEmpty(entryDelimiter)) {
			throw new IllegalArgumentException(firstPartOfExceptionMessage + " Entry delimiter must not be empty!");
		}

		if (CommonTools.isEmpty(keyAndValueDelimiter)) {
			throw new IllegalArgumentException(firstPartOfExceptionMessage + " Key/value delimiter must not be empty!");
		}

		Arguments.notNull(keyValueMapString, firstPartOfExceptionMessage + " Key/value list string must not be null!");

		final Map<String, String> result = new HashMap<>();

		if (!CommonTools.isEmpty(keyValueMapString)) {

			final String[] entries = keyValueMapString.split(entryDelimiter);
			for (final String entry : entries) {
				final String[] keyAndValue = entry.split(keyAndValueDelimiter);

				if (keyAndValue.length != Numbers.TWO) {
					throw new IllegalArgumentException(
							firstPartOfExceptionMessage + " Expected 2 strings when splitting key and value but got " + keyAndValue.length + "!");
				}

				String key = keyAndValue[0];
				String value = keyAndValue[1];

				if (trimmingEnabled) {
					key = key.trim();
					value = value.trim();
				}

				result.put(key, value);
			}
		}

		return result;
	}

	/**
	 * Not tested yet!
	 *
	 * Gets the inverse map of the passed map, i.e. keys and values are swapped.
	 *
	 * @return the inverse map or <code>null</code>, if the passed map is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the map contains duplicate values.
	 */
	public static <K, V> Map<V, K> getInverseMap(final Map<K, V> map) {
		final Map<V, K> inverseMap = new HashMap<>();
		for (final Entry<K, V> entry : map.entrySet()) {
			final V value = entry.getValue();

			if (inverseMap.containsKey(value)) {
				throw new IllegalArgumentException("Cannot create inverse map, because the passed map contains duplicate values! "
						+ CommonTools.getParametersString("duplicate value that has been found", value, "passed map", map));
			}

			// swap key and value
			inverseMap.put(value, entry.getKey());
		}
		return inverseMap;
	}

	/**
	 * <p>
	 * Creates the intersection of {@link Map}s - a new {@link HashMap} only containing those entries that are contained in all of the source maps.
	 * </p>
	 * <p>
	 * <b>Note:</b> This method only checks the keys in the maps, the values in the resulting intersection-map will always be those of the first map.
	 * </p>
	 *
	 * @param maps
	 *            the source maps.
	 * @return A new {@link HashMap} containing the intersection of the maps <b>or</b> <code>null</code> if no maps were passed to this method or any
	 *         of them is null <b>or</b> the first map, if only one map has been passed to this method.
	 */
	public static <K, V> Map<K, V> createIntersection(final Map<K, V>... maps) {
		Arguments.arrayNotEmpty((Object[]) maps);
		if (CommonTools.isAnyNull((Object[]) maps)) {
			throw new IllegalArgumentException("The passed maps must not be null! " + CommonTools.getParametersString("maps", Arrays.asList(maps)));
		}

		final Map<K, V> firstMap = maps[0];
		if (maps.length == Numbers.ONE) {
			return firstMap;
		}

		final Map<K, V> intersection = new HashMap<>();
		for (final Entry<K, V> entry : firstMap.entrySet()) {
			boolean otherMapsContainKey = true;

			for (int i = Numbers.ONE; i < maps.length; i++) {
				final Map<K, V> otherMap = maps[i];
				if (!otherMap.containsKey(entry.getKey())) {
					otherMapsContainKey = false;
				}
			}

			if (otherMapsContainKey) {
				intersection.put(entry.getKey(), entry.getValue());
			}
		}
		return intersection;
	}

	/**
	 * {@link GenericTaskWithContext#perform(Object) Performs} the specified <code>task</code> on the entries of the passed <code>map</code>. See
	 * {@link #withEntriesDo(Set, GenericTaskWithContext)}.
	 */
	public static <K, V> void withEntriesDo(final Map<K, V> map, final GenericTaskWithContext<Entry<K, V>> task) {
		if (map != null && !map.isEmpty()) {
			withEntriesDo(map.entrySet(), task);
		}
	}

	/**
	 * {@link GenericTaskWithContext#perform(Object) Performs} the specified <code>task</code> on the passed <code>entries</code>.
	 */
	public static <K, V> void withEntriesDo(final Set<Entry<K, V>> entries, final GenericTaskWithContext<Entry<K, V>> task) {
		if (entries != null && !entries.isEmpty()) {
			for (final Entry<K, V> entry : entries) {
				if (entry != null) {
					task.perform(entry);
				}
			}
		}
	}

	/**
	 * {@link GenericTaskWithContext#perform(Object) Performs} the specified <code>task</code> on the entries of the passed <code>map</code>. See
	 * {@link #withEntriesDo(Set, GenericTaskWithContext)}.
	 */
	public static <K, V> void withEntriesDo(final Map<K, V> map, final GenericTaskWithNullableContext<Entry<K, V>> task) {
		if (map != null && !map.isEmpty()) {
			withEntriesDo(map.entrySet(), task);
		}
	}

	/**
	 * {@link GenericTaskWithContext#perform(Object) Performs} the specified <code>task</code> on the passed <code>entries</code>.
	 */
	public static <K, V> void withEntriesDo(final Set<Entry<K, V>> entries, final GenericTaskWithNullableContext<Entry<K, V>> task) {
		if (entries != null && !entries.isEmpty()) {
			for (final Entry<K, V> entry : entries) {
				task.perform(entry);
			}
		}
	}

	/**
	 * Creates a map with the passed key/value pairs.
	 *
	 * @param keyAndValuePairs
	 *            keys/value pairs (i.e. key, value, key, value, ...)
	 * @return the map.
	 */
	public static Map<Object, Object> getMap(final Object... keyAndValuePairs) {
		return putAllToMap(new HashMap<>(), keyAndValuePairs);
	}

	/** Adds given key/value pairs to given map. */
	public static <K, V> Map<K, V> putAllToMap(Map<K, V> map, final Object... keyAndValuePairs) {
		if (keyAndValuePairs != null) {

			if (!CommonTools.isEven(keyAndValuePairs.length)) {
				throw new IllegalArgumentException("Cannot create map because one value is missing! " + Arrays.asList(keyAndValuePairs));
			}

			for (int i = 0; i < keyAndValuePairs.length - 1; i += 2) {
				map.put((K) keyAndValuePairs[i], (V) keyAndValuePairs[i + 1]);
			}
		}
		return map;
	}

	/**
	 * Creates a new map with the specified <code>keys</code> and <code>values</code>. The passed collections must be the same size (and not
	 * <code>null</code>).
	 *
	 * @param duplicateKeysAllowed
	 *            if enabled, duplicate keys are ignored (i.e. last value wins). If disabled, an <code>IllegalArgumentException</code> is thrown.
	 * @throws IllegalArgumentException
	 *             if the collections are not the same size or if a duplicate key is found and this is not allowed (see
	 *             <code>duplicateKeysAllowed</code>).
	 *
	 * @see #mapKeysToListOfValues(Collection, Collection)
	 */
	public static <K, V> Map<K, V> mapKeysToValues(final Collection<K> keys, final Collection<V> values, boolean duplicateKeysAllowed)
			throws IllegalArgumentException {

		Arguments.notNullWithNames("keys", keys, "values", values);
		if (keys.size() != values.size()) {
			throw new IllegalArgumentException("The passed collections do not have the same size! "
					+ CommonTools.getParametersString("keys size", keys.size(), "values size", values.size(), "keys", keys, "values", values));
		}

		final Map<K, V> map = new HashMap<>();
		Iterator<K> keysIterator = keys.iterator();
		Iterator<V> valuesIterator = values.iterator();

		while (keysIterator.hasNext()) {
			K key = keysIterator.next();
			if (!duplicateKeysAllowed && map.containsKey(key)) {
				throw new IllegalArgumentException("Error while mapping keys to values: duplicate key found! "
						+ CommonTools.getParametersString("duplicate key", key, "keys", keys, "values", values));
			}
			map.put(keysIterator.next(), valuesIterator.next());
		}

		return map;
	}

	/**
	 * Creates a new map with the specified <code>keys</code> and lists of <code>values</code>. The passed collections must be the same size (and not
	 * <code>null</code>). Duplicate keys are expected, which is why the values of the map are lists.
	 *
	 * @throws IllegalArgumentException
	 *             if the collections are not the same size.
	 * @see #mapKeysToValues(Collection, Collection, boolean)
	 */
	public static <K, V> Map<K, List<V>> mapKeysToListOfValues(final Collection<K> keys, final Collection<V> values) throws IllegalArgumentException {

		Arguments.notNullWithNames("keys", keys, "values", values);
		if (keys.size() != values.size()) {
			throw new IllegalArgumentException("The passed collections do not have the same size! "
					+ CommonTools.getParametersString("keys size", keys.size(), "values size", values.size(), "keys", keys, "values", values));
		}

		final Map<K, List<V>> map = new HashMap<>();
		Iterator<K> keysIterator = keys.iterator();
		Iterator<V> valuesIterator = values.iterator();

		while (keysIterator.hasNext()) {
			K key = keysIterator.next();
			if (!map.containsKey(key)) {
				map.put(key, new ArrayList<V>());
			}
			map.get(key).add(valuesIterator.next());
		}

		return map;
	}

	/**
	 * Instantiates and returns a new {@link Map}.
	 */
	public static <K, V> Map<K, V> newMap() {
		return new HashMap<>();
	}

	/**
	 * Adds the passed <code>keysAndValues</code> to the <code>map</code>. Note that the method cannot make sure that keys are of type <code>K</code>
	 * and values of type <code>V</code>!
	 *
	 * @param map
	 *            the map to add to; must not be <code>null</code>
	 * @param keysAndValues
	 *            a list containing pairs of keys and values, e.g. "key1", "value1, "key2", "value2, ...
	 * @throws IllegalArgumentException
	 *             if the passed <code>map</code> is <code>null</code> or if their is a key without a value in <code>keysAndValues</code>.
	 */
	public static <S, K extends S, V extends S> void putAll(Map<K, V> map, List<S> keysAndValues) throws IllegalArgumentException {
		Arguments.notNullWithNames("map", map);

		if (keysAndValues != null) {
			int size = keysAndValues.size();
			if (!CommonTools.isEven(size)) {
				throw new IllegalArgumentException("Each key must have a value! " + keysAndValues);
			}

			Iterator<S> iterator = keysAndValues.iterator();
			while (iterator.hasNext()) {
				@SuppressWarnings("unchecked")
				K key = (K) iterator.next();
				@SuppressWarnings("unchecked")
				V value = (V) iterator.next();

				map.put(key, value);
			}
		}
	}

	/**
	 * Converts the passed <code>map</code> to an array that contains pairs of keys and values, e.g. "key1", "value1, " key2", "value2, ...
	 *
	 * @param map
	 *            the map containing the keys and values
	 * @param componentType
	 *            the component type of the array. This parameter is used mainly to avoid casts in the calling method. No type checks are performed!
	 * @return a (1 dimensional) array containing pairs of keys and values.
	 * @param <K>
	 *            the type of the keys
	 * @param <V>
	 *            the type of the values
	 * @param <S>
	 *            a super type of <code>K</code> and <code>V</code>.
	 */
	public static <S, K extends S, V extends S> S[] toArray(Map<K, V> map, Class<S> componentType) {
		Arguments.notNullWithNames("componentType", componentType);
		int arraySize = NullSafe.size(map) * 2;

		@SuppressWarnings("unchecked")
		S[] array = (S[]) new Object[arraySize];

		int i = 0;
		for (Entry<K, V> entry : map.entrySet()) {
			array[i++] = entry.getKey();
			array[i++] = entry.getValue();
		}
		return array;
	}

	/**
	 * Turns {@code Map<K, V>} to {@code Map<K, W>} by applying the <tt>valueMapping</tt> on each value. The returned map preserves the order of
	 * iteration of the original map.
	 */
	public static <K, V, W> Map<K, W> mapValues(Map<K, V> map, Function<? super V, ? extends W> valueMapping) {
		return mapValues(map, valueMapping, newLinkedMap());
	}

	public static <K, V, W> Map<K, W> mapValues(Map<K, V> map, Function<? super V, ? extends W> valueMapping, Map<K, W> result) {
		for (Entry<K, V> e : map.entrySet()) {
			result.put(e.getKey(), valueMapping.apply(e.getValue()));
		}

		return result;
	}

}
