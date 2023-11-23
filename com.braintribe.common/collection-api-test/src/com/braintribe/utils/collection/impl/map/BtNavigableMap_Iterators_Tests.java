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
package com.braintribe.utils.collection.impl.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.utils.collection.impl.BtNavigableMap;

/**
 * @deprecated {@link BtNavigableMap} was only created for GWT compatibility. GWT now supports {@link TreeMap}, so use that!!!
 */
@Deprecated
public class BtNavigableMap_Iterators_Tests extends BtNavigableMap_HighLevelTestBase {

	private TreeMap<Long, Long> treeMap;

	@Override
	@Before
	public void setUp() {
		super.setUp();

		addNewEntriesFor(longsForRange(1, 100));
		treeMap = new TreeMap<Long, Long>();
		for (long l = 1; l <= 100; l++) {
			treeMap.put(l, 10 * l);
		}
	}

	@Test
	public void testIteratorListsValuesInCorrectOrder() {
		assertIteratorLists("Normal", map.keySet().iterator(), treeMap, true);
		assertIteratorLists("SubMap Normal", map.headMap(200L).keySet().iterator(), treeMap, true);

		NavigableMap<Long, Long> subSub = (NavigableMap<Long, Long>) (map.headMap(200L).tailMap(-1L));
		assertIteratorLists("SubMap SubMap Normal", subSub.keySet().iterator(), treeMap, true);
	}

	private void assertIteratorLists(String mapName, Iterator<Long> iterator, Map<Long, Long> expectedValues, boolean ascending) {
		Set<Long> remainingValues = new HashSet<Long>(expectedValues.keySet());
		String itName = "[" + mapName + " iterator] ";
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

	@Test
	public void testSubMapsHaveCorrectRange() {
		testForMap("Normal", map, 1L, 10L, 20L, 100L);

		testForMap("Normal SubMap", map.subMap(10L, true, 90L, true), 10L, 20L, 30L, 90L);
	}

	/**
	 * This method gets a Map[Long] and four indexedValues, minimum Value in the Map, maximum Value in the Map, and two
	 * indexedValues in between, to be used as sub-map bounds. This map should contain all the indexedValues in range
	 * [min, max] (both including). This method checks the content of this map and also of many possible sub-maps. The
	 * <code>mapName</code> is only used to generate nice messages.
	 */
	private void testForMap(String mapName, NavigableMap<Long, Long> map, Long min, Long low, Long high, Long max) {
		int diff = high > low ? 1 : -1;

		assertRange(mapName, map, min, max);
		assertRange(mapName + " Head", map.headMap(high), min, high - diff);
		assertRange(mapName + " Head In", map.headMap(high, true), min, high);
		assertRange(mapName + " Tail", map.tailMap(low), low, max);
		assertRange(mapName + " Tail Ex", map.tailMap(low, false), low + diff, max);

		testForSubMap(mapName + " SubMap In In", map, low, true, high, true);
		testForSubMap(mapName + " SubMap Ex Ex", map, low, false, high, false);
		testForSubMap(mapName + " SubMap In Ex", map, low, true, high, false);
		testForSubMap(mapName + " SubMap Ex In", map, low, false, high, true);
	}

	private void testForSubMap(String mapName, NavigableMap<Long, Long> map, Long low, boolean includeLow, Long high, boolean includeHigh) {
		int diff = high > low ? 1 : -1;
		Long expHigh = includeHigh ? high : high - diff;
		Long expLow = includeLow ? low : low + diff;

		NavigableMap<Long, Long> subMap = map.subMap(low, includeLow, high, includeHigh);
		assertRange(mapName, subMap, expLow, expHigh);

		assertTrue(includeLow == subMap.containsKey(low));
		assertTrue(includeHigh == subMap.containsKey(high));

		assertFalse(subMap.containsKey(high + diff));
		assertFalse(subMap.containsKey(low - diff));
	}

	private void assertRange(String mapName, Map<Long, Long> map, Long low, Long high) {
		String s = "[" + mapName + "] ";

		Long ll = Math.min(high, low);
		Long hh = Math.max(high, low);

		for (Long l = ll; l <= hh; l++) {
			assertTrue(s + l + " not found in map", map.containsKey(l));
		}

		assertEquals(s + "Wrong map size", hh - ll + 1, map.size());
	}

	@Test
	public void testSubMapsWithNullHaveCorrectRange() {
		map.put(null, null);
		assertContainsNull("Normal with null", map, true);
		assertContainsNull("HeadMap with null", map.headMap(-1L), true);
		assertContainsNull("SubMap with null", map.subMap(null, 0L), true);

	}

	private void assertContainsNull(String mapName, Map<Long, Long> map, boolean ascending) {
		String s = "[" + mapName + "] ";
		assertTrue(s + " Should contain null", map.containsKey(null));
		if (ascending) {
			assertNull(s + " First element should be null", map.keySet().iterator().next());
		} else {
			assertEquals(s + " Last element should be null", null, getLast(map));
		}
	}

	private Long getLast(Map<Long, Long> map) {
		Long last = null;
		for (Iterator<Long> it = map.keySet().iterator(); it.hasNext();) {
			last = it.next();
		}
		return last;
	}

}
