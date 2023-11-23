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
package com.braintribe.wire.test.basic;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.test.basic.contract.ConcurrencyTestContract;

public class ConcurrencyTest {

	@Test
	public void concurrentSingletonAccessSimple() throws Exception {

		WireContext<ConcurrencyTestContract> context = Wire.contextWithStandardContractBinding(ConcurrencyTestContract.class).build();

		testConcurrently(context.contract()::simpleInstance);

	}

	@Test
	public void concurrentSingletonAccessCyclic() throws Exception {

		WireContext<ConcurrencyTestContract> context = Wire.contextWithStandardContractBinding(ConcurrencyTestContract.class).build();

		testConcurrently(context.contract()::cyclicInstance);

	}

	protected void testConcurrently(Supplier<Object> instanceSupplier) throws Exception {

		ExecutorService executor = Executors.newCachedThreadPool();

		try {

			List<Callable<Object>> tests = new ArrayList<>(5);

			for (int i = 0; i < 5; i++) {
				tests.add(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						System.out.println(Thread.currentThread().getName() + " will try to get the managed instance");
						Object result = instanceSupplier.get();
						System.out.println(Thread.currentThread().getName() + " got managed instance: " + result);
						return result;
					}
				});
			}

			Set<Object> occurences = new HashSet<>();

			for (Future<Object> future : executor.invokeAll(tests, 20, TimeUnit.SECONDS)) {
				occurences.add(future.get());
			}

			assertThat(occurences).hasSize(1);

		} catch (CancellationException c) {
			Assert.fail("The concurrent access on the managed instance deadlocked");
		} finally {
			executor.shutdownNow();
		}

	}

}
