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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

public class LockHolder implements Runnable {

	private Lock lock;
	private boolean release = false;
	private boolean hasLock = false;
	private CountDownLatch countdownLatch;
	
	public LockHolder(Lock lock, CountDownLatch countdownLatch) {
		this.lock = lock;
		this.countdownLatch = countdownLatch;
	}
	
	public void release() {
		release = true;
	}
	
	public boolean hasLock() {
		return hasLock;
	}
	
	@Override
	public void run() {
		
		lock.lock();
		try {
			hasLock = true;
			countdownLatch.countDown();
			
			while (!release) {
				try {
					Thread.sleep(100L);
				} catch(InterruptedException e) {
					return;
				}
			}
			
		} finally {
			lock.unlock();
		}
		
		hasLock = false;
	}

}
