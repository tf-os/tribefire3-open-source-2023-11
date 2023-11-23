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
package com.braintribe.utils;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is a ID generator for long values. The aim of this class is to provide a long number that is almost unique (or, unique enough). It cannot be
 * guaranteed that the numbers returned by this class are unique over multiple JVMs, but it tries... The Ids generated are comprised of two separate
 * numbers: the current timestamp (see {@link java.lang.System#currentTimeMillis()}) and a sequence number that is atomically incremented with each
 * newly generated Id.
 */
public class LongIdGenerator {

	// To improve the uniqueness over multiple JVMs, we use a random offset for the sequencer
	public static AtomicLong atomicLongCounter = new AtomicLong(new SecureRandom().nextLong());
	private static ReentrantLock lock = new ReentrantLock();

	public static long provideLongId() {

		lock.lock();
		try {
			// Take the next sequence number and use the 21 least significant bits
			long counter = atomicLongCounter.incrementAndGet();
			counter = counter & 0x1fffff;

			long nowMs = System.currentTimeMillis();
			// We make space for the sequence number. Thus, we shift the timestamp by 21 bits to the left
			// We have now space for approx 2 mio sequence numbers (per Millisecond) (2097151, to be exact)
			nowMs = nowMs << 21;

			long id = nowMs | counter;

			return id;

		} finally {
			lock.unlock();
		}

	}
}
