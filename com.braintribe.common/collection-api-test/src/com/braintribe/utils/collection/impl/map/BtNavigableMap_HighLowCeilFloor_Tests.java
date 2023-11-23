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
import static org.junit.Assert.fail;

import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.utils.collection.impl.BtNavigableMap;
import com.braintribe.utils.collection.impl.ComparableComparator;

/**
 * @deprecated {@link BtNavigableMap} was only created for GWT compatibility. GWT now supports {@link TreeMap}, so use that!!!
 */
@Deprecated
public class BtNavigableMap_HighLowCeilFloor_Tests extends BtNavigableMap_HighLevelTestBase {

	private boolean setInitialized;
	private NavigableMap<Long, Long> emptyMap;
	private NavigableMap<Long, Long> subMap;

	private static Long SUBSET_MIN = 100L;
	private static Long SUBSET_MAX = 900L;

	@Override
	@Before
	public void setUp() {
		if (!setInitialized) {
			super.setUp();
			insertEvenNumberFromOneToThousand();
			subMap = map.subMap(SUBSET_MIN, true, SUBSET_MAX, true);
			emptyMap = new BtNavigableMap<Long, Long>(ComparableComparator.<Long> instance());
			setInitialized = true;
		}
	}

	private void insertEvenNumberFromOneToThousand() {
		addNewEntriesFor(longsSeries(0, 501, 2));
	}

	@Test
	public void testFirst_Last() {
		assertEquals("Wrong first", (Long) 0L, map.firstKey());
		assertEquals("Wrong last", (Long) 1000L, map.lastKey());

		assertEquals("Wrong subset first", SUBSET_MIN, subMap.firstKey());
		assertEquals("Wrong subset last", SUBSET_MAX, subMap.lastKey());
	}

	@Test
	public void testAllWithEmptyMap() {
		runEmptyMapTestFor("empty map", emptyMap);
		runEmptyMapTestFor("empty SubMap", map.subMap(10000L, 10050L));
	}

	private void runEmptyMapTestFor(String mapName, NavigableMap<Long, Long> map) {
		String s = " for [" + mapName + "] should be null";
		tryFirstLast(mapName, map);
		assertNull("Lower" + s, map.lowerKey(10000L));
		assertNull("Higher" + s, map.higherKey(10000L));
		assertNull("Floor" + s, map.floorKey(10000L));
		assertNull("Ceiling" + s, map.ceilingKey(10000L));

		assertNull("Lower" + s, map.lowerKey(10050L));
		assertNull("Higher" + s, map.higherKey(10050L));
		assertNull("Floor" + s, map.floorKey(10050L));
		assertNull("Ceiling" + s, map.ceilingKey(10050L));
	}

	private void tryFirstLast(String mapName, NavigableMap<Long, Long> map) {
		try {
			map.firstKey();
			fail("first() on [" + mapName + "] should throw Exception");
		} catch (NoSuchElementException e) {
			// NO OP
		}
		try {
			map.lastKey();
			fail("last() on [" + mapName + " should throw Exception");
		} catch (NoSuchElementException e) {
			// NO OP
		}
	}

	@Test
	public void testLower() {
		assertEquals("Wrong lower", null, map.lowerKey(0L));
		assertLower(0L, null);
		for (long l = 1; l <= 1002; l++) {
			Long expectedLower = (l % 2 == 0) ? l - 2 : l - 1;
			assertLower(l, expectedLower);
		}
	}

	private void assertLower(Long num, Long expected) {
		assertEquals("Wrong lower for: " + num, expected, map.lowerKey(num));
		if (isInSubMap(num)) {
			expected = num.equals(SUBSET_MIN) ? null : expected;
			assertEquals("Wrong lower for: " + num, expected, subMap.lowerKey(num));
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
		assertEquals("Wrong floor for: " + num, expected, map.floorKey(num));
		if (isInSubMap(num)) {
			assertEquals("Wrong floor for: " + num, expected, subMap.floorKey(num));
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
		assertEquals("Wrong higher for: " + num, expected, map.higherKey(num));
		if (isInSubMap(num)) {
			expected = num.equals(SUBSET_MAX) ? null : expected;
			assertEquals("Wrong higher for: " + num, expected, subMap.higherKey(num));
		}
	}

	@Test
	public void testCeiling() {
		assertCeiling(1001L, null);
		assertCeiling(1002L, null);
		for (long l = -1; l <= 1000; l++) {
			Long expectedCeiling = (l % 2 == 0) ? l : l + 1;
			assertCeiling(l, expectedCeiling);
		}
	}

	private void assertCeiling(Long num, Long expected) {
		assertEquals("Wrong ceiling for: " + num, expected, map.ceilingKey(num));
		if (isInSubMap(num)) {
			assertEquals("Wrong ceiling for: " + num, expected, subMap.ceilingKey(num));
		}
	}

	@Test
	public void testSubMapFloorCeiling() {
		assertEquals("Wrong lower for: " + (SUBSET_MAX + 10), SUBSET_MAX, subMap.lowerKey(SUBSET_MAX + 10));
		assertEquals("Wrong floor for: " + (SUBSET_MAX + 10), SUBSET_MAX, subMap.floorKey(SUBSET_MAX + 10));

		assertEquals("Wrong higher for: " + (SUBSET_MIN - 10), SUBSET_MIN, subMap.higherKey(SUBSET_MIN - 10));
		assertEquals("Wrong ceiling for: " + (SUBSET_MIN - 10), SUBSET_MIN, subMap.ceilingKey(SUBSET_MIN - 10));

		assertEquals("Wrong floor for: " + (SUBSET_MIN - 1), null, subMap.floorKey(SUBSET_MIN - 1));

		assertEquals("Wrong ceiling for: " + (SUBSET_MAX + 1), null, subMap.ceilingKey(SUBSET_MAX + 1));

	}

	private boolean isInSubMap(Long num) {
		return num != null && num >= SUBSET_MIN && num <= SUBSET_MAX;
	}

}
