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
import com.braintribe.web.multipart.impl.FormDataMultipartConstants;

public class ChunkedInputStream extends AbstractPartInputStream implements FormDataMultipartConstants {

	// optimize for 4 hex digit chunk size
	// buffer + 2xCRLF
	// [\r\n]FFFF\r\n
	private static final int CHUNK_SIZE_DECLARATION_BUFFER_SIZE = 8;

	private InputStream delegate;
	private int bytesRemainingInChunk;
	private int chunkSizeDeclarationBufferOffset = CHUNK_SIZE_DECLARATION_BUFFER_SIZE;
	private boolean endOfChunkedStreamReached = false;
	private boolean atVeryBeginningOfInputStream = true;

	private long nanoTimeCounterDelegate = 0;
	private long nanoTimeCounter2 = 0;
	private int chunkCounter;

	private final byte[] oneByteBuffer = new byte[1];
	byte[] chunkSizeDeclarationBuffer = new byte[CHUNK_SIZE_DECLARATION_BUFFER_SIZE];

	public ChunkedInputStream(InputStream delegate) {
		this.delegate = delegate;
	}

	public static int hexCharByteToInt(int x) {
		if (x >= 48 && x <= 57) {
			return x - 48;
		} else if (x >= 65 && x <= 70) {
			return x - 55;
		} else if (x >= 97 && x <= 102) {
			return x - 87;
		}

		throw new IllegalArgumentException("Byte " + x + " does not match a hex digit character");
	}

	private int readLinebreakAndNextChunkSize() throws IOException {
		int bytesRead = readIntoChunkSizeDeclarationBuffer();

		if (bytesRead < HTTP_LINEBREAK.length) {
			throw new EOFException("Expected linebreak before chunk size declaration but got EOF");
		} else if (bytesRead == HTTP_LINEBREAK.length) {
			throw new EOFException("Expected chunk size declaration after linebreak but got EOF");
		} else if (chunkSizeDeclarationBuffer[0] != CR || chunkSizeDeclarationBuffer[1] != LF) {
			throw new IllegalStateException(
					"Expected linebreak before chunk size declaration but got: " + Arrays.toString(Arrays.copyOf(chunkSizeDeclarationBuffer, 2)));
		}

		chunkSizeDeclarationBufferOffset += HTTP_LINEBREAK.length;
		return readNextChunkSizeFromBuffer(bytesRead);

	}

	private int readIntoChunkSizeDeclarationBuffer() throws IOException {
		int unreadBytesInChunkSizeDeclarationBuffer = chunkSizeDeclarationBuffer.length - chunkSizeDeclarationBufferOffset;
		System.arraycopy(chunkSizeDeclarationBuffer, chunkSizeDeclarationBufferOffset, chunkSizeDeclarationBuffer, 0,
				unreadBytesInChunkSizeDeclarationBuffer);

		long time = System.nanoTime();
		// TODO: remove readFully
		int bytesRead = IOTools.readFully(delegate, chunkSizeDeclarationBuffer, unreadBytesInChunkSizeDeclarationBuffer,
				chunkSizeDeclarationBufferOffset);
		nanoTimeCounterDelegate += System.nanoTime() - time;

		if (bytesRead == -1 && unreadBytesInChunkSizeDeclarationBuffer <= HTTP_LINEBREAK.length) {
			throw new EOFException("Expected chunk size declaration but got EOF");
		}

		chunkSizeDeclarationBufferOffset = 0;

		return bytesRead;
	}

