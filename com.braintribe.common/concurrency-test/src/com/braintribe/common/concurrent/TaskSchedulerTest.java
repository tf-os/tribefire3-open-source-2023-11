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
package com.braintribe.common.concurrent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link TaskScheduler}
 * 
 * @author peter.gazdik
 */
public class TaskSchedulerTest {

	private int runs;

	private ScheduledExecutorService executor;
	private TaskScheduler scheduler;

	@Before
	public void setup() {
		executor = Executors.newScheduledThreadPool(10);
		scheduler = taskScheduler();
	}

	private TaskScheduler taskScheduler() {
		TaskSchedulerImpl bean = new TaskSchedulerImpl();
		bean.setName("Platform-Task-Scheduler");
		bean.setExecutor(executor);

		return bean;
	}

	@After
	public void shutdown() throws InterruptedException {
		executor.shutdownNow();
		executor.awaitTermination(10, TimeUnit.SECONDS);
	}

	// ###############################################
	// ## . . . . . . . . . Tests . . . . . . . . . ##
	// ###############################################

	@Test
	public void smokeTest() throws Exception {
		scheduler.scheduleAtFixedRate("Job1", this::task_IncreaseRuns, 0, 20, TimeUnit.MILLISECONDS) //
				.interruptOnCancel(false) //
				.done();

		sleep(100);

		scheduler.awaitTermination();

		// Check task was called at least once, but the value is typically 7/9 for my test runs
		assertThat(runs).isGreaterThan(1);
	}

	private void task_IncreaseRuns() {
		runs++;
	}

	/** The scheduler uses default {@link TaskScheduleBuilder#errorHandler(TaskErrorHandler)}. */
	@Test
	public void stopsRunningWithException() throws Exception {
		scheduler.scheduleAtFixedRate("Job1", this::task_ThrowException, 0, 10, TimeUnit.MILLISECONDS) //
				.done();

		sleep(100);

		scheduler.awaitTermination();

		assertThat(runs).isEqualTo(1);
	}

	@Test
	public void keepsRunningWhenExceptionIgnored() throws Exception {
		scheduler.scheduleAtFixedRate("Job1", this::task_ThrowException, 0, 10, TimeUnit.MILLISECONDS) //
				.errorHandler(e -> {/* NO OP */}) //
				.done();

		sleep(100);

		scheduler.awaitTermination();

		// Check task was called at least once, but the value is typically 7/9 for my test runs
		assertThat(runs).isGreaterThan(1);
	}

	private void task_ThrowException() {
		runs++;
		throw new RuntimeException();
	}

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			throw new RuntimeException("Unexpected interrupt", e);
		}
	}
}
