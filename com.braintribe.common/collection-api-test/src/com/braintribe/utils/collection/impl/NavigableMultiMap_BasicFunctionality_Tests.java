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

import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.utils.collection.api.NavigableMultiMap;

/**
 * 
 */
public class NavigableMultiMap_BasicFunctionality_Tests extends AbstractNavigableMultiMapTests {

	@Test
	public void testGettingFromEmptyMap() {
		testGettingFromEmptyMapWith(multiMap);
		testGettingFromEmptyMapWith(multiMap.subMap(-500L, true, 5000L, true));
	}

	private void testGettingFromEmptyMapWith(NavigableMultiMap<Long, Long> multiMap) {
		assertNull(multiMap.get(1L));
		Assertions.assertThat(multiMap.getAll(1L)).isNotNull().isEmpty();

		assertNotContains(multiMap.getLowest(1l));
		assertNotContains(multiMap.getHighest(1l));
	}

	@Test
	public void testSimpleAddingAndRemoving() {
		testSimpleAddingAndRemovingWith(multiMap);
		testSimpleAddingAndRemovingWith(multiMap.subMap(-500L, true, 5000L, true));
	}

	private void testSimpleAddingAndRemovingWith(NavigableMultiMap<Long, Long> multiMap) {
		assertEmpty(multiMap);

		multiMap.put(1L, 10L);
		multiMap.put(1L, 13L);
		multiMap.put(1L, 12L);
		multiMap.put(1L, 11L);
		assertContainsKeyValues(multiMap, 1L, 10L, 11L, 12L, 13L);
		assertContainsKeyValue(1L, 10L);

		multiMap.remove(1L, 11L);
		assertContainsKeyValues(multiMap, 1L, 10L, 12L, 13L);

		multiMap.put(1L, null);
		assertContainsKeyValue(1L, null);

		multiMap.clear();
		assertEmpty(multiMap);
	}

}
