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
package tribefire.cortex.leadership.impl;

import static com.braintribe.utils.SysPrint.spOut;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.concurrent.TaskScheduler;
import com.braintribe.common.concurrent.TaskSchedulerImpl;
import com.braintribe.model.processing.lock.api.Locking;
import com.braintribe.model.processing.lock.impl.SemaphoreBasedLocking;

import tribefire.cortex.leadership.api.LeadershipContext;
import tribefire.cortex.leadership.api.LeadershipListener;
import tribefire.cortex.leadership.api.LeadershipManager;

/**
 * Tests for {@link LockingBasedLeadershipManager}
 */
public class LockingBasedLeadershipManagerTest {

	public static final long CDL_TIMEOUT_SEC = 10 * 60 * 60;

	private static int LEADERSHIP_REFRESH_INTERVAL_MS = 10;

	private static boolean LOG_OUTPUT = false;

	private ExecutorService executor;
	private ScheduledExecutorService scheduledThreadPool;
	private Locking locking;

	@Before
	public void setup() {
		this.executor = Executors.newCachedThreadPool();
		this.scheduledThreadPool = Executors.newScheduledThreadPool(20);
		this.locking = new SemaphoreBasedLocking();
	}

	@After
	public void shutDown() {
		executor.shutdown();
		scheduledThreadPool.shutdown();
	}

	@Test
	public void basicTest() {
		runTest(5, 5, 5);
	}

	@Test
	public void basicConcurrentTest() {
		runTest(20, 20, 20);
	}

	@Test
	public void manyMachinesConcurrentTest() {
		runTest(25, 1, 1);
	}

	@Test
	public void manyDomainsConcurrentTest() {
		runTest(1, 100, 1);
	}

	@Test
	public void manyListenersConcurrentTest() {
		runTest(1, 1, 100);
	}

	private void runTest(int nMachines, int nDomains, int nListeners) {
		new LeadershipTest(nMachines, nDomains, nListeners).run();
	}

	class LeadershipTest {

		private final List<SingleThreadTest> singleTests = newList();

		public LeadershipTest(int nMachines, int nDomains, int nListeners) {
			for (int m = 0; m < nMachines; m++) {
				LockingBasedLeadershipManager leadershipManager = leadershipManger(m);

				for (int d = 0; d < nDomains; d++)
					for (int l = 0; l < nListeners; l++)
						singleTests.add(new SingleThreadTest(m, d, l, leadershipManager));
			}
		}

		private LockingBasedLeadershipManager leadershipManger(int machineId) {
			LockingBasedLeadershipManager result = new LockingBasedLeadershipManager();
			result.setName("Machine-" + machineId);
			result.setLocking(locking);

			taskScheduler().scheduleAtFixedRate( //
					"leadership-refresher-machine-" + machineId, //
					() -> refreshLeadershipsForEligibleDomains(result, machineId), //
					LEADERSHIP_REFRESH_INTERVAL_MS, LEADERSHIP_REFRESH_INTERVAL_MS, TimeUnit.MILLISECONDS) //
					.done();

			return result;
		}

		@SuppressWarnings("unused") // machineId is here just in case we need to debug
		private void refreshLeadershipsForEligibleDomains(LockingBasedLeadershipManager result, int machineId) {
			result.refreshLeadershipsForEligibleDomains();
		}

		private TaskScheduler taskScheduler() {
			TaskSchedulerImpl bean = new TaskSchedulerImpl();
			bean.setName("test-scheduler");
			bean.setExecutor(scheduledThreadPool);

			return bean;
		}

		public void run() {
			List<Future<?>> futures = submitTasks();
			waitForTasksToFinish(futures);
			assertLeadershipGrantedToEveryone();
		}

		private List<Future<?>> submitTasks() {
			List<Future<?>> futures = newList();
			for (SingleThreadTest singleThreadTest : singleTests) {
				Future<?> future = executor.submit(singleThreadTest);
				futures.add(future);
			}
			return futures;
		}

		private void waitForTasksToFinish(List<Future<?>> futures) {
			for (Future<?> future : futures) {
				try {
					future.get(10, TimeUnit.HOURS);
				} catch (Exception e) {
					throw new RuntimeException("Error while waiting for test.", e);
				}
			}
		}

		private void assertLeadershipGrantedToEveryone() {
			for (SingleThreadTest singleThreadTest : singleTests)
				singleThreadTest.assertWasGranted();
		}

	}

	class SingleThreadTest implements Runnable, LeadershipListener {

		private final LeadershipManager leadershipManger;
		private final int machineId;
		private final int domainId;
		private final int listenerId;
		private final CountDownLatch cdl = new CountDownLatch(1);
		private boolean wasGranted;

		public SingleThreadTest(int m, int d, int l, LeadershipManager leadershipManger) {
			this.machineId = m;
			this.domainId = d;
			this.listenerId = l;
			this.leadershipManger = leadershipManger;
		}

		@Override
		public void run() {
			String domain = "domain-" + domainId;

			maybeLog("ADDING");
			leadershipManger.addLeadershipListener(domain, this);

			wasGranted = awaitCdl();

			maybeLog("REMOVING");
			leadershipManger.removeLeadershipListener(domain, this);
		}

		private boolean awaitCdl() {
			try {
				return cdl.await(CDL_TIMEOUT_SEC, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				throw new RuntimeException("Unexpected interrupt.", e);
			}
		}

		@Override
		public void onLeadershipGranted(LeadershipContext context) {
			maybeLog("GRANTED");
			cdl.countDown();
		}

		@Override
		public void surrenderLeadership(LeadershipContext context) {
			// ignored
		}

		public void assertWasGranted() {
			assertThat(wasGranted).isTrue();
		}

		private void maybeLog(String what) {
			if (LOG_OUTPUT)
				spOut(what + " D:" + domainId + " M:" + machineId + " L:" + listenerId);
		}

		@Override
		public String toString() {
			return "LeadershipTestTask [machineId=" + machineId + ", domainId=" + domainId + ", listenerId=" + listenerId + "]";
		}
	}

}
