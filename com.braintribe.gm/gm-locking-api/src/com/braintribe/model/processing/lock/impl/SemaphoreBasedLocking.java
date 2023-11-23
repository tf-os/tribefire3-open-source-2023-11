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
package com.braintribe.model.processing.lock.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import com.braintribe.model.processing.lock.api.Locking;

/**
 * @author peter.gazdik
 */
public class SemaphoreBasedLocking implements Locking {

	private final Map<String, ReadWriteLock> locks = new ConcurrentHashMap<>();

	@Override
	public ReadWriteLock forIdentifier(String id) {
		return locks.computeIfAbsent(id, k -> new SemaphoreBasedRwLock());
	}

	class SemaphoreBasedRwLock implements ReadWriteLock {

		private final SemaphoreBasedLock lock = new SemaphoreBasedLock();

		@Override
		public Lock readLock() {
			return lock;
		}

		@Override
		public Lock writeLock() {
			return lock;
		}

	}

	class SemaphoreBasedLock implements Lock {

		private final Semaphore semaphore = new Semaphore(1);

		@Override
		public void lock() {
			semaphore.acquireUninterruptibly();
		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			semaphore.acquire();
		}

		@Override
		public boolean tryLock() {
			return semaphore.tryAcquire();
		}

		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			return semaphore.tryAcquire(time, unit);
		}

		@Override
		public void unlock() {
			semaphore.release();
		}

		@Override
		public Condition newCondition() {
			throw new UnsupportedOperationException("Method 'RwLockBasedLocking.SemaphoreBasedLock.newCondition' is not supported!");
		}

	}

}
