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

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.braintribe.utils.junit.core.rules.ConcurrentRule;

/**
 * 
 */
public class ConcurrentSyncObjectProviderDemo {

	@Rule
	public ConcurrentRule concurrentRule = new ConcurrentRule(2);

	private SyncObjectProvider<List<?>> syncProvider = new ConcurrentSyncObjectProvider<List<?>>();

	private boolean waitFlag = true;
	private Object LOCK = new Object();

	// ##################################
	// ## . . . . DEMO TESTS . . . . . ##
	// ##################################

	@Test
	public void works() throws Exception {
		List<?> emptyList = new ArrayList<Object>();
		try {
			Object syncObject = syncProvider.acquireSyncObject(emptyList);
			synchronized (syncObject) {
				/* first thread waits a little, so second thread has enough time to get the same sync object */
				Thread.sleep(100);
				System.out.println("[SAME SYNC OBJECT]: " + syncObject);
			}
		} finally {
			syncProvider.releaseSyncObject(emptyList);
		}
	}

	@Test
	public void works2() throws Exception {
		List<?> emptyList = new ArrayList<Object>();
		waitIfFirstThread();

		/*
		 * the first thread that enters the try-block has to wake up the other after he releases the object, thus there
		 * will be two different sync objects
		 */
		try {
			Object syncObject = syncProvider.acquireSyncObject(emptyList);
			synchronized (syncObject) {
				System.out.println("[DIFFERENT SYNC OBJECT]: " + syncObject);
			}
		} finally {
			syncProvider.releaseSyncObject(emptyList);
		}

		wakeUpWaitingThread();
	}

	private void waitIfFirstThread() throws InterruptedException {
		synchronized (LOCK) {
			if (waitFlag) {
				waitFlag = false;
				LOCK.wait();
			}
		}
	}

	private void wakeUpWaitingThread() {
		synchronized (LOCK) {
			LOCK.notifyAll();
		}
	}

}
