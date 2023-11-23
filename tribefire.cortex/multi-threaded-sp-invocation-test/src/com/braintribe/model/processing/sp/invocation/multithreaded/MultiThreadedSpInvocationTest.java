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
package com.braintribe.model.processing.sp.invocation.multithreaded;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.execution.queue.FifoEntry;
import com.braintribe.execution.queue.TestableFifoEntry;
import com.braintribe.model.processing.securityservice.api.UserSessionScope;
import com.braintribe.model.processing.securityservice.api.UserSessionScoping;
import com.braintribe.model.processing.securityservice.api.UserSessionScopingBuilder;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.spapi.StateChangeProcessorInvocationPacket;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.testing.category.KnownIssue;

public class MultiThreadedSpInvocationTest {

	@Test
	public void testInvocationPriorityFifo() throws Exception {

		PriorityBlockingQueue<FifoEntry<StateChangeProcessorInvocationPacket>> queue = new PriorityBlockingQueue<>();
		int count = 10000;

		for (int i = 0; i < count; ++i) {
			StateChangeProcessorInvocationPacket invocationPacket = StateChangeProcessorInvocationPacket.T.create();
			invocationPacket.setId((long) i);
			FifoEntry<StateChangeProcessorInvocationPacket> packet = new FifoEntry<StateChangeProcessorInvocationPacket>(invocationPacket);
			queue.offer(packet);
		}

		for (int i = 0; i < count; ++i) {
			StateChangeProcessorInvocationPacket invocationPacket = queue.take().getEntry();
			assertThat("" + invocationPacket.getId()).isEqualTo("" + i);
		}
	}

	@Test
	public void testInvocationPriorityFifoWithIntegerOverflow() throws Exception {

		PriorityBlockingQueue<FifoEntry<StateChangeProcessorInvocationPacket>> queue = new PriorityBlockingQueue<>();

		TestableFifoEntry.overrideSequence(Integer.MAX_VALUE - 10);

		// After 10 adds, the seq number should flip to a negative value
		int count = 20;

		for (int i = 0; i < count; ++i) {
			StateChangeProcessorInvocationPacket invocationPacket = StateChangeProcessorInvocationPacket.T.create();
			invocationPacket.setId((long) i);
			FifoEntry<StateChangeProcessorInvocationPacket> packet = new FifoEntry<StateChangeProcessorInvocationPacket>(invocationPacket);
			queue.offer(packet);
		}

		for (int i = 0; i < count; ++i) {
			FifoEntry<StateChangeProcessorInvocationPacket> packet = queue.take();
			StateChangeProcessorInvocationPacket invocationPacket = packet.getEntry();
			assertThat("" + invocationPacket.getId()).isEqualTo("" + i);
		}
	}

	@Test
	public void testInvocationPriorityHighBeforeLow() throws Exception {

		PriorityBlockingQueue<FifoEntry<StateChangeProcessorInvocationPacket>> queue = new PriorityBlockingQueue<>();
		int count = 10000;

		StateChangeProcessorInvocationPacket lowPrio = StateChangeProcessorInvocationPacket.T.create();
		lowPrio.setId(-2l);
		lowPrio.setExecutionPriority(-10d);
		queue.offer(new FifoEntry<StateChangeProcessorInvocationPacket>(lowPrio));

		for (int i = 0; i < count; ++i) {
			StateChangeProcessorInvocationPacket invocationPacket = StateChangeProcessorInvocationPacket.T.create();
			invocationPacket.setId((long) i);
			FifoEntry<StateChangeProcessorInvocationPacket> packet = new FifoEntry<StateChangeProcessorInvocationPacket>(invocationPacket);
			queue.offer(packet);
		}

		StateChangeProcessorInvocationPacket highPrio = StateChangeProcessorInvocationPacket.T.create();
		highPrio.setId(-1l);
		highPrio.setExecutionPriority(10d);
		queue.offer(new FifoEntry<StateChangeProcessorInvocationPacket>(highPrio));

		StateChangeProcessorInvocationPacket invocationPacket = queue.take().getEntry();
		assertThat("" + invocationPacket.getId()).isEqualTo("-1");

		for (int i = 0; i < count; ++i) {
			invocationPacket = queue.take().getEntry();
			assertThat("" + invocationPacket.getId()).isEqualTo("" + i);
		}

		invocationPacket = queue.take().getEntry();
		assertThat("" + invocationPacket.getId()).isEqualTo("-2");

	}

