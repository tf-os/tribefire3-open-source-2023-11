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

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.common.lcd.GenericCheck;
import com.braintribe.common.lcd.GenericRuntimeException;
import com.braintribe.common.lcd.GenericTaskWithContext;
import com.braintribe.common.lcd.GenericTaskWithNullableContext;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.common.lcd.Pair;

/**
 * This class provides utility methods related to {@link Collection}s.
 *
 * @author michael.lafite
 */
public class CollectionTools {

	protected CollectionTools() {
		// nothing to do
	}

	/**
	 * Adds the elements of <code>sourceCollections</code> to the <code>targetCollection</code>.
	 *
	 * @see #addElementsToCollection(Collection, boolean, Collection...)
	 */
	public static <C extends Collection<E>, E> C addElementsToCollection(final C targetCollection,
			final Collection<? extends E>... sourceCollections) {
		return addElementsToCollection(targetCollection, false, sourceCollections);
	}

	/**
	 * Adds the elements of <code>sourceCollections</code> to the <code>targetCollection</code>.
	 *
	 * @param <E>
	 *            the type (or super type) of the elements in the lists.
	 * @param targetCollection
	 *            the collection the elements a added to.
	 * @param skipDuplicates
	 *            if <code>true</code>, duplicates will be skipped.
	 * @param sourceCollections
	 *            the collections whose elements are added to the target list.
	 * @return the target collection with all the elements of the source collections.
	 */
	public static <C extends Collection<E>, E> C addElementsToCollection(final C targetCollection, final boolean skipDuplicates,
			final Collection<? extends E>... sourceCollections) {
		for (final Collection<? extends E> collection : sourceCollections) {
			if (skipDuplicates) {
				for (final E element : collection) {
					if (!targetCollection.contains(element)) {
						targetCollection.add(element);
					}
				}
			} else {
				targetCollection.addAll(collection);
			}
		}
		return targetCollection;
	}

	/**
	 * Adds the passed <code>elements</code> to the <code>targetCollection</code>.
	 *
	 * @see #addElementsToCollection(Collection, boolean, Collection...)
	 */
	public static <C extends Collection<E>, E> C addElementsToCollection(final C targetCollection, final E... elements) {
		return addElementsToCollection(targetCollection, false, elements);
	}

	/**
	 * Adds the passed <code>elements</code> to the <code>targetList</code>.
	 *
	 * @param <E>
	 *            the type (or super type) of the elements to add.
	 * @param targetCollection
	 *            the collection the elements are added to.
	 * @param skipDuplicates
	 *            if <code>true</code>, duplicates will be skipped.
	 * @param elements
	 *            the elements to add to the list.
	 * @return the target collection containing the added elements.
	 */
	public static <C extends Collection<E>, E> C addElementsToCollection(final C targetCollection, final boolean skipDuplicates,
			final E... elements) {
		if (!ArrayTools.isEmpty(elements)) {
			for (final E element : elements) {
				if (skipDuplicates && targetCollection.contains(element)) {
					continue;
				}
				targetCollection.add(element);
			}
		}
		return targetCollection;
	}

	/**
	 * Removes duplicates from the passed <code>collection</code> (only the first element will be kept). The duplicates check supports
	 * <code>null</code> elements and (for non-<code>null</code> elements) relies on {@link Object#equals(Object) equals} check to find duplicates.
	 *
	 * @param collection
	 *            the collection to process.
	 * @throws IllegalArgumentException
	 *             if the passed <code>collection</code> is <code>null</code>.
	 */
	public static void removeDuplicates(final Collection<?> collection) throws IllegalArgumentException {
		Arguments.notNullWithNames("collection", collection);
		final Iterator<?> iterator = collection.iterator();
		final Set<Object> temp = new HashSet<>();
		while (iterator.hasNext()) {
			final Object element = iterator.next();
			if (temp.contains(element)) {
				// this element is a duplicate
				iterator.remove();
			} else {
				temp.add(element);
			}
		}
	}

	/**
	 * Checks if the size of the passed collection is valid.
	 *
	 * @return the passed collection.
	 * @throws GenericRuntimeException
	 *             if the size is invalid.
	 */
	public static <T extends Collection<?>> T checkSize(final T collection, final Integer minSize, final Integer maxSize)
			throws GenericRuntimeException {

		Double minSizeAsDouble = null;
		if (minSize != null) {
			minSizeAsDouble = Double.valueOf(minSize);
		}
		Double maxSizeAsDouble = null;
		if (maxSize != null) {
			maxSizeAsDouble = Double.valueOf(maxSize);
		}

		if (CommonTools.isInRange(collection.size(), minSizeAsDouble, maxSizeAsDouble)) {
			return collection;
		}
		throw new RuntimeException(
				"Collection size " + collection.size() + " is not in valid range [" + minSize + "," + maxSize + "]! Collection: " + collection);
	}

