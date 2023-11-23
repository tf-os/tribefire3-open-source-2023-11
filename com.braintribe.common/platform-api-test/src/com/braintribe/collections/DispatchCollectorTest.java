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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.collections.dispatchcollector.DispatchCollector;
import com.braintribe.collections.dispatchcollector.DispatchReceiver;
import com.braintribe.collections.dispatchcollector.HoldingDispatchReceiver;

public class DispatchCollectorTest {

	protected DispatchCollector<String> collector = null;

	@Ignore
	public void initialize(int sizeThreshold, long timeThreshold, DispatchReceiver<String> receiver) throws Exception {
		this.collector = new DispatchCollector<>(sizeThreshold, timeThreshold, receiver);
	}

	@After
	public void cleanup() {
		if (this.collector != null) {
			this.collector.shutdown();
			this.collector = null;
		}
	}

	@Test
	public void testSizeThreshold() throws Exception {
		HoldingDispatchReceiver<String> receiver = new HoldingDispatchReceiver<>();
		Collection<String> dispatchedCollection = null;

		this.initialize(5, -1L, receiver);

		this.collector.add("1");
		assertThat(receiver.getCollection()).isNull();
		this.collector.add("2");
		assertThat(receiver.getCollection()).isNull();
		this.collector.add("3");
		assertThat(receiver.getCollection()).isNull();
		this.collector.add("4");
		assertThat(receiver.getCollection()).isNull();
		this.collector.add("5");
		dispatchedCollection = receiver.getCollection();
		assertThat(dispatchedCollection).isNotNull();
		assertThat(dispatchedCollection.size()).isEqualTo(5);
		List<String> expected = new ArrayList<>();
		expected.add("1");
		expected.add("2");
		expected.add("3");
		expected.add("4");
		expected.add("5");
		assertThat(dispatchedCollection).isEqualTo(expected);

		this.collector.add("6");
		assertThat(dispatchedCollection.size()).isEqualTo(5);
		assertThat(dispatchedCollection).isEqualTo(expected);
		this.collector.add("7");
		assertThat(dispatchedCollection.size()).isEqualTo(5);
		assertThat(dispatchedCollection).isEqualTo(expected);
		this.collector.add("8");
		assertThat(dispatchedCollection.size()).isEqualTo(5);
		assertThat(dispatchedCollection).isEqualTo(expected);
		this.collector.add("9");
		assertThat(dispatchedCollection.size()).isEqualTo(5);
		assertThat(dispatchedCollection).isEqualTo(expected);
		this.collector.add("10");
		dispatchedCollection = receiver.getCollection();
		assertThat(dispatchedCollection).isNotNull();
		assertThat(dispatchedCollection.size()).isEqualTo(5);
		expected.clear();
		expected.add("6");
		expected.add("7");
		expected.add("8");
		expected.add("9");
		expected.add("10");
		assertThat(dispatchedCollection).isEqualTo(expected);

	}

	@Test
	public void testZeroSizeThreshold() throws Exception {
		HoldingDispatchReceiver<String> receiver = new HoldingDispatchReceiver<>();
		Collection<String> dispatchedCollection = null;

		this.initialize(0, -1L, receiver);

		assertThat(receiver.getCollection()).isNull();

		this.collector.add("1");

		dispatchedCollection = receiver.getCollection();
		assertThat(dispatchedCollection).isNotNull();
		assertThat(dispatchedCollection.size()).isEqualTo(1);

		List<String> expected = new ArrayList<>();
		expected.add("1");
		assertThat(dispatchedCollection).isEqualTo(expected);

		this.collector.add("2");
		dispatchedCollection = receiver.getCollection();
		assertThat(dispatchedCollection).isNotNull();
		assertThat(dispatchedCollection.size()).isEqualTo(1);

		expected.clear();
		expected.add("2");
		assertThat(dispatchedCollection).isEqualTo(expected);

	}

	@Test
	public void testZeroThreshold() throws Exception {
		HoldingDispatchReceiver<String> receiver = new HoldingDispatchReceiver<>();
		Collection<String> dispatchedCollection = null;

		this.initialize(0, 0L, receiver);

		assertThat(receiver.getCollection()).isNull();

		this.collector.add("1");

		dispatchedCollection = receiver.getCollection();
		assertThat(dispatchedCollection).isNotNull();
		assertThat(dispatchedCollection.size()).isEqualTo(1);

		List<String> expected = new ArrayList<>();
		expected.add("1");
		assertThat(dispatchedCollection).isEqualTo(expected);

		this.collector.add("2");
		dispatchedCollection = receiver.getCollection();
		assertThat(dispatchedCollection).isNotNull();
		assertThat(dispatchedCollection.size()).isEqualTo(1);

		expected.clear();
		expected.add("2");
		assertThat(dispatchedCollection).isEqualTo(expected);

	}

	@Test
	public void testTimeAndCountThreshold() throws Exception {
		HoldingDispatchReceiver<String> receiver = new HoldingDispatchReceiver<>();
		Collection<String> dispatchedCollection = null;

		this.initialize(5, 1000L, receiver);

		assertThat(receiver.getCollection()).isNull();

		this.collector.add("1");

		assertThat(receiver.getCollection()).isNull();

		Thread.sleep(1500L);

		dispatchedCollection = receiver.getCollection();
		assertThat(dispatchedCollection).isNotNull();
		assertThat(dispatchedCollection.size()).isEqualTo(1);

		this.collector.add("2");
		this.collector.add("3");

		Thread.sleep(2500L);

		List<String> expected = new ArrayList<>();
		expected.add("2");
		expected.add("3");

		dispatchedCollection = receiver.getCollection();
		assertThat(dispatchedCollection).isNotNull();
		assertThat(dispatchedCollection.size()).isEqualTo(2);
		assertThat(dispatchedCollection).isEqualTo(expected);

		this.collector.add("4");
		this.collector.add("5");
		this.collector.add("6");
		this.collector.add("7");
		this.collector.add("8");

		expected.clear();
		expected.add("4");
		expected.add("5");
		expected.add("6");
		expected.add("7");
		expected.add("8");

		dispatchedCollection = receiver.getCollection();
		assertThat(dispatchedCollection).isNotNull();
		assertThat(dispatchedCollection.size()).isEqualTo(5);
		assertThat(dispatchedCollection).isEqualTo(expected);

	}

	@Test
	public void testNoThreshold() throws Exception {
		try {
			HoldingDispatchReceiver<String> receiver = new HoldingDispatchReceiver<>();
			this.initialize(-1, -1L, receiver);
			throw new AssertionError("The expected IllegalArgumentException did not happen.");
		} catch (IllegalArgumentException iaeExpected) {
			// That's good
		}
	}

	@Test
	public void testNoReceiver() throws Exception {
		try {
			this.initialize(0, 0, null);
			throw new AssertionError("The expected NullPointerException did not happen.");
		} catch (NullPointerException inpeExpected) {
			// That's good
		}
	}
}
