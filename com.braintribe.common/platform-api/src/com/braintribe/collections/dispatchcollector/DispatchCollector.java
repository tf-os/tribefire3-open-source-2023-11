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
package com.braintribe.collections.dispatchcollector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.logging.Logger;

/**
 * This is a collector (a class that holds a collection of objects) that has a very specific purpose: when either a time
 * threshold or a size threshold is reached, the collected elements will be forwarded (dispatched) to a provided
 * receiver and removed from the internal collection. This class helps to accumulate objects (e.g., events) and process
 * them collectively.
 *
 * Note that the dispatching is not exact. When the time- or size-threshold is reached, newly added elements may still
 * be part of the dispatch package, thus maybe exceeding the expected number of elements in the collection.
 */
public class DispatchCollector<T> implements Runnable {

	protected static Logger logger = Logger.getLogger(DispatchCollector.class);

	protected ConcurrentLinkedDeque<T> collection = new ConcurrentLinkedDeque<>();

	protected int sizeThreshold = -1;
	protected long timeThreshold = -1;
	protected DispatchReceiver<T> receiver = null;

	protected ScheduledExecutorService scheduler = null;
	protected ReentrantLock schedulerLock = new ReentrantLock();
	protected ScheduledFuture<?> scheduledFuture = null;

	protected boolean shutdownRequested = false;
	protected long latestDispatch = -1;

	public DispatchCollector(int sizeThreshold, long timeThreshold, DispatchReceiver<T> receiver)
			throws IllegalArgumentException, NullPointerException {
		if (sizeThreshold < 0 && timeThreshold < 0) {
			throw new IllegalArgumentException("Both size- and time-threshold must not have a negative value.");
		}
		if (receiver == null) {
			throw new NullPointerException("The received must not be null.");
		}
		this.sizeThreshold = sizeThreshold;
		this.timeThreshold = timeThreshold;
		this.receiver = receiver;
	}

	public void add(T element) {
		this.add(element, false);
	}

	public void add(T element, boolean forceDispatch) {
		if (element == null) {
			return;
		}
		this.ensureScheduler();

		// When this is the first element in the collection,
		// start the interval from now on
		if (this.collection.isEmpty()) {
			this.latestDispatch = System.currentTimeMillis();
		}

		this.collection.add(element);
		if (!forceDispatch) {
			if (this.sizeThreshold == 0 || (this.sizeThreshold > 0 && this.collection.size() >= this.sizeThreshold)) {
				forceDispatch = true;
			} else if (this.timeThreshold == 0) {
				forceDispatch = true;
			}
		}
		if (forceDispatch) {
			this.flush();
		}
	}

	public void flush() {
		List<T> elements = new ArrayList<>(this.collection.size());
		for (Iterator<T> it = this.collection.iterator(); it.hasNext();) {
			elements.add(it.next());
			it.remove();
		}
		this.receiver.receive(elements);
		this.latestDispatch = System.currentTimeMillis();
	}

	public void shutdown() {
		this.shutdownRequested = true;
		if (this.scheduler != null) {
			this.scheduler.shutdown();
		}
		if (this.scheduledFuture != null) {
			try {
				this.scheduledFuture.get(2000L, TimeUnit.MILLISECONDS);
			} catch (CancellationException ce) {
				// ignore
			} catch (Exception e) {
				logger.debug("Error while waiting for scheduled future", e);
			}
		}
	}

	protected void ensureScheduler() {
		if (this.scheduler != null) {
			return;
		}
		if (this.timeThreshold <= 0) {
			return;
		}
		if (this.sizeThreshold == 0 || this.sizeThreshold == 1) {
			return;
		}

		this.schedulerLock.lock();
		try {
			if (this.scheduler == null) {
				this.scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r);
						t.setName("DispatchCollector Watchdog (" + sizeThreshold + "/" + timeThreshold + ")");
						t.setDaemon(true);
						return t;
					}

				});
				this.scheduledFuture = this.scheduler.scheduleWithFixedDelay(this, 0L, this.timeThreshold, TimeUnit.MILLISECONDS);
			}
		} finally {
			this.schedulerLock.unlock();
		}
	}

	@Override
	public void run() {
		if (this.shutdownRequested) {
			return;
		}

		int size = this.collection.size();
		if (size == 0) {
			return;
		}

		boolean doDispatch = false;

		if (this.sizeThreshold >= 0 && size >= this.sizeThreshold) {

			doDispatch = true;

		} else if (this.timeThreshold == 0) {

			doDispatch = true;

		} else if (this.timeThreshold >= 0) {

			long now = System.currentTimeMillis();
			long timeSinceLastDispatch = now - this.latestDispatch;
			if (timeSinceLastDispatch >= this.timeThreshold) {
				doDispatch = true;
			}

		}

		if (doDispatch) {
			this.flush();
		}

	}

}
