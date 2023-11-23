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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * PGA: This implementation is flawed - it only works in my code as I only care that the synchronization works the first time. Obviously,
 * after the SyncObject is released, another once could be created while some other thread out there still has the first one. I will fix
 * this at a later time.
 * 
 * {@link SyncObjectProvider} which causes blocking of two threads iff those two threads are synchronizing on two equivalent objects A and
 * B. The equivalence is considered in terms of {@link Object#equals(Object)} method.
 * 
 * Note that there is a hash map in the background, so it is necessary for objects being synchronized on to correctly implement the
 * {@link Object#hashCode()} method as well.
 */
public class ConcurrentSyncObjectProvider<T> implements SyncObjectProvider<T> {

	private final ConcurrentMap<T, SyncObject> syncObjects = new ConcurrentHashMap<T, SyncObject>();

	@Override
	public Object acquireSyncObject(T arg) {
		SyncObject newValue = new SyncObject();
		SyncObject oldValue = syncObjects.putIfAbsent(arg, newValue);

		return oldValue != null ? oldValue : newValue;
	}

	@Override
	public void releaseSyncObject(T arg) {
		SyncObject syncObject = syncObjects.get(arg);

		if (syncObject != null && syncObject.creator == Thread.currentThread()) {
			syncObjects.remove(arg);
		}
	}

	static class SyncObject {
		final Thread creator = Thread.currentThread();
	}
}
