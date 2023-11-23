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
package com.braintribe.utils.xml.dom.iterator;

import java.util.Iterator;
import java.util.function.Predicate;

import org.w3c.dom.Element;

import com.braintribe.utils.xml.dom.DomUtils;

/**
 * 
 * an implementation of an iterator that uses the FilterApi to implement a filtering while iterating.
 * 
 * @author pit
 * 
 */
public class FilteringElementIterator implements Iterator<Element> {

	private Element element = null;
	private Predicate<Element> filter = null;

	public FilteringElementIterator(final Element parent, final Predicate<Element> filter) {
		this.filter = filter;
		this.element = DomUtils.getFirstElement(parent);
		if (this.element == null) {
			return;
		}
		do {
			if (filter.test(this.element)) {
				return;
			} else {
				this.element = DomUtils.getNextElement(this.element);
			}
		} while (this.element != null);
	}

	@Override
	public Element next() {
		final Element retval = this.element;
		do {
			this.element = DomUtils.getNextElement(this.element);
		} while ((this.element != null) && (!this.filter.test(this.element)));
		return retval;
	}

	@Override
	public boolean hasNext() {
		return this.element != null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
