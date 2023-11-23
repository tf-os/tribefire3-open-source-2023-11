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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * <p>
 * This is a simple extension of {@link StreamHandler} which logs to a file configured by the system property
 * <b>logging.staticfilehandler.filepath</b>
 *
 * @author christina.wilpernig, dirk.scheffler
 */
public class StaticFileHandler extends StreamHandler {

	public static final String SYSTEM_PROPERTY_FILEPATH = "logging.staticfilehandler.filepath";
	private OutputStream out;
	private ReentrantLock lock = new ReentrantLock();

	/**
	 * Creates a new <code>StaticFileHandler</code> instance. This
	 * {@link StreamHandler#setOutputStream(java.io.OutputStream) sets the OutputStream} to the {@link FileOutputStream}
	 * configured by <b>logging.staticfilehandler.filepath</b>
	 */
	public StaticFileHandler() {
		String fileName = System.getProperty(SYSTEM_PROPERTY_FILEPATH);
		try {
			out = new FileOutputStream(fileName);
			setOutputStream(out);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void publish(LogRecord record) {
		lock.lock();
		try {
			super.publish(record);
			flush();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void close() {
		lock.lock();
		try {
			flush();
			out.close();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} finally {
			lock.unlock();
		}
	}
}
