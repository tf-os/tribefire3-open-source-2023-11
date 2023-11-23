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
package com.braintribe.coding;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.cc.lcd.CodingMap;

/**
 * tests for the coding map class in com.braintribe.lcd.CodingMap
 *
 * @author pit
 *
 */
public class CodingMapTest {
	private final Bean bean1 = new Bean("one");
	private final Bean bean2 = new Bean("two");
	private final Bean bean3 = new Bean("three");
	private final Bean bean4 = new Bean("four");
	private final Bean bean5 = new Bean("five");

	private final Bean dupBean1 = new Bean("one");

	private CodingMap<BeanWrapper, Bean, String> setup() {
		final CodingMap<BeanWrapper, Bean, String> map = new CodingMap<>(new HashMap<BeanWrapper, String>(), new WrapperCodec());

		map.put(this.bean1, "bean one");
		map.put(this.bean2, "bean two");
		map.put(this.bean3, "bean three");

		return map;

	}

	@Test
	public void testSize() {
		final CodingMap<BeanWrapper, Bean, String> map = setup();
		final int sizeBefore = map.size();
		map.put(this.bean4, "bean four");
		final int sizeAfter = map.size();
		Assert.assertTrue("size hasn't grown", sizeAfter > sizeBefore);
	}

	@Test
	public void testContainsKey() {
		final CodingMap<BeanWrapper, Bean, String> map = setup();
		Assert.assertTrue("bean1 is not contained", map.containsKey(this.bean1));
		Assert.assertTrue("bean4 is contained", !map.containsKey(this.bean4));
		Assert.assertTrue("dupBean1 is not contained", map.containsKey(this.dupBean1));
	}

	@Test
	public void testContainsValue() {
		final CodingMap<BeanWrapper, Bean, String> map = setup();
		Assert.assertTrue("bean1 is not contained", map.containsValue("bean one"));
		Assert.assertTrue("bean4 is contained", !map.containsValue("bean four"));
	}

	@Test
	public void testGet() {
		final CodingMap<BeanWrapper, Bean, String> map = setup();
		final String value1 = map.get(this.bean1);
		Assert.assertTrue("value of bean1 not found", value1 != null);
		final String value4 = map.get(this.bean4);
		Assert.assertTrue("value of bean4 found", value4 == null);
		Assert.assertTrue("value of dupBean1 not found", value1 != null);
		final String dupValue1 = map.get(this.dupBean1);
		Assert.assertTrue("values of bean1 and dupBean1 are not identical", value1.equalsIgnoreCase(dupValue1));
	}

	@Test
	public void testPut() {
		final CodingMap<BeanWrapper, Bean, String> map = setup();
		final int sizeBefore = map.size();
		map.put(this.bean4, "bean 4");
		final int sizeAfterAdd = map.size();
		Assert.assertTrue("put hasn't grown map", sizeAfterAdd > sizeBefore);
		final String redefineValue = "bean one redefined";
		map.put(this.dupBean1, redefineValue);
		final int sizeAfterRedefine = map.size();
		Assert.assertTrue("redefining put has grown map", sizeAfterAdd == sizeAfterRedefine);
		final String value1 = map.get(this.bean1);
		Assert.assertTrue("redefined value not set in map", value1.equalsIgnoreCase(redefineValue));
	}

	@Test
	public void testRemove() {
		final CodingMap<BeanWrapper, Bean, String> map = setup();
		final int sizeBefore = map.size();
		final String bean2Value = map.remove(this.bean2);
		final int sizeAfterFirstRemove = map.size();
		Assert.assertTrue("value is not what was expected", bean2Value.equalsIgnoreCase("bean two"));
		Assert.assertTrue("map hasn't shrunk", sizeAfterFirstRemove < sizeBefore);
		final String bean1Value = map.remove(this.dupBean1);
		final int sizeAfterSecondRemove = map.size();
		Assert.assertTrue("value is not what was expected", bean1Value.equalsIgnoreCase("bean one"));
		Assert.assertTrue("map hasn't shrunk", sizeAfterSecondRemove < sizeAfterFirstRemove);
	}

	@Test
	public void testPutAll() {
		final CodingMap<BeanWrapper, Bean, String> map = setup();
		final int sizeBefore = map.size();
		final Map<Bean, String> addingMap = new HashMap<>();
		addingMap.put(this.bean4, "bean four");
		addingMap.put(this.bean5, "bean five");
		final String redefinable = "bean one redefined";
		addingMap.put(this.dupBean1, redefinable);

		map.putAll(addingMap);
		final int sizeAfter = map.size();

		Assert.assertTrue("map hasn't grown as expected", sizeAfter - sizeBefore == 2);
		final String dupValue1 = map.get(this.dupBean1);
		Assert.assertTrue("redefined value not set in map", dupValue1.equalsIgnoreCase(redefinable));
	}

	@Test
	public void testClearAndIsEmpty() {
		final CodingMap<BeanWrapper, Bean, String> map = setup();
		map.clear();
		final int sizeAfter = map.size();
		Assert.assertTrue("map hasn't cleared", sizeAfter == 0);
		Assert.assertTrue("map isn't empty", map.isEmpty());
	}

	@Test
	public void testKeySet() {
		try {
			final CodingMap<BeanWrapper, Bean, String> map = setup();
			final Set<Bean> beans = map.keySet();
			Assert.assertTrue("wrong size of keyset ", beans.size() == map.size());
			/* for (Bean bean : beans) { Assert.assertTrue("wrong bean class", bean.getClass().getName().equalsIgnoreCase(
			 * "com.braintribe.coding.Bean")); } */
		} catch (final Exception e) {
			Assert.fail("Exception [" + e + "] thrown");
		}
	}

	@Test
	public void testValuesCollection() {
		try {
			final CodingMap<BeanWrapper, Bean, String> map = setup();
			final Collection<String> values = map.values();
			Assert.assertTrue("wrong size of values collection ", values.size() == map.size());
		} catch (final Exception e) {
			Assert.fail("Exception [" + e + "] thrown");
		}
	}

	@Test
	public void testEntrySet() {
		try {
			final String redefinedValue = "bean one redefined";
			final CodingMap<BeanWrapper, Bean, String> map = setup();
			final Set<Entry<Bean, String>> entries = map.entrySet();
			Assert.assertTrue("wrong size of values collection ", entries.size() == map.size());
			for (final Entry<Bean, String> entry : entries) {
				final String beanName = entry.getKey().getName();
				// System.out.println( "name : " + beanName + "->" + entry.getValue());
				if (beanName.equalsIgnoreCase("one")) {
					entry.setValue(redefinedValue);
				}
			}
			final String value1 = map.get(this.dupBean1);
			Assert.assertTrue("set value hasn't worked as expected ", redefinedValue.equalsIgnoreCase(value1));

		} catch (final Exception e) {
			Assert.fail("Exception [" + e + "] thrown");
		}
	}

}
