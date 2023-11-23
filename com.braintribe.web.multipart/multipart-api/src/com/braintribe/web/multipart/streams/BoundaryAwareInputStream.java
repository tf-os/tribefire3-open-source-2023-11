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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.braintribe.utils.IOTools;

public class BoundaryAwareInputStream extends AbstractPartInputStream {

	public static final byte CR = 0x0D;
	public static final byte LF = 0x0A;

	private final byte[] boundary;
	private final int boundarySize;
	private final int fullBoundarySize;
	private InputStream in;

	private static int bufferSize = IOTools.SIZE_8K;
	private final byte[] buffer;
	private int bufferOffset;
	private int bufferBytesUsed;
	private final byte[] singleByteArray = new byte[1];

	public BoundaryAwareInputStream(InputStream in, byte[] customBoundaryPart) {
		this.in = in;
		boundary = new byte[customBoundaryPart.length + 4];
		boundary[0] = CR;
		boundary[1] = LF;
		boundary[2] = '-';
		boundary[3] = '-';
		System.arraycopy(customBoundaryPart, 0, boundary, 4, customBoundaryPart.length);

		buffer = new byte[bufferSize];

		boundarySize = boundary.length;
		fullBoundarySize = boundarySize + 2;

		// The buffer must theoretically always support being rewinded a full boundary size
		bufferOffset = fullBoundarySize;
		bufferBytesUsed = bufferOffset;

	}

	public byte readBufferedByte() throws IOException {
		if (bufferOffset >= bufferBytesUsed) {
			readDelegateIntoBuffer();
		}

		return buffer[bufferOffset++];
	}

	/**
	 * @return if the value is negative it means that the absolute value is the number of bytes read but a CR will be
	 *         the next character
	 */
	private int readBufferedBytes(byte b[], int off, int len) throws IOException {
		boolean crDetected = false;
		int i = 0;
		int s = bufferOffset;

		for (; i < len; i++) {
			if (bufferOffset >= bufferBytesUsed) {
				// transfer what has bean read
				int transferAmount = bufferOffset - s;
				if (transferAmount > 0) {
					System.arraycopy(buffer, s, b, off, transferAmount);
					off += transferAmount;
				}

				readDelegateIntoBuffer();
				s = bufferOffset;

			}

			if (buffer[bufferOffset++] == CR) {
				crDetected = true;
				break;
			}
		}

		int transferAmount = bufferOffset - s;
		if (crDetected) {
			transferAmount--;
		}

		if (transferAmount > 0) {
			System.arraycopy(buffer, s, b, off, transferAmount);
		}

		return crDetected ? -(i + 1) : i;
	}

	private int readDelegateIntoBuffer() throws IOException {
		int historyStart = bufferOffset - fullBoundarySize;

		// move history to start
		if (historyStart > 0) {
			System.arraycopy(buffer, historyStart, buffer, 0, fullBoundarySize);
			bufferOffset = fullBoundarySize;
		}

		int bytesRead = in.read(buffer, bufferOffset, bufferSize - bufferOffset);

		if (bytesRead == -1) {
			throw new EOFException("Unexpected end of stream. Multipart stream must end with the final boundary.");
		}

		bufferBytesUsed = bytesRead + bufferOffset;

		return bytesRead;
	}

	private void rewind(int i) {
		if (i > bufferOffset) {
			throw new IllegalArgumentException("Cannot rewind " + i + " which is more than current bufferOffset: " + bufferOffset);
		}

		bufferOffset -= i;
	}

	@Override
	public int read(byte[] b, int off, int l) throws IOException {
		if (in == null) {
			return -1;
		}

		int i = 0;
		while (i < l) {
			int amount = readBufferedBytes(b, off + i, l - i);

			if (amount > 0) {
				i += amount;
				continue;
			} else {
				i += -amount - 1;
			}

			boolean matchesBoundary = true;
			for (int k = 1; k < boundarySize; k++) {
				byte boundaryCandidate = readBufferedByte();

				if (boundary[k] != boundaryCandidate) {
					rewind(k);
					matchesBoundary = false;
					break;
				}
			}

			if (matchesBoundary) {
				in = null; // Part end, don't allow further reading
				return i == 0 ? -1 : i;
			}

			b[off + i] = CR;
			i++;
		}

		return l;
	}

	@Override
	public int read() throws IOException {
		int res = read(singleByteArray, 0, 1);

		if (res == -1) {
			return -1;
		}

		return 0xff & singleByteArray[0];
	}

	@Override
	public byte[] getUnreadBuffer() {
		return Arrays.copyOfRange(buffer, bufferOffset, bufferBytesUsed);
	}
}
