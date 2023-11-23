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
package com.braintribe.model.processing.lock.dmb.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.processing.lock.api.LockBuilder;

public class DmbLockManagerTest {

	private static DmbLockManager mgr = null;

	@BeforeClass
	public static void beforeClass() throws Exception {
		mgr = new DmbLockManager();
	}

	@Test
	public void simpleTest() throws Exception {

		Lock lock = createLock(true);
		lock.lock();
		try {
			System.out.println("simpleTest...so far, so good");
		} finally {
			lock.unlock();
		}

	}

	@Test
	public void multiThreadedWriter() throws Exception {

		int iterations = 100;
		int workers = 10;
		ExecutorService service = Executors.newFixedThreadPool(workers);
		File tempFile = File.createTempFile("multiThreadedWriter", ".txt");
		ExclusiveLockWriter.writeNumber(tempFile, 0);

		Lock lock = createLock(true);

		try {
			List<Future<?>> futures = new ArrayList<>();

			for (int i = 0; i < workers; ++i) {
				futures.add(service.submit(new ExclusiveLockWriter(i, tempFile, iterations, lock)));
			}

			for (Future<?> f : futures) {
				f.get();
			}

			int actual = ExclusiveLockWriter.getNumber(tempFile);
			int expected = workers * iterations;

			if (actual == expected) {
				System.out.println("All well.");
			} else {
				throw new Exception("Expected: " + expected + ", actual: " + actual);
			}

		} finally {
			service.shutdown();
			tempFile.delete();
		}

	}

	@Test
	public void multipleReadWriteTest() throws Exception {

		LockBuilder lb = createLockBuilder();
		Lock writeLock = lb.exclusive();
		Lock readLock = lb.shared();

		int iterations = 100;
		int workers = 10;
		ExecutorService service = Executors.newFixedThreadPool(workers);
		File tempFile = File.createTempFile("multiThreadedWriter", ".txt");
		ExclusiveLockWriter.writeNumber(tempFile, 0);

		try {
			List<Future<?>> futures = new ArrayList<>();

			for (int i = 0; i < workers; ++i) {
				futures.add(service.submit(new ExclusiveLockWriter(i, tempFile, iterations, writeLock)));
				futures.add(service.submit(new SharedLockReader(i, tempFile, iterations, readLock)));
			}

			for (Future<?> f : futures) {
				f.get();
			}

			int actual = ExclusiveLockWriter.getNumber(tempFile);
			int expected = workers * iterations;

			if (actual == expected) {
				System.out.println("All well.");
			} else {
				throw new Exception("Expected: " + expected + ", actual: " + actual);
			}

		} finally {
			service.shutdown();
			tempFile.delete();
		}

	}

	@Test
	public void simpleReadWriteTest() throws Exception {

		LockBuilder lb = createLockBuilder();
		Lock writeLock = lb.exclusive();
		Lock readLock = lb.shared();

		int readers = 10;
		int writers = 10;
		CountDownLatch readersLockCountdown = new CountDownLatch(readers);
		CountDownLatch writersLockCountdown = new CountDownLatch(1);
		ExecutorService service = Executors.newFixedThreadPool(readers + writers);

		try {
			List<Future<?>> futures = new ArrayList<>();
			List<LockHolder> readLockHolders = new ArrayList<>();
			List<LockHolder> writeLockHolders = new ArrayList<>();

			// Create all readers; they should have a read lock immediately

			for (int i = 0; i < readers; ++i) {

				LockHolder lockHolder = new LockHolder(readLock, readersLockCountdown);
				readLockHolders.add(lockHolder);

				futures.add(service.submit(lockHolder));
			}

			readersLockCountdown.await(1, TimeUnit.MINUTES);
			if (readersLockCountdown.getCount() > 0) {
				throw new Exception("Not all readers acquired a lock within the required time.");
			}

			for (LockHolder lh : readLockHolders) {
				assertThat(lh.hasLock()).isTrue();
			}

			for (LockHolder lh : readLockHolders) {
				lh.release();
			}

			// Create all writers, only one should have a lock

			for (int i = 0; i < writers; ++i) {

				LockHolder lockHolder = new LockHolder(writeLock, writersLockCountdown);
				writeLockHolders.add(lockHolder);

				futures.add(service.submit(lockHolder));
			}

			writersLockCountdown.await(1, TimeUnit.MINUTES);
			if (readersLockCountdown.getCount() > 0) {
				throw new Exception("Not a single writer acquired a lock within the required time.");
			}

			int writeLockCount = 0;
			for (LockHolder lh : writeLockHolders) {
				if (lh.hasLock()) {
					writeLockCount++;
				}
			}
			assertThat(writeLockCount).isEqualTo(1);

			// Cleanup

			for (LockHolder lh : writeLockHolders) {
				lh.release();
			}

		} finally {
			service.shutdown();
		}

	}

	@Test
	public void foreignThreadUnlock() throws Exception {

		final Lock lock = createLock(true);

		Thread first = Thread.ofVirtual().unstarted(() -> {
			lock.lock();
		});

		first.setName("Locker");
		first.start();
		first.join(10000L);

		boolean tryLockResult = lock.tryLock();
		assertThat(tryLockResult).isFalse();

		lock.unlock();
	}

	@Test
	public void evictionTest() throws Exception {

		final Lock initialLock = createLock(true);
		mgr.setEvictionThreshold(1);
		initialLock.lock();
		try {

			for (int i = 0; i < 1000; ++i) {
				createLock(true);
			}

			// Initial lock (because it is locked) and the newest should remain

			assertThat(mgr.locks.size()).isEqualTo(2);

		} finally {
			initialLock.unlock();
		}

	}

	@Test
	public void multiThreadedWriterWithFrequentEviction() throws Exception {

		int iterations = 100;
		int workers = 100;
		ExecutorService service = Executors.newFixedThreadPool(workers);
		File tempFile = File.createTempFile("multiThreadedWriter", ".txt");
		ExclusiveLockWriter.writeNumber(tempFile, 0);

		Lock lock = createLock(true);
		mgr.setEvictionThreshold(1);
		mgr.setEvictionInterval(1L);

		try {
			List<Future<?>> futures = new ArrayList<>();

			for (int i = 0; i < workers; ++i) {
				futures.add(service.submit(new ExclusiveLockWriter(i, tempFile, iterations, lock)));
			}

			for (Future<?> f : futures) {
				f.get();
			}

			int actual = ExclusiveLockWriter.getNumber(tempFile);
			int expected = workers * iterations;

			if (actual == expected) {
				System.out.println("All well.");
			} else {
				throw new Exception("Expected: " + expected + ", actual: " + actual);
			}

		} finally {
			service.shutdown();
			tempFile.delete();
		}

	}

	private LockBuilder createLockBuilder() throws Exception {
		String identifier = UUID.randomUUID().toString();
		LockBuilder lockBuilder = mgr.forIdentifier(identifier);
		return lockBuilder;
	}
	private Lock createLock(boolean exclusive) throws Exception {
		LockBuilder lockBuilder = createLockBuilder();
		if (exclusive) {
			return lockBuilder.exclusive();
		} else {
			return lockBuilder.shared();
		}
	}

}
