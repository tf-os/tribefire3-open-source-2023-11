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
package com.braintribe.model.processing.mqrpc.server;

import java.util.concurrent.locks.ReentrantLock;

public class ActivityCounter {
	private volatile int count;
	private ReentrantLock lock = new ReentrantLock();

	public void inc() {
		lock.lock();
		try {
			count++;
		} finally {
			lock.unlock();
		}
	}

	public void dec() {
		lock.lock();
		try {
			if (--count == 0) {
				notify();
			}
		} finally {
			lock.unlock();
		}
	}

	public boolean awaitZeroActivity(long maxWaitInMs) {
		lock.lock();
		try {
			if (count == 0)
				return true;

			try {
				wait(maxWaitInMs);
			} catch (InterruptedException e) {
				// ignore;
			}

			return count == 0;
		} finally {
			lock.unlock();
		}
	}
}
