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
package com.braintribe.utils.collection.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Before;

import com.braintribe.utils.collection.api.NavigableMultiMap;

/**
 * 
 */
public class NavigableMultiMap_Iterators_Tests extends AbstractNavigableMultiMapTests {

	private TreeMap<Long, Long> treeMap;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		addNewEntries();
		treeMap = new TreeMap<>();
		for (long l = 1; l <= 100; l++)
			treeMap.put(l, 10 * l);
	}

	private void addNewEntries() {
		for (long i = 1; i <= 100; i++) {
			multiMap.put(i, 10 * i);
		}
	}

	public void testIteratorListsValuesInCorrectOrder() {
		assertIteratorLists("Normal", multiMap.keySet().iterator(), treeMap, true);
		// assertIteratorLists("Descending of KeySet", multiMap.keySet().descendingIterator(), treeMap, false);
		// assertIteratorLists("Descending of DescendingKeySet", multiMap.descendingKeySet().descendingIterator(),
		// treeMap, true);
		// assertIteratorLists("DescendingKeySet", multiMap.descendingKeySet().iterator(), treeMap, false);
		assertIteratorLists("SubMap Normal", multiMap.headMap(200L, false).keySet().iterator(), treeMap, true);
		// assertIteratorLists("SubMap Desc of DesceKeySet", multiMap.headMap(200L,
		// false).descendingKeySet().descendingIterator(), treeMap, true);
		// assertIteratorLists("SubMap DescendingKeySet", multiMap.headMap(200L, false).descendingKeySet().iterator(),
		// treeMap, false);

		NavigableMultiMap<Long, Long> subSub = multiMap.headMap(200L, false).tailMap(-1L, true);
		assertIteratorLists("SubMap SubMap Normal", subSub.keySet().iterator(), treeMap, true);
		// assertIteratorLists("SubMap SubMap Descending", subSub.descendingKeySet().iterator(), treeMap, false);

		// assertIteratorLists("Descending SubMap", multiMap.descendingMap().keySet().iterator(), treeMap, false);
		// assertIteratorLists("Desc SubMap Desc of KeySet", (multiMap.descendingMap().keySet()).descendingIterator(),
		// treeMap, true);
		// assertIteratorLists("Desc SubMap Desc of DescKeySet",
		// multiMap.descendingMap().descendingKeySet().descendingIterator(), treeMap, false);
		// assertIteratorLists("Descending SubMap DescendingKeySet",
		// multiMap.descendingMap().descendingKeySet().iterator(), treeMap, true);

		// subSub = multiMap.descendingMap().headMap(-1L, false);
		// assertIteratorLists("Descending SubMap SubMap", subSub.keySet().iterator(), treeMap, false);
		// assertIteratorLists("Descending SubMap descending", subSub.descendingKeySet().iterator(), treeMap, true);
	}

	private void assertIteratorLists(String multiMapName, Iterator<Long> iterator, Map<Long, Long> expectedValues, boolean ascending) {
		Set<Long> remainingValues = new HashSet<Long>(expectedValues.keySet());
		String itName = "[" + multiMapName + " iterator] ";
		Long previous = null;
		while (iterator.hasNext()) {
			Long l = iterator.next();
			assertTrue(itName + "Value not expected: " + l, expectedValues.containsKey(l));
			assertTrue(itName, remainingValues.remove(l));

			if (previous != null) {
				assertTrue(itName + "Wrong order", l != previous && (ascending == (l > previous)));
			}
			previous = l;
		}
		assertTrue(itName + " did not list these indexedValues:" + remainingValues, remainingValues.isEmpty());
	}

	public void testSubMapsHaveCorrectRange() {
		testForMap("Normal", multiMap, 1L, 10L, 20L, 100L);
		// testForMap("Descending ", multiMap.descendingMap(), 100L, 20L, 10L, 1L);
		// testForMap("Descending Descending", multiMap.descendingMap().descendingMap(), 1L, 10L, 20L, 100L);

		testForMap("Normal SubMap", multiMap.subMap(10L, true, 90L, true), 10L, 20L, 30L, 90L);
		// testForMap("Descending SubMap", multiMap.descendingMap().subMap(90L, true, 10L, true), 90L, 30L, 20L, 10L);
	}

	/**
	 * This method gets a Map[Long] and four indexedValues, minimum Value in the Map, maximum Value in the Map, and two
	 * indexedValues in between, to be used as sub-multiMap bounds. This multiMap should contain all the indexedValues
	 * in range [min, max] (both including). This method checks the content of this multiMap and also of many possible
	 * submultiMaps. The <code>multiMapName</code> is only used to generate nice messages.
	 */
	private void testForMap(String multiMapName, NavigableMultiMap<Long, Long> multiMap, Long min, Long low, Long high, Long max) {
		int diff = high > low ? 1 : -1;

		assertRange(multiMapName, multiMap, min, max);
		assertRange(multiMapName + " Head", multiMap.headMap(high, false), min, high - diff);
		assertRange(multiMapName + " Head In", multiMap.headMap(high, true), min, high);
		assertRange(multiMapName + " Tail", multiMap.tailMap(low, true), low, max);
		assertRange(multiMapName + " Tail Ex", multiMap.tailMap(low, false), low + diff, max);

		testForSubMap(multiMapName + " SubMap In In", multiMap, low, true, high, true);
		testForSubMap(multiMapName + " SubMap Ex Ex", multiMap, low, false, high, false);
		testForSubMap(multiMapName + " SubMap In Ex", multiMap, low, true, high, false);
		testForSubMap(multiMapName + " SubMap Ex In", multiMap, low, false, high, true);
	}

	private void testForSubMap(String multiMapName, NavigableMultiMap<Long, Long> multiMap, Long low, boolean includeLow, Long high,
			boolean includeHigh) {
		int diff = high > low ? 1 : -1;
		Long expHigh = includeHigh ? high : high - diff;
		Long expLow = includeLow ? low : low + diff;

		NavigableMultiMap<Long, Long> subMap = multiMap.subMap(low, includeLow, high, includeHigh);
		assertRange(multiMapName, subMap, expLow, expHigh);

		assertTrue(includeLow == subMap.containsKey(low));
		assertTrue(includeHigh == subMap.containsKey(high));

		assertFalse(subMap.containsKey(high + diff));
		assertFalse(subMap.containsKey(low - diff));
	}

	private void assertRange(String multiMapName, NavigableMultiMap<Long, Long> multiMap, Long low, Long high) {
		String s = "[" + multiMapName + "] ";

		Long ll = Math.min(high, low);
		Long hh = Math.max(high, low);

		for (Long l = ll; l <= hh; l++) {
			assertTrue(s + l + " not found in multiMap", multiMap.containsKey(l));
		}

		assertEquals(s + "Wrong multiMap size", hh - ll + 1, multiMap.size());
	}

	public void testSubMapsWithNullHaveCorrectRange() {
		multiMap.put(null, null);
		assertContainsNull("Normal with null", multiMap, true);
		assertContainsNull("HeadMap with null", multiMap.headMap(-1L, false), true);
		// assertContainsNull("Descending TailMap with null", multiMap.descendingMap().tailMap(4L, true), false);
		assertContainsNull("SubMap with null", multiMap.subMap(null, true, 0L, false), true);

	}

	private void assertContainsNull(String multiMapName, NavigableMultiMap<Long, Long> multiMap, boolean ascending) {
		String s = "[" + multiMapName + "] ";
		assertTrue(s + " Should contain null", multiMap.containsKey(null));
		if (ascending) {
			assertNull(s + " First element should be null", multiMap.keySet().iterator().next());
		} else {
			assertEquals(s + " Last element should be null", null, getLast(multiMap));
		}
	}

	private Long getLast(NavigableMultiMap<Long, Long> multiMap) {
		Long last = null;
		for (Iterator<Long> it = multiMap.keySet().iterator(); it.hasNext();) {
			last = it.next();
		}
		return last;
	}

}
