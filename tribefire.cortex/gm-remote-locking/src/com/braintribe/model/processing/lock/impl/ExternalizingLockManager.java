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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.lock.api.LockBuilder;
import com.braintribe.model.processing.lock.api.LockManager;
import com.braintribe.model.processing.lock.api.LockService;
import com.braintribe.model.processing.lock.api.LockServiceException;
import com.braintribe.model.processing.time.TimeSpanConversion;
import com.braintribe.model.time.TimeSpan;

public class ExternalizingLockManager implements LockManager {
	private static final Logger logger = Logger.getLogger(ExternalizingLockManager.class);
	private LockService lockService;

	@Required
	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	@Override
	public LockBuilder forIdentifier(String id) {
		return new AbstractLockBuilder(id) {

			@Override
			public Lock shared() {
				return new ExternalizingLock(this, false);
			}

			@Override
			public Lock exclusive() {
				return new ExternalizingLock(this, true);
			}
		};
	}

	private class ExternalizingLock implements Lock {
		private AbstractLockBuilder lockBuilder;
		private boolean exclusive;

		public ExternalizingLock(AbstractLockBuilder lockBuilder, boolean exclusive) {
			this.lockBuilder = lockBuilder;
			this.exclusive = exclusive;
		}

		@Override
		public void lock() {
			while (true) {
				try {
					if (tryLock(Long.MAX_VALUE, TimeUnit.DAYS))
						return;
				} catch (InterruptedException e) {
					// ignore as there is a special lock method that supports interruptibility
					logger.debug("non interruptible lock() call internally catched an InterruptedException");
				}
			}

		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			while (true) {
				if (tryLock(Long.MAX_VALUE, TimeUnit.DAYS))
					return;
			}
		}

		@Override
		public boolean tryLock() {
			try {
				return tryLock(0, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				return false;
			}
		}

		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {

			String identification = lockBuilder.getIdentifier();
			Thread currentThread = Thread.currentThread();
			String oldThreadName = currentThread.getName();
			currentThread.setName(oldThreadName.concat(" > waiting for lock ").concat(identification).concat(" with lock timeout ")
					.concat("" + lockBuilder.getLockTtlMs()));

			try {
				long convertedTime = unit.convert(time, TimeUnit.SECONDS);
				TimeSpan timeout = TimeSpan.T.create();
				timeout.setValue(convertedTime);
				timeout.setUnit(com.braintribe.model.time.TimeUnit.second);

				long lockTimeoutMs = lockBuilder.getLockTtlMs();
				TimeSpan lockTtl = TimeSpanConversion.fromValue(lockTimeoutMs, com.braintribe.model.time.TimeUnit.milliSecond).toTimeSpan();

				return lockService.tryLock(lockBuilder.getIdentifier(), lockBuilder.getHolderId(), lockBuilder.getCallerSignature(),
						lockBuilder.getMachineSignature(), exclusive, timeout, lockTtl);
			} catch (LockServiceException e) {
				String msg = String.format("error while locking for identifier [%s] with holder id [%s] by [%s] on machine [%s]",
						lockBuilder.getIdentifier(), lockBuilder.getHolderId(), lockBuilder.getCallerSignature(), lockBuilder.getMachineSignature());
				logger.error(msg, e);
				return false;
			} finally {
				currentThread.setName(oldThreadName);
			}
		}

		@Override
		public void unlock() {
			try {
				lockService.unlock(lockBuilder.getIdentifier(), lockBuilder.getHolderId(), lockBuilder.getCallerSignature(),
						lockBuilder.getMachineSignature(), exclusive);

			} catch (LockServiceException e) {
				String msg = String.format("error while unlocking for identifier [%s] with holder id [%s] by [%s] on machine [%s]",
						lockBuilder.getIdentifier(), lockBuilder.getHolderId(), lockBuilder.getCallerSignature(), lockBuilder.getMachineSignature());
				logger.error(msg, e);
			}
		}

		@Override
		public Condition newCondition() {
			throw new UnsupportedOperationException("not supported in this implementation");
		}

	}

	@Override
	public String description() {
		return "Externalizing Lock Manager";
	}
}
