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
package com.braintribe.common;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link ReadWriteLock} implementation which uses a single {@link ReentrantLock} as a delegate, i.e. both Lock methods return the same {@link Lock}
 * instance.
 *
 * @author peter.gazdik
 */
public class MutuallyExclusiveReadWriteLock implements ReadWriteLock {

	private final Lock lock = new ReentrantLock();

	@Override
	public Lock readLock() {
		return lock;
	}

	@Override
	public Lock writeLock() {
		return lock;
	}

}
