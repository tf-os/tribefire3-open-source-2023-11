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

/**
 * Implementation of {@link java.lang.Iterable} using FilterElementIterator. Enables usage of java
 * "for (Element e: iterable )" construction
 */
public class FilteringElementIterable implements Iterable<Element> {

	private final Element parent;
	private final Predicate<Element> filter;

	public FilteringElementIterable(final Element parent, final Predicate<Element> filter) {
		super();
		this.parent = parent;
		this.filter = filter;
	}

	// removed because of Java5 issue: @Override
	@Override
	public Iterator<Element> iterator() {
		return new FilteringElementIterator(this.parent, this.filter);
	}

}
