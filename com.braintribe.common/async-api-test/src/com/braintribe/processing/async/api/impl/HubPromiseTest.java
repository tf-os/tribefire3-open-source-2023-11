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
package com.braintribe.processing.async.api.impl;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.exception.CanceledException;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.processing.async.api.PromiseState;
import com.braintribe.processing.async.impl.HubPromise;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;

public class HubPromiseTest {

	private static ExecutorService executor = null;
	
	@BeforeClass
	public static void beforeClass() {
		executor = Executors.newFixedThreadPool(10);
	}
	@AfterClass
	public static void afterClass() {
		if (executor != null) {
			executor.shutdownNow();
		}
	}
	
	
	@Test
	public void testGetSingleThread() throws Exception {
		
		HubPromise<String> promise = new HubPromise<>("hello, world");
		
		Instant start = NanoClock.INSTANCE.instant();
		String result = promise.get();
		System.out.println("testGetSingleThread: Got result \""+result+"\" after "+StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS));
		
		assertThat(result).isEqualTo("hello, world");
	}

	@Test
	public void testGetWithOnSuccess() throws Exception {
		
		HubPromise<String> promise = new HubPromise<>();
		
		executor.submit(() -> {
			try {
				Thread.sleep(100L);
				promise.onSuccess("hello, world");
			} catch (InterruptedException e) {
				//ignore
			}
		});
		
		Instant start = NanoClock.INSTANCE.instant();
		String result = promise.get();
		System.out.println("testGetWithOnSuccess: Got result \""+result+"\" after "+StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS));
		
		assertThat(result).isEqualTo("hello, world");
	}
	
	@Test
	public void testGetWithCancel() throws Exception {
		
		HubPromise<String> promise = new HubPromise<>();
		
		executor.submit(() -> {
			try {
				Thread.sleep(100L);
				promise.onSuccess("hello, world");
			} catch (InterruptedException e) {
				//ignore
			}
		});
		promise.cancel();
		
		String result = null;
		try {
			result = promise.get();
			throw new AssertionError("We should have gotten a CanceledException.");
		} catch(CanceledException ce) {
			//expected
		}
		
		assertThat(result).isNull();
	}
	
	@Test
	public void testGetWithAsyncCallback() throws Exception {
		
		LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
		AsyncCallback<String> acb = new TestAsyncCallback(queue);
		
		HubPromise<String> promise = new HubPromise<>();
		
		executor.submit(() -> {
			try {
				Thread.sleep(100L);
				promise.onSuccess("hello, world");
			} catch (InterruptedException e) {
				//ignore
			}
		});
		
		Instant start = NanoClock.INSTANCE.instant();
		promise.get(acb);
		
		String result = queue.poll(1, TimeUnit.SECONDS);
		
		System.out.println("testGetWithAsyncCallback: Got result \""+result+"\" after "+StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS));
		
		assertThat(result).isEqualTo("hello, world");
	}
	
	@Test
	public void testGetWithMultipleAsyncCallback() throws Exception {
		
		LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
		
		int count = 10;
		AsyncCallback<String>[] acbs = new AsyncCallback[count]; 
		for (int i=0; i<count; ++i) {
			acbs[i] = new TestAsyncCallback(queue);
		}
		
		HubPromise<String> promise = new HubPromise<>();
		
		executor.submit(() -> {
			try {
				Thread.sleep(100L);
				promise.onSuccess("hello, world");
			} catch (InterruptedException e) {
				//ignore
			}
		});
		
		for (AsyncCallback<String> acb : acbs) {
			promise.get(acb);
		}
		
		Thread.sleep(1000L);
		
		assertThat(queue.size()).isEqualTo(count);
		
		while(!queue.isEmpty()) {
			String result = queue.take();
			assertThat(result).isEqualTo("hello, world");
		}
	}
	
	@Test
	public void testGetWithAsyncCallbackAndLateRegistration() throws Exception {
		
		LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
		AsyncCallback<String> acb = new TestAsyncCallback(queue);
		
		HubPromise<String> promise = new HubPromise<>("hello, world");
		
		promise.get(acb);
		
		String result = queue.poll(1, TimeUnit.SECONDS);
		
		System.out.println("testGetWithAsyncCallbackAndLateRegistration: Got result \""+result+"\"");
		
		assertThat(result).isEqualTo("hello, world");
	}
	
	@Test
	public void testStateChangeFailure() throws Exception {
		
		HubPromise<String> promise = new HubPromise<>();
		
		promise.onFailure(new Exception());
		try {
			promise.onFailure(new Exception());
			throw new AssertionError("We should have gotten a IllegalStateException.");
		} catch(IllegalStateException ise) {
			//expected
		}
	}
	
	@Test
	public void testStateChangeFailureWithNullException() throws Exception {
		
		HubPromise<String> promise = new HubPromise<>();
		
		promise.onFailure(null);
		try {
			promise.onFailure(new Exception());
			throw new AssertionError("We should have gotten a IllegalStateException.");
		} catch(IllegalStateException ise) {
			//expected
		}
	}
	
	@Test
	public void testStateChangeFailureWithSuccess() throws Exception {
		
		HubPromise<String> promise = new HubPromise<>();
		
		promise.onFailure(new Exception());
		try {
			promise.onSuccess("hello, world");
			throw new AssertionError("We should have gotten a IllegalStateException.");
		} catch(IllegalStateException ise) {
			//expected
		}
	}
	
	@Test
	public void testStateChangeFailureWithAsync() throws Exception {
		
		HubPromise<String> promise = new HubPromise<>();
		
		promise.onFailure(new Exception());
		
		TestAsyncCallback acb = new TestAsyncCallback(null);
		promise.get(acb);
		
		assertThat(acb.getError()).isNotNull();
	}
	
	@Test
	public void testStateChangeFailureWithAsync2() throws Exception {
		
		HubPromise<String> promise = new HubPromise<>();
		
		promise.onFailure(new Exception());
		
		TestAsyncCallback acb = new TestAsyncCallback(null);
		promise.get(acb);
		
		assertThat(acb.getError()).isNotNull();
		
		try {
			promise.onSuccess("hello, world");
			throw new AssertionError("We should have gotten a IllegalStateException.");
		} catch(IllegalStateException ise) {
			//expected
		}

	}
	
	@Test
	public void testGetWithOnSuccessAndWait() throws Exception {
		
		HubPromise<String> promise = new HubPromise<>();
		
		executor.submit(() -> {
			try {
				Thread.sleep(100L);
				promise.onSuccess("hello, world");
			} catch (InterruptedException e) {
				//ignore
			}
		});
		
		PromiseState state = promise.waitFor();
		assertThat(state).isEqualTo(PromiseState.done);
		
		Instant start = NanoClock.INSTANCE.instant();
		String result = promise.get();
		Instant end = NanoClock.INSTANCE.instant();
		Duration duration = Duration.between(start, end);
		
		assertThat(duration.toMillis()).isLessThan(100);
		
		System.out.println("testGetWithOnSuccessAndWait: Got result \""+result+"\" after "+StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS));
		
		assertThat(result).isEqualTo("hello, world");
	}
	
	@Test
	public void testGetWithOnSuccessAndWaitTimeout() throws Exception {
		
		HubPromise<String> promise = new HubPromise<>();
		
		executor.submit(() -> {
			try {
				Thread.sleep(100L);
				promise.onSuccess("hello, world");
			} catch (InterruptedException e) {
				//ignore
			}
		});
		
		PromiseState state = promise.waitFor(10, TimeUnit.MILLISECONDS);
		assertThat(state).isEqualTo(PromiseState.outstanding);
		
		Instant start = NanoClock.INSTANCE.instant();
		String result = promise.get();
		
		System.out.println("testGetWithOnSuccessAndWaitTimeout: Got result \""+result+"\" after "+StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS));
		
		assertThat(result).isEqualTo("hello, world");
	}
}
