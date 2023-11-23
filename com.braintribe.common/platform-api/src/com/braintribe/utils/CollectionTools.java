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
package com.braintribe.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.braintribe.common.lcd.GenericRuntimeException;
import com.braintribe.utils.lcd.Not;
import com.braintribe.utils.lcd.NullSafe;

/**
 * This class may contain Java-only (i.e. GWT incompatible) code. For further information please see {@link com.braintribe.utils.lcd.CollectionTools}.
 *
 * @author michael.lafite
 */
public final class CollectionTools extends com.braintribe.utils.lcd.CollectionTools {

	private CollectionTools() {
		// no instantiation required
	}

	/**
	 * Adds <code>elements</code> of type <code>T</code> to the <code>targetCollection</code>.
	 *
	 * @param <E>
	 *            The type of the <code>elements</code>
	 * @param <C>
	 *            The type of the <code>targetCollection</code> (required because the collection is returned)
	 * @param elements
	 *            the elements to add to the <code>targetCollection</code>
	 * @param targetCollection
	 *            the collection the <code>elements</code> are added to
	 * @param clazz
	 *            the class object used to check whether the elements can be cast to type <code>T</code>.
	 * @param ignoreCastExceptions
	 *            if <code>true</code>, elements that cannot be cast are just ignored. Otherwise an exception is thrown if any element has the wrong
	 *            type.
	 * @param skipNulls
	 *            if <code>true</code>, <code>null</code> elements will be skipped (i.e. not inserted in the <code>targetCollection</code>).
	 * @return the <code>targetCollection</code> (which is returned just for convenience)
	 * @throws IllegalArgumentException
	 *             if any element has the wrong type AND ignoring cast exceptions is disabled.
	 */
	public static <C extends Collection<E>, E> C addElementsToCollection(final Collection<?> elements, final C targetCollection, final Class<E> clazz,
			final boolean ignoreCastExceptions, final boolean skipNulls) throws IllegalArgumentException {
		for (final Object element : elements) {
			if (element == null) {
				if (!skipNulls) {
					// since null values are not skipped, we can add it (without any checks, since it's just null)
					targetCollection.add(null);
				}
			} else if (clazz.isAssignableFrom(element.getClass())) {
				// element has the correct type
				targetCollection.add(clazz.cast(element));
			} else {
				// class cast exception! (element has wrong type)
				if (!ignoreCastExceptions) {
					throw new RuntimeException("Cannot cast " + element.getClass().getName() + " to " + clazz.getName() + "! element: "
							+ com.braintribe.utils.lcd.CommonTools.getStringRepresentation(element));
				}
			}
		}
		return targetCollection;
	}

	/**
	 * Invokes {@link #addElementsToCollection(Collection, Collection, Class, boolean, boolean)} with <code>null</code> values not skipped.
	 */
	public static <C extends Collection<E>, E> C addElementsToCollection(final Collection<?> elements, final C targetCollection, final Class<E> clazz,
			final boolean ignoreCastExceptions) throws IllegalArgumentException {
		return addElementsToCollection(elements, targetCollection, clazz, ignoreCastExceptions, false);
	}

	/**
	 * Returns a list containing the passed <code>elements</code> which must be instances of <code>E</code>.
	 *
	 * @param <E>
	 *            the type of the elements in the list.
	 * @param clazz
	 *            the class object used to check whether the elements can be cast to type <code>E</code>.
	 * @param elements
	 *            the elements to add to the list.
	 * @return the list with the <code>elements</code>.
	 * @throws IllegalArgumentException
	 *             if any element has the wrong type.
	 * @see #addElementsToCollection(Collection, Collection, Class, boolean)
	 */
	public static <E> List<E> getParameterizedList(final Class<E> clazz, final Collection<?> elements) throws IllegalArgumentException {
		return addElementsToCollection(elements, new ArrayList<E>(), clazz, false);
	}

	/**
	 * Returns a list containing the passed <code>elements</code> which must be instances of <code>E</code>.
	 *
	 * @param <E>
	 *            the type of the elements in the list.
	 * @param elements
	 *            the elements to add to the list.
	 * @param clazz
	 *            the class object used to check whether the elements can be cast to type <code>E</code>.
	 * @return the list with the <code>elements</code>.
	 * @throws IllegalArgumentException
	 *             if any element has the wrong type.
	 * @see #addElementsToCollection(Collection, Collection, Class, boolean)
	 */
	public static <E> List<E> getParameterizedList(final Class<E> clazz, final Object... elements) throws IllegalArgumentException {
		return addElementsToCollection(CommonTools.toList(elements), new ArrayList<E>(), clazz, false);
	}

	/**
	 * Throws a {@link ClassCastException} if any of the passed elements cannot be cast to the specified class. <code>null</code>s are allowed though!
	 */
	public static <E> void checkElementTypes(final Class<E> clazz, final Collection<?> elements) throws ClassCastException {
		for (final Object element : NullSafe.iterable(elements)) {
			if (element == null) {
				// null values are allowed
				continue;
			}
			if (!clazz.isAssignableFrom(element.getClass())) {
				throw new ClassCastException("Type check failed. Element " + CommonTools.getStringRepresentation(element) + " of type "
						+ element.getClass() + " cannot be cast to " + clazz + "! Elements: " + elements);
			}
		}
	}

