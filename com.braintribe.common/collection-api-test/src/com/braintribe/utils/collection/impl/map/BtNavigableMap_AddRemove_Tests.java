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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.utils.collection.impl.BtNavigableMap;

/**
 * @deprecated {@link BtNavigableMap} was only created for GWT compatibility. GWT now supports {@link TreeMap}, so use that!!!
 */
@Deprecated
public class BtNavigableMap_AddRemove_Tests extends BtNavigableMap_HighLevelTestBase {

	@Override
	@Before
	public void setUp() {
		super.setUp();
	}

	@Test
	public void testAddNull() {
		standardPut(null);
		assertSize(1);
		assertContains((Long) null);
	}

	@Test
	public void testRemoveNull() {
		standardPut(null);
		standardRemove(null);
		assertSize(0);
		assertNotContains((Long) null);
	}

	@Test
	public void testAddSingleValue() {
		standardPut(10L);
		assertSize(1);
		assertContains(10L);
	}

	@Test
	public void testRemoveSingleValue() {
		standardPut(10L);
		remove(10L);
		assertSize(0);
		assertNotContains(10L);
	}

	@Test
	public void testAddSomeValues() {
		runAddTestFor(10L, 5L, 7L, null, 8L, 45L, 2L);
		assertNotContains(3L, 9L);
	}

	@Test
	public void testRemoveSomeValues() {
		runRemoveTestFor(10L, 5L, 7L, null, 8L, 45L, 2L);
	}

	@Test
	public void testAddSameValueMultipleTimes() {
		addNewEntriesFor(1L, 2L, 3L);
		assertSize(3);
		addExistingEntriesFor(3L, 3L, 3L, 3L, 2L, 1L);
		addNewEntriesFor(4L, 5L);
		assertSize(5);
		addExistingEntriesFor(4L, 5L);
		addNewEntriesFor((Long) null);
		assertSize(6);
		addExistingEntriesFor(null, null, null);
		assertSize(6);
	}

	@Test
	public void testAddingAndRemovingManyInMixedOrder() {
		runRemoveTestFor(longsForRangeRnd(1, 10000, 200));
	}

	@Test
	public void testKeySet() {
		Long[] keys = longsForRangeRnd(1, 1000, 200);
		addNewEntriesFor(keys);
		Set<Long> expectedKeySet = new HashSet<Long>(Arrays.asList(keys));
		assertEquals("Wrong keySet", expectedKeySet, map.keySet());

		putAndCheckKeySet(expectedKeySet, null);
		putAndCheckKeySet(expectedKeySet, 1001L);
		putAndCheckKeySet(expectedKeySet, 1002L);
		putAndCheckKeySet(expectedKeySet, 1003L);
	}

	@Test
	public void testKeySetForSubMap() {
		Long[] keys = longsForRangeRnd(1, 1000, 200);
		addNewEntriesFor(keys);
		NavigableSet<Long> expectedKeySet = new TreeSet<Long>(Arrays.asList(keys));
		assertEquals("Wrong keySet", expectedKeySet, map.keySet());
		assertEquals("Wrong keySet", expectedKeySet.subSet(10L, 20L), map.subMap(10L, 20L).keySet());
	}

	private void putAndCheckKeySet(Set<Long> expectedKeySet, Long key) {
		expectedKeySet.add(key);
		standardPut(key);

		assertEquals("Wrong keySet", expectedKeySet, map.keySet());
	}

	@Test
	public void testRemoveFromSubMap() {
		runAddTestFor(longsForRange(1, 50));
		NavigableMap<Long, Long> subMap = map.subMap(10L, false, 20L, true);
		assert_YES_Remove(subMap, 15L);
		assert_NOT_Remove(subMap, 15L);
		assert_NOT_Remove(subMap, 5L);
	}

	private void assert_YES_Remove(Map<Long, Long> map, Long key) {
		assertEquals("Value should have been removed.", getStandardValueForKey(key), map.remove(key));
	}

	private void assert_NOT_Remove(Map<Long, Long> map, Long key) {
		assertNull("Value should have been removed.", map.remove(key));
	}

	@Test
	public void testClear() {
		Long[] keys = longsForRange(1, 1);
		addNewEntriesFor(keys);
		map.clear();
		assertSize(0);
		assertEmpty(map);
		assertNotContains(keys);
		// testing if map is in consistent state and ready to be used again
		runRemoveTestFor(keys);
	}