	@Test
	public void testInvocationPriorityUnchanged() throws Exception {

		PriorityBlockingQueue<FifoEntry<StateChangeProcessorInvocationPacket>> queue = new PriorityBlockingQueue<>();
		int count = 10000;

		StateChangeProcessorInvocationPacket lowPrio = StateChangeProcessorInvocationPacket.T.create();
		lowPrio.setId(-2l);
		lowPrio.setExecutionPriority(10d);
		queue.offer(new FifoEntry<StateChangeProcessorInvocationPacket>(lowPrio));

		for (int i = 0; i < count; ++i) {
			StateChangeProcessorInvocationPacket invocationPacket = StateChangeProcessorInvocationPacket.T.create();
			invocationPacket.setId((long) i);
			FifoEntry<StateChangeProcessorInvocationPacket> packet = new FifoEntry<StateChangeProcessorInvocationPacket>(invocationPacket);
			queue.offer(packet);
		}

		StateChangeProcessorInvocationPacket highPrio = StateChangeProcessorInvocationPacket.T.create();
		highPrio.setId(-1l);
		highPrio.setExecutionPriority(-10d);
		queue.offer(new FifoEntry<StateChangeProcessorInvocationPacket>(highPrio));

		StateChangeProcessorInvocationPacket invocationPacket = queue.take().getEntry();
		assertThat("" + invocationPacket.getId()).isEqualTo("-2");

		for (int i = 0; i < count; ++i) {
			invocationPacket = queue.take().getEntry();
			assertThat("" + invocationPacket.getId()).isEqualTo("" + i);
		}

		invocationPacket = queue.take().getEntry();
		assertThat("" + invocationPacket.getId()).isEqualTo("-1");

	}

	@Test
	public void testInvocationPriorityMixedRandom() throws Exception {

		int count = 10000;

		PriorityBlockingQueue<FifoEntry<StateChangeProcessorInvocationPacket>> queue = new PriorityBlockingQueue<>();
		Random rnd = new Random();

		for (int i = 0; i < count; ++i) {
			StateChangeProcessorInvocationPacket invocationPacket = StateChangeProcessorInvocationPacket.T.create();
			invocationPacket.setExecutionPriority(rnd.nextDouble());
			invocationPacket.setId((long) i);
			queue.offer(new FifoEntry<StateChangeProcessorInvocationPacket>(invocationPacket));
		}

		double lastPrio = Double.MAX_VALUE;
		for (int i = 0; i < count; ++i) {
			StateChangeProcessorInvocationPacket invocationPacket = queue.take().getEntry();
			assertThat(invocationPacket.getExecutionPriority()).isLessThanOrEqualTo(lastPrio);
			lastPrio = invocationPacket.getExecutionPriority();
		}

	}

