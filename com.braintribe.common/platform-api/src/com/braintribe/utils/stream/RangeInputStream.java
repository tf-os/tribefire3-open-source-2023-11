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

/**
 * InputStream wrapper that limits the read input to the specified range.
 *
 * @author Roman Kurmanowytsch
 */
public class RangeInputStream extends InputStream {

	private InputStream parent;

	private long remaining;

	/**
	 * Created the InputStream wrapper, starting the input at <code>start</code> (0-based) and returns the content up until <code>end</code> is
	 * reached.
	 *
	 * @param parent
	 *            The source InputStream.
	 * @param start
	 *            The starting point.
	 * @param end
	 *            The (exclusive) end point. If less then 0, {@link Long#MAX_VALUE} will be used.
	 * @throws IOException
	 *             Thrown if the underlying InputStream throws an exception.
	 *
	 */
	public RangeInputStream(InputStream parent, long start, long end) throws IOException {
		if (end < 0) {
			end = Long.MAX_VALUE;
		}
		if (end < start) {
			throw new IllegalArgumentException("end < start");
		}

		if (parent.skip(start) < start) {
			throw new IOException("Unable to skip leading bytes");
		}

		this.parent = parent;
		remaining = end - start;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {

		if (remaining <= 0) {
			return -1;
		}
		int available = (len > remaining) ? (int) remaining : len;
		int actualRead = parent.read(b, off, available);
		if (actualRead >= 0) {
			remaining -= actualRead;
		}
		return actualRead;
	}

	@Override
	public int read() throws IOException {
		return --remaining >= 0 ? parent.read() : -1;
	}

	@Override
	public int available() throws IOException {
		int available = parent.available();
		if (available > remaining) {
			return (int) remaining;
		} else {
			return available;
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		parent.close();
	}

	@Override
	public boolean markSupported() {
		return false;
	}
}