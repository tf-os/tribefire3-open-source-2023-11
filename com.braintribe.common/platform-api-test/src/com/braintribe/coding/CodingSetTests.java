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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.cc.lcd.CodingSet;

/**
 * tests the CodingSet com.braintribe.common.lcd.CodingSet and com.braintribe.common.lcd.CodingIterator
 *
 * @author pit
 *
 */
public class CodingSetTests {
	private final Bean bean1 = new Bean("one");
	private final Bean bean2 = new Bean("two");
	private final Bean bean3 = new Bean("three");
	private final Bean bean4 = new Bean("four");
	private final Bean bean5 = new Bean("five");

	private final Bean dupBean1 = new Bean("one");

	private CodingSet<BeanWrapper, Bean> setUpCodingSet() {
		CodingSet<BeanWrapper, Bean> codingSet = new CodingSet<>(new HashSet<>(), new WrapperCodec());

		codingSet.add(bean1);
		codingSet.add(bean2);
		codingSet.add(bean3);

		return codingSet;
	}

	@Test
	public void testContains() {
		final CodingSet<BeanWrapper, Bean> codingSet = setUpCodingSet();
		Assert.assertTrue("contains failed", codingSet.contains(this.bean1));
		Assert.assertTrue("contains failed", codingSet.contains(this.dupBean1));
	}

	@Test
	public void testToArray() {
		CodingSet<BeanWrapper, Bean> codingSet = setUpCodingSet();
		Object[] result = codingSet.toArray();
		for (Object obj : result) {
			assertThat(obj).isExactlyInstanceOf(Bean.class);
		}
	}

	@Test
	public void testToSpecifiedArray() {
		CodingSet<BeanWrapper, Bean> codingSet = setUpCodingSet();
		Object[] result = codingSet.toArray(new Bean[0]);
		for (Object obj : result) {
			assertThat(obj).isExactlyInstanceOf(Bean.class);
		}
	}

	@Test
	public void testAdd() {
		final CodingSet<BeanWrapper, Bean> codingSet = setUpCodingSet();
		final int sizeBefore = codingSet.size();
		codingSet.add(this.bean4);
		final int sizeAfter = codingSet.size();
		Assert.assertTrue("cannot add element", sizeBefore < sizeAfter);
		codingSet.add(this.dupBean1);
		final int sizeAfter2 = codingSet.size();
		Assert.assertTrue("added duplicate element", sizeAfter2 == sizeAfter);

	}

	@Test
	public void testAddAll() {
		final CodingSet<BeanWrapper, Bean> codingSet = setUpCodingSet();

		final int sizeBefore = codingSet.size();

		final Collection<Bean> collection = new ArrayList<>();
		collection.add(this.bean4);
		collection.add(this.bean5);
		collection.add(this.dupBean1);

		codingSet.addAll(collection);

		final int sizeAfter = codingSet.size();

		Assert.assertTrue("not the correct numbers of beans were added ", sizeAfter - sizeBefore == 2);

	}

	@Test
	public void testRemove() {
		final CodingSet<BeanWrapper, Bean> codingSet = setUpCodingSet();
		final int sizeBefore = codingSet.size();
		codingSet.remove(this.dupBean1);
		final int sizeAfter = codingSet.size();
		Assert.assertTrue("cannot seem to remove ", sizeAfter <= sizeBefore);
	}

	@Test
	public void testRemoveAll() {
		final CodingSet<BeanWrapper, Bean> codingSet = setUpCodingSet();
		final Collection<Bean> collection = new ArrayList<>();
		collection.add(this.bean2);
		collection.add(this.bean4);

		codingSet.removeAll(collection);

		Assert.assertTrue("bean 1 is not contained", codingSet.contains(this.bean1));
		Assert.assertTrue("bean 3 is not contained", codingSet.contains(this.bean3));
		Assert.assertTrue("bean 2 is stil contained", !codingSet.contains(this.bean2));

	}

	@Test
	public void testRetainAll() {
		final CodingSet<BeanWrapper, Bean> codingSet = setUpCodingSet();
		final Collection<Bean> collection = new ArrayList<>();
		collection.add(this.bean2);
		collection.add(this.bean4);

		codingSet.retainAll(collection);

		Assert.assertTrue("bean 1 is still contained", !codingSet.contains(this.bean1));
		Assert.assertTrue("bean 3 is still contained", !codingSet.contains(this.bean3));
		Assert.assertTrue("bean 2 is not contained anymore", codingSet.contains(this.bean2));

	}

	@Test
	public void testIsEmptyAndClear() {
		final CodingSet<BeanWrapper, Bean> codingSet = setUpCodingSet();
		Assert.assertTrue("codingset is empty", !codingSet.isEmpty());
		codingSet.clear();
		Assert.assertTrue("codingset is not empty", codingSet.isEmpty());

	}

	@Test
	public void testIterator() {

		final CodingSet<BeanWrapper, Bean> codingSet = setUpCodingSet();

		Iterator<Bean> iterator = codingSet.iterator();

		try {
			while (iterator.hasNext()) {
				final Bean bean = iterator.next();
				assertThat(bean.getName()).isNotNull();
			}
		} catch (final Exception e) {
			fail("Exception thrown [" + e + "]");
		}

		iterator = codingSet.iterator();

		try {
			while (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		} catch (final Exception e) {
			fail("Exception thrown [" + e + "]");
		}

		Assert.assertTrue("iterator didn't remove", codingSet.isEmpty());

	}

}
