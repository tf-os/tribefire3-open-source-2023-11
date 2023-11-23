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
package com.braintribe.wire.test.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.test.concurrency.bean.ConcurrentlyAccessed;
import com.braintribe.wire.test.concurrency.bean.CycleBean;
import com.braintribe.wire.test.concurrency.wire.ConcurrencyTestWireModule;
import com.braintribe.wire.test.concurrency.wire.contract.ConcurrencyTestMainContract;


public class ConcurrencyTest {
	@Test
	public void concurrencyTest() throws Exception {

		WireContext<ConcurrencyTestMainContract> context = Wire.context(ConcurrencyTestWireModule.INSTANCE);

		final int numThreads = 20;
		
		ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(numThreads);
		
		List<Future<String>> futures = new ArrayList<>();
		
		for (int i = 0; i < numThreads; i++) {
			Future<String> future = newFixedThreadPool.submit(() -> {
				ConcurrentlyAccessed concurrentlyAccessed = context.contract().concurrentlyAccessed();
				
				return concurrentlyAccessed.getValue();
			});
			
			futures.add(future);
		}
		
		for (Future<String> future: futures) {
			String value = future.get();
			
			assertThat(value).describedAs("bean was not properly initialized when beeing returned in a concurrent setup").isEqualTo("done");
		}
		
		// check cyclic reference
		CycleBean cycleBean = context.contract().cycleBean();
		
		CycleBean backlinkBean = cycleBean.getOther();
		CycleBean subjectBean = backlinkBean.getOther();
		
		assertThat(cycleBean).describedAs("bean cycle was not properly established").isSameAs(subjectBean);
		
		context.shutdown();
		newFixedThreadPool.shutdown();
	}
}
