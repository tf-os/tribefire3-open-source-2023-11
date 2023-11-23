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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Test;

import com.braintribe.common.lcd.equalscheck.CustomEqualsCollection;
import com.braintribe.common.lcd.equalscheck.CustomEqualsCollectionImpl;
import com.braintribe.common.lcd.equalscheck.CustomEqualsCollectionTools;
import com.braintribe.common.lcd.equalscheck.CustomEqualsList;
import com.braintribe.common.lcd.equalscheck.CustomEqualsSet;
import com.braintribe.common.lcd.equalscheck.EqualsCheck;
import com.braintribe.common.lcd.equalscheck.IgnoreCaseEqualsCheck;
import com.braintribe.utils.lcd.CommonTools;

/**
 * Provides tests for {@link CustomEqualsCollection} and related classes.
 *
 * @author michael.lafite
 */
public class CustomEqualsCollectionTest {

	@Test
	public void test() {
		final EqualsCheck<String> equalsCheck = new IgnoreCaseEqualsCheck();
		testWithCollection(new CustomEqualsSet<>(equalsCheck, new HashSet<String>()), true, false);
		testWithCollection(new CustomEqualsSet<>(equalsCheck, new TreeSet<String>()), false, false);
		testWithCollection(new CustomEqualsList<>(equalsCheck, new ArrayList<String>()), true, true);
		testWithCollection(new CustomEqualsList<>(equalsCheck, new LinkedList<String>()), true, true);
		testWithCollection(new CustomEqualsCollectionImpl<String, Collection<String>>(equalsCheck, new ArrayBlockingQueue<String>(1000), true), false,
				true);
		testWithCollection(new CustomEqualsCollectionImpl<String, Collection<String>>(equalsCheck, new ArrayDeque<String>(), true), false, true);
	}

	private static void testWithCollection(final CustomEqualsCollection<String> collection, final boolean nullsSupported,
			final boolean duplicatesAllowed) {
		assertEquals(0, collection.size());
		assertTrue(collection.add("abc"));
		assertEquals(1, collection.size());
		if (duplicatesAllowed) {
			assertTrue(collection.add("abc"));
			assertEquals(2, collection.size());
		} else {
			assertFalse(collection.add("abc"));
			assertEquals(1, collection.size());
		}
		collection.clear();
		assertEquals(0, collection.size());

		assertTrue(collection.add("abc"));
		assertEquals(1, collection.size());
		assertFalse(collection.remove("not_exists"));
		assertEquals(1, collection.size());
		assertTrue(collection.remove("ABC"));
		assertEquals(0, collection.size());

		assertTrue(collection.add("abc"));
		assertEquals(1, collection.size());
		assertTrue(collection.add("abcd"));
		assertEquals(2, collection.size());
		if (nullsSupported) {
			assertFalse(collection.remove(null));
			assertEquals(2, collection.size());
			assertTrue(collection.add(null));
			assertEquals(3, collection.size());
			if (duplicatesAllowed) {
				assertTrue(collection.add(null));
				assertEquals(4, collection.size());
				assertTrue(collection.remove(null));
			} else {
				assertFalse(collection.add(null));
				assertEquals(3, collection.size());
			}

			assertTrue(collection.remove(null));
			assertEquals(2, collection.size());
			assertFalse(collection.remove(null));
			assertEquals(2, collection.size());
		}
		collection.clear();
		assertEquals(0, collection.size());

		assertTrue(collection.add("dEf"));
		assertTrue(collection.add("aBcD"));
		assertFalse(collection.removeAll(CommonTools.getList("x", "y")));
		assertTrue(collection.removeAll(CommonTools.getList("DEF", "x")));
		assertTrue(collection.removeAll(CommonTools.getList("DEF", "ABCD")));
		assertFalse(collection.removeAll(CommonTools.getList("DEF", "ABCD")));
		assertEquals(0, collection.size());

		assertTrue(collection.addAll(CommonTools.getList("a", "b", "c", "A", "B", "C")));
		if (duplicatesAllowed) {
			assertEquals(6, collection.size());
			assertTrue(collection.removeAll(CommonTools.getList("a", "b", "c")));
		} else {
			assertEquals(3, collection.size());
		}

		assertTrue(collection.collectionEquals(CommonTools.getList("a", "b", "c")));
		assertFalse(collection.collectionEquals(CommonTools.getList("a", "b", "c", "c")));

		assertFalse(collection.retainAll(CommonTools.getList("a", "b", "c")));
		assertEquals(3, collection.size());
		assertFalse(collection.retainAll(CommonTools.getList("a", "b", "c")));
		assertTrue(collection.retainAll(CommonTools.getList("a", "b")));
		assertEquals(2, collection.size());
		assertTrue(collection.collectionEquals(CommonTools.getList("a", "b")));
		assertTrue(collection.collectionEquals(CommonTools.getList("A", "B")));

		assertEquals(1, collection.countOccurrences("a"));
		assertEquals(0, collection.countOccurrences("x"));
		assertEquals(1, collection.getOccurrences("b").size());
		assertTrue("b".equalsIgnoreCase(collection.removeOccurrences("b").get(0)));
		assertEquals(1, collection.size());
		collection.clear();

		if (duplicatesAllowed) {
			collection.add("a");
			collection.add("a");
			collection.add("b");
			collection.add("b");
			assertEquals(4, collection.size());
			assertEquals(2, collection.countOccurrences("B"));
			assertEquals(2, collection.removeOccurrences("B").size());
			assertEquals(2, collection.size());
			assertTrue(collection.collectionEquals(CommonTools.getList("A", "A")));
			collection.clear();
		}

		if (collection instanceof CustomEqualsList) {
			final CustomEqualsList<String> list = (CustomEqualsList<String>) collection;
			list.add("1");
			list.add("3");
			list.add(1, "2");

			assertFalse(list.collectionEquals(CommonTools.getList("1", "2", "3", "4")));
			assertTrue(list.collectionEquals(CommonTools.getList("3", "2", "1")));
			assertFalse(list.orderEquals(CommonTools.getList("3", "2", "1")));
			assertTrue(list.orderEquals(CommonTools.getList("1", "2", "3")));

			assertEquals(1, list.indexOf("2"));
		}
		collection.clear();

		// get missing and additional elements
		collection.addAll(CommonTools.getList("a", "B", "c", "D"));
		assertTrue(CustomEqualsCollectionTools.collectionEquals(collection.getAdditionalElements(CommonTools.getList("a", "b")),
				CommonTools.getList("c", "D"), collection.getEqualsCheck()));
		assertTrue(CustomEqualsCollectionTools.collectionEquals(collection.getMissingElements(CommonTools.getList("a", "b", "e", "f")),
				CommonTools.getList("e", "f"), collection.getEqualsCheck()));
	}

}