	/**
	 * Checks if the size of the passed collection is valid.
	 *
	 * @return the passed collection.
	 * @throws GenericRuntimeException
	 *             if the size is invalid.
	 */
	public static <T extends Collection<?>> T checkSize(final T collection, final Integer expectedSize) throws GenericRuntimeException {
		return checkSize(collection, expectedSize, expectedSize);
	}

	/**
	 * @see CommonTools#toCollection(Object...)
	 */
	public static <E> Collection<E> toCollection(final E... elements) {
		return CommonTools.toCollection(elements);
	}

	/**
	 * Gets a string representation of the passed collection. Invokes
	 * {@link #getStringRepresentation(Collection, String, String, String, String, boolean)} with prefix "[", suffix "]", separator ",",
	 * sizeIncluded==true.
	 */
	public static String toString(final Collection<?> collection, final String collectionName) {
		return getStringRepresentation(collection, collectionName, "[", "]", ",", true);
	}

	public static String toString(final Enumeration<?> enumeration, final String enumerationName) {
		return getStringRepresentation(enumeration, enumerationName, "[", "]", ",", true);
	}

	/**
	 * @deprecated replaced by {@link #getStringRepresentation(Collection, String, String, String, String, boolean)}
	 */
	@Deprecated
	// we don't want a container object here
	public static String getStringRepresenation(final Collection<?> collection, final String collectionName, final String elementsPrefix,
			final String elementsSuffix, final String separator, final boolean sizeIncluded) {
		return getStringRepresentation(collection, collectionName, elementsPrefix, elementsSuffix, separator, sizeIncluded);
	}

	/**
	 * Gets a string representation of the passed collection. The returned string has the following format "[collectionName][elementsPrefix][size]
	 * elements: [element_1 string][separator]...[separator][element_n string][elementsSuffix]" or "null" if a null pointer is passed. Method
	 * {@link StringTools#getStringRepresentation(Object)} is used to get the string representation for the individual elements.
	 */
	// we don't want a container object here
	public static String getStringRepresentation(final Collection<?> collection, final String collectionName, final String elementsPrefix,
			final String elementsSuffix, final String separator, final boolean sizeIncluded) {
		if (collection == null) {
			return "null";
		}
		final StringBuilder stringBuilder = new StringBuilder();
		if (collectionName != null) {
			stringBuilder.append(collectionName);
		}
		if (elementsPrefix != null) {
			stringBuilder.append(elementsPrefix);
		}

		if (sizeIncluded) {
			final int size = collection.size();
			StringTools.append(stringBuilder, size, CommonTools.getSingularOrPlural("element", size), ": ");
		}

		if (!collection.isEmpty()) {
			final Iterator<?> iterator = getIterator(collection);
			while (true) {
				stringBuilder.append(CommonTools.getStringRepresentation(iterator.next()));
				if (iterator.hasNext()) {
					stringBuilder.append(separator);
					continue;
				}
				break; // SuppressPMDWarnings (break okay here)
			}
		}

		if (elementsSuffix != null) {
			stringBuilder.append(elementsSuffix);
		}

		return CommonTools.objNotNull(stringBuilder.toString());
	}

	/**
	 * Gets a string representation of the passed enumeration. The returned string has the following format "[enumerationName][elementsPrefix][size]
	 * elements: [element_1 string][separator]...[separator][element_n string][elementsSuffix]" or "null" if a null pointer is passed. Method
	 * {@link StringTools#getStringRepresentation(Object)} is used to get the string representation for the individual elements.
	 */
	// we don't want a container object here
	public static String getStringRepresentation(final Enumeration<?> enumeration, final String enumerationName, final String elementsPrefix,
			final String elementsSuffix, final String separator, final boolean sizeIncluded) {
		if (enumeration == null) {
			return "null";
		}
		List<Object> list = new ArrayList<>();
		while (enumeration.hasMoreElements()) {
			list.add(enumeration.nextElement());
		}
		return getStringRepresentation(list, enumerationName, elementsPrefix, elementsSuffix, separator, sizeIncluded);
	}

	/**
	 * Returns an {@link Collection#iterator() iterator} for the passed <code>collection</code>.
	 */

	public static <E> Iterator<E> getIterator(final Collection<E> collection) {
		return collection.iterator();
	}

