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
package com.braintribe.tribefire.jinni.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import com.braintribe.utils.stream.DelegateOutputStream;
import com.braintribe.utils.stream.NullOutputStream;

public class JinniLogHandler extends StreamHandler {

	public static OutputStream out = NullOutputStream.getInstance();

	/**
	 * Creates a new <code>ConsoleHandler</code> instance. This {@link StreamHandler#setOutputStream(java.io.OutputStream) sets the OutputStream} to
	 * <code>System.out</code>.
	 */
	public JinniLogHandler() {
		super();
		setOutputStream(new JinniLogOutputStream());
	}

	/**
	 * Similar to {@link java.util.logging.ConsoleHandler#publish(LogRecord)}.
	 */
	@Override
	public void publish(LogRecord record) {
		super.publish(record);
		flush();
	}

	/**
	 * Similar to {@link java.util.logging.ConsoleHandler#close()}.
	 */
	@Override
	public void close() {
		flush();
	}

	private static class JinniLogOutputStream extends DelegateOutputStream {

		@Override
		protected OutputStream openDelegate() throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		protected OutputStream getDelegate() throws IOException {
			return out;
		}
	}
}
