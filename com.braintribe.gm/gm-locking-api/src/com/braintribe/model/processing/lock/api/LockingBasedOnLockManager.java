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
package com.braintribe.model.processing.lock.api;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("deprecation")
public class LockingBasedOnLockManager implements Locking {

	private final LockManager lockManager;

	public LockingBasedOnLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}

	@Override
	public ReadWriteLock forIdentifier(String id) {
		LockBuilder lb = lockManager.forIdentifier(id).lockTtl(60, TimeUnit.SECONDS);

		return new ReadWriteLock() {
			// @formatter:off
			@Override public Lock writeLock() { return lb.exclusive(); }
			@Override public Lock readLock() { return lb.shared(); }
			// @formatter:on
		};
	}

}
