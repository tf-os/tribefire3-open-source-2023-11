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
package com.braintribe.model.access;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A implementation of {@link Iterable} that dynamically fetches batches of
 * elements from a given expert ({@link PageProvider}) and iterate through each
 * retrieved batch in memory. <br />
 * This iterator is typically used when implementing one of
 * {@link BasicAccessAdapter#queryPopulation(String)} methods and the actual
 * backend-system is capable of doing pagination.<br />
 * In that case one can implement the pagination support by coding something
 * like this:
 * 
 * <pre>
 * protected Iterable<GenericEntity> queryPopulation(String typeSignature) throws ModelAccessException {
 * 	return new PagigantedIterator<GenericEntity>(new PageProvider() {
 * 		public Collection<T> nextPage() {
 * 			// implement paging logic
 * 		}
 * 	});
 * }
 * </pre>
 * 
 * Note, that by default invoking {@link #hasNext()} on instances of this
 * {@link Iterable} could already load the next page by calling
 * {@link PageProvider#nextPage()}.<br />
 * 
 * If that's not the expected behavior and the {@link PageProvider}
 * implementation is capable of indicating the availability of more data one can
 * additionally implement the {@link NextPageAware} interface.<br />
 * In other words, the {@link PaginatedIterable#hasNext()} method checks it's
 * pageProvider implementation for {@link NextPageAware} and either returns
 * its {@link NextPageAware#hasNextPage()} or loads the next page in order to
 * determine the information. <br />
 * <br />
 * This class acts as both, {@link Iterable} as well as {@link Iterator}. Thus,
 * one can use it either way depending on particular needs.<br />
 * Usually the usage as an {@link Iterable} is expected which returns a
 * reference to itself in it's {@link #iterator()} implementation.
 */
public class PaginatedIterable<T> implements Iterator<T>, Iterable<T> {

	private PageProvider<T> pageProvider;
	private Iterator<T> currentIterator;

	public PaginatedIterable(PageProvider<T> pageProvider) {
		this.pageProvider = pageProvider;
	}

	/**
	 * Returns a self reference to support the {@link Iterable} interface.
	 */
	@Override
	public Iterator<T> iterator() {
		return this;
	}

	/**
	 * Returns whether or not there is more data to iterate. The procedure to
	 * determine that information depends on the provided {@link PageProvider}.
	 * <br />
	 * If the pageProvider implements {@link NextPageAware} the result of
	 * {@link NextPageAware#hasNextPage()} will be returned. <br />
	 * Otherwise the next page will be loaded into memory using
	 * {@link PageProvider#nextPage()} and the resulting collection will be
	 * asked for hasNext().
	 */
	@Override
	public boolean hasNext() {

		if (currentIterator != null) {
			// We have a current page. Check whether there is more to load.
			if (currentIterator.hasNext()) {
				// More to load in current page. We can stop here.
				return true;
			}
		}

		// No more elements in current page or there is no page loaded so far.
		// Now check whether the PageProvider can tell if a page is
		// available.
		if (pageProvider instanceof NextPageAware) {
			return ((NextPageAware) pageProvider).hasNextPage();
		}

		// The PageProvider is not capable of telling whether there is more to
		// load. So we ask for the next page and return the information based on
		// the result.
		if (!ensureIterator()) {
			return false;
		}
		return currentIterator.hasNext();
	}

	/**
	 * Returns the next element to iterate by either returning the next element
	 * of the current loaded page or load the next page and return the first
	 * element.
	 */
	@Override
	public T next() {
		if (!ensureIterator()) {
			throw new NoSuchElementException("no element available.");
		}
		return currentIterator.next();
	}

	/**
	 * This implementation always throws an
	 * {@link UnsupportedOperationException} since this {@link Iterator} is
	 * intended to be read-only.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove operation not permitted on this readonly iterator.");
	}

	private boolean ensureIterator() {
		if (currentIterator == null) {
			Collection<T> page = pageProvider.nextPage();
			if (page == null || page.isEmpty()) {
				return false;
			}
			currentIterator = page.iterator();
		}

		if (!currentIterator.hasNext()) {
			currentIterator = null;
			return ensureIterator();
		}

		return true;
	}

}
