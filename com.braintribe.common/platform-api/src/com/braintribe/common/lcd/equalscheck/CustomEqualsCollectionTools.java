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
package com.braintribe.common.lcd.equalscheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * {@link Collection} related utility methods providing support for custom {@link EqualsCheck}s.
 *
 * @author michael.lafite
 */
public final class CustomEqualsCollectionTools {

	private CustomEqualsCollectionTools() {
		// no instantiation required
	}

	/**
	 * Returns all the elements in the <code>collection</code> that equal the <code>searchedElement</code> (according to the specified
	 * <code>equalsCheck</code>). Depending on argument <code>removeElement</code>, the elements are also removed from the <code>collection</code>.
	 */
	private static <E> List<E> getOccurrences(final Collection<E> collection, final Object searchedElement, final EqualsCheck<E> equalsCheck,
			final boolean removeElements, final Integer maxCount) {
		if (maxCount != null && maxCount < 0) {
			throw new IllegalArgumentException("Invalid max count " + maxCount + "!");
		}
		final List<E> result = new ArrayList<>();
		final Iterator<E> iterator = collection.iterator();
		while (iterator.hasNext() && (maxCount == null || maxCount > result.size())) {
			final E element = iterator.next();
			if (equalsCheck.equals(element, searchedElement)) {
				if (removeElements) {
					iterator.remove();
				}
				result.add(element);
			}
		}
		return result;
	}

	public static <E> List<E> getOccurrences(final Collection<E> collection, final Object searchedElement, final EqualsCheck<E> equalsCheck) {
		return getOccurrences(collection, searchedElement, equalsCheck, false, null);
	}

	public static <E> List<E> removeOccurrences(final Collection<E> collection, final Object searchedElement, final EqualsCheck<E> equalsCheck) {
		return getOccurrences(collection, searchedElement, equalsCheck, true, null);
	}

	public static <E> int countOccurrences(final Collection<E> collection, final Object searchedElement, final EqualsCheck<E> equalsCheck) {
		return getOccurrences(collection, searchedElement, equalsCheck, false, null).size();
	}

	/**
	 * @deprecated use {@link #countOccurrences(Collection, Object, EqualsCheck)} instead.
	 */
	@Deprecated
	public static <E> int countOccurences(final Collection<E> collection, final Object searchedElement, final EqualsCheck<E> equalsCheck) {
		return countOccurrences(collection, searchedElement, equalsCheck);
	}

	public static <E> E get(final Collection<E> collection, final Object searchedElement, final EqualsCheck<E> equalsCheck) {
		final List<E> list = getOccurrences(collection, searchedElement, equalsCheck, false, 1);
		E result = null;
		if (!list.isEmpty()) {
			result = list.get(0);
		}
		return result;
	}

	public static <E> boolean remove(final Collection<E> collection, final Object elementToRemove, final EqualsCheck<E> equalsCheck) {
		return !getOccurrences(collection, elementToRemove, equalsCheck, true, 1).isEmpty();
	}

	public static <E> boolean contains(final Collection<E> collection, final Object searchedElement, final EqualsCheck<E> equalsCheck) {
		return countOccurrences(collection, searchedElement, equalsCheck) > 0;
	}

	public static <E> boolean add(final Collection<E> collection, final E elementToAdd, final EqualsCheck<E> equalsCheck,
			final boolean duplicatesAllowed) {
		if (!duplicatesAllowed && contains(collection, elementToAdd, equalsCheck)) {
			return false;
		}
		return collection.add(elementToAdd);
	}

	public static <E> boolean containsAll(final Collection<E> collection, final Collection<?> searchedElements, final EqualsCheck<E> equalsCheck) {
		for (final Object searchedElement : searchedElements) {
			if (!contains(collection, searchedElement, equalsCheck)) {
				return false;
			}
		}
		return true;
	}

	public static <E> boolean addAll(final Collection<E> collection, final Collection<? extends E> elementsToAdd, final EqualsCheck<E> equalsCheck,
			final boolean duplicatesAllowed) {
		boolean modified = false;
		for (final E elementToAdd : elementsToAdd) {
			modified = add(collection, elementToAdd, equalsCheck, duplicatesAllowed) || modified;
		}
		return modified;
	}

	public static <E> boolean removeAll(final Collection<E> collection, final Collection<?> elementsToRemove, final EqualsCheck<E> equalsCheck) {
		boolean modified = false;
		for (final Object elementToRemove : elementsToRemove) {
			modified = remove(collection, elementToRemove, equalsCheck) || modified;
		}
		return modified;
	}

	public static <E> boolean retainAll(final Collection<E> collection, final Collection<?> elementsToRetain, final EqualsCheck<E> equalsCheck) {
		boolean modified = false;
		final Iterator<E> iterator = collection.iterator();
		while (iterator.hasNext()) {
			final E element = iterator.next();
			final boolean contained = containsUsingReversedEqualsCheck(elementsToRetain, element, equalsCheck);
			if (!contained) {
				iterator.remove();
				modified = true;
			}
		}
		return modified;
	}

