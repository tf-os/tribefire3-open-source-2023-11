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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.utils.collection.api.NavigableMultiMap;
import com.braintribe.utils.collection.impl.compare.ComparableComparator;

/**
 * 
 */
public class NavigableMultiMap_HighLowCeilFloor_Tests extends AbstractNavigableMultiMapTests {

	private boolean mapInitialized;
	private NavigableMultiMap<Long, Long> emptyMap;
	private NavigableMultiMap<Long, Long> descendingMultiMap;
	private NavigableMultiMap<Long, Long> subMap;
	private NavigableMultiMap<Long, Long> descendingSubMap;
	private NavigableSet<Long> keySet;
	private NavigableSet<Long> descendingKeySet;

	private static Long MAX = 1000L;

	private static Long SUBSET_MIN = 40L;
	private static Long SUBSET_MAX = 110L;

	@Override
	@Before
	public void setUp() throws Exception {
		if (!mapInitialized) {
			super.setUp();
			initContent();

			ComparableComparator<Long> lc = ComparableComparator.instance();
			emptyMap = new ComparatorBasedNavigableMultiMap<>(lc, lc);

			descendingMultiMap = multiMap.descendingMap();
			subMap = multiMap.subMap(SUBSET_MIN, 100 * SUBSET_MIN, true, SUBSET_MAX, 100 * SUBSET_MAX, true);
			descendingSubMap = subMap.descendingMap();
			keySet = multiMap.keySet();
			descendingKeySet = keySet.descendingSet();

			mapInitialized = true;
		}
	}

	protected void initContent() {
		put(null, 0L);
		for (long i = 0; i < 501; i++) {
			put(2 * i, 190 * i);
			put(2 * i, 200 * i);
		}
	}

	@Test
	public void testFirstKey_LastKey() {
		assertEquals("Wrong first", (Long) null, multiMap.firstKey());
		assertEquals("Wrong last", MAX, multiMap.lastKey());

		assertEquals("Wrong first", null, keySet.first());
		assertEquals("Wrong last", MAX, keySet.last());

		assertEquals("Wrong first", MAX, descendingMultiMap.firstKey());
		assertEquals("Wrong last", (Long) null, descendingMultiMap.lastKey());

		assertEquals("Wrong first", MAX, descendingKeySet.first());
		assertEquals("Wrong last", (Long) null, descendingKeySet.last());

		assertEquals("Wrong subset first", SUBSET_MIN, subMap.firstKey());
		assertEquals("Wrong subset last", SUBSET_MAX, subMap.lastKey());

		assertEquals("Wrong descending-subset first", SUBSET_MAX, descendingSubMap.firstKey());
		assertEquals("Wrong descending-subset last", SUBSET_MIN, descendingSubMap.lastKey());
	}

	@Test
	public void testFirst_Last() {
		assertEntryForKey("Wrong first", null, multiMap.firstEntry());
		assertEntryForKey("Wrong last", MAX, multiMap.lastEntry());

		assertEntryForKey("Wrong first", MAX, descendingMultiMap.firstEntry());
		assertEntryForKey("Wrong last", null, descendingMultiMap.lastEntry());

		assertEntryForKey("Wrong subset first", SUBSET_MIN, subMap.firstEntry());
		assertEntryForKey("Wrong subset last", SUBSET_MAX, subMap.lastEntry());

		assertEntryForKey("Wrong descending-subset first", SUBSET_MAX, descendingSubMap.firstEntry());
		assertEntryForKey("Wrong descending-subset last", SUBSET_MIN, descendingSubMap.lastEntry());
	}

	private void assertEntryForKey(String string, Long key, Entry<Long, Long> entry) {
		Long value = key == null ? 0 : 100 * key;
		assertEquals(string + " key", key, entry.getKey());
		assertEquals(string + " value", value, entry.getValue());
	}

