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
package com.braintribe.logging.juli.handlers;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * This is a simple extension of {@link StreamHandler} which logs to the console. This class is similar to
 * {@link java.util.logging.ConsoleHandler}, but logs to <code>System.out</code> (instead of <code>System.err</code>).
 * <p>
 * Note that this class cannot just extend {@link java.util.logging.ConsoleHandler} and
 * {@link StreamHandler#setOutputStream(java.io.OutputStream) change the OutputStream}, since that would close the
 * initial <code>OutputStream</code> which means one could no longer write to <code>System.err</code>.
 *
 * @author michael.lafite
 */
public class ConsoleHandler extends StreamHandler {

	private ReentrantLock lock = new ReentrantLock();

	/**
	 * Creates a new <code>ConsoleHandler</code> instance. This {@link StreamHandler#setOutputStream(java.io.OutputStream)
	 * sets the OutputStream} to <code>System.out</code>.
	 */
	public ConsoleHandler() {
		setOutputStream(System.out);
	}

	// /**
	// * This method merely exists to make it final so that it can be safely called from the constructor.
	// */
	// @Override
	// protected final synchronized void setOutputStream(OutputStream out) throws SecurityException {
	// super.setOutputStream(out);
	// }

	/**
	 * Similar to {@link java.util.logging.ConsoleHandler#publish(LogRecord)}.
	 */
	@Override
	public void publish(LogRecord logRecord) {
		lock.lock();
		try {
			super.publish(logRecord);
			flush();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Similar to {@link java.util.logging.ConsoleHandler#close()}.
	 */
	@Override
	public void close() {
		lock.lock();
		try {
			flush();
		} finally {
			lock.unlock();
		}
	}
}
