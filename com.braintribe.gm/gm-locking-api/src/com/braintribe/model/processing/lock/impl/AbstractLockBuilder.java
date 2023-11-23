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

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import com.braintribe.model.processing.lock.api.LockBuilder;

public abstract class AbstractLockBuilder implements LockBuilder, ReadWriteLock {
	protected String identifier;
	protected String callerSignature;
	protected String machineSignature;
	protected String holderId = UUID.randomUUID().toString();
	protected long lockTtlMs = -1L;

	protected AbstractLockBuilder(String id) {
		this.identifier = id;
	}

	@Override
	public Lock readLock() {
		return shared();
	}

	@Override
	public Lock writeLock() {
		return exclusive();
	}

	@Override
	public LockBuilder caller(String callerSignatureParam) {
		this.callerSignature = callerSignatureParam;
		return this;
	}

	@Override
	public LockBuilder machine(String machineSignatureParam) {
		this.machineSignature = machineSignatureParam;
		return this;
	}

	@Override
	@Deprecated
	public LockBuilder lockTimeout(long time, TimeUnit unit) {
		this.lockTtlMs = TimeUnit.MILLISECONDS.convert(time, unit);
		return this;
	}

	@Override
	public LockBuilder lockTtl(long time, TimeUnit unit) {
		this.lockTtlMs = TimeUnit.MILLISECONDS.convert(time, unit);
		return this;
	}

	@Override
	public LockBuilder holderId(String holderId) {
		this.holderId = holderId;
		return this;
	}

	public String getMachineSignature() {
		return machineSignature;
	}

	public String getCallerSignature() {
		return callerSignature;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getHolderId() {
		return holderId;
	}

	/**
	 * @deprecated Use {@link #getLockTtlMs()} instead
	 */
	@Deprecated
	public long getLockTimeoutMs() {
		return lockTtlMs;
	}

	public long getLockTtlMs() {
		return lockTtlMs;
	}
}
