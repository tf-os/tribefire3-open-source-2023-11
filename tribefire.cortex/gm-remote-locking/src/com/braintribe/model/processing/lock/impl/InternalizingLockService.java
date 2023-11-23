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

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import com.braintribe.cfg.Required;
import com.braintribe.model.processing.lock.api.LockBuilder;
import com.braintribe.model.processing.lock.api.LockManager;
import com.braintribe.model.processing.lock.api.LockService;
import com.braintribe.model.processing.lock.api.LockServiceException;
import com.braintribe.model.processing.time.TimeSpanConversion;
import com.braintribe.model.time.TimeSpan;

public class InternalizingLockService implements LockService {
	private LockManager lockManager;
	
	@Required
	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}

	@Override
	public boolean tryLock(String identification, String holderId, String callerSignature, String machineSignature, boolean exclusive, TimeSpan timeout) throws LockServiceException, InterruptedException {
		return tryLock(identification, holderId, callerSignature, machineSignature, exclusive, timeout, null);
	}
	
	
	@Override
	public boolean tryLock(String identification, String holderId, String callerSignature, String machineSignature, boolean exclusive, TimeSpan timeout, TimeSpan lockTtl) throws LockServiceException, InterruptedException {
		
		long lockTtlInMs = -1L;
		if (lockTtl != null) {
			Duration duration = TimeSpanConversion.getDuration(lockTtl);
			lockTtlInMs = duration.toMillis();
		}
		
		LockBuilder lockBuilder = lockManager
				.forIdentifier(identification)
				.holderId(holderId)
				.caller(callerSignature)
				.machine(machineSignature)
				.lockTtl(lockTtlInMs, TimeUnit.MILLISECONDS);
		
		Lock lock = exclusive? lockBuilder.exclusive(): lockBuilder.shared();

		long convertedTimeout = (long)TimeSpanConversion.fromTimeSpan(timeout).unit(com.braintribe.model.time.TimeUnit.milliSecond).toValue();
		
		boolean lockAcquired = lock.tryLock(convertedTimeout, TimeUnit.MILLISECONDS);
		
		return lockAcquired;
	}

	@Override
	public void unlock(String identification, String holderId, String callerSignature, String machineSignature, boolean exclusive) throws LockServiceException {
		LockBuilder lockBuilder = lockManager.forIdentifier(identification).holderId(holderId).caller(callerSignature).machine(machineSignature);
		Lock lock = exclusive? lockBuilder.exclusive(): lockBuilder.shared();

		lock.unlock();
	}

}