	/**
	 * Gets the first element from the passed <code>collection</code> that is an instance of the specified <code>searchedElementType</code>.
	 */

	public static <E> E getFirstElement(final Collection<?> collection, final Class<E> searchedElementType) {
		for (final Object element : NullSafe.iterable(collection)) {
			if (element != null && searchedElementType.isInstance(element)) {
				@SuppressWarnings("unchecked")
				final E searchedElement = (E) element;
				return searchedElement;
			}
		}
		return null;
	}

	/**
	 * Gets all elements from the passed <code>collection</code> that are an instance of the specified <code>searchedElementType</code>.
	 * <code>null</code> values are skipped.
	 */
	public static <E> List<E> getElements(final Collection<?> collection, final Class<E> searchedElementType) {
		final List<E> result = addElementsToCollection(collection, new ArrayList<E>(), searchedElementType, true, true);
		return result;
	}

	/**
	 * Gets the single element from the passed <code>collection</code> that is an instance of the specified <code>searchedElementType</code>.
	 *
	 * @throws GenericRuntimeException
	 *             if not exactly 1 element matches.
	 */
	public static <E> E getSingleElement(final Collection<?> collection, final Class<E> searchedElementType) throws GenericRuntimeException {
		final List<E> matchingElements = addElementsToCollection(collection, new ArrayList<E>(), searchedElementType, true, true);
		checkSize(matchingElements, 1);

		// matching element cannot be null!

		final E result = Not.Null(matchingElements.get(0));
		return result;
	}

	/** returns a {@link List} containing all non-null elements from given {@link Iterable} matching given type */
	public static <E> List<E> findAllByType(final Iterable<? super E> iterable, final Class<? extends E> type) {
		final List<E> result = new ArrayList<>();

		for (final Object o : iterable) {
			if (type.isInstance(o)) {
				result.add(type.cast(o));
			}
		}

		return result;
	}

	/**
	 * @see ArrayTools#toArray(Collection, Class)
	 */
	public static <E> E[] toArray(final Collection<? extends E> collection, final Class<E> componentType) {
		return ArrayTools.toArray(collection, componentType);
	}

	/**
	 * Gets the element with the specified <code>index</code> from the passed <code>list</code>, unless the index is out of bounds (in which case the
	 * method returns <code>null</code>).
	 */
	public static <E> E getElementUnlessIndexOutOfBounds(final List<E> list, final int index) {
		if (index < 0 || index >= list.size()) {
			return null;
		}
		return list.get(index);
	}

	public static <E> List<E> requireNonEmpty(final List<E> list, String msg) {
		if (null == list || list.isEmpty()) {
			throw new IllegalArgumentException(msg);
		}
		return list;
	}

	public static <E> Set<E> requireNonEmpty(final Set<E> list, String msg) {
		if (null == list || list.isEmpty()) {
			throw new IllegalArgumentException(msg);
		}
		return list;
	}

	/**
	 * Sorts a {@link List#subList(int, int) sub list} of the passed <code>list</code>.
	 *
	 * @param list
	 *            the list to sort.
	 * @param fromIndex
	 *            the start index (inclusive) or <code>null</code>.
	 * @param toIndex
	 *            the end index (exclusive) or <code>null</code>.
	 */
	public static <E extends Comparable<? super E>> void sortSubList(List<E> list, Integer fromIndex, Integer toIndex) {
		if (fromIndex == null) {
			fromIndex = 0;
		}
		if (toIndex == null) {
			toIndex = list.size();
		}

		List<E> subList = list.subList(fromIndex, toIndex);
		Collections.sort(subList);
	}

	/**
	 * Splits a collection of elements into separate lists, each one having maximum element count defined by {@code batchSize}. If the source
	 * collection is empty, an empty list will be returned.
	 *
	 * @param source
	 *            The source collection.
	 * @param batchSize
	 *            The maximum number of elements per list returned by this method.
	 * @return A list of lists of all the elements of the source collection.
	 * @throws NullPointerException
	 *             Thrown if the source is null.
	 * @throws IllegalArgumentException
	 *             Thrown if the batchSize is 0 or less.
	 */
	public static <T> List<List<T>> split(Collection<T> source, int batchSize) throws NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("The collection must not be null.");
		}
		if (batchSize <= 0) {
			throw new IllegalArgumentException("The batchSize must be a positive number.");
		}
		if (source.isEmpty()) {
			return new ArrayList<>();
		}

		List<List<T>> result = new ArrayList<>((source.size() / batchSize) + 1);
		List<T> current = new ArrayList<>(batchSize);
		result.add(current);
		Iterator<T> it = source.iterator();
		int count = 0;

		while (it.hasNext()) {
			if (count >= batchSize) {
				current = new ArrayList<>(batchSize);
				result.add(current);
				count = 0;
			}
			current.add(it.next());

			count++;
		}

		return result;
	}

}
