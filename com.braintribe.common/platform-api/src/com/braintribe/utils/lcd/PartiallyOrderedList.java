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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A list of elements where we can control the relative ordering of the elements. Using the method {@link #with(Object)} , one can associate a so
 * called position identifier to an entry - with a chain like {@code list.with(identifier).add(value);}.
 * <p>
 * Each {@linkplain #add(Object)} invocation may thus be preceded by one {@link #with(Object)} invocation (if multiple are called, the last value is
 * used) and arbitrary many {@link #before(Object)} and {@link #after(Object)} invocations. As arguments for the last two methods, one may use
 * identifiers of other values (which may or may not have already been inserted), and the list guarantees that the order of elements will respect
 * these constraints. Elements may be retrieved by {@link #list()} method.
 * <p>
 * The order of values with no position identifier is not defined, however it is guaranteed, that invoking methods on this class in the same order
 * always produces same result (so for instance the order does not depend on the hash-code value, which may differ for different runs using the same
 * objects).
 * <p>
 * Example: {@code list.with(middlePriority).after(highPriority).before(lowPriority).add(middlePriorityElement)}
 *
 * NOTE: This implementation is NOT THREAD SAFE and if one wants to make it such, then the whole add-element-chain (see example above with calls of
 * "with", "before", "after" and "add") must happen as an atomic operation.
 *
 * @param <E>
 *            element type
 * @param <P>
 *            position identifier type
 *
 * @deprecated use SimplePartiallyOrderedList in collection-api
 */
@Deprecated
public class PartiallyOrderedList<E, P> {
	private List<E> list;
	private final List<ElementDesc<P, E>> descList = new ArrayList<>();

	private final Set<P> currentBefores = new HashSet<>();
	private final Set<P> currentAfters = new HashSet<>();
	private P currentPositionIdentifier = null;

	private final Map<P, Limits<P>> limits = new HashMap<>();

	public PartiallyOrderedList() {
		// Whenever this class is initialized, it is also filled with approx 1-3 elements
		// Hence, we set the initial size here
		// Default size would be 10
		this(new ArrayList<E>(3));
	}

	/**
	 * Constructor that takes an existing {@link List} as a delegate. Every change done on the instance of constructed {@link PartiallyOrderedList} is
	 * immediately reflected in this delegate.
	 */
	public PartiallyOrderedList(List<E> list) {
		this.list = list;
	}

	private static class Limits<P> {
		final Set<P> befores = new HashSet<>();
		final Set<P> afters = new HashSet<>();
	}

	private static class ElementDesc<P, E> {
		E element;
		P positionIdentifier;

		ElementDesc(E e, P p) {
			this.element = e;
			this.positionIdentifier = p;
		}

		@Override
		public String toString() {
			return "[" + element + "(" + positionIdentifier + ")]";
		}
	}

	public PartiallyOrderedList<E, P> with(P positionIdentifier) {
		this.currentPositionIdentifier = positionIdentifier;
		return this;
	}

	public PartiallyOrderedList<E, P> before(P positionIdentifier) {
		if (this.currentAfters.contains(positionIdentifier)) {
			throw new IllegalArgumentException("Position added both as 'before' and 'after': " + positionIdentifier);
		}

		currentBefores.add(positionIdentifier);
		return this;
	}

	public PartiallyOrderedList<E, P> after(P positionIdentifier) {
		if (currentBefores.contains(positionIdentifier)) {
			throw new IllegalArgumentException("Position added both as 'before' and 'after': " + positionIdentifier);
		}

		currentAfters.add(positionIdentifier);
		return this;
	}

	public void add(E element) {
		int position;

		if (currentPositionIdentifier == null) {
			position = 0;
		} else {
			position = findPositionForCurrent();
		}

		list.add(position, element);
		descList.add(position, new ElementDesc<>(element, currentPositionIdentifier));

		clearCurrentContext();
	}

	public void remove(Object element) {
		int index = indexOf(element);
		if (index >= 0) {
			list.remove(index);
			descList.remove(index);
		}
	}

	private int indexOf(Object element) {
		int index = 0;
		for (E e : list) {
			if (element == null) {
				if (e == null) {
					return index;
				}
			} else {
				if (element.equals(e)) {
					return index;
				}
			}
			index++;
		}
		return -1;
	}

	private int findPositionForCurrent() {
		Limits<P> currentLimits = registerCurrentLimits();

		int min = findMin(currentLimits);
		int max = findMax(currentLimits);

		if (max < min) {
			throw new RuntimeException("Inconsistent order. Befores: " + currentLimits.befores + " Afters: " + currentLimits.afters
					+ " Current list state: " + descList);
		}

		return min;
	}

	private int findMin(Limits<P> limits) {
		Set<P> afters = limits.afters;

		for (int i = descList.size() - 1; i >= 0; i--) {
			P p = descList.get(i).positionIdentifier;
			if (afters.contains(p)) {
				return i + 1;
			}
		}

		return 0;
	}

	private int findMax(Limits<P> limits) {
		Set<P> befores = limits.befores;

		for (int i = 0; i < descList.size(); i++) {
			P p = descList.get(i).positionIdentifier;
			if (befores.contains(p)) {
				return i;
			}
		}

		return descList.size();
	}

	private Limits<P> registerCurrentLimits() {
		Limits<P> currentLimits = acquireLimits(currentPositionIdentifier);

		for (P before : currentBefores) {
			currentLimits.befores.add(before);
			acquireLimits(before).afters.add(currentPositionIdentifier);
		}

		for (P after : currentAfters) {
			currentLimits.afters.add(after);
			acquireLimits(after).befores.add(currentPositionIdentifier);
		}

		return currentLimits;
	}

	private Limits<P> acquireLimits(P p) {
		Limits<P> result = limits.get(p);

		if (result == null) {
			result = new Limits<>();
			limits.put(p, result);
		}

		return result;
	}

	private void clearCurrentContext() {
		currentBefores.clear();
		currentAfters.clear();
		currentPositionIdentifier = null;
	}

	public List<E> list() {
		return list;
	}
}