	@Test
	public void testClearSubMap() {
		addNewEntriesFor(longsForRange(1, 100));

		NavigableMap<Long, Long> subMap = map.subMap(10L, 20L);
		testClearSubMap(subMap, 9L, 20L);
		assertSize(90);
	}

	private void testClearSubMap(NavigableMap<Long, Long> subMap, long lastValueBeforeGap, long firstValueAfterGap) {
		subMap.clear();
		assertEmpty(subMap);

		assertNotContains(longsForRange(lastValueBeforeGap + 1, firstValueAfterGap - 1));
		assertContains(lastValueBeforeGap, firstValueAfterGap);
	}

	private void assertEmpty(NavigableMap<Long, Long> map) {
		assertEquals("Set should be empty", 0, map.size());
		assertTrue("Set should be empty", map.isEmpty());
		assertTrue("Set should be empty", map.entrySet().isEmpty());
		assertTrue("KeySet should be empty", map.keySet().isEmpty());
		assertTrue("values() should be empty", map.values().isEmpty());
	}

	@Test
	public void testPollFirst() {
		runAddTestFor(longsForRange(1, 100));

		assertPollFirst(map, 1L);

		NavigableMap<Long, Long> subMap = map.subMap(9L, false, 21L, false);
		assertPollFirst(subMap, 10L);

		map.put(null, null);
		assertPollFirst(map, null);
	}

	private void assertPollFirst(NavigableMap<Long, Long> map, Long key) {
		Entry<Long, Long> first = map.pollFirstEntry();
		assertEquals("Wrong pollFirst key", key, first.getKey());
		assertEquals("Wrong pollFirst value", getStandardValueForKey(key), first.getValue());
		assertNotContains(key);
	}

	@Test
	public void testPollLast() {
		runAddTestFor(longsForRange(1, 100));
		assertPollLast(map, 100L);

		NavigableMap<Long, Long> subMap = map.subMap(9L, false, 21L, false);
		assertPollLast(subMap, 20L);
	}

	@Test
	public void testPollFromEmptySet() {
		runAddTestFor(longsForRange(1, 30));
		assertPollReturnsNull(map.tailMap(20L));
		assertPollReturnsNull(map);
	}

	private void assertPollReturnsNull(NavigableMap<Long, Long> map) {
		map.clear();
		assertNull(map.pollFirstEntry());
		assertNull(map.pollLastEntry());
	}

	private void assertPollLast(NavigableMap<Long, Long> map, Long key) {
		Entry<Long, Long> last = map.pollLastEntry();
		assertEquals("Wrong pollLast key", key, last.getKey());
		assertEquals("Wrong pollLast value", getStandardValueForKey(key), last.getValue());
		assertNotContains(key);
	}

	@Test
	public void testRemovingWithIterator() {
		boolean doWithValues = false;
		for (int i = 0; i < 2; i++) {
			runRemovingForIterator(false, doWithValues);
			runRemovingForIterator(true, doWithValues);
			doWithValues = true;
		}
	}

	private void runRemovingForIterator(boolean subMap, boolean doWithValues) {
		String iteratorName = "Iterator" + (subMap ? "[SubMap]" : "");
		addEntriesFor(longsForRange(1, 4096));
		for (int i = 1; i < 13; i++) {
			removeElementsWithOddIndices(subMap, doWithValues);
			assertEachElementDivisibleBy(1 << i, iteratorName);
		}
		assertEquals("[" + iteratorName + "] Set should containt single element only by now", 1, map.size());
		assertTrue("[" + iteratorName + "] Set should containt 4096L", map.containsKey(4096L));
	}

	private void assertEachElementDivisibleBy(int divisor, String name) {
		int expectedSize = 4096 / divisor;
		assertEquals("Wrong expected size", expectedSize, map.size());
		for (long i = 1; i <= expectedSize; i += divisor) {
			long value = i * divisor;
			assertTrue("[" + name + "] Failer for divisor: " + divisor + ". Value not found: " + value, map.containsKey(value));
		}
	}

	private void removeElementsWithOddIndices(boolean subMap, boolean doWithValues) {
		NavigableMap<Long, Long> s = subMap ? map.subMap(-1L, 5000L) : map;
		Iterator<?> it;
		if (doWithValues) {
			it = s.values().iterator();
		} else {
			it = s.entrySet().iterator();
		}
		boolean shouldRemove = false;
		while (it.hasNext()) {
			it.next();
			shouldRemove = !shouldRemove;
			if (shouldRemove)
				it.remove();
		}
	}
}
