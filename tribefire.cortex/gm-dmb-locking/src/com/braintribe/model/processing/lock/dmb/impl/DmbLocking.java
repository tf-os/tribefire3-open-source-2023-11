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

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import com.braintribe.collections.EvictingConcurrentHashMap;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.model.processing.lock.api.Locking;

/**
 * Implementation of the {@link Locking} interface that relies on <code>MBeans</code> to share the locks across ClassLoader-boundaries.
 * <p>
 * The locks are, in contrast to the original implementation, not {@link ReentrantLock} locks, but rather {@link StampedLock} locks. This is necessary
 * because the {@link ReentrantLock} requires that the {@link ReentrantLock#unlock()} method is called from the same thread as
 * {@link ReentrantLock#lock()}. This is not always possible as the InternalizingLockService may use this LockManager and it cannot make this
 * guarantee.
 * <p>
 * The locks are stored in a {@link EvictingConcurrentHashMap} so that the number of stored locks does not get excessive. Only locks that are not
 * currently in <code>locked</code> state will be evicted from the map.
 * <p>
 * Implementation note: The previous implementations using weak references to prevent stale locks was not correct. Because of the
 * InternalizingLockService, locks may become unreferenced and removed by the GC, although a remote instance may still have a reference to this lock.
 *
 * @author roman.kurmanowytsch
 */
public class DmbLocking implements Locking {

	public static final String DmbLockMapObjectName = "tribefire.cortex:type=Locks";

	protected EvictingConcurrentHashMap<String, StampedLockEntry> locks = null;
	protected int evictionThreshold = 1_000;
	protected long evictionInterval = Numbers.MILLISECONDS_PER_MINUTE;

	public DmbLocking() throws Exception {
		Map<String, Object> lockManagerData = acquirelockManagerData();
		locks = (EvictingConcurrentHashMap<String, StampedLockEntry>) lockManagerData.get("locks");
	}

	private static Map<String, Object> acquirelockManagerData() throws Exception {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		synchronized (mbs) {
			ObjectName name = new ObjectName(DmbLockMapObjectName);

			if (!mbs.isRegistered(name)) {
				StandardMBean mbean = new StandardMBean(new HashMap<>(), Map.class);
				mbs.registerMBean(mbean, name);
			}

			Map<String, Object> lockManagerData = DynamicMBeanProxy.create(Map.class, name);

			// Initial setup of the locks map. Note the eviction policy that allows only locks to be removed that
			// are not in <code>locked</code> state.
			EvictingConcurrentHashMap<String, StampedLockEntry> locks = new EvictingConcurrentHashMap<>(e -> !e.getValue().isLocked());
			locks.setEvictionInterval(Numbers.MILLISECONDS_PER_MINUTE);
			locks.setEvictionThreshold(1_000);
			lockManagerData.put("locks", locks);

			return lockManagerData;
		}
	}

	private StampedLockEntry acquireLock(String id) {
		synchronized (locks) {
			StampedLockEntry lock = locks.get(id);

			if (lock == null) {
				StampedLock stampedLock = new StampedLock();
				lock = new StampedLockEntry(stampedLock);

				locks.put(id, lock);
			}

			return lock;
		}
	}

	@Override
	public ReadWriteLock forIdentifier(String id) {
		return new ReadWriteLock() {
			// @formatter:off
			@Override public Lock writeLock() { return acquireLock(id).asWriteLock(); }
			@Override public Lock readLock()  { return acquireLock(id).asReadLock(); }
			// @formatter:on
		};
	}

	public void setEvictionThreshold(int evictionThreshold) {
		this.evictionThreshold = evictionThreshold;
		if (locks != null) {
			locks.setEvictionThreshold(evictionThreshold);
		}
	}

	public void setEvictionInterval(long evictionInterval) {
		this.evictionInterval = evictionInterval;
		if (locks != null) {
			locks.setEvictionInterval(evictionInterval);
		}
	}

}
