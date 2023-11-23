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
package com.braintribe.utils.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class LockRegistry {

	private LockRegistry() {
		//Singleton
	}
	private static class LazyHolder {
        public static final LockRegistry _instance = new LockRegistry();
    }	
	public static LockRegistry getInstance() {
		return LazyHolder._instance;
	}

	protected Map<String,Integer> lockCount = new HashMap<String,Integer>();
	protected Map<String,ReentrantLock> lockMap = new HashMap<String,ReentrantLock>();
	protected ReentrantLock masterLock = new ReentrantLock();
	
	public ReentrantLock acquireLock(String uid) {
		
		ReentrantLock lock = null;
		try {
			masterLock.lock();
			
			lock = this.lockMap.get(uid);
			if (lock == null) {
				lock = new ReentrantLock();
				this.lockMap.put(uid, lock);
				this.lockCount.put(uid, 1);
			} else {
				Integer count = this.lockCount.get(uid);
				if (count == null) {
					this.lockCount.put(uid, 1);
				} else {
					this.lockCount.put(uid, count+1);
				}
			}
			
		} finally {
			masterLock.unlock();
		}
		
		return lock;
	}
	
	public void releaseLock(String uid) {
		try {
			masterLock.lock();
			
			Integer count = this.lockCount.get(uid);
			if (count != null) {
				if (count == 1) {
					//Final release
					this.lockCount.remove(uid);
					this.lockMap.remove(uid);
				} else {
					this.lockCount.put(uid, count-1);
				}
			}
			
		} finally {
			masterLock.unlock();
		}
	}
}
