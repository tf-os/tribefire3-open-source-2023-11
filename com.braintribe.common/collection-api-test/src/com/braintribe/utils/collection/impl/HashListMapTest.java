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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.collection.api.ListMap;

public class HashListMapTest {

	@Test
	public void testCommonMapOps() {
		ListMap<String, Integer> listMap = new HashListMap<>();
		
		listMap.put("Hi", new LinkedList<>());
		listMap.put("Bye", null);
		listMap.put(null, Arrays.asList(1,2,3));
		
		assertThat(listMap).hasSize(3);
		assertThat(listMap.get("Hi")).isEqualTo(Arrays.asList());
		assertThat(listMap.get(null)).containsExactly(1,2,3);
		assertThat(listMap.get("null")).isNull();
		assertThat(listMap.get("Bye")).isNull();
		
		listMap.remove(null);
		
		assertThat(listMap.get(null)).isNull();
		assertThat(listMap.get("Hi")).isEqualTo(Arrays.asList());
		
		listMap.clear();
		
		assertThat(listMap.get("Hi")).isNull();
		assertThat(listMap.size()).isEqualTo(0);
	}
	
	@Test
	public void testPutSingleElement() {
		ListMap<String, Integer> listMap = new HashListMap<>();
		listMap.putSingleElement("1", 1);
		
		assertThat(listMap.get("2")).isNull();
		assertThat(listMap.get("1")).containsExactly(1);
		
		listMap.putSingleElement("1", 7);
		
		assertThat(listMap.get("2")).isNull();
		assertThat(listMap.get("1")).containsExactly(1,7);
		
		listMap.putSingleElement("2", 89);
		
		assertThat(listMap.get("2")).containsExactly(89);
		
		listMap.put("3", CollectionTools.getList(111,222,333,444,555));
		listMap.put("4", null);
		
		assertThat(listMap.get("3")).containsExactly(111,222,333,444,555);
		assertThat(listMap.get("4")).isNull();
		
		listMap.putSingleElement("3", 999);		
		listMap.putSingleElement("4", 3456);
		
		assertThat(listMap.get("3")).containsExactly(111,222,333,444,555,999);
		assertThat(listMap.get("4")).containsExactly(3456);
	}
	
	@Test
	public void testRemoveSingleElement() {
		ListMap<String, Integer> listMap = new HashListMap<>();
		listMap.putSingleElement("1", 1);
		
		assertThat(listMap.removeSingleElement("1", 2)).isFalse();
		assertThat(listMap.get("1")).containsExactly(1);
		
		listMap.putSingleElement("1", 2);
		
		assertThat(listMap.removeSingleElement("1", 2)).isTrue();
		assertThat(listMap.get("1")).containsExactly(1);
		
		assertThat(listMap.removeSingleElement("1", 1)).isTrue();
		assertThat(listMap.get("1")).isNotNull().isEmpty();
		
		listMap.put("a", CollectionTools.getList(1,2,3,4,5,6));
		listMap.put("b", CollectionTools.getList(1,2,3,4,5,6));
		
		listMap.removeSingleElement("a", 3);
		
		assertThat(listMap.get("a")).containsExactly(1,2,4,5,6);
		assertThat(listMap.get("b")).containsExactly(1,2,3,4,5,6);
		
		List<Integer> intList = new LinkedList<>(Arrays.asList(9,8,7,6,5,4,3));
		
		listMap.put("c", intList);
		listMap.put("d", intList);
		
		listMap.removeSingleElement("c", 9);
		
		assertThat(intList).containsExactly(8,7,6,5,4,3);
		assertThat(listMap.get("c")).containsExactly(8,7,6,5,4,3);
		assertThat(listMap.get("d")).containsExactly(8,7,6,5,4,3);
	}
	
	@Test
	public void testGetSingleElement() {
		ListMap<String, Integer> listMap = new HashListMap<>();
		listMap.putSingleElement("1", 1);
		listMap.put("a", CollectionTools.getList(1,2,3,4,5,6));
		List<Integer> intList = listMap.get("1");
		
		assertThat(listMap.getSingleElement("1")).isEqualTo(1);
		assertThatThrownBy(() -> listMap.getSingleElement("a")).isExactlyInstanceOf(IllegalStateException.class);
		
		intList.clear();
		
		assertThat(listMap.getSingleElement("1")).isNull();
		
		listMap.putSingleElement("1", null);
		
		assertThat(listMap.getSingleElement("1")).isNull();
		
		listMap.putSingleElement("1", null);
		
		assertThatThrownBy(() -> listMap.getSingleElement("1")).isExactlyInstanceOf(IllegalStateException.class);
		
		listMap.put("1", null);

		assertThat(listMap.getSingleElement("1")).isNull();
	}

	@Test
	public void testContainsKeyValue() {
		ListMap<String, Integer> listMap = new HashListMap<>();
		listMap.putSingleElement("1", 1);
		listMap.put("a", CollectionTools.getList(1,2,3,4,5,6));
		
		assertThat(listMap.containsKeyValue("a", 3)).isTrue();
		assertThat(listMap.containsKeyValue("a", 0)).isFalse();
		assertThat(listMap.containsKeyValue("a", null)).isFalse();
		assertThat(listMap.containsKeyValue("1", 1)).isTrue();
		assertThat(listMap.containsKeyValue("10", 1)).isFalse();
		assertThat(listMap.containsKeyValue(null, 1)).isFalse();
		
		listMap.putSingleElement(null, 1);

		assertThat(listMap.containsKeyValue(null, 1)).isTrue();
		
		listMap.putSingleElement("a", null);
		
		assertThat(listMap.containsKeyValue("a", null)).isTrue();
	}
}
