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

import static com.braintribe.utils.SysPrint.spOut;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This is not a real test, just main method to show task scheduling works.
 * <p>
 * <ul>
 * <li>We start {@link PeriodicJob} every 500 ms.
 * <li>Each run sleeps 250 and prints before/after.
 * <li>1st job runs 500-750 ms after job scheduled.
 * <li>2nd job runs 1000-1250 ms after job scheduled.
 * <li>Main thread calls shutdown 1100ms after job scheduled.
 * <li>Job is then either interrupted, or runs till the end (depending on {@link #INTERRUPT_TASKS_ON_SHUTDOWN}), and no job starts after that.
 * </ul>
 * 
 * @author peter.gazdik
 */
public class TaskSchedulerMain {

	private static final boolean INTERRUPT_TASKS_ON_SHUTDOWN = false;

	public static void main(String[] args) {
		new TaskSchedulerMain().run();
		spOut("Ending Main Thread");
	}

	private void run() {
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
		TaskScheduler scheduler = taskScheduler(executor);

		scheduler.scheduleAtFixedRate("Job1", new PeriodicJob("Job1"), 500, 500, TimeUnit.MILLISECONDS) //
				.interruptOnCancel(INTERRUPT_TASKS_ON_SHUTDOWN) //
				.done();

		spOut("Main is going to sleep");
		try {
			Thread.sleep(1100);
		} catch (InterruptedException e) {
			throw new RuntimeException("", e);
		}

		spOut("Main woke up and will shut down.");

		scheduler.awaitTermination();

		try {
			executor.shutdownNow();
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private TaskScheduler taskScheduler(ScheduledExecutorService executor) {
		TaskSchedulerImpl bean = new TaskSchedulerImpl();
		bean.setName("Platform-Task-Scheduler");
		bean.setExecutor(executor);

		return bean;
	}

	private class PeriodicJob implements Runnable {

		private final String name;

		private int run = 0;

		public PeriodicJob(String name) {
			this.name = name;
		}

		@Override
		public void run() {
			print("[" + name + "] #" + ++run + " STARTED");

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				print("[" + name + "] #" + run + " INTERRUPTED!!!!");
				return;
			}

			print("[" + name + "] #" + run + " finished");
		}

		private void print(String s) {
			System.out.println(s);
		}

	}

}
