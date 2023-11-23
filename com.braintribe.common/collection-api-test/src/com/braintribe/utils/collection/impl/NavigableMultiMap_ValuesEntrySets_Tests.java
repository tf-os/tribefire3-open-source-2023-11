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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

/**
 * 
 */
public class NavigableMultiMap_ValuesEntrySets_Tests extends AbstractNavigableMultiMapTests {

	private static final long MAX = 100L;
	private static final Map<Long, Long> m = new HashMap<Long, Long>();
	private static final Long[] keys = new Long[(int) MAX];

	static {
		m.put(null, null);
		int counter = 0;
		for (long l = 1l; l <= MAX; l++) {
			m.put(l, getStandardValueForKey(l));
			keys[counter++] = l;
		}
	}

	@Test
	public void testPutAllAndContainsValue() {
		multiMap.putAll(m);
		assertContains();
	}

	private void assertContains() {
		assertContains(keys);
		assertContainsValues();
	}

	private void assertContainsValues() {
		assertTrue(multiMap.containsValue(getStandardValueForKey(null)));
		for (Long l: keys) {
			assertTrue(multiMap.containsValue(getStandardValueForKey(l)));
		}
	}

	@Test
	public void testValueIterator() {
		multiMap.putAll(m);
		Iterator<Long> it = multiMap.values().iterator();
		boolean wasNull = false;
		while (it.hasNext()) {
			Long next = it.next();
			if (next != null) {
				if (next % 5 == 0) {
					it.remove();
				}
			} else {
				wasNull = true;
			}
		}
		assertContainsNonFiveMultiples();
		assertTrue("Null was not listed as value", wasNull);

	}

	@Test
	public void testValuesRemoveIterator() {
		multiMap.putAll(m);
		Collection<Long> values = multiMap.values();
		assertTrue(values.contains(null));
		for (Long l: keys) {
			if (l % 5 == 0) {
				values.remove(getStandardValueForKey(l));
			}
		}

		assertContainsNonFiveMultiples();
	}

	private void assertContainsNonFiveMultiples() {
		for (long l = 1l; l < 100l; l++) {
			assertEquals("Wrong 'contains' result for: " + l, l % 5 != 0, multiMap.containsKey(l));
			assertEquals("Wrong 'contains' result for: " + l, l % 5 != 0, multiMap.values().contains(getStandardValueForKey(l)));
		}
	}

	@Test
	public void testEntrySet() {
		multiMap.putAll(m);
		Set<Entry<Long, Long>> entrySet = multiMap.entrySet();
		for (Entry<Long, Long> entry: entrySet) {
			assertTrue(entrySet.contains(entry));
		}
		Entry<Long, Long> firstEntry = entrySet.iterator().next();
		entrySet.remove(firstEntry);
		assertFalse(entrySet.contains(firstEntry));

		entrySet.clear();
		assertSize(0);
	}

	@Test
	public void testEntrySetOfSubMap() {
		multiMap.putAll(m);
		Set<Entry<Long, Long>> entrySet = multiMap.subMap(10L, true, 80L, false).entrySet();
		for (Entry<Long, Long> entry: entrySet) {
			assertTrue(entrySet.contains(entry));
		}
		Entry<Long, Long> firstEntry = entrySet.iterator().next();
		assertEquals("Wrong first entry key", (Long) 10L, firstEntry.getKey());
		entrySet.remove(firstEntry);
		assertFalse(entrySet.contains(firstEntry));

		entrySet.clear();
		assertSize(31); // 100 - (80-10) + 1 (null)
	}

}