	@Test
	public void testAllWithEmptyMap() {
		runEmptySetTestFor("empty set", emptyMap);

		runEmptySetTestFor("empty SubSet", multiMap.subMap(10000L, true, 10050L, false));

		runEmptySetTestFor("empty descending SubSet", multiMap.subMap(10000L, true, 10050L, false).descendingMap());
	}

	private void runEmptySetTestFor(String setName, NavigableMultiMap<Long, Long> multiMap) {
		String s = " for [" + setName + "] should be null";
		tryFirstLast(setName, multiMap);
		assertNull("Lower" + s, multiMap.lowerKey(10L));
		assertNull("LowerEntry" + s, multiMap.lowerEntry(10L));
		assertNull("Higher" + s, multiMap.higherKey(10L));
		assertNull("HigherEntry" + s, multiMap.higherEntry(10L));
		assertNull("Floor" + s, multiMap.floorKey(10L));
		assertNull("FloorEntry" + s, multiMap.floorEntry(10L));
		assertNull("Ceiling" + s, multiMap.ceilingKey(10L));
		assertNull("CeilingEnry" + s, multiMap.ceilingEntry(10L));
	}

	private void tryFirstLast(String setName, NavigableMultiMap<Long, Long> multiMap) {
		try {
			multiMap.firstKey();
			fail("first() on [" + setName + "] should throw Exception");
		} catch (NoSuchElementException e) {
			// NO OP
		}
		try {
			multiMap.lastKey();
			fail("last() on empty set should throw Exception");
		} catch (NoSuchElementException e) {
			// NO OP
		}
	}

	@Test
	public void testLower() {
		assertEquals("Wrong lower", null, multiMap.lowerKey(0L));
		assertEquals("Wrong lower", null, keySet.lower(0L));
		assertEquals("Wrong lower", null, multiMap.lowerEntry(null, null));
		assertLower(0L, null);
		for (long l = 1; l <= 1002; l++) {
			Long expectedLower = (l % 2 == 0) ? l - 2 : l - 1;
			assertLower(l, expectedLower);
		}
	}

	private void assertLower(Long num, Long expected) {
		assertEquals("Wrong lower for: " + num, expected, multiMap.lowerKey(num));
		assertEquals("Wrong lower for: " + num, expected, keySet.lower(num));
		assertEquals("Wrong descending higher for: " + num, expected, descendingMultiMap.higherKey(num));
		assertEquals("Wrong descending higher for: " + num, expected, descendingKeySet.higher(num));
		if (isInSubMap(num)) {
			expected = num.equals(SUBSET_MIN) ? null : expected;
			assertEquals("Wrong lower for: " + num, expected, subMap.lowerKey(num));
			assertEquals("Wrong descending higher for: " + num, expected, descendingSubMap.higherKey(num));
		}
	}

	@Test
	public void testFloor() {
		assertFloor(-1L, null);
		for (long l = 0; l < 1002; l++) {
			Long expectedFloor = (l % 2 == 0) ? l : l - 1;
			assertFloor(l, expectedFloor);
		}
		assertFloor(1002L, 1000L);
	}

	private void assertFloor(Long num, Long expected) {
		assertEquals("Wrong floor for: " + num, expected, multiMap.floorKey(num));
		assertEquals("Wrong floor for: " + num, expected, keySet.floor(num));
		assertEquals("Wrong descending ceiling for: " + num, expected, descendingMultiMap.ceilingKey(num));
		assertEquals("Wrong descending ceiling for: " + num, expected, descendingKeySet.ceiling(num));
		if (isInSubMap(num)) {
			assertEquals("Wrong floor for: " + num, expected, subMap.floorKey(num));
			assertEquals("Wrong descending ceiling for: " + num, expected, descendingSubMap.ceilingKey(num));
		}
	}

	@Test
	public void testHigher() {
		assertHigher(1000L, null);
		assertHigher(1001L, null);
		for (long l = -2; l < 1000; l++) {
			Long expectedHigher = (l % 2 == 0) ? l + 2 : l + 1;
			assertHigher(l, expectedHigher);
		}
	}

