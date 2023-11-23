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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.braintribe.utils.lcd.Not;

/**
 * Implementation of {@link CustomEqualsCollection} that uses a configurable {@link #getDelegate() delegate collection} but intercepts all methods
 * where the {@link #getEqualsCheck() equals check} is relevant (i.e. <code>add, remove, contains</code>, etc.). Note that this implementation may be
 * VERY slow when compared to the delegate collection, because it usually has to iterate through the elements (instead of using hash codes, etc.).
 *
 * @see CustomEqualsSet
 * @see CustomEqualsList
 *
 * @author michael.lafite
 *
 * @param <E>
 *            The type of the elements in this collection
 * @param <C>
 *            The type of the delegate collection
 */

public class CustomEqualsCollectionImpl<E, C extends Collection<E>> implements CustomEqualsCollection<E> {

	protected final C delegate;

	protected final EqualsCheck<E> equalsCheck;

	protected final boolean duplicatesAllowed;

	public CustomEqualsCollectionImpl(final EqualsCheck<E> equalsCheck, final C delegate, final boolean duplicatesAllowed) {
		this.equalsCheck = equalsCheck;
		this.delegate = delegate;
		this.duplicatesAllowed = duplicatesAllowed;
	}

	@Override
	public List<E> getOccurrences(final Object searchedElement) {
		return CustomEqualsCollectionTools.getOccurrences(this, searchedElement, getEqualsCheck());
	}

	@Override
	public int countOccurrences(final Object searchedElement) {
		return CustomEqualsCollectionTools.countOccurrences(this, searchedElement, getEqualsCheck());
	}

	@Override
	public List<E> removeOccurrences(final Object searchedElement) {
		return CustomEqualsCollectionTools.removeOccurrences(this, searchedElement, getEqualsCheck());
	}

	@Override
	public List<E> getAdditionalElements(final Collection<?> collectionToCompareTo) {
		return CustomEqualsCollectionTools.getAdditionalElements(this, collectionToCompareTo, getEqualsCheck());
	}

	@Override
	public <F> List<F> getMissingElements(final Collection<F> collectionToCompareTo) {
		return CustomEqualsCollectionTools.getMissingElements(this, collectionToCompareTo, getEqualsCheck());
	}

	/**
	 * Delegates to the wrapped collection, i.e. does NOT use the custom equals check!
	 *
	 * @see #collectionEquals(Object)
	 */
	@Override
	public boolean equals(final Object object) {
		return getDelegate().equals(object);
	}

	@Override
	public EqualsCheck<E> getEqualsCheck() {
		return Not.Null(this.equalsCheck);
	}

	@Override
	public boolean collectionEquals(final Object otherCollection) {
		return CustomEqualsCollectionTools.collectionEquals(this, otherCollection, getEqualsCheck());
	}

	@Override
	public boolean orderEquals(final Object otherCollection) {
		return CustomEqualsCollectionTools.orderEquals(this, otherCollection, getEqualsCheck());
	}

	@Override
	public C getDelegate() {
		return Not.Null(this.delegate);
	}

	@Override
	public boolean isDuplicatesAllowed() {
		return this.duplicatesAllowed;
	}

	public E get(final Object searchedElement) {
		return CustomEqualsCollectionTools.get(getDelegate(), searchedElement, getEqualsCheck());
	}

	@Override
	public boolean contains(final Object searchedElement) {
		return CustomEqualsCollectionTools.contains(getDelegate(), searchedElement, getEqualsCheck());
	}

	@Override
	public boolean add(final E elementToAdd) {
		return CustomEqualsCollectionTools.add(getDelegate(), elementToAdd, getEqualsCheck(), this.duplicatesAllowed);
	}

	@Override
	public boolean remove(final Object elementToRemove) {
		return CustomEqualsCollectionTools.remove(getDelegate(), elementToRemove, getEqualsCheck());
	}

	@Override
	public boolean containsAll(final Collection<?> searchedElements) {
		if (searchedElements == null) {
			throw new NullPointerException("No searched elements specified!"); // SuppressPMDWarnings (NPE okay here)
		}
		return CustomEqualsCollectionTools.containsAll(getDelegate(), searchedElements, getEqualsCheck());
	}

	@Override
	public boolean addAll(final Collection<? extends E> elementsToAdd) {
		if (elementsToAdd == null) {
			throw new NullPointerException("No elements to add specified!"); // SuppressPMDWarnings (NPE okay here)
		}
		return CustomEqualsCollectionTools.addAll(getDelegate(), elementsToAdd, getEqualsCheck(), this.duplicatesAllowed);
	}

	@Override
	public boolean removeAll(final Collection<?> elementsToRemove) {
		if (elementsToRemove == null) {
			throw new NullPointerException("No elements to remove specified!"); // SuppressPMDWarnings (NPE okay here)
		}
		return CustomEqualsCollectionTools.removeAll(getDelegate(), elementsToRemove, getEqualsCheck());
	}

	@Override
	public boolean retainAll(final Collection<?> elementsToRetain) {
		if (elementsToRetain == null) {
			throw new NullPointerException("No elements to retain specified!"); // SuppressPMDWarnings (NPE okay here)
		}
		return CustomEqualsCollectionTools.retainAll(getDelegate(), elementsToRetain, getEqualsCheck());
	}

	// *** delegating methods ***

	@Override
	public int size() {
		return getDelegate().size();
	}

	@Override
	public boolean isEmpty() {
		return getDelegate().isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return getDelegate().iterator();
	}

	@Override
	public Object[] toArray() {
		return getDelegate().toArray();
	}

	@Override
	public <T> T[] toArray(final T[] array) {
		return getDelegate().toArray(array);
	}

	@Override
	public void clear() {
		getDelegate().clear();
	}

	@Override
	public String toString() {
		return getDelegate().toString();
	}

	@Override
	public int hashCode() {
		return getDelegate().hashCode();
	}

}
