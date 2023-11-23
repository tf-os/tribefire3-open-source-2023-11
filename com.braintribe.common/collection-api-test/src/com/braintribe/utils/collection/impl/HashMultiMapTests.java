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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.collection.impl.CollectionTestTools.longsForRange;
import static com.braintribe.utils.collection.impl.CollectionTestTools.longsForRangeRnd;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.utils.collection.api.MultiMap;

public class HashMultiMapTests extends AbstractMultiMapTests<MultiMap<Long, Long>> {

	@Before
	public void setUp() {
		multiMap = new HashMultiMap<>();
	}

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
	public void testKeySet() {
		Long[] keys = longsForRangeRnd(1, 1000, 200);
		addNewEntriesFor(keys);
		Set<Long> expectedKeySet = new HashSet<Long>(Arrays.asList(keys));
		assertEquals("Wrong keySet", expectedKeySet, multiMap.keySet());

		putAndCheckKeySet(expectedKeySet, null);
		putAndCheckKeySet(expectedKeySet, 1001L);
		putAndCheckKeySet(expectedKeySet, 1002L);
		putAndCheckKeySet(expectedKeySet, 1003L);
	}

	private void putAndCheckKeySet(Set<Long> expectedKeySet, Long key) {
		expectedKeySet.add(key);
		standardPut(key);

		assertEquals("Wrong keySet", expectedKeySet, multiMap.keySet());
	}

	@Test
	public void testRemovingWithIterator() {
		runRemovingForIterator(true);
		runRemovingForIterator(false);
	}

	private void runRemovingForIterator(boolean doWithValues) {
		addEntriesFor(longsForRange(1, 4096));
		for (int i = 1; i < 13; i++) {
			Set<Long> remainingValues = removeElementsWithOddIndices(doWithValues);
			assertEachElementDivisibleBy(1 << i, remainingValues);
		}
		assertEquals("Set should containt single element only by now", 1, multiMap.size());
		assertTrue("Set should containt 4096L", multiMap.containsKey(4096L));
		assertEquals("KeySet should only contain one element", 1, multiMap.keySet().size());
	}

	private void assertEachElementDivisibleBy(int divisor, Set<Long> remainingValues) {
		int expectedSize = 4096 / divisor;
		assertEquals("Wrong expected size", expectedSize, multiMap.size());
		for (Long value : remainingValues) {
			long key = CollectionTestTools.getStandardKeyForValue(value);
			assertTrue("Failer for divisor: " + divisor + ". Key not found: " + key, multiMap.containsKey(key));
		}

	}

	private Set<Long> removeElementsWithOddIndices(boolean doWithValues) {
		Iterator<?> it;
		if (doWithValues)
			it = multiMap.values().iterator();
		else
			it = multiMap.entrySet().iterator();

		Set<Long> result = new HashSet<Long>();
		boolean shouldRemove = false;
		while (it.hasNext()) {
			Long next;
			if (doWithValues)
				next = (Long) it.next();
			else
				next = ((Map.Entry<Long, Long>) it.next()).getValue();

			shouldRemove = !shouldRemove;
			if (shouldRemove)
				it.remove();
			else
				result.add(next);
		}

		return result;
	}

	// ###################################
	// ## . . . . . Multi . . . . . . . ##
	// ###################################

	@Test
	public void adding_Multi() {
		simpleAdd();
		multiMap.put(null, null);

		assertThat(multiMap.containsKeyValue(0L, null)).isTrue();
		assertThat(multiMap.containsKeyValue(1L, null)).isTrue();
		assertThat(multiMap.containsKeyValue(1L, 1L)).isTrue();
		assertThat(multiMap.containsKeyValue(1L, 2L)).isTrue();

		assertThat(multiMap).containsValues(null, 1L, 2L, 3L);
		assertThat(multiMap.values()).contains(null, 1L, 2L, 3L);

		assertThat(multiMap.keySet()).contains(null, 1L, 2L, 3L);
		assertThat(multiMap).containsKeys(null, 1L, 2L, 3L);
	}

