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
package tribefire.cortex.leadership.impl;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.NullSafe.nonNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.lock.api.Locking;

import tribefire.cortex.leadership.api.LeadershipListener;
import tribefire.cortex.leadership.api.LeadershipManager;
import tribefire.cortex.leadership.impl.LockingBasedLeadershipManager.DomainEntry.LeadershipEntry;

/**
 * {@link LeadershipManager} implementation based on {@link Locking}.
 * 
 * <h3>Acquiring leadership automatically</h3>
 * 
 * Leadership for all domains where it is possible should be acquired automatically, ideally as a scheduled task. It should be configured externally,
 * and an attempt is done by calling {@link #refreshLeadershipsForEligibleDomains()}.
 * 
 * @author peter.gazdik
 */
public class LockingBasedLeadershipManager implements LeadershipManager {

	private static final Logger log = Logger.getLogger(LockingBasedLeadershipManager.class);

	private Locking locking;
	public String name;

	// copy-on-write -> iterating over all entries periodically implies no synchronization is needed
	private volatile Map<String, DomainEntry> cow_domainToEntry = newMap();
	private final String domainToEntryLock = "domainToEntry-" + System.identityHashCode(this);

	@Required
	public void setLocking(Locking locking) {
		this.locking = locking;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void addLeadershipListener(String domainId, LeadershipListener listener) {
		nonNull(domainId, "domainId");
		nonNull(listener, "listener");

		LeadershipEntry leadershipEntry = registerListener(domainId, listener);
		leadershipEntry.tryAcquireLeadershipAfterCreated();
	}

	// creating a non-empty domain entry is an atomic operation
	private LeadershipEntry registerListener(String domainId, LeadershipListener listener) {
		synchronized (domainToEntryLock) {
			Map<String, DomainEntry> copy = newMap(cow_domainToEntry);
			DomainEntry domainEntry = copy.computeIfAbsent(domainId, DomainEntry::new);
			cow_domainToEntry = copy;

			return domainEntry.addListener(listener);
		}
	}

	@Override
	public void removeLeadershipListener(String domainId, LeadershipListener listener) {
		nonNull(domainId, "domainId");
		nonNull(listener, "listener");

		DomainEntry domainEntry = cow_domainToEntry.get(domainId);
		if (domainEntry == null) {
			log.warn("Removing leadership listener, but no entry found. DomainId: " + domainId + ", listener: " + listener);
			return;
		}

		log.trace(() -> "Removing leadership listener for domainId:" + domainId);

		domainEntry.removeListener(listener);
		if (domainEntry.isEmpty())
			removeDomainEntryIfEmpty(domainId);
	}

	/**
	 * This method should be called periodically to try to get leadership for domains where that is possible (i.e. leadership lock is available).
	 */
	public void refreshLeadershipsForEligibleDomains() {
		for (DomainEntry domainEntry : cow_domainToEntry.values())
			domainEntry.tryAcquireLeadership();
	}

	private void removeDomainEntryIfEmpty(String domainId) {
		// removing a domain entry if empty is an atomic operation
		synchronized (domainToEntryLock) {
			DomainEntry domainEntry = cow_domainToEntry.get(domainId);

			// we check this in a synced block, in case another thread is registering a new listener (also happens in a synchronized block)
			// This ensures we remove an EMPTY entry, not one where someone has added a listener just after we checked it was empty
			if (domainEntry != null && domainEntry.isEmpty()) {
				Map<String, DomainEntry> copy = newMap(cow_domainToEntry);
				copy.remove(domainId);
				cow_domainToEntry = copy;
			}
		}
	}

	@Override
	public String description() {
		return "Locking Based Leadership - " + name;
	}

	class DomainEntry {

		private final Map<LeadershipListener, LeadershipEntry> listenerToEntry = newMap();

		// writing to hasLock in synchronized on both "lock" and "listenerIdToEntry" instances
		// it's not entirely needed, but is easier to analyze
		// key problem is that if new listener is registered, it either :
		// - needs to be granted leadership because we already have the lock,
		// - needs to be added to listeners before they are all copied, to be notified next time the lock is acquired

		// Problem would be if existing listeners would be copied, then new listener would be added without knowing the lock was acquired

		/* Syncing hasLock assignment also on "listenerIdToEntry" makes sure that setting hasLock to true and adding new listener or removing listener
		 * doesn't happen at the same time. */
		private volatile boolean hasLock;
		private final Lock lock;
		private final String lockLock;

		public DomainEntry(String domainId) {
			this.lock = locking.forIdentifier("leadership", domainId).writeLock();
			this.lockLock = domainId + "-lock";
		}

		public LeadershipEntry addListener(LeadershipListener listener) {
			LeadershipEntry entry = new LeadershipEntry(listener);
			synchronized (listenerToEntry) {
				listenerToEntry.put(listener, entry);
			}
			return entry;
		}

		public void removeListener(LeadershipListener listener) {
			if (removeListenerAndCheckIfLast(listener))
				releaseLockIfEmpty();
		}

		private boolean removeListenerAndCheckIfLast(LeadershipListener listener) {
			synchronized (listenerToEntry) {
				if (listenerToEntry.remove(listener) == null)
					log.warn("Entry about listener not found: " + listener);

				return listenerToEntry.isEmpty();
			}
		}

		// This cannot lead to lock being locked and not released, because this method is only called from "addLeadershipListener()", and a
		// removeLeadershipListener() must follow.
		public void tryGrantQuicklyAfterListenerAdded(LeadershipEntry entry) {
			if (hasLock) {
				synchronized (lockLock) {
					if (hasLock) {
						entry.s_grantLeadership();
						return;
					}
				}
			}

			tryAcquireLeadership();
		}

		/* package */ void tryAcquireLeadership() {
			if (hasLock)
				return;

			synchronized (lockLock) {
				if (hasLock)
					return;

				if (lock.tryLock()) {
					log.trace(() -> "Leadership lock acquired: " + lockLock);

					List<LeadershipEntry> entries = null;

					synchronized (listenerToEntry) {
						hasLock = true;
						entries = newList(listenerToEntry.values());

						// This could be called from a scheduler while the listeners were removed, in which case we want to release the lock
						if (entries.isEmpty()) {
							s_unlock();
							return;
						}
					}

					for (LeadershipEntry entry : entries) {
						entry.s_grantLeadership();
					}
				}
			}
		}

		public boolean isEmpty() {
			synchronized (listenerToEntry) {
				return listenerToEntry.isEmpty();
			}
		}

		/* package */ void releaseLockIfEmpty() {
			if (!hasLock)
				return;

			synchronized (lockLock) {
				if (!hasLock)
					return;

				synchronized (listenerToEntry) {
					if (listenerToEntry.isEmpty())
						s_unlock();
				}
			}
		}

		private void s_unlock() {
			log.trace(() -> "Releasing leadership lock: " + lockLock);

			try {
				lock.unlock();
			} catch (Exception e) {
				log.error(() -> "Cannot realease leadership lock: " + lockLock);
				throw e;
			}
			hasLock = false;
		}

		class LeadershipEntry {
			public final LeadershipListener listener;
			public boolean wasGranted;

			public LeadershipEntry(LeadershipListener listener) {
				this.listener = listener;
			}

			public void tryAcquireLeadershipAfterCreated() {
				DomainEntry.this.tryGrantQuicklyAfterListenerAdded(this);
			}

			// access is synchronized on lockLock
			public void s_grantLeadership() {
				if (!wasGranted) {
					listener.onLeadershipGranted(null);
					wasGranted = true;
				}
			}

		}
	}

}
