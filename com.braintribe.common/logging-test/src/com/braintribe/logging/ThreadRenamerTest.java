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
package com.braintribe.logging;


import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import com.braintribe.common.lcd.Numbers;

public class ThreadRenamerTest {

	@Test
	public void testThreadRenamerSimple() {
		
		ThreadRenamer tr = new ThreadRenamer(true);
		
		final String threadName = UUID.randomUUID().toString();
		final String pushedName = UUID.randomUUID().toString();

		Thread.currentThread().setName(threadName);
		
		tr.push(() -> pushedName);

		String changedName = Thread.currentThread().getName();
		assertThat(changedName).contains(threadName);
		assertThat(changedName).contains(pushedName);
		
		tr.pop();
		
		assertThat(Thread.currentThread().getName()).isEqualTo(threadName);
		
	}
	
	@Test
	public void testThreadRenamerMultithreaded() throws Exception {
		
		final ThreadRenamer tr = new ThreadRenamer(true);

		int worker = 10;
		int iterations = Numbers.THOUSAND;
		
		ExecutorService service = Executors.newFixedThreadPool(worker);
		try {
			
			List<Future<?>> futures = new ArrayList<>(worker);
			for (int i=0; i<worker; ++i) {
				futures.add(service.submit(new Runnable() {

					@Override
					public void run() {
						final String myName = UUID.randomUUID().toString();
						Thread.currentThread().setName(myName);
						for (int j=0; j<iterations; ++j) {
							
							final String pushedName = UUID.randomUUID().toString();
							tr.push(() -> pushedName);
							String changedName = Thread.currentThread().getName();
							assertThat(changedName).contains(myName);
							assertThat(changedName).contains(pushedName);
							tr.pop();
							assertThat(Thread.currentThread().getName()).isEqualTo(myName);
						}
						
						Stack<String> stack = tr.nameStack.get();
						assertThat(stack).isEmpty();

					}
					
				}));
			}
			
			for (Future<?> f : futures) {
				f.get();
			}
			
		} finally {
			service.shutdown();
		}
		
	}
	
	@Test
	public void testThreadRenamerThreadLocal() throws Exception {
		
		ThreadRenamer tr = new ThreadRenamer(true);
		
		final String threadName = UUID.randomUUID().toString();
		final String pushedName = UUID.randomUUID().toString();

		Thread.currentThread().setName(threadName);
		
		tr.push(() -> pushedName);

		String changedName = Thread.currentThread().getName();
		assertThat(changedName).contains(threadName);
		assertThat(changedName).contains(pushedName);
		
		tr.pop();
		
		assertThat(Thread.currentThread().getName()).isEqualTo(threadName);
		
		Stack<String> stack = tr.nameStack.get();
		assertThat(stack).isEmpty();
	}
	
	@Test
	public void testExceptionOnTooManyPops() throws Exception {
		
		ThreadRenamer tr = new ThreadRenamer(true);
		
		final String pushedName = UUID.randomUUID().toString();
		
		tr.push(() -> pushedName);
		
		tr.pop();
		
		try {
			//This is one pop too much... expecting an exception
			tr.pop();
			
			fail("The additional pop() should have thrown an exception");
			
		} catch(EmptyStackException e) {
			//cool
		}
	}
}
