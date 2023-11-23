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

import java.util.Collections;
import java.util.Iterator;

/**
 * {@link Iterable} which enables iteration over multiple {@linkplain Iterable}s.
 *
 * @author peter.gazdik
 */
public class CompositeIterable<E> implements Iterable<E> {
	private final Iterable<? extends Iterable<? extends E>> iterables;

	public CompositeIterable(Iterable<? extends Iterable<? extends E>> iterables) {
		this.iterables = iterables;
	}

	@Override
	public Iterator<E> iterator() {
		return new CompositeIterator();
	}

	private class CompositeIterator implements Iterator<E> {

		private final Iterator<? extends Iterable<? extends E>> iteratorsIt = iterables.iterator();

		private Iterator<? extends E> currentIt = Collections.emptyIterator();
		private Iterator<? extends E> nextIt = Collections.emptyIterator();

		public CompositeIterator() {
			moveNextIt();
		}

		@Override
		public boolean hasNext() {
			return currentIt.hasNext() || nextIt.hasNext();
		}

		@Override
		public E next() {
			if (!hasNext()) {
				throw new IllegalStateException("Iterator has already reached the end.");
			}

			if (currentIt.hasNext()) {
				return currentIt.next();
			}

			currentIt = nextIt;
			moveNextIt();

			return currentIt.next();
		}

		@Override
		public void remove() {
			currentIt.remove();
		}

		private void moveNextIt() {
			while (iteratorsIt.hasNext()) {
				nextIt = iteratorsIt.next().iterator();
				if (nextIt.hasNext()) {
					return;
				}
			}
		}

	}
}
