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

import static com.braintribe.utils.collection.impl.CollectionTestTools.getStandardValueForKey;
import static com.braintribe.utils.collection.impl.CollectionTestTools.longsForRange;
import static com.braintribe.utils.collection.impl.CollectionTestTools.longsForRangeRnd;
import static com.braintribe.utils.collection.impl.CollectionTestTools.orderToIteratorName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.braintribe.utils.collection.api.NavigableMultiMap;

/**
 * 
 */
public class NavigableMultiMap_AddRemove_Tests extends AbstractNavigableMultiMapTests {

	@Test
	public void advancedAdding() {
		standardAdd();
		standardContains();
	}

	@Test
	public void removing() {
		standardAdd();

		for (long i = 0; i < 200; i++) {
			assertTrue(remove(i, 100 * i));
			assertNotContains(i, 100 * i);
		}

		assertSize(240);
	}

	@Test
	public void removingAll() {
		standardAdd();

		for (long i = 0; i < 200; i++) {
			Collection<Long> removed = multiMap.removeAll2(i);
			assertThat(removed).hasSize(numberOfValuesForKey(i));
		}

		assertSize(1); // null -> 0

		multiMap.removeAll2(null);

		assertEmpty();
	}

	@Test
	public void keySet() {
		Long[] keys = longsForRangeRnd(1, 1000, 200);
		addNewEntriesFor(keys);
		Set<Long> expectedKeySet = new HashSet<Long>(Arrays.asList(keys));
		assertEquals("Wrong keySet", expectedKeySet, multiMap.keySet());

		putAndCheckKeySet(expectedKeySet, null);
		putAndCheckKeySet(expectedKeySet, 1001L);
		putAndCheckKeySet(expectedKeySet, 1002L);
		putAndCheckKeySet(expectedKeySet, 1003L);
	}

	@Test
	public void keySetForSubMap() {
		Long[] keys = longsForRangeRnd(1, 1000, 200);
		addNewEntriesFor(keys);
		NavigableSet<Long> expectedKeySet = new TreeSet<Long>(Arrays.asList(keys));
		assertEquals("Wrong keySet", expectedKeySet, multiMap.keySet());
		assertEquals("Wrong keySet", expectedKeySet.subSet(10L, 20L), multiMap.subMap(10L, true, 20L, false).keySet());
	}

	private void putAndCheckKeySet(Set<Long> expectedKeySet, Long key) {
		expectedKeySet.add(key);
		standardPut(key);

		assertEquals("Wrong keySet", expectedKeySet, multiMap.keySet());
	}

	@Test
	public void removeFromSubMap() {
		addNewEntriesFor(longsForRange(1, 50));
		NavigableMultiMap<Long, Long> subMap = multiMap.subMap(10L, false, 20L, true);
		assert_YES_Remove(subMap, 15L);
		assert_NOT_Remove(subMap, 15L);
		assert_NOT_Remove(subMap, 5L);
	}

	private void assert_YES_Remove(NavigableMultiMap<Long, Long> map, Long key) {
		assertTrue("Value should have been removed.", standardRemove(map, key));
	}

	private void assert_NOT_Remove(NavigableMultiMap<Long, Long> map, Long key) {
		assertFalse("Value should have been removed.", standardRemove(map, key));
	}

	private boolean standardRemove(NavigableMultiMap<Long, Long> map, Long key) {
		return map.remove(key, getStandardValueForKey(key));
	}

	@Test
	public void clear() {
		Long[] keys = longsForRange(1, 100);
		addNewEntriesFor(keys);
		multiMap.clear();
		assertSize(0);
		assertEmpty(multiMap);
		assertNotContains(keys);
	}

	@Test
	public void clearSubMap() {
		addNewEntriesFor(longsForRange(1, 100));

		NavigableMultiMap<Long, Long> subMap = multiMap.subMap(10L, true, 20L, false);
		testClearSubMap(subMap, 9L, 20L);
		assertSize(90);

		subMap = multiMap.subMap(29L, false, 39L, true).descendingMap();
		testClearSubMap(subMap, 29L, 40L);
		assertSize(80);
	}

	private void testClearSubMap(NavigableMultiMap<Long, Long> subMap, long lastValueBeforeGap, long firstValueAfterGap) {
		subMap.clear();
		assertEmpty(subMap);

		assertNotContains(longsForRange(lastValueBeforeGap + 1, firstValueAfterGap - 1));
		assertContains(lastValueBeforeGap, firstValueAfterGap);
	}

