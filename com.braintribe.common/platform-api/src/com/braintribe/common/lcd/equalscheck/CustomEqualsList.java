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
import java.util.List;
import java.util.ListIterator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * {@link List} extension of {@link CustomEqualsCollectionImpl}.
 *
 * @author michael.lafite
 *
 * @param <E>
 *            The type of the elements in this collection
 */
@SuppressFBWarnings({ "BC_BAD_CAST_TO_ABSTRACT_COLLECTION", "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE" })
public class CustomEqualsList<E> extends CustomEqualsCollectionImpl<E, List<E>> implements List<E> {

	public CustomEqualsList(final EqualsCheck<E> equalsCheck, final List<E> delegate) {
		super(equalsCheck, delegate, true);
	}

	@Override
	public int indexOf(final Object searchedElement) {
		return CustomEqualsCollectionTools.indexOf(this, searchedElement, getEqualsCheck());
	}

	@Override
	public int lastIndexOf(final Object searchedElement) {
		return CustomEqualsCollectionTools.lastIndexOf(this, searchedElement, getEqualsCheck());
	}

	// *** delegating methods ***

	@Override
	public boolean addAll(final int index, final Collection<? extends E> elementsToAdd) {
		if (elementsToAdd == null) {
			throw new NullPointerException("No elements to add specified!"); // SuppressPMDWarnings (NPE okay here)
		}
		return getDelegate().addAll(index, elementsToAdd);
	}

	@Override
	public E get(final int index) {
		return getDelegate().get(index);
	}

	@Override
	public E set(final int index, final E element) {
		return getDelegate().set(index, element);
	}

	@Override
	public void add(final int index, final E element) {
		getDelegate().add(index, element);
	}

	@Override
	public E remove(final int index) {
		return getDelegate().remove(index);
	}

	@Override
	public ListIterator<E> listIterator() {
		return getDelegate().listIterator();
	}

	@Override
	public ListIterator<E> listIterator(final int index) {
		return getDelegate().listIterator(index);
	}

	@Override
	public List<E> subList(final int fromIndex, final int toIndex) {
		return getDelegate().subList(fromIndex, toIndex);
	}
}
