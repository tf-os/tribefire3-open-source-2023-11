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
package com.braintribe.utils.system.exec;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import com.braintribe.logging.Logger;

/**
 * This class is mainly used when an external process is started in Java. Its main purpose is to buffer the output and
 * error streams of an external process started from within the VM.
 * 
 * see http://hacks.oreilly.com/pub/h/1092
 * 
 * @author roman.kurmanowytsch
 * @deprecated There are some cases where the streams are simply blocked. Switched to file output.
 */
@Deprecated
public class InputStreamHandler extends Thread {
	private static Logger logger = Logger.getLogger(InputStreamHandler.class);

	public static String newline = System.getProperty("line.separator");

	/**
	 * Stream being read
	 */
	protected final InputStream stream;

	protected volatile boolean bufferReady = false;

	/**
	 * The StringBuffer holding the captured output
	 */
	protected final StringBuffer stringBuffer;

	public InputStreamHandler(StringBuffer captureBuffer, InputStream stream) {
		this.stream = stream;
		this.stringBuffer = captureBuffer;
	}

	/**
	 * Stream the data.
	 */

	@Override
	public void run() {
		Writer writer = new StringWriter();
		try {
			readCharacters(writer);
			this.stringBuffer.append(writer.toString());
		} catch (IOException ioe) {
			// ignore
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
				logger.warn("Could not close StringWriter ");
			}
			this.bufferReady = true;
		}
	}

	protected void readCharacters(Writer writer) throws IOException {
		Reader br = new InputStreamReader(stream);
		try {
			char[] buffer = new char[4096];
			int n = 0;
			while (-1 != (n = br.read(buffer))) {
				writer.write(buffer, 0, n);
			}
		} finally {
			br.close();
		}
	}

	/**
	 * @return True, when the process has finished writing.
	 */
	public boolean isBufferReady() {
		return bufferReady;
	}

	public static String getBuildVersion() {
		return "$Build_Version$ $Id: InputStreamHandler.java 86406 2015-05-28 14:39:44Z roman.kurmanowytsch $";
	}
}
