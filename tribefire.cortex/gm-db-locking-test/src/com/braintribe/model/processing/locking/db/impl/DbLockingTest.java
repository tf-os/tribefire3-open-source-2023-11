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
package com.braintribe.model.processing.locking.db.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

import org.junit.Test;

import com.braintribe.common.db.DbVendor;
import com.braintribe.model.processing.locking.db.test.wire.contract.DbLockingTestContract;
import com.braintribe.utils.StringTools;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

/**
 * Tests for {@link DbLocking}
 * 
 * @see DbLockingTestContract
 */
public class DbLockingTest extends AbstractDbLockingTestBase {

	public static final String IDENTIFIER = "someIdentifier";

	public DbLockingTest(DbVendor vendor) {
		super(vendor);
	}

	// ###############################################
	// ## . . . . . . . . . Tests . . . . . . . . . ##
	// ###############################################

	@Test(timeout = 5000)
	public void testLockTryLock() throws Exception {
		Lock lock = createLock();
		lock.lock();
		lock.unlock();

		assertThat(lock.tryLock()).isTrue();
	}

	/* Test a long identifier */
	@Test(timeout = 5000)
	public void testLockWithLongIdentifier() throws Exception {
		String id = "MoreThan300Chars" + StringTools.repeat("1234567890", 30);
		Lock lock = createLock(id);
		lock.lock();
		lock.unlock();

		assertThat(lock.tryLock()).isTrue();
	}
	/**
	 * Simulate that a lock is stuck because of e.g. a crash; check if tryLock without parameter works
	 */
	@Test(timeout = 5000)
	public void testLockRecover1() throws Exception {
		Lock lock = createLock();
		lock.lock();

		assertThat(lock.tryLock()).isFalse();
	}

	/**
	 * Simulate that a lock is stuck because of e.g. a crash; there is a lock - check if tryLock with parameter works
	 */
	@Test(timeout = 5000)
	public void testLockRecover2() throws Exception {
		Lock lock = createLock();
		lock.lock();

		boolean tryLock = lock.tryLock(1, TimeUnit.SECONDS);
		assertThat(tryLock).isFalse();
	}

	@Test(timeout = 15000, expected = TimeoutException.class)
	public void testLockLock() throws Exception {
		Lock lock = createLock();
		lock.lock();

		// This will time-out, because the lock is already locked
		// And thus also tests the refresher, as the lock expires after 2 seconds
		runInParallelWithTimeout(3, TimeUnit.SECONDS, lock::lock);
	}

	// ###############################################
	// ## . . . . . . . . Helpers . . . . . . . . . ##
	// ###############################################

	private void runInParallelWithTimeout(long timeout, TimeUnit unit, Runnable runnable) throws TimeoutException, InterruptedException {
		ExecutorService pool = Executors.newFixedThreadPool(1);

		try {
			TimeLimiter timeLimiter = SimpleTimeLimiter.create(pool);
			timeLimiter.runWithTimeout(runnable, timeout, unit);

		} finally {
			pool.shutdown();
		}
	}

	private Lock createLock() {
		return createLock(IDENTIFIER);
	}

	private Lock createLock(String identifier) {
		return locking.forIdentifier(identifier).readLock();
	}

}
