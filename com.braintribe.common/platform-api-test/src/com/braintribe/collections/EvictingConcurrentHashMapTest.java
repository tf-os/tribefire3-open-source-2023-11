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
package com.braintribe.collections;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.collections.EvictingConcurrentHashMap.KeyWithTimestamp;
import com.braintribe.testing.category.Slow;

public class EvictingConcurrentHashMapTest {

	@Test
	public void simpleTest() {

		EvictingConcurrentHashMap<String, String> map = new EvictingConcurrentHashMap<>(k -> false, true);

		map.put("hello", "world");

		assertThat(map.size()).isEqualTo(1);
		assertThat(map.get("hello")).isEqualTo("world");

		map.remove("hello");

		assertThat(map.size()).isEqualTo(0);
		assertThat(map.get("hello")).isNull();
	}

	@Test
	public void sizeThresholdTest() {

		EvictingConcurrentHashMap<String, String> map = new EvictingConcurrentHashMap<>(k -> true, true);
		map.setEvictionThreshold(5);

		map.put("1", "1");
		map.put("2", "2");
		map.put("3", "3");
		map.put("4", "4");
		map.put("5", "5");

		assertThat(map.size()).isEqualTo(5);

		map.put("6", "6");

		assertThat(map.size()).isEqualTo(1);
	}

	@Test
	@Category(Slow.class)
	public void sizeEvictionIntervalTest() throws Exception {

		EvictingConcurrentHashMap<String, String> map = new EvictingConcurrentHashMap<>(k -> true, true);
		map.setEvictionInterval(2000L);

		for (int i = 0; i < 100; ++i) {
			map.put("" + i, "" + i);
		}
		assertThat(map.size()).isEqualTo(100);

		Thread.sleep(10_000L);

		map.put("hello", "world");

		assertThat(map.size()).isEqualTo(1);
	}

	@Test
	@Category(Slow.class)
	public void keyWithTimestampEvictionTest() throws Exception {

		// Evict everything older than a second.
		EvictingConcurrentHashMap<KeyWithTimestamp<String>, String> map = new EvictingConcurrentHashMap<>(
				k -> (System.currentTimeMillis() - k.getKey().getTimestamp()) > 2000L, true);
		map.setEvictionThreshold(1); // Try to evict every time

		for (int i = 0; i < 100; ++i) {
			map.put(new KeyWithTimestamp<>("" + i), "" + i);
		}
		assertThat(map.size()).isEqualTo(100);

		Thread.sleep(10_000L);

		map.put(new KeyWithTimestamp<>("hello"), "world");

		assertThat(map.size()).isEqualTo(1);
	}
}