	private int readNextChunkSizeFromBuffer(int bytesReadIntoBuffer) throws IOException {

		int size = 0;
		int hexDigitCounter = 0;

		while (chunkSizeDeclarationBufferOffset < bytesReadIntoBuffer) {
			int digit = chunkSizeDeclarationBuffer[chunkSizeDeclarationBufferOffset];

			if (digit == CR)
				break;

			// 0000 0010
			size = size << 4;
			// 0010 0000
			size = size | hexCharByteToInt(digit);
			// 0010 1001

			hexDigitCounter++;
			chunkSizeDeclarationBufferOffset++;

			// optimized for 4 hex digit chunk sizes (in which the following never would get called)
			// support up to 8 hex digit chunk sizes (FFFF_FFFF)
			if (chunkSizeDeclarationBufferOffset + HTTP_LINEBREAK.length == bytesReadIntoBuffer && hexDigitCounter < 8) {
				bytesReadIntoBuffer = readIntoChunkSizeDeclarationBuffer();
			}
		}

		if (hexDigitCounter == 0) {
			throw new IllegalStateException(
					"Expected beginning of a chunk size declaration here '" + new String(chunkSizeDeclarationBuffer) + "' but could not find it.");
		}

		if (chunkSizeDeclarationBuffer[chunkSizeDeclarationBufferOffset] != CR) {
			throw new IllegalStateException("Expected CR after chunk size declaration but found none");
		}

		if (chunkSizeDeclarationBuffer[++chunkSizeDeclarationBufferOffset] != LF) {
			throw new IllegalStateException("Chunk size declaration did not end with CRLF");
		}

		// 7FFF_FFFF is Integer.MAX_VALUE so a higher value would create an overflow and produce a negative int value
		if (hexDigitCounter > 8 || size < 0) {
			throw new IllegalStateException("Chunk size too large or too small. Only from 0x0 to 0x7FFF_FFFF are supported");
		}

		chunkSizeDeclarationBufferOffset++;
		chunkCounter++;

		return size;
	}

	@Override
	public int read() throws IOException {
		if (endOfChunkedStreamReached) {
			return -1;
		}

		read(oneByteBuffer);
		return oneByteBuffer[0] & 0xff;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		long time2 = System.nanoTime();

		if (endOfChunkedStreamReached) {
			return -1;
		}

		if (atVeryBeginningOfInputStream) {
			int bytesRead = readIntoChunkSizeDeclarationBuffer();
			bytesRemainingInChunk = readNextChunkSizeFromBuffer(bytesRead);
			atVeryBeginningOfInputStream = false;
		}

		int bytesRead;

		// optimize for read()
		/*if (len == 1) {
			int readByte;

			// first use buffered bytes
			if (chunkSizeDeclarationBuffer.length > chunkSizeDeclarationBufferOffset) {
				readByte = chunkSizeDeclarationBuffer[chunkSizeDeclarationBufferOffset];
				chunkSizeDeclarationBufferOffset++;
			} else {
				long time = System.nanoTime();
				readByte = delegate.read();
				nanoTimeCounterDelegate += System.nanoTime() - time;
				if (readByte == -1) {
					throw new EOFException("Unexpected EOF when reading from delegate stream");
				}
			}

			b[off] = (byte) readByte;
			bytesRead = 1;
		} else {*/

			int maxBytesToRead = Math.min(bytesRemainingInChunk, len);

			// first use buffered bytes
			int unreadBytesInChunkSizeDeclarationBuffer = chunkSizeDeclarationBuffer.length - chunkSizeDeclarationBufferOffset;
			int bytesToReadFromBuffer = Math.min(maxBytesToRead, unreadBytesInChunkSizeDeclarationBuffer);
			System.arraycopy(chunkSizeDeclarationBuffer, chunkSizeDeclarationBufferOffset, b, off, bytesToReadFromBuffer);
			chunkSizeDeclarationBufferOffset += bytesToReadFromBuffer;
			bytesRead = bytesToReadFromBuffer;

			// then read from delegate
			long time = System.nanoTime();
//			int available = delegate.available();
			int bytesReadFromDelegate = delegate.read(b, off + bytesToReadFromBuffer, maxBytesToRead - bytesToReadFromBuffer);
			nanoTimeCounterDelegate += System.nanoTime() - time;

			if (bytesReadFromDelegate == -1) {
				throw new EOFException("Unexpected EOF when reading from delegate stream");
			}

			bytesRead += bytesReadFromDelegate;
//		}

		bytesRemainingInChunk -= bytesRead;

		if (bytesRemainingInChunk == 0) {

			bytesRemainingInChunk = readLinebreakAndNextChunkSize();

		} else if (bytesRemainingInChunk < 0) {
			throw new IndexOutOfBoundsException("Less than 0 bytes remaining in chunk");
		}

		if (bytesRemainingInChunk == 0) {
			endOfChunkedStreamReached = true;
		}
		nanoTimeCounter2 += System.nanoTime() - time2;

		return bytesRead;
	}

	@Override
	public void close() throws IOException {
		super.close();

		endOfChunkedStreamReached = true;
	}

	@Override
	public byte[] getUnreadBuffer() {
		return Arrays.copyOfRange(chunkSizeDeclarationBuffer, chunkSizeDeclarationBufferOffset, CHUNK_SIZE_DECLARATION_BUFFER_SIZE);
	}
}
