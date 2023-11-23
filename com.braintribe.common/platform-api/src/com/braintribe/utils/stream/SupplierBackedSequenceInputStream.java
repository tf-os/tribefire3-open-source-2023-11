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
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * An {@link InputStream} that is backed by a sequence of InputStream suppliers. This lifts some limits of the {@link SequenceInputStream}: With this
 * it's possible to add new InputStream suppliers after the stream was already created and even after it has been read partly or until the end. It is
 * possible that {@link InputStream#read()} returns <code>-1</code> but later on again has more data because a new InputStream supplier was added or
 * further data got available from the last InputStream.
 */
public class SupplierBackedSequenceInputStream extends InputStream {
	private Iterator<? extends Supplier<InputStream>> e;
	private InputStream in;

	public SupplierBackedSequenceInputStream(Iterable<? extends Supplier<InputStream>> e) {
		this(e.iterator());
	}

	public SupplierBackedSequenceInputStream(Iterator<? extends Supplier<InputStream>> e) {
		this.e = e;
	}

	public SupplierBackedSequenceInputStream(Supplier<InputStream>... s) {
		this(Arrays.asList(s));
	}

	/**
	 * Continues reading in the next stream if an EOF is reached.
	 */
	private void nextStream() throws IOException {
		if (e == null) {
			return;
		}

		if (in != null) {
			in.close();
		}

		if (e.hasNext()) {
			in = e.next().get();
			if (in == null) {
				throw new NullPointerException();
			}
		} else {
			in = null;
		}
	}

	/**
	 * Returns an estimate of the number of bytes that can be read (or skipped over) from the current underlying input stream without blocking by the
	 * next invocation of a method for the current underlying input stream. The next invocation might be the same thread or another thread. A single
	 * read or skip of this many bytes will not block, but may read or skip fewer bytes.
	 * <p>
	 * This method simply calls {@code available} of the current underlying input stream and returns the result.
	 *
	 * @return an estimate of the number of bytes that can be read (or skipped over) from the current underlying input stream without blocking or
	 *         {@code 0} if this input stream has been closed by invoking its {@link #close()} method
	 * @exception IOException
	 *                if an I/O error occurs.
	 *
	 * @since JDK1.1
	 */
	@Override
	public int available() throws IOException {
		if (in == null) {
			return 0; // no way to signal EOF from available()
		}
		return in.available();
	}

	/**
	 * Reads the next byte of data from this input stream. The byte is returned as an <code>int</code> in the range <code>0</code> to
	 * <code>255</code>. If no byte is available because the end of the stream has been reached, the value <code>-1</code> is returned. This method
	 * blocks until input data is available, the end of the stream is detected, or an exception is thrown.
	 * <p>
	 * This method tries to read one character from the current substream. If it reaches the end of the stream, it calls the <code>close</code> method
	 * of the current substream and begins reading from the next substream.
	 *
	 * @return the next byte of data, or <code>-1</code> if the end of the stream is reached.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public int read() throws IOException {
		if (in == null) {
			nextStream();
		}

		while (in != null) {
			int c = in.read();
			if (c != -1) {
				return c;
			}
			nextStream();
		}
		return -1;
	}

	/**
	 * Reads up to <code>len</code> bytes of data from this input stream into an array of bytes. If <code>len</code> is not zero, the method blocks
	 * until at least 1 byte of input is available; otherwise, no bytes are read and <code>0</code> is returned.
	 * <p>
	 * The <code>read</code> method of {@link SupplierBackedSequenceInputStream} tries to read the data from the current substream. If it fails to
	 * read any characters because the substream has reached the end of the stream, it calls the <code>close</code> method of the current substream
	 * and begins reading from the next substream.
	 *
	 * @param b
	 *            the buffer into which the data is read.
	 * @param off
	 *            the start offset in array <code>b</code> at which the data is written.
	 * @param len
	 *            the maximum number of bytes read.
	 * @return int the number of bytes read.
	 * @exception NullPointerException
	 *                If <code>b</code> is <code>null</code>.
	 * @exception IndexOutOfBoundsException
	 *                If <code>off</code> is negative, <code>len</code> is negative, or <code>len</code> is greater than <code>b.length - off</code>
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public int read(byte b[], int off, int len) throws IOException {
		if (in == null) {
			nextStream();
		}

		if (in == null) {
			return -1;
		} else if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		do {
			int n = in.read(b, off, len);
			if (n > 0) {
				return n;
			}
			nextStream();
		} while (in != null);
		return -1;
	}

	/**
	 * Closes this input stream and releases any system resources associated with the stream. A closed {@link SupplierBackedSequenceInputStream}
	 * cannot perform input operations and cannot be reopened.
	 * <p>
	 * If this stream was created from an {@link Iterable}, all remaining elements are requested from the enumeration and closed before the
	 * <code>close</code> method returns.
	 *
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public void close() throws IOException {
		do {
			nextStream();
		} while (in != null);

		e = null;
	}
}
