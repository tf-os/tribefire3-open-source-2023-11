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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import com.braintribe.utils.collection.impl.BtNavigableMap;

import static org.junit.Assert.*;

/**
 * @deprecated {@link BtNavigableMap} was only created for GWT compatibility. GWT now supports {@link TreeMap}, so use that!!!
 */
@Deprecated
public abstract class BtNavigableMap_HighLevelTestBase extends BtNavigableMapTestBase {

	protected void runRemoveTestFor(Long... keys) {
		runAddTestFor(keys);
		removeContainingValues(permutation(keys, 555));
		assertSize(0);
	}

	private void removeContainingValues(Long[] keys) {
		for (Long key: keys) {
			assertContainsKey(key);
			if (key == null) {
				assertNull(map.remove(key));
			} else {
				assertNotNull(map.remove(key));
			}
			assertNull(map.remove(key));
			assertNotContainsKey(key);
		}
	}

	protected void runAddTestFor(Long... keys) {
		addNewEntriesFor(keys);
		assertSize(keys.length);
		assertContains(keys);
		assertIteratorsListAllValues(keys);
	}

	protected void addNewEntriesFor(Long... keys) {
		for (Long key: keys) {
			assertNull(standardPut(key));
		}
	}

	protected void addEntriesFor(Long... keys) {
		for (Long key: keys) {
			standardPut(key);
		}
	}

	protected void addExistingEntriesFor(Long... keys) {
		for (Long key: keys) {
			Long expectedReturn = getStandardValueForKey(key);
			assertEquals("Wrong value returned", expectedReturn, standardPut(key));
		}
	}

	protected void assertIteratorsListAllValues(Long[] values) {
		Set<Long> expectedValues = new HashSet<Long>(Arrays.asList(values));
		checkIterator(expectedValues, map.keySet().iterator(), true);
	}

	private void checkIterator(Set<Long> expectedValues, Iterator<Long> iterator, boolean ascending) {
		String name = "iterator";
		Set<Long> listedValues = new HashSet<Long>();
		int counter = 0;
		Long current, previous = null;

		while (iterator.hasNext()) {
			// this is important in case iterator would be in an infinite loop
			int expectedCount = expectedValues.size();
			if (++counter > expectedCount) {
				fail(name + " has already listed " + counter + " indexedValues, one less was expected");
			}
			listedValues.add(current = iterator.next());

			if (current == null || previous == null) {
				if (current == null && ((ascending && counter > 1) || (!ascending) && counter < expectedCount)) {
					fail(name + " has returned these two value in this order: " + previous + " ," + current);
				}
				previous = current;
				continue;
			}

			if ((counter > 1) && ((current > previous) != (ascending))) {
				fail(name + " has returned these two value in this order: " + previous + " ," + current);
			}

			previous = current;
		}

		assertEquals(name + " does not provide same number of indexedValues as expected.", expectedValues.size(), listedValues.size());
		assertTrue(name + " does not list all the indexedValues", expectedValues.containsAll(listedValues));
	}

	protected void assertContains(Long... keys) {
		for (Long value: keys) {
			assertContainsKey(value);
		}
	}

	private void assertContainsKey(Long key) {
		assertTrue("Key not found in the map: " + key, map.containsKey(key));
		Long value = getStandardValueForKey(key);
		assertEquals("Value not found in the map", value, map.get(key));
	}

	protected void assertNotContains(Long... keys) {
		for (Long key: keys) {
			assertNotContainsKey(key);
		}
	}

	private void assertNotContainsKey(Long key) {
		assertFalse("Value found in the map but should not be:" + key, map.containsKey(key));
		assertNull("Value found in the map but should not be:" + key, map.get(key));
	}

}