	/**
	 * Gets a list filled with the passed element.
	 */
	public static <E> List<E> getFilledList(final E element, final int size) {
		final List<E> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			list.add(element);
		}
		return list;
	}

	/**
	 * Returns <code>true</code> if the passed <code>collection</code> is <code>null</code> or empty.
	 */
	public static boolean isEmpty(final Collection<?> collection) {
		return CommonTools.isEmpty(collection);
	}

	/**
	 * Returns <code>true</code> if the passed <code>enumeration</code> is <code>null</code> or is empty (i.e. has no more elements).
	 */
	public static boolean isEmpty(final Enumeration<?> enumeration) {
		return CommonTools.isEmpty(enumeration);
	}

	/**
	 * Returns <code>true</code> if the passed collection contains at least one of the specified elements.
	 */
	public static boolean containsAny(final Collection<?> collection, final Collection<?> elementsToSearch) {
		if (CommonTools.isAnyNull(collection, elementsToSearch)) {
			throw new IllegalArgumentException("Illegal arguments (null pointers)! "
					+ CommonTools.getParametersString("collection", collection, "elementsToSearch", elementsToSearch));
		}
		for (final Object elementToSearch : elementsToSearch) {
			if (collection.contains(elementToSearch)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if the passed collection contains all searched elements.
	 */
	public static boolean containsAll(final Collection<?> collection, final Collection<?> elementsToSearch) {
		return getFirstMissingElement(collection, elementsToSearch) == null;
	}

	/**
	 * Checks whether the passed <code>collection</code> contains all searched elements.
	 *
	 * @throws GenericRuntimeException
	 *             if the check fails.
	 */
	public static void checkContainsAll(final Collection<?> collection, final Collection<?> elementsToSearch) throws GenericRuntimeException {
		final Object missingElement = getFirstMissingElement(collection, elementsToSearch);
		if (missingElement != null) {
			throw new GenericRuntimeException("At least one of the searched elements is missing in the collection! " + CommonTools
					.getParametersString("missing element", missingElement, "searched elements", elementsToSearch, "collection", collection));
		}
	}

	/**
	 * Returns the first missing element in the passed collection (or <code>null</code>, if no element is missing).
	 */
	public static <E> E getFirstMissingElement(final Collection<?> collection, final Collection<E> elementsToSearch) {
		if (CommonTools.isAnyNull(collection, elementsToSearch)) {
			throw new IllegalArgumentException("Illegal arguments (null pointers)! "
					+ CommonTools.getParametersString("collection", collection, "elementsToSearch", elementsToSearch));
		}
		for (final E elementToSearch : elementsToSearch) {
			if (!collection.contains(elementToSearch)) {
				return elementToSearch;
			}
		}
		return null;
	}

	/**
	 * Returns a list of all elements in the passed collection. If a collection element is a {@link Collection} itself, then instead of adding the
	 * collection object, the elements of that collection are added to the list (using recursion).
	 */
	public static List<Object> getElementsRecursively(final Collection<?> collection) {
		final List<Object> result = new ArrayList<>();

		if (!isEmpty(collection)) {
			for (final Object element : collection) {
				if (element instanceof Collection<?>) {
					result.addAll(getElementsRecursively((Collection<?>) element));
				} else {
					result.add(element);
				}
			}
		}
		return result;
	}

	/**
	 * Removes all <code>null</code> elements from the passed collection.
	 */
	public static void removeNulls(final Collection<?> collection) {
		if (collection != null) {
			final Iterator<?> iterator = collection.iterator();
			while (iterator.hasNext()) {
				final Object element = iterator.next();
				if (element == null) {
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Returns a new list containing all the elements of the passed <code>list</code> (which must not be <code>null</code>).
	 */
	public static <E> List<E> copy(final List<E> list) {
		Arguments.notNull(list);
		final List<E> result = new ArrayList<>();
		result.addAll(list);
		return result;
	}

	/**
	 * Returns a new set containing all the elements of the passed <code>set</code> (which must not be <code>null</code> ).
	 */
	public static <E> Set<E> copy(final Set<E> set) {
		Arguments.notNull(set);
		final Set<E> result = new HashSet<>();
		result.addAll(set);
		return result;
	}

	/**
	 * Gets a list filled with the elements from the passed collection.
	 *
	 * @param <E>
	 *            the type of the elements.
	 * @param collection
	 *            the collection containing the elements to be added to the new list.
	 * @return an list with the elements from the passed collection.
	 */
	public static <E> List<E> getList(final Collection<E> collection) {
		final List<E> list = new ArrayList<>();
		if (collection != null) {
			list.addAll(collection);
		}
		return list;
	}

	/**
	 * Gets a list filled with the passed elements. See {@link CommonTools#getList(Object...)}.
	 */
	public static <E> List<E> getList(final E... elements) {
		return CommonTools.getList(elements);
	}

	/**
	 * Gets a list filled with the passed elements. See {@link CommonTools#getObjectList(Object...)}.
	 */
	public static List<Object> getObjectList(final Object... elements) {
		return CommonTools.getObjectList(elements);
	}

	/**
	 * Gets a set filled with the elements from the passed collection.
	 *
	 * @param <E>
	 *            the type of the elements.
	 * @param collection
	 *            the collection containing the elements to be added to the new set.
	 * @return a set with the elements from the passed collection.
	 */
	public static <E> Set<E> getSet(final Collection<E> collection) {
		final Set<E> set = new HashSet<>();
		if (!isEmpty(collection)) {
			set.addAll(collection);
		}
		return set;
	}

	/**
	 * Gets a set filled with the passed elements.
	 *
	 * @param <E>
	 *            the type of the elements.
	 * @param elements
	 *            the elements to add.
	 * @return a new set containing the passed elements.
	 */
	public static <E> Set<E> getSet(final E... elements) {
		final Set<E> set = new HashSet<>();
		if (elements != null) {
			for (final E element : elements) {
				set.add(element);
			}
		}
		return set;
	}

	/**
	 * Returns <code>true</code> if all of the passed collections are empty.
	 *
	 * @see #isEmpty(Collection)
	 * @see #isAnyEmpty(Collection...)
	 */
	public static boolean isAllEmpty(final Collection<?>... collections) {
		if (CommonTools.isEmpty(collections)) {
			throw new IllegalArgumentException(
					"Collection array must not be null or empty! collections: " + CommonTools.getStringRepresentation(collections));
		}
		for (final Collection<?> collection : collections) {
			if (!isEmpty(collection)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns <code>true</code> if any of the passed collections are empty.
	 *
	 * @see #isEmpty(Collection)
	 * @see #isAllEmpty(Collection...)
	 */
	public static boolean isAnyEmpty(final Collection<?>... collections) {
		if (CommonTools.isEmpty(collections)) {
			throw new IllegalArgumentException(
					"Collection array must not be null or empty! collections: " + CommonTools.getStringRepresentation(collections));
		}
		for (final Collection<?> collection : collections) {
			if (isEmpty(collection)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns all possible pairs of elements where one element is an element from the first collection and the other one an element from the second
	 * collection, i.e. the <a href="http://en.wikipedia.org/wiki/Cartesian_product">cartesian product</a> of both collections.
	 */
	public static <T, U> List<Pair<T, U>> createCartesianProduct(final Collection<T> collection1, final Collection<U> collection2) {
		final List<Pair<T, U>> result = new ArrayList<>();
		for (final T object1 : collection1) {
			for (final U object2 : collection2) {
				result.add(new Pair<>(object1, object2)); // SuppressPMDWarnings (instantiation inside loop is fine
															// here)
			}
		}
		return result;
	}

	/**
	 * Returns the intersection of both collections, i.e. the elements that both collections contain.
	 */
	public static <E> List<E> getIntersection(final Collection<E> collection1, final Collection<E> collection2) {
		final List<E> result = new ArrayList<>();
		if (!isAnyEmpty(collection1, collection2)) {
			for (final E element : collection1) {
				if (collection2.contains(element)) {
					result.add(element);
				}
			}
		}
		return result;
	}

	/**
	 * Returns the union of both collections, i.e. the elements that are contained in <code>collection1</code>, <code>collection2</code> or in both
	 * collections.
	 */
	public static <E> List<E> getUnion(final Collection<E> collection1, final Collection<E> collection2) {
		final List<E> result = new ArrayList<>();
		addAll(result, collection1);
		result.addAll(getRelativeComplement(collection1, collection2));
		return result;
	}

	/**
	 * Returns the symmetric difference, i.e. the elements that are contained in one of the collections but not in both.
	 */
	public static <E> List<E> getSymmetricDifference(final Collection<E> collection1, final Collection<E> collection2) {
		final List<E> result = new ArrayList<>();
		result.addAll(getRelativeComplement(collection1, collection2));
		result.addAll(getRelativeComplement(collection2, collection1));
		return result;
	}

	/**
	 * Gets the relative complement of <code>collection1</code> with respect to <code>collection2</code>, i.e. all elements that are contained in
	 * <code>collection2</code> but not in <code>collection1</code>.
	 */
	public static <E> List<E> getRelativeComplement(final Collection<E> collection1, final Collection<E> collection2) {
		final List<E> result = new ArrayList<>();
		if (!isEmpty(collection2)) {
			if (isEmpty(collection1)) {
				result.addAll(collection2);
			} else {
				for (final E element : collection2) {
					if (!collection1.contains(element)) {
						result.add(element);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns all elements that <code>collectionToCheck</code> contains in addition to the elements in <code>collectionToCompareTo</code>, i.e. all
	 * elements of <code>collectionToCheck</code> that are not contained in <code>collectionToCompareTo</code>.
	 */
	public static <E> List<E> getAdditionalElements(final Collection<E> collectionToCheck, final Collection<E> collectionToCompareTo) {
		return getRelativeComplement(collectionToCompareTo, collectionToCheck);
	}

	/**
	 * Returns all elements that are missing in <code>collectionToCheck</code> but contained in <code>collectionToCompareTo</code>.
	 */
	public static <E> List<E> getMissingElements(final Collection<E> collectionToCheck, final Collection<E> collectionToCompareTo) {
		return getRelativeComplement(collectionToCheck, collectionToCompareTo);
	}

	/**
	 * Adds the <code>elementsToAdd</code> to the <code>collection</code>. <code>elementsToAdd</code> may be <code>null</code> (in which case no
	 * elements are added).
	 *
	 * @throws NullPointerException
	 *             if the <code>collection</code> is <code>null</code>.
	 */
	public static <E> void addAll(final Collection<E> collection, final Collection<? extends E> elementsToAdd) {
		Arguments.notNull(collection,
				"Cannot add elements to collection because the collection is null! The following elements would have been added: " + elementsToAdd);
		if (elementsToAdd != null) {
			collection.addAll(elementsToAdd);
		}
	}

	/**
	 * Returns <code>true</code> if at least one element of the passed collection is <code>null</code>.
	 *
	 * @see ArrayTools#isAllNull(Object...)
	 */
	public static boolean isAnyNull(final Collection<?> collection) {
		return ArrayTools.isAnyNull(toArray(collection));
	}

	/**
	 * Returns <code>true</code> if all elements of the passed collection are <code>null</code>.
	 *
	 * @see ArrayTools#isAllNull(Object...)
	 */
	public static boolean isAllNull(final Collection<?> collection) {
		return ArrayTools.isAllNull(toArray(collection));
	}

	/**
	 * Returns the number of null pointers in the passed collection.
	 *
	 * @see ArrayTools#getNullPointerCount(Object...)
	 */
	public static int getNullPointerCount(final Collection<?> collection) {
		return ArrayTools.getNullPointerCount(toArray(collection));
	}

	/**
	 * Returns the number of elements in the passed collection that are not <code>null</code>.
	 *
	 * @see ArrayTools#getNonNullPointerCount(Object...)
	 */
	public static int getNonNullPointerCount(final Collection<?> collection) {
		return ArrayTools.getNonNullPointerCount(toArray(collection));
	}

	/**
	 * Returns the first element of the passed collection that is not <code>null</code>.
	 *
	 * @see ArrayTools#getFirstNonNullPointer(Object...)
	 */

	public static Object getFirstNonNullPointer(final Collection<?> collection) {
		return ArrayTools.getFirstNonNullPointer(toArray(collection));
	}

	/**
	 * Decodes the passed <code>encodedList</code>.
	 *
	 * @param encodedCollection
	 *            the encoded collection.
	 * @param delimiter
	 *            the delimiter used to get the tokens from the string (a regular expression).
	 * @param removeEmptyStrings
	 *            if <code>true</code> empty strings are removed from the list.
	 * @param emptyStringsAreFatal
	 *            if <code>true</code> an exception is thrown if an empty string is found. If this option is enabled, <code>removeEmptyStrings</code>
	 *            must be disabled.
	 * @param trimStrings
	 *            if <code>true</code> all strings in the returned list will be trimmed.
	 * @param delimiterIsARegularExpression
	 *            whether or not the <code>delimiter</code> is a regular expression.
	 * @return the list of strings.
	 * @throws IllegalArgumentException
	 *             if <code>removeEmptyStrings</code> and <code>emptyStringsAreFatal</code> are both enabled or if <code>emptyStringsAreFatal</code>
	 *             is enabled and an empty string is found.
	 */
	public static List<String> decodeCollection(final String encodedCollection, final String delimiter, final boolean removeEmptyStrings,
			final boolean emptyStringsAreFatal, final boolean trimStrings, final boolean delimiterIsARegularExpression)
			throws IllegalArgumentException {
		if (removeEmptyStrings && emptyStringsAreFatal) {
			throw new IllegalArgumentException("Empty strings shall be removed AND are fatal. Only one of these options should be enabled!");
		}

		final List<String> stringList = new ArrayList<>();

		String[] values;
		if (delimiterIsARegularExpression) {
			// we set -1 here because the method should provide trailing empty strings (unless empty strings are
			// removed).
			values = encodedCollection.split(delimiter, -1);
		} else {
			values = StringTools.splitString(encodedCollection, delimiter);
		}

		for (String value2 : values) {
			String value = value2;
			if (trimStrings) {
				value = value.trim();
			}
			stringList.add(value);
		}

		if (!removeEmptyStrings && !emptyStringsAreFatal) {
			return stringList;
		}

		final Iterator<String> iterator = stringList.iterator();
		while (iterator.hasNext()) {
			final String string = iterator.next();
			if (CommonTools.isEmpty(string)) {
				if (emptyStringsAreFatal) {
					throw new IllegalArgumentException("String " + StringTools.getStringRepresentation(encodedCollection)
							+ " contains at least one empty string: " + StringTools.getStringRepresentation(delimiter) + ", delimiter: "
							+ StringTools.getStringRepresentation(delimiter));
				}
				if (removeEmptyStrings) {
					iterator.remove();
				}
			}
		}
		return stringList;
	}

	/**
	 * Adds the <code>element</code> to the <code>collection</code>, unless the <code>element</code> is <code>null</code>.
	 *
	 * @throws NullPointerException
	 *             if the <code>collection</code> is <code>null</code>.
	 */
	public static <E> void addIfNotNull(final E element, final Collection<E> collection) {
		if (element != null) {
			collection.add(element);
		}
	}

	/**
	 * Adds the <code>element</code> to the <code>collection</code>, unless the <code>collection</code> already contains that <code>element</code>.
	 *
	 * @throws NullPointerException
	 *             if the <code>collection</code> is <code>null</code>.
	 */
	public static <E> void addIfNotContained(final E element, final Collection<E> collection) {
		if (!collection.contains(element)) {
			collection.add(element);
		}
	}

	/**
	 * Searches the <code>searchedElement</code> in the passed <code>collection</code>. The <code>comparator</code> is used to compare the elements
	 * (the comparison result <code>0</code> means the elements are considered equal).
	 */

	public static <E> E getElement(final Collection<E> collection, final E searchedElement, final Comparator<E> comparator) {
		for (final E element : NullSafe.iterable(collection)) {
			if (comparator.compare(element, searchedElement) == Numbers.ZERO) {
				return element;
			}
		}
		return null;
	}

	/**
	 * Checks if the passed <code>collection</code> contains the <code>searchedElement</code>. See {@link #getElement(Collection, Object, Comparator)}
	 * for more information.
	 */
	public static <E> boolean contains(final Collection<E> collection, final E searchedElement, final Comparator<E> comparator) {
		return getElement(collection, searchedElement, comparator) != null;
	}

	/**
	 * Returns the result of {@link CommonTools#getElementToIndexMap(List)}.
	 */
	public static <E> Map<E, Integer> getElementToIndexMap(final List<E> list) {
		return CommonTools.getElementToIndexMap(list);
	}

	/** Returns a map where keys are elements given on input and each key is identical to it's associated value. */
	public static <E> Map<E, E> getIdentityMap(final Iterable<E> it) {
		final Map<E, E> map = new HashMap<>();
		for (final E e : it) {
			map.put(e, e);
		}
		return map;
	}

	/**
	 * Returns a sub list of the passed <code>list</code>. Indices are inclusive and may be <code>null</code>.
	 */
	public static <E> List<E> getElements(final List<E> list, final Integer firstElementIndex, final Integer lastElementIndex) {

		Integer normalizedFirstElementIndex = firstElementIndex;
		if (normalizedFirstElementIndex == null) {
			normalizedFirstElementIndex = 0;
		}

		Integer normalizedLastElementIndex = lastElementIndex;
		if (normalizedLastElementIndex == null) {
			normalizedLastElementIndex = list.size() - 1;
		}

		if (normalizedFirstElementIndex < 0 || normalizedLastElementIndex < 0) {
			throw new IllegalArgumentException("The passed indices must not be less than 0! "
					+ CommonTools.getParametersString("firstElementIndex", firstElementIndex, "lastElementIndex", lastElementIndex));
		}

		final List<E> result = list.subList(normalizedFirstElementIndex, normalizedLastElementIndex + 1);
		return result;
	}

	/**
	 * Either returns the passed <code>collection</code> or an (unmodifiable) empty collection. Works like {@link NullSafe#collection(Collection)}.
	 */
	public static <E> Collection<E> thisOrEmpty(final Collection<E> collection) {
		return NullSafe.collection(collection);
	}

	/**
	 * Returns the list of elements that pass the <code>check</code>.
	 */
	public static <E> List<E> getElements(final Collection<E> collection, final GenericCheck<E> check) {
		final List<E> result = new ArrayList<>();
		for (final E element : NullSafe.iterable(collection)) {
			if (check.check(element)) {
				result.add(element);
			}
		}
		return result;
	}

	/**
	 * Removes all elements that pass the <code>check</code>.
	 */
	public static <E> void removeElements(final Collection<E> collection, final GenericCheck<E> check) {
		final Iterator<E> iterator = NullSafe.iterator(collection);
		while (iterator.hasNext()) {
			final E element = iterator.next();

			if (check.check(element)) {
				iterator.remove();
			}
		}
	}

	/**
	 * Returns the size of the collection or <code>0</code>, if the passed <code>collection</code> argument is <code>null</code>.
	 *
	 * @deprecated Please use {@link NullSafe#size(Collection)} instead.
	 */
	@Deprecated
	public static int getSizeNullSafe(final Collection<?> collection) {
		return NullSafe.size(collection);
	}

	/**
	 * @see ArrayTools#toArray(Collection)
	 */
	public static Object[] toArray(final Collection<?> collection) {
		return ArrayTools.toArray(collection);
	}

	/**
	 * @see ArrayTools#toArray(Collection, Object[])
	 */
	public static <E> void toArray(final Collection<? extends E> collection, final E[] targetArrayWithCorrectSize) {
		ArrayTools.toArray(collection, targetArrayWithCorrectSize);
	}

	/**
	 * {@link GenericTaskWithContext#perform(Object) Performs} the specified <code>task</code> on the elements of the passed <code>collection</code>.
	 */
	public static <E> void withElementsDo(final Collection<E> collection, final GenericTaskWithContext<E> task) {
		if (collection != null && !collection.isEmpty()) {
			for (final E element : collection) {
				if (element != null) {
					task.perform(element);
				}
			}
		}
	}

	/**
	 * {@link GenericTaskWithContext#perform(Object) Performs} the specified <code>task</code> on the elements of the passed <code>collection</code>.
	 */
	public static <E> void withElementsDo(final Collection<E> collection, final GenericTaskWithNullableContext<E> task) {
		if (collection != null && !collection.isEmpty()) {
			for (final E element : collection) {
				task.perform(element);
			}
		}
	}

	/**
	 * Returns the first element returned by the <code>collection</code>'s iterator.
	 *
	 * @throws IndexOutOfBoundsException
	 *             if there is not at least one element.
	 */
	public static <E> E getFirstElement(final Collection<E> collection) throws IndexOutOfBoundsException {
		if (isEmpty(collection)) {
			throw new IndexOutOfBoundsException("Cannot get first element from collection because it is null/empty! "
					+ CommonTools.getParametersString("collection", collection));
		}
		return collection.iterator().next();
	}

	/**
	 * Returns the first element returned by the <code>collection</code>'s iterator or <code>null</code>, if the <code>collection</code> is
	 * <code>null</code> or empty.
	 */
	public static <E> E getFirstElementOrNull(final Collection<E> collection) throws IndexOutOfBoundsException {
		if (isEmpty(collection)) {
			return null;
		}
		return collection.iterator().next();
	}

	/**
	 * {@link #checkSize(Collection, Integer) Checks the size} of the passed <code>collection</code> and then {@link #getFirstElement(Collection) gets
	 * the first element}.
	 */
	public static <E> E getSingleElement(final Collection<E> collection) throws IndexOutOfBoundsException {
		checkSize(collection, 1);
		return getFirstElement(collection);
	}

	/**
	 * Gets a sorted list containing the passed {@link Comparable comparable} <code>elements</code>.
	 */
	public static <E extends Comparable<E>> List<E> getSortedList(final Collection<E> elements) {
		Arguments.notNull(elements);
		final List<E> list = new ArrayList<>(elements);
		Collections.sort(list);
		return list;
	}

	/**
	 * Gets a sorted list containing the passed <code>elements</code>. The elements will be sorted using the specified <code>comparator</code>.
	 */
	public static <E> List<E> getSortedList(final Collection<E> elements, final Comparator<? super E> comparator) {
		Arguments.notNull(elements);
		final List<E> list = new ArrayList<>(elements);
		Collections.sort(list, comparator);
		return list;
	}

	/**
	 * Transforms the passed <code>elements</code> using the <code>transformer</code>.
	 */
	public static <E, F> List<F> transform(final Collection<E> elements, Function<E, F> transformer) {
		Arguments.notNull(elements);
		List<F> transformedElements = new ArrayList<>();
		for (final E element : elements) {
			F transformedElement;
			try {
				transformedElement = transformer.apply(element);
			} catch (Exception e) {
				throw new RuntimeException("Error while transforming element! " + CommonTools.getParametersString("element", element, "transformer",
						transformer, "transformer class", transformer.getClass()), e);
			}
			transformedElements.add(transformedElement);
		}
		return transformedElements;
	}

	// ################################################################
	// ## . . . . . . Removing elements from collections . . . . . . ##
	// ################################################################

	/**
	 * Basically removes all elements from <tt>s1</tt> which are inside <tt>s2</tt>. The sets being equivalent means it does not matter against which
	 * set we perform the contains check, thus we can chose optimize the implementation based on sizes of s1 and s2.
	 */
	public static void removeAllWhenEquivalentSets(Set<?> s1, Set<?> s2) {
		if (s1.size() < s2.size()) {
			removeAllFromFirstWhichAreInSecond(s1, s2);

		} else {
			for (Object o : s2) {
				s1.remove(o);
			}
		}
	}

	/**
	 * OK, this might be weird, as this is exactly what the {@link Collection#removeAll(Collection)} method does. However, the standard implementation
	 * of this method for {@link Set} in java has a bug (introduced in {@link AbstractSet} implementation as an optimization), so the
	 * <tt>removeAll</tt> method is not reliable.
	 */
	public static void removeAllFromFirstWhichAreInSecond(Iterable<?> first, Collection<?> second) {
		Iterator<?> it = first.iterator();
		while (it.hasNext()) {
			if (second.contains(it.next())) {
				it.remove();
			}
		}
	}

	/**
	 * Returns a list which contains the list given as parameter, except for those on positions given by <tt>indices</tt>.
	 *
	 * The returned list is a new instance, except for the trivial cases when one of given collections is empty, when it returns the original list,
	 * unmodified.
	 */
	public static <T> List<T> removeByIndices(List<T> list, Collection<Integer> indices) {
		List<Integer> sortedIndices = new ArrayList<>(indices);
		Collections.sort(sortedIndices);

		return removeBySortedIndices(list, sortedIndices);
	}

	/**
	 * Returns a list which contains the list given as parameter, except for those on positions given by <tt>indices</tt>, assuming this list is
	 * sorted in ascending order.
	 *
	 * The returned list is a new instance, except for the trivial cases when one of given lists is empty, when it returns the original list,
	 * unmodified.
	 */
	public static <T> List<T> removeBySortedIndices(List<T> list, List<Integer> indices) {
		if (list.isEmpty() || indices.isEmpty()) {
			return list;
		}

		List<T> result = new ArrayList<>();

		Iterator<T> it = list.iterator();
		Iterator<Integer> indexIt = indices.iterator();

		int counter = 0;
		int nextRemoveIndex = indexIt.next();
		while (it.hasNext()) {
			T item = it.next();

			if (counter != nextRemoveIndex) {
				result.add(item);

			} else {
				nextRemoveIndex = indexIt.hasNext() ? indexIt.next() : list.size();
			}

			counter++;
		}

		return result;
	}

	/**
	 * Splits a big list of items in multiple separate lists, each holding maxSize elements. The last resulting list may contain less items. Note that
	 * the resulting lists are subLists of the original list. Hence, modifications to these lists are reflected in the original list and vice versa.
	 *
	 * @param <T>
	 *            The type of the elements in the list.
	 * @param fullList
	 *            The full list of all elements to be split.
	 * @param maxSize
	 *            The maximum size of a single resulting list.
	 * @return A list of lists, each having a maximum of maxSize elements.
	 * @throws IllegalArgumentException
	 *             When the maxSize value is less or equal 0.
	 */
	public static <T> List<List<T>> splitList(List<T> fullList, int maxSize) {

		if (maxSize <= 0) {
			throw new IllegalArgumentException("The maxSize parameter must not be 0 or negative");
		}

		List<List<T>> result = new ArrayList<>();

		if ((fullList != null) && (!fullList.isEmpty())) {

			int fullSize = fullList.size();
			int lastIndex = fullSize - 1;

			int start = 0;
			int stop = Math.min(maxSize, fullSize);
			while (start <= lastIndex) {
				List<T> subList = fullList.subList(start, stop);
				result.add(subList);
				start += maxSize;
				stop += maxSize;
				if (stop > fullSize) {
					stop = fullSize;
				}
			}

		}

		return result;
	}

	/**
	 * Similar to {@link #splitList(List, int)}, but a little more flexible when it comes to input and output.
	 */
	public static <T, C extends Collection<T>> List<C> split(Iterable<? extends T> elements, int limitSize, Supplier<C> partFactory) {
		List<C> result = newList();

		int partSize = limitSize;
		C part = null;

		for (T element : elements) {
			if (partSize == limitSize) {
				partSize = 0;
				result.add(part = partFactory.get());
			}

			part.add(element);
			partSize++;
		}

		return result;
	}

}