	private void assertEmpty(NavigableMultiMap<Long, Long> multiMap) {
		assertEquals("Set should be empty", 0, multiMap.size());
		assertTrue("Set should be empty", multiMap.isEmpty());
		assertTrue("Set should be empty", multiMap.entrySet().isEmpty());
		assertTrue("KeySet should be empty", multiMap.keySet().isEmpty());
		assertTrue("values() should be empty", multiMap.values().isEmpty());
		assertTrue("Set should be empty", multiMap.descendingKeySet().isEmpty());
	}

	@Test
	public void pollFirst() {
		addNewEntriesFor(longsForRange(1, 100));

		assertPollFirst(multiMap, 1L);
		assertPollFirst(multiMap.descendingMap(), 100L);

		NavigableMultiMap<Long, Long> subMap = multiMap.subMap(9L, false, 21L, false);
		assertPollFirst(subMap, 10L);
		assertPollFirst(subMap.descendingMap(), 20L);

		multiMap.put(null, null);
		assertPollFirst(multiMap, null);
	}

	private void assertPollFirst(NavigableMultiMap<Long, Long> multiMap, Long key) {
		Entry<Long, Long> first = multiMap.pollFirstEntry();
		assertEquals("Wrong pollFirst key", key, first.getKey());
		assertEquals("Wrong pollFirst value", getStandardValueForKey(key), first.getValue());
		assertNotContains(key);
	}

	@Test
	public void pollLast() {
		addNewEntriesFor(longsForRange(1, 100));
		assertPollLast(multiMap, 100L);
		assertPollLast(multiMap.descendingMap(), 1L);

		NavigableMultiMap<Long, Long> subMap = multiMap.subMap(9L, false, 21L, false);
		assertPollLast(subMap, 20L);
		assertPollLast(subMap.descendingMap(), 10L);

		multiMap.put(null, null);
		assertPollLast(multiMap.descendingMap(), null);
	}

	@Test
	public void pollFromEmptySet() {
		addNewEntriesFor(longsForRange(1, 30));
		assertPollReturnsNull(multiMap.tailMap(20L, true));
		assertPollReturnsNull(multiMap.headMap(10L, false).descendingMap());
		assertPollReturnsNull(multiMap);
	}

	private void assertPollReturnsNull(NavigableMultiMap<Long, Long> map) {
		map.clear();
		assertNull(map.pollFirstEntry());
		assertNull(map.pollLastEntry());
	}

	private void assertPollLast(NavigableMultiMap<Long, Long> multiMap, Long key) {
		Entry<Long, Long> last = multiMap.pollLastEntry();
		assertEquals("Wrong pollLast key", key, last.getKey());
		assertEquals("Wrong pollLast value", getStandardValueForKey(key), last.getValue());
		assertNotContains(key);
	}

	@Test
	public void removingWithIterator() {
		boolean doWithValues = false;
		for (int i = 0; i < 2; i++) {
			runRemovingForIterator(false, true, doWithValues);
			runRemovingForIterator(false, false, doWithValues);
			runRemovingForIterator(true, true, doWithValues);
			runRemovingForIterator(true, false, doWithValues);
			doWithValues = true;
		}
	}

	private void runRemovingForIterator(boolean subMap, boolean ascending, boolean doWithValues) {
		String iteratorName = orderToIteratorName(ascending) + (subMap ? "[SubMap]" : "");
		addEntriesFor(longsForRange(1, 4096));
		for (int i = 1; i < 13; i++) {
			removeElementsWithOddIndices(subMap, ascending, doWithValues);
			assertEachElementDivisibleBy(1 << i, iteratorName);
		}
		assertEquals("[" + iteratorName + "] Set should containt single element only by now", 1, multiMap.size());
		assertTrue("[" + iteratorName + "] Set should containt 4096L", multiMap.containsKey(4096L));
	}

	private void assertEachElementDivisibleBy(int divisor, String name) {
		int expectedSize = 4096 / divisor;
		assertEquals("Wrong expected size", expectedSize, multiMap.size());
		for (long i = 1; i <= expectedSize; i += divisor) {
			long value = i * divisor;
			assertTrue("[" + name + "] Failer for divisor: " + divisor + ". Value not found: " + value, multiMap.containsKey(value));
		}
	}

	private void removeElementsWithOddIndices(boolean subMap, boolean ascending, boolean doWithValues) {
		NavigableMultiMap<Long, Long> s = subMap ? multiMap.subMap(-1L, true, 5000L, false) : multiMap;
		Iterator<?> it;
		if (doWithValues)
			it = ascending ? s.values().iterator() : s.descendingMap().values().iterator();
		else
			it = ascending ? s.entrySet().iterator() : s.descendingMap().entrySet().iterator();

		boolean shouldRemove = !ascending;
		while (it.hasNext()) {
			it.next();
			shouldRemove = !shouldRemove;
			if (shouldRemove)
				it.remove();
		}
	}

}