	public static <E> int indexOf(final Collection<E> collection, final Object searchedElement, final EqualsCheck<E> equalsCheck) {
		final Iterator<E> iterator = collection.iterator();
		int result = -1;
		int index = 0;
		while (iterator.hasNext()) {
			final E element = iterator.next();
			if (equalsCheck.equals(element, searchedElement)) {
				result = index;
				break;
			}
			index++;
		}
		return result;
	}

	public static <E> int lastIndexOf(final List<E> list, final Object searchedElement, final EqualsCheck<E> equalsCheck) {
		final ListIterator<E> iterator = list.listIterator();
		int result = 0;
		int index = list.size() - 1;
		while (iterator.hasNext()) {
			final E element = iterator.next();
			if (equalsCheck.equals(element, searchedElement)) {
				result = index;
				break;
			}
			index--;
		}
		return result;
	}

	/**
	 * Returns <code>true</code>, if <code>collection2</code> is a <code>Collection</code> that contains exactly the same elements (according to the
	 * specified <code>equalsCheck</code>) with exactly the same cardinalities. Also returns <code>true</code>, if both collections are
	 * <code>null</code>.
	 */
	public static <E> boolean collectionEquals(final Collection<E> collection1, final Object collection2, final EqualsCheck<E> equalsCheck) {
		if (collection1 == null && collection2 == null) {
			return true;
		}
		if (collection1 == null || collection2 == null) {
			return false;
		}

		if (!(collection2 instanceof Collection)) {
			return false;
		}

		if (collection1.size() != ((Collection<?>) collection2).size()) {
			return false;
		}

		for (final E element : collection1) {
			final int count1 = countOccurrences(collection1, element, equalsCheck);
			final int count2 = countOccurrencesUsingReversedEqualsCheck((Collection<?>) collection2, element, equalsCheck);
			if (count1 != count2) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Works like {@link #collectionEquals(Collection, Object, EqualsCheck)}, but also checks the order of the elements.
	 */
	public static <E> boolean orderEquals(final Collection<E> collection1, final Object collection2, final EqualsCheck<E> equalsCheck) {
		if (collection1 == null && collection2 == null) {
			return true;
		}
		if (collection1 == null || collection2 == null) {
			return false;
		}

		if (!collectionEquals(collection1, collection2, equalsCheck)) {
			return false;
		}

		// now also check order of elements
		final Iterator<E> it1 = collection1.iterator();
		final Iterator<?> it2 = ((Collection<?>) collection2).iterator();
		while (it1.hasNext()) {
			if (!equalsCheck.equals(it1.next(), it2.next())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Works like {@link #getOccurrences(Collection, Object, EqualsCheck, boolean, Integer)}.
	 */
	public static <E, F> List<F> getOccurrencesUsingReversedEqualsCheck(final Collection<F> collection, final E searchedElement,
			final EqualsCheck<E> equalsCheck, final boolean removeElements, final Integer maxCount) {
		if (maxCount != null && maxCount < 0) {
			throw new IllegalArgumentException("Invalid max count " + maxCount + "!");
		}
		final List<F> result = new ArrayList<>();
		final Iterator<F> iterator = collection.iterator();
		while (iterator.hasNext()) {
			final F element = iterator.next();
			if (equalsCheck.equals(searchedElement, element)) {
				if (removeElements) {
					iterator.remove();
				}
				result.add(element);
				if (maxCount != null && result.size() > maxCount) {
					break;
				}
			}
		}
		return result;
	}

	private static <E, F> int countOccurrencesUsingReversedEqualsCheck(final Collection<F> collection, final E searchedElement,
			final EqualsCheck<E> equalsCheck) {
		return getOccurrencesUsingReversedEqualsCheck(collection, searchedElement, equalsCheck, false, null).size();
	}

	private static <E, F> boolean containsUsingReversedEqualsCheck(final Collection<F> collection, final E searchedElement,
			final EqualsCheck<E> equalsCheck) {
		return countOccurrencesUsingReversedEqualsCheck(collection, searchedElement, equalsCheck) > 0;
	}

	/**
	 * Returns all elements that <code>collectionToCheck</code> contains in addition to the elements in <code>collectionToCompareTo</code>, i.e. all
	 * elements of <code>collectionToCheck</code> that are not contained in <code>collectionToCompareTo</code>.
	 */
	public static <E> List<E> getAdditionalElements(final Collection<E> collectionToCheck, final Collection<?> collectionToCompareTo,
			final EqualsCheck<E> equalsCheck) {
		final List<E> result = new ArrayList<>();
		for (final E element : collectionToCheck) {
			if (!containsUsingReversedEqualsCheck(collectionToCompareTo, element, equalsCheck)) {
				result.add(element);
			}
		}
		return result;
	}

	/**
	 * Returns all elements that are missing in <code>collectionToCheck</code>, but contained in <code>collectionToCompareTo</code>.
	 */
	public static <E, F> List<F> getMissingElements(final Collection<E> collectionToCheck, final Collection<F> collectionToCompareTo,
			final EqualsCheck<E> equalsCheck) {
		final List<F> result = new ArrayList<>();
		for (final F element : collectionToCompareTo) {
			if (!contains(collectionToCheck, element, equalsCheck)) {
				result.add(element);
			}
		}
		return result;
	}
}