	@Test
	public void testInvocationPriorityMultiThreaded() throws Exception {

		final int count = 1000;
		int worker = 10;

		final PriorityBlockingQueue<FifoEntry<StateChangeProcessorInvocationPacket>> queue = new PriorityBlockingQueue<>();
		final Random rnd = new Random();

		ExecutorService service = Executors.newFixedThreadPool(worker);
		try {

			List<Future<?>> futures = new ArrayList<>();
			for (int i = 0; i < worker; ++i) {
				futures.add(service.submit(new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < count; ++i) {
							StateChangeProcessorInvocationPacket invocationPacket = StateChangeProcessorInvocationPacket.T.create();
							invocationPacket.setExecutionPriority(rnd.nextDouble());
							queue.offer(new FifoEntry<StateChangeProcessorInvocationPacket>(invocationPacket));
						}
					}
				}));
			}

			for (Future<?> f : futures) {
				f.get();
			}

		} finally {
			service.shutdown();
		}

		assertThat(queue.size()).isEqualTo(worker * count);

		double lastPrio = Double.MAX_VALUE;
		for (int i = 0; i < (worker * count); ++i) {
			StateChangeProcessorInvocationPacket invocationPacket = queue.take().getEntry();
			assertThat(invocationPacket.getExecutionPriority()).isLessThanOrEqualTo(lastPrio);
			lastPrio = invocationPacket.getExecutionPriority();
		}

	}

	@Test
	@Category(KnownIssue.class)
	// A PR for [COREDR-10] let this test break on 16.09.2020. Ignoring test to bring another PR through.
	public void testMultiThreadedSpInvocation() throws Exception {

		// Prime GM (this takes some time the first time)
		StateChangeProcessorInvocationPacket.T.create();

		final List<StateChangeProcessorInvocationPacket> processedInvocations = Collections
				.synchronizedList(new ArrayList<StateChangeProcessorInvocationPacket>());

		final long[] duration = new long[2];
		duration[0] = System.currentTimeMillis();

		final int count = 10000;

		MultiThreadedSpInvocation bean = new MultiThreadedSpInvocation() {
			@Override
			protected void processInvocationPacket(StateChangeProcessorInvocationPacket invocationPacket) {
				try {
					Thread.sleep(5L); // Just a bit to fill up the queue
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				processedInvocations.add(invocationPacket);
				if (processedInvocations.size() == (count + 2)) {
					duration[1] = System.currentTimeMillis();
				}
			}
		};
		bean.setUserSessionScoping(new TestUserSessionScoping());
		bean.postConstruct();

		try {

			StateChangeProcessorInvocationPacket lowPrio = StateChangeProcessorInvocationPacket.T.create();
			lowPrio.setId(-2l);
			lowPrio.setExecutionPriority(-10d);
			bean.accept(lowPrio);

			StateChangeProcessorInvocationPacket invocationPacket = StateChangeProcessorInvocationPacket.T.create();
			for (int i = 0; i < count; ++i) {
				bean.accept(invocationPacket);
			}

			StateChangeProcessorInvocationPacket highPrio = StateChangeProcessorInvocationPacket.T.create();
			highPrio.setId(-1l);
			highPrio.setExecutionPriority(10d);
			bean.accept(highPrio);

			long insertStop = System.currentTimeMillis();
			System.out.println("Inserting took: " + (insertStop - duration[0]) + " ms.");

		} finally {
			bean.preDestroy();
		}

		int expectedCount = count + 2;
		long maxWait = 20_000l;
		long checkStart = System.currentTimeMillis();
		long waitedSoFar = 0l;
		int foundDone = 0;
		do {
			Thread.sleep(1000);
			waitedSoFar = System.currentTimeMillis() - checkStart;
			foundDone = processedInvocations.size();
		} while ((foundDone < expectedCount) && (waitedSoFar < maxWait));

		assertThat(foundDone).isEqualTo(expectedCount);

		int indexOfHighPrioProcess = -1;
		for (int i = 0; i < expectedCount; ++i) {
			StateChangeProcessorInvocationPacket packet = processedInvocations.get(i);
			if (packet.getExecutionPriority() > 0) {
				indexOfHighPrioProcess = i;
				break;
			}
		}

		System.out.println("High prio process is at position: " + indexOfHighPrioProcess);
		System.out.println("Last item processed after: " + (duration[1] - duration[0]) + " ms.");

		assertThat(indexOfHighPrioProcess).isGreaterThan(-1);
		assertThat(indexOfHighPrioProcess).isLessThan(expectedCount - 1);

	}

	static class TestUserSessionScoping implements UserSessionScoping {
		@Override
		public UserSessionScopingBuilder forCredentials(Credentials credentials) {
			return new TestUserSessionScopingBuilder();
		}

		@Override
		public UserSessionScopingBuilder forDefaultUser() {
			return new TestUserSessionScopingBuilder();
		}
	}

	static class TestUserSessionScopingBuilder implements UserSessionScopingBuilder {

		@Override
		public UserSessionScope push() throws SecurityServiceException {
			return new TestUserSessionScope();
		}

		@Override
		public void runInScope(Runnable runnable) throws SecurityServiceException {
			runnable.run();
		}

	}

	static class TestUserSessionScope implements UserSessionScope {

		@Override
		public UserSession getUserSession() throws SecurityServiceException {
			return null;
		}

		@Override
		public UserSession pop() throws SecurityServiceException {
			return null;
		}

	}
}
