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
package com.braintribe.common.lcd;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * {@link Lock} implementation that does nothing at all.
 *
 * @author peter.gazdik
 */
public class EmptyLock implements Lock {

	public static final EmptyLock INSTANCE = new EmptyLock();

	private EmptyLock() {
	}

	@Override
	public void unlock() {
		// nothing to do
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) {
		return true;
	}

	@Override
	public boolean tryLock() {
		return true;
	}

	@Override
	public Condition newCondition() {
		throw new UnsupportedOperationException("Method 'EmptyLock.newCondition' is not supported!");
	}

	@Override
	public void lockInterruptibly() {
		// nothing to do
	}

	@Override
	public void lock() {
		// nothing to do
	}

}
