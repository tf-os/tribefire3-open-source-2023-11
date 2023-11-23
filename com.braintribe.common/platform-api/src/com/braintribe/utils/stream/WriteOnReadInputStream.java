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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>
 * A {@link InputStream} wrapper which writes the read bytes to an optionally given {@link OutputStream}.
 *
 */
public class WriteOnReadInputStream extends InputStream {

	private InputStream in;
	private OutputStream out;
	private long writeCount = 0;
	boolean closeIn = true;
	boolean closeOut;

	public WriteOnReadInputStream(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}

	public WriteOnReadInputStream(InputStream in, OutputStream out, boolean closeInputStreamOnClose, boolean closeOutputStreamOnClose) {
		this.in = in;
		this.out = out;
		this.closeIn = closeInputStreamOnClose;
		this.closeOut = closeOutputStreamOnClose;
	}

	public long getWriteCount() {
		return this.writeCount;
	}

	@Override
	public int read() throws IOException {
		int b = in.read();
		if (out != null && b > -1) {
			out.write(b);
			writeCount++;
		}
		return b;
	}

	@Override
	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {

		if (len < 1) {
			return 0;
		}

		int r = in.read(b, off, len);

		if (out != null && r > 0) {
			out.write(b, off, r);
			writeCount += r;
		}

		return r;

	}

	@Override
	public long skip(long n) throws IOException {
		throw new IOException("Seek not supported");
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public void close() throws IOException {

		IOException error = null;

		if (closeIn) {
			try {
				in.close();
			} catch (IOException e) {
				error = e;
			}
		}

		if (out != null) {

			try {
				out.flush();
			} catch (IOException e) {
				if (error != null) {
					e.addSuppressed(error);
				}
				error = e;
			}

			if (closeOut) {
				try {
					out.close();
				} catch (IOException e) {
					if (error != null) {
						e.addSuppressed(error);
					}
					error = e;
				}
			}
		}

		if (error != null) {
			throw error;
		}

	}

	@Override
	public void mark(int readlimit) {
		// Ignored.
	}

	@Override
	public void reset() throws IOException {
		throw new IOException("Mark not supported");
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	public long consume() throws IOException {
		return consume(new byte[8192]);
	}

	public long consume(byte[] b) throws IOException {
		int count;
		long totalCount = 0;
		while ((count = read(b)) != -1) {
			totalCount += count;
		}
		return totalCount;
	}

}
