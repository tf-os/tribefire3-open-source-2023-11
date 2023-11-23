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
package com.braintribe.logging.juli.handlers.util;

import java.io.IOException;
import java.io.Writer;

/**
 * This implementation of a Writer does absolutely nothing. Every method invocation will be ignored. It also does not perform parameter validity
 * checks. All data sent to this Writer is lost. This class may come handy when an interface requires a Writer but the output is of no interest.
 */
public class NullWriter extends Writer {

	public static final NullWriter INSTANCE = new NullWriter();

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		// /dev/null
	}

	@Override
	public void flush() throws IOException {
		// /dev/null
	}

	@Override
	public void close() throws IOException {
		// /dev/null
	}

	@Override
	public void write(int c) throws IOException {
		// /dev/null
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		// /dev/null
	}

	@Override
	public void write(String str) throws IOException {
		// /dev/null
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		// /dev/null
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		// /dev/null
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		// /dev/null
		return this;
	}

	@Override
	public Writer append(char c) throws IOException {
		// /dev/null
		return this;
	}

}
