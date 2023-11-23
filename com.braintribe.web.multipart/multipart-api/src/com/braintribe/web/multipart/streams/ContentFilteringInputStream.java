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
package com.braintribe.web.multipart.streams;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.braintribe.utils.IOTools;

public class ContentFilteringInputStream extends FilterInputStream {
	private final byte[] oneByteBuffer = new byte[1];
	private byte[] ignorePrefix;
	private byte[] ignoreSuffix;
	private byte[] readBuffer;
	private long dataLength;
	private long pos; // counts read bytes since first char of prefix

	public ContentFilteringInputStream(InputStream in, int readBufferSize) {
		super(in);
		this.readBuffer = new byte[readBufferSize];

		ignorePrefix = new byte[0];
		ignoreSuffix = new byte[0];
	}

	public ContentFilteringInputStream(InputStream in) {
		this(in, IOTools.SIZE_4K);
	}

	public void expect(String prefix, long dataLength, String suffix) {
		expectRaw(prefix.getBytes(), dataLength, suffix.getBytes());
	}

	public void expectRaw(byte[] prefix, long dataLength, byte[] suffix) {
		this.ignorePrefix = prefix;
		this.ignoreSuffix = suffix;
		this.dataLength = dataLength;

		pos = 0;
	}

	private int capToInt(long l) {
		return (int) Math.max(Math.min(l, Integer.MAX_VALUE), Integer.MIN_VALUE);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		final long fullLength = ignorePrefix.length + dataLength + ignoreSuffix.length;

		if (pos >= fullLength) {
			throw new EOFException("Either no prefix, suffix and dataLength was set or you already read everything you expected to read.");
		}

		final long readData = Math.max(0, pos - ignorePrefix.length);
		final long remainingData = dataLength - readData;

		// In the currently to read portion is nothing to ignore and can be read directly into b
		if (pos > ignorePrefix.length && remainingData > len) {
			int readBytes = in.read(b, off, len);
			pos += readBytes;
			return readBytes;
		} else {
			int bytesReadDuringLastRun;
			int skippedDuringLastRun;

			do {
				int toSkip = (int) Math.max(0, ignorePrefix.length - pos);
				final int maxLengthToRead = capToInt(fullLength - pos);

				int optimalLengthToRead = (remainingData > len) ? (len + toSkip) : maxLengthToRead;

				if (optimalLengthToRead > readBuffer.length) {
					readBuffer = new byte[optimalLengthToRead];
				}

				bytesReadDuringLastRun = in.read(readBuffer, 0, optimalLengthToRead);
				
				// Assert expected prefix is there
				for (int i=0; i<toSkip && i<bytesReadDuringLastRun; i++) {
					if (readBuffer[i] != ignorePrefix[(int) (pos + i)]) {
						throw new IllegalStateException("Expected to find prefix but found unexpected byte at pos " + (pos+i) + ": " + readBuffer[i]);
					}
				}

				if (bytesReadDuringLastRun == -1) {
					throw new EOFException("Expected to read '" + Arrays.toString(ignorePrefix) + "' then a sequence of " + dataLength
							+ " bytes then '" + Arrays.toString(ignoreSuffix) + "' but got EOF somewhere in between");
				}

				skippedDuringLastRun = toSkip;
				pos += bytesReadDuringLastRun;
			} while (pos < ignorePrefix.length);

			int lengthToPassThrough = (int) Math.min(remainingData, bytesReadDuringLastRun - skippedDuringLastRun);
			int readSuffixBytes =  bytesReadDuringLastRun - lengthToPassThrough - skippedDuringLastRun;
			
			System.out.println("read " + readSuffixBytes + " suffix bytes");
			
			// Assert expected suffix is there
			for (int i=0; i<readSuffixBytes; i++) {
				int startOfSuffix = lengthToPassThrough + skippedDuringLastRun;
				if (readBuffer[startOfSuffix + i] != ignoreSuffix[i]) {
					throw new IllegalStateException("Expected to find suffix but found unexpected byte at pos " + (pos - readSuffixBytes + i) + ": " + readBuffer[startOfSuffix + i]);
				}
			}

			System.arraycopy(readBuffer, skippedDuringLastRun, b, off, lengthToPassThrough);

			if (pos > ignoreSuffix.length + dataLength && pos < fullLength) {
				int unreadSuffixBytes = (int) (fullLength - pos);
				IOTools.readFully(in, readBuffer, 0, unreadSuffixBytes);
				
				// Assert expected remaining suffix is there
				for (int i=0; i<unreadSuffixBytes; i++) {
					int startOfUnreadSuffix = ignoreSuffix.length - unreadSuffixBytes;
					if (readBuffer[i] != ignoreSuffix[startOfUnreadSuffix + i]) {
						throw new IllegalStateException("Expected to find suffix but found unexpected byte at pos " + (pos + i) + ": " + readBuffer[i]);
					}
				}
			}

			return lengthToPassThrough;
		}
	}

	@Override
	public int read() throws IOException {
		if (read(oneByteBuffer) == -1)
			return -1;

		return oneByteBuffer[0] & 0xff;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

}
