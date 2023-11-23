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
package com.braintribe.utils.collection;

import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * A list of elements where we can control the relative ordering of the elements. Each contained element is associated with a so called position
 * identifier (using {@link #with(Object)} method), like this: {@code list.with(identifier).add(value);}.
 * 
 * <h3>Basic features</h3>
 * 
 * Adding is done by starting with the {@link #with(Object)} call (which associates the element with a position identifier) and arbitrary many
 * {@link PosAdditionContext#before(Object)} and {@link PosAdditionContext#after(Object)} calls which specify where identifier should be relative to
 * other identifiers. As arguments for the last two methods, one may use identifiers of other values (which may or may not have already been
 * inserted), and the set guarantees that the order of elements will respect these constraints.
 * <p>
 * Elements may be retrieved by {@link #stream()} method, which returns a stream of the contained elements. Note that there may be duplicates in this
 * stream, if the same value is associated with multiple positions.
 * <p>
 * To remove an element by it's value call {@link #remove(Object)} or {@link #removeSoft(Object)}, to remove it by it's position call
 * {@link #removeElement(Object)} or {@link #removeElementSoft(Object)}.
 * <p>
 * To replace an element for given position identifier use {@link #replace(Object, Object)}.
 * 
 * <h3>Example</h3>
 * 
 * {@code list.with(middlePriority).after(highPriority).before(lowPriority).add(middlePriorityElement)}
 *
 * @param <E>
 *            element type
 * @param <P>
 *            position identifier type
 */
public interface PartiallyOrderedSet<E, P> extends Collection<E> {

	PosAdditionContext<E, P> with(P positionIdentifier);

	/**
	 * Returns <tt>true</tt> if this set contains any information related to given position identifier. Note that this doesn't mean there is a value
	 * associated with given position identifier.
	 * <p>
	 * Having position identifiers with no value makes sense in case we just want a delimiter where we control that some elements are inserted before
	 * and some after this delimiter.
	 */
	boolean containsPosition(P positionIdentifier);

	/** @return <tt>true</tt> iff this set contains some value for given position identifier. */
	boolean containsElementOnPosition(P positionIdentifier);

	/**
	 * @return element previously associated with given position.
	 * 
	 * @throws NoSuchElementException
	 *             if there is no element associated with given position.
	 * 
	 */
	E replace(P positionIdentifier, E element);

	/**
	 * Removes the value (including any ordering information for the corresponding position) from the set. If multiple positions correspond to given
	 * value, the one that would appear first in the streaming/iteration order is removed. To remove a value but keep the position information use
	 * {@link #removeSoft(Object)}.
	 */
	@Override
	boolean remove(Object o);

	/** @see #remove(Object) */
	boolean removeSoft(Object element);

	/**
	 * Removes the value for given position, including any ordering information for the position. To remove a value on given position but keep the
	 * position information use {@link #removeElementSoft(Object)}.
	 * 
	 * Returns the value associated with given position (might be <tt>null</tt>), or <tt>null</tt> if no value is associated with given position. To
	 * disambiguate the meaning of a possible <tt>null</tt> value do a {@link #containsElementOnPosition(Object)} check first.
	 */
	E removeElement(P positionIdentifier);

	/** @see #removeElement(Object) */
	E removeElementSoft(P positionIdentifier);

	interface PosAdditionContext<E, P> {

		/**
		 * Specifies that currently added element's position (specified using {@link PartiallyOrderedSet#with(Object)}) must precede the position
		 * given here.
		 */
		PosAdditionContext<E, P> before(P positionIdentifier);

		/**
		 * Specifies that currently added element's position (specified using {@link PartiallyOrderedSet#with(Object)}) must succeed the position
		 * given here.
		 */
		PosAdditionContext<E, P> after(P positionIdentifier);

		/** Adds an element to this set, whose position was specified using {@link PartiallyOrderedSet#with(Object)}. */
		boolean add(E element);
	}

}
