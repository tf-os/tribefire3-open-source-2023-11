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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Wrapper around a {@link Lock} object that registers every lock/unlock invocations
 * to keep a state of whether the lock is actually locked or not.
 * 
 * @author roman.kurmanowytsch
 *
 */
public class StampedLockDelegate implements Lock {

	private Lock delegate;
	private StampedLockEntry lockEntry;

	public StampedLockDelegate(Lock delegate, StampedLockEntry lockEntry) {
		this.delegate = delegate;
		this.lockEntry = lockEntry;
		
	}

	@Override
	public void lock() {
		delegate.lock();
		lockEntry.registerLock();
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		delegate.lockInterruptibly();
		lockEntry.registerLock();
	}

	@Override
	public boolean tryLock() {
		boolean locked = delegate.tryLock();
		if (locked) {
			lockEntry.registerLock();
		}
		return locked;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		boolean locked = delegate.tryLock(time, unit);
		if (locked) {
			lockEntry.registerLock();
		}
		return locked;
	}

	@Override
	public void unlock() {
		delegate.unlock();
		lockEntry.registerUnlock();
	}

	@Override
	public Condition newCondition() {
		return delegate.newCondition();
	}

}