	@Test
	public void addingReturnsRightValue() {
		assertThat(multiMap.put(1L, 2L)).isNull();
		assertThat(multiMap.put(1L, 2L)).isEqualTo(2L);
	}

	@Test
	public void contains_Multi() {
		simpleAdd();

		assertThat(multiMap.containsKeyValue(0L, null)).isTrue();
	}

	@Test
	public void retrieving_Multi() {
		simpleAdd();

		assertThat(multiMap.get(null)).isNull();
		assertThat(multiMap.get(1L)).isNull();
		assertThat(multiMap.get(2L)).isGreaterThanOrEqualTo(0L).isLessThanOrEqualTo(2L);
		assertThat(multiMap.get(3L)).isEqualTo(3L);
	}

	@Test
	public void retrievingAll_Multi() {
		simpleAdd();
		multiMap.put(4L, null);

		assertThat(multiMap.getAll(null)).isEmpty();
		assertThat(multiMap.getAll(1L)).hasSize(3).contains(null, 1L, 2L);
		assertThat(multiMap.getAll(2L)).hasSize(2).contains(1L, 2L);
		assertThat(multiMap.getAll(3L)).hasSize(1).contains(3L);
		assertThat(multiMap.getAll(4L)).hasSize(1).contains((Long) null);
	}

	@Test
	public void removingForKeyValue_Multi() {
		simpleAdd();
		multiMap.remove(0L, 0L); // no effect, but different use-case
		multiMap.remove(0L, null);
		multiMap.remove(1L, 5L); // no effect, but different use-case
		multiMap.remove(1L, 1L);
		multiMap.remove(1L, null);
		multiMap.remove(1L, 2L);
		assertValuesFor_1_WereRemoved();
	}

	@Test
	public void removingAllForkey_Multi() {
		simpleAdd();
		multiMap.removeAll(-1L); // no effect, but different use-case
		multiMap.removeAll(0L);
		multiMap.removeAll(1L);
		assertValuesFor_1_WereRemoved();
	}

	@Test
	public void removingWithIteratorMulti() {
		simpleAdd();

		Iterator<Entry<Long, Long>> it = multiMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Long, Long> e = it.next();
			if (e.getKey() < 2L) {
				it.remove();
			}
		}

		assertValuesFor_1_WereRemoved();
	}

	private void assertValuesFor_1_WereRemoved() {
		assertSize(3);
		assertContainsKeyValue(2L, 1L);
		assertContainsKeyValue(2L, 2L);
		assertContainsKeyValue(3L, 3L);

		assertThat(multiMap.containsKey(1L)).isFalse();
		assertThat(multiMap.containsKey(2L)).isTrue();
		assertThat(multiMap.containsKey(3L)).isTrue();
		assertThat(multiMap.keySet()).doesNotContain(1L);
		assertThat(multiMap.keySet()).contains(2L, 3L);

		assertThat(multiMap.entrySet()).hasSize(3);
	}

	@Test
	public void clearDirectly() {
		simpleAdd();
		multiMap.clear();
		assertIsEmpty();
	}

	@Test
	public void clearEntrySet() {
		simpleAdd();
		multiMap.entrySet().clear();
		assertIsEmpty();
	}

	@Test
	public void clearKeySet() {
		simpleAdd();
		multiMap.keySet().clear();
		assertIsEmpty();
	}

	@Test
	public void clearValues() {
		simpleAdd();
		multiMap.values().clear();
		assertIsEmpty();
	}

	private void assertIsEmpty() {
		assertThat(multiMap).isEmpty();
		assertThat(multiMap.keySet()).isEmpty();
		assertThat(multiMap.entrySet()).isEmpty();
	}

	private void simpleAdd() {
		multiMap.put(0L, null);
		multiMap.put(1L, null);
		multiMap.put(1L, 1L);
		multiMap.put(1l, 2L);
		multiMap.put(2L, 1L);
		multiMap.put(2L, 2L);
		multiMap.put(3L, 3L);
	}

}
