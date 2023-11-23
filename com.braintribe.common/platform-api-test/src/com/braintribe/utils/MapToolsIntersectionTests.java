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
package com.braintribe.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.utils.lcd.MapTools;

/**
 * Tests for {@link MapTools#createIntersection(Map...)}
 *
 *
 */
@SuppressWarnings("unchecked")
public class MapToolsIntersectionTests {

	List<String> commonKeys = Arrays.asList("a", "b", "c");

	// @Test
	// public void createMapIntersectionWithNoParameters() {
	// final Map<Object, Object> result = MapTools.createIntersection();
	// Assert.assertNull(result);
	// }
	//
	// @Test
	// public void createMapIntersectionWithSingleNull() {
	// final Map<Object, Object> result = MapTools.createIntersection((Map<Object, Object>[]) null);
	// Assert.assertNull(result);
	// }
	//
	// @Test
	// public void createMapIntersectionWithMultipleNull() {
	// final Map<Object, Object> result = MapTools.createIntersection(null, null, null);
	// Assert.assertNull(result);
	// }

	@Test
	public void createMapIntersectionWithSingleMap() {
		final Map<String, String> stringMap = createMapWithCommonKeys();
		final Map<String, String> result = MapTools.createIntersection(stringMap);
		assertMapContainsCommonKeysAndHasExpectedSize(result);
	}

	private void assertMapContainsCommonKeysAndHasExpectedSize(final Map<String, String> result) {
		for (final String key : this.commonKeys) {
			Assert.assertTrue("The resulting intersection should contain the key " + key, result.containsKey(key));
		}
		Assert.assertEquals(this.commonKeys.size(), result.size());
	}

	private Map<String, String> createMapWithCommonKeys() {
		final Map<String, String> stringMap = new HashMap<>();
		for (final String key : this.commonKeys) {
			stringMap.put(key, key + "value");
		}
		return stringMap;
	}

	@Test
	public void createMapIntersectionWithMultipleMaps() {
		final Map<String, String> firstMap = createMapWithCommonKeys();
		firstMap.put("x", "x");
		final Map<String, String> secondMap = createMapWithCommonKeys();
		final Map<String, String> thirdMap = createMapWithCommonKeys();
		thirdMap.put("n", "n");
		thirdMap.put("m", "m");
		thirdMap.put("h", "h");
		thirdMap.put("g", "g");
		thirdMap.put("x", "x");

		final Map<String, String> result = MapTools.createIntersection(firstMap, secondMap, thirdMap);
		assertMapContainsCommonKeysAndHasExpectedSize(result);
	}

}
