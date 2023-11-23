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
import java.io.OutputStream;

public class RangeOutputStream extends OutputStream {

	private OutputStream delegate;

	private final Range range;
	protected long pos;

	public RangeOutputStream(final OutputStream delegate, long firstBytePos, Long lastBytePos) {
		this.delegate = delegate;
		this.range = new Range(firstBytePos, lastBytePos);
	}

	public RangeOutputStream(final OutputStream delegate, Range range) {
		this.delegate = delegate;
		if (range == null) {
			throw new NullPointerException("Range is null!");
		}

		this.range = range;
	}

	public Range getRange() {
		return range;
	}

	@Override
	public void write(int b) throws IOException {
		if (range.contains(pos)) {
			delegate.write(b);
		}
		pos++;
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public void write(byte[] b) throws IOException {
		this.write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		long start = range.getFirstBytePos();
		Long end = range.getLastBytePos();
		if (pos + len >= start && (end == null || pos <= end)) {
			long skipStart = Math.max(0, start - pos);
			long newOff = off + skipStart;
			long newLen = len - skipStart;
			if (end != null) {
				newLen = min(newLen, end - pos + 1, end - start + 1);
			}
			delegate.write(b, (int) newOff, (int) newLen);
		}
		pos += len;
	}

	private long min(long a, long b, long c) {
		return Math.min(a, Math.min(b, c));
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	protected int mathMin(int a, long b) {
		if (b < Integer.MAX_VALUE) {
			return Math.min(a, (int) b);
		} else {
			return a;
		}
	}

	public static class Range {

		private Long firstBytePos;
		private Long lastBytePos;

		public Range(long firstBytePos, Long lastBytePos) {
			this.firstBytePos = firstBytePos;
			this.lastBytePos = lastBytePos;
			if (lastBytePos != null && lastBytePos == Long.MAX_VALUE) {
				this.lastBytePos = null;
			}
		}

		/**
		 * @return The first byte position (inclusive) in the range. Never {@code null}.
		 */
		public Long getFirstBytePos() {
			return firstBytePos;
		}

		/**
		 * @return The last byte position (inclusive) in the range. Can be {@code null}.
		 */
		public Long getLastBytePos() {
			return lastBytePos;
		}

		/**
		 * @return Whether this is a closed range (both first and last byte position specified).
		 */
		public boolean isClosed() {
			return firstBytePos != null && lastBytePos != null;
		}

		/**
		 * @return The size in bytes if the range is closed, -1 otherwise.
		 */
		public long size() {
			return isClosed() ? (lastBytePos - firstBytePos + 1) : -1;
		}

		/**
		 * @return Returns whether the given byte position is within this range.
		 */
		public boolean contains(long pos) {
			if (pos < firstBytePos) {
				return false;
			}
			return lastBytePos == null || pos <= lastBytePos;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(firstBytePos).append('-');
			if (lastBytePos != null) {
				builder.append(lastBytePos);
			}
			return builder.toString();
		}
	}
}
