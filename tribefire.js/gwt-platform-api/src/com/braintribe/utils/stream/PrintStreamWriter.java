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
package com.braintribe.utils.stream;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

public class PrintStreamWriter extends Writer {
	private PrintStream stream;
	private boolean ignoreClose;
	
	public PrintStreamWriter(PrintStream stream, boolean ignoreClose) {
		super();
		this.stream = stream;
		this.ignoreClose = ignoreClose;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		stream.append(new String(cbuf, off, len));
	}
	
	@Override
	public void write(char[] cbuf) throws IOException {
		stream.print(cbuf);
	}
	
	@Override
	public void write(String str) throws IOException {
		stream.print(str);
	}
	
	@Override
	public void write(String str, int off, int len) throws IOException {
		stream.append(str, off, off + len);
	}

	@Override
	public void flush() throws IOException {
		stream.flush();
	}

	@Override
	public void close() throws IOException {
		
		if (!ignoreClose)
			stream.close();
		else
			flush();
	}

}
