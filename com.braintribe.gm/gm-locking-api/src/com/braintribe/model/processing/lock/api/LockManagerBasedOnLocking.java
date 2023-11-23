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
public class LockManagerBasedOnLocking implements LockManager {

	private final Locking locking;
	private final String description;

	public LockManagerBasedOnLocking(Locking locking, String description) {
		this.locking = locking;
		this.description = description;
	}

	@Override
	public LockBuilder forIdentifier(String id) {
		ReadWriteLock rwLock = locking.forIdentifier(id);

		return new LockBuilder() {
			// @formatter:off
			@Override public Lock shared()    { return rwLock.readLock(); }
			@Override public Lock exclusive() { return rwLock.writeLock(); }

			@Override public LockBuilder machine(String machineSignature)      { return this; }
			@Override public LockBuilder lockTtl(long time, TimeUnit unit)     { return this; }
			@Override public LockBuilder lockTimeout(long time, TimeUnit unit) { return this; }
			@Override public LockBuilder holderId(String holderId)             { return this; }
			@Override public LockBuilder caller(String callerSignature)        { return this; }		
			// @formatter:on
		};
	}

	@Override
	public String description() {
		return description;
	}

}
