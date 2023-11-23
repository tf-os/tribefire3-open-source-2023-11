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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author peter.gazdik
 */
public class CompositeIterableTests {

	private Iterable<? extends Iterable<String>> iterables;

	@Test
	public void emptyMainIterable() {
		iterables = Collections.emptyList();
		assertElements();
	}

	@Test
	public void emptyNestedIterable() {
		iterables = iterables(emptyList());
		assertElements();
	}

	@Test
	public void emptyNestedIterables() {
		iterables = iterables(emptyList(), emptyList());
		assertElements();
	}

	@Test
	public void singleElementList() {
		iterables = iterables(asList("one"));
		assertElements("one");
	}

	@Test
	public void multiElementList() {
		iterables = iterables(asList("one", "two"));
		assertElements("one", "two");
	}

	@Test
	public void multiElementLists() {
		iterables = iterables(asList("one", "two"), asList("three"));
		assertElements("one", "two", "three");
	}

	@Test
	public void multiElementListsWithNull() {
		iterables = iterables(asList("one", null), asList("three"));
		assertElements("one", null, "three");
	}

	@Test
	public void multiElementListsWithAndEmptyNull() {
		iterables = iterables(asList("one", null), emptyList(), asList("three"));
		assertElements("one", null, "three");
	}

	@Test
	@SuppressWarnings("unused") // for those labels
	public void removing() {
		List<String> l1 = CollectionTools2.asList("one", "two");
		List<String> l2 = CollectionTools2.asList("three", "four");

		Iterator<String> it = compositeIterable(iterables(l1, l2)).iterator();

		// @formatter:off
		one:   it.next();
		two:   it.next(); it.remove();
		three: it.next(); it.remove();
		four:  it.next();
		// @formatter:on

		assertThat(it.hasNext()).isFalse();

		assertThat(l1).containsExactly("one");
		assertThat(l2).containsExactly("four");
	}

	// #####################################
	// ## . . . . . . Helpers . . . . . . ##
	// #####################################

	private static List<Iterable<String>> iterables(Iterable<String>... iterables) {
		return Arrays.asList(iterables);
	}

	private static List<String> emptyList() {
		return Collections.emptyList();
	}

	private void assertElements(String... expectedElements) {
		int pos = 0;

		for (String actual : compositeIterable(iterables)) {
			if (expectedElements.length == pos) {
				// not probable
				Assert.fail("Composite iterable provided more elements than expected!");
			}

			String expected = expectedElements[pos];

			if (!CommonTools.equalsOrBothNull(actual, expected)) {
				Assert.fail("Wrong value on position '" + pos + "' Expected: " + expected + ", actual: " + actual);
			}

			pos++;
		}

		if (expectedElements.length > pos) {
			Assert.fail("Composite iterable provided less elements than expected! Only provided " + pos + " element(s).");
		}
	}

	private static CompositeIterable<String> compositeIterable(Iterable<? extends Iterable<String>> its) {
		return new CompositeIterable<>(its);
	}

}