	private void assertHigher(Long num, Long expected) {
		assertEquals("Wrong higher for: " + num, expected, multiMap.higherKey(num));
		assertEquals("Wrong higher for: " + num, expected, keySet.higher(num));
		assertEquals("Wrong descending lower for: " + num, expected, descendingMultiMap.lowerKey(num));
		assertEquals("Wrong descending lower for: " + num, expected, descendingKeySet.lower(num));
		if (isInSubMap(num)) {
			expected = num.equals(SUBSET_MAX) ? null : expected;
			assertEquals("Wrong higher for: " + num, expected, subMap.higherKey(num));
			assertEquals("Wrong descending lower for: " + num, expected, descendingSubMap.lowerKey(num));
		}
	}

	@Test
	public void testCeiling() {
		assertCeiling(1001L, null);
		assertCeiling(1002L, null);
		// for (long l = -1; l <= 1000; l++) {
		for (long l = 40; l <= 1000; l++) {
			Long expectedCeiling = (l % 2 == 0) ? l : l + 1;
			assertCeiling(l, expectedCeiling);
		}
	}

	private void assertCeiling(Long num, Long expected) {
		assertEquals("Wrong ceiling for: " + num, expected, multiMap.ceilingKey(num));
		assertEquals("Wrong ceiling for: " + num, expected, keySet.ceiling(num));
		assertEquals("Wrong descending floor for: " + num, expected, descendingMultiMap.floorKey(num));
		assertEquals("Wrong descending floor for: " + num, expected, descendingKeySet.floor(num));
		if (isInSubMap(num)) {
			assertEquals("Wrong ceiling for: " + num, expected, subMap.ceilingKey(num));
			assertEquals("Wrong descending floor for: " + num, expected, descendingSubMap.floorKey(num));
		}
	}

	@Test
	public void testSubSetFloorCeiling() {
		assertEquals("Wrong floor for: " + (SUBSET_MAX + 10), SUBSET_MAX, subMap.lowerKey(SUBSET_MAX + 10));
		assertEquals("Wrong floor for: " + (SUBSET_MAX + 10), SUBSET_MAX, subMap.floorKey(SUBSET_MAX + 10));

		assertEquals("Wrong floor for: " + (SUBSET_MIN - 10), SUBSET_MIN, subMap.higherKey(SUBSET_MIN - 10));
		assertEquals("Wrong floor for: " + (SUBSET_MIN - 10), SUBSET_MIN, subMap.ceilingKey(SUBSET_MIN - 10));

		assertEquals("Wrong floor for: " + (SUBSET_MIN - 1), null, subMap.floorKey(SUBSET_MIN - 1));
		assertEquals("Wrong ceiling for: " + (SUBSET_MIN - 1), null, descendingSubMap.ceilingKey(SUBSET_MIN - 1));

		assertEquals("Wrong ceiling for: " + (SUBSET_MAX + 1), null, subMap.ceilingKey(SUBSET_MAX + 1));
		assertEquals("Wrong floor for: " + (SUBSET_MAX + 1), null, descendingSubMap.floorKey(SUBSET_MAX + 1));

	}

	@Test
	public void testLowest() {
		Assertions.assertThat(multiMap.getLowest(2L)).isEqualTo(190L);
		Assertions.assertThat(subMap.getLowest(2L)).isNull();
		Assertions.assertThat(subMap.getLowest(60L)).isEqualTo(190 * 30L);
		Assertions.assertThat(subMap.getLowest(160L)).isNull();
	}

	@Test
	public void testHighest() {
		Assertions.assertThat(multiMap.getHighest(2L)).isEqualTo(200L);
		Assertions.assertThat(subMap.getHighest(2L)).isNull();
		Assertions.assertThat(subMap.getHighest(60L)).isEqualTo(200 * 30L);
		Assertions.assertThat(subMap.getHighest(160L)).isNull();
	}

	private boolean isInSubMap(Long num) {
		return num != null && num >= SUBSET_MIN && num <= SUBSET_MAX;
	}

}
