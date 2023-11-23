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

import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.utils.IOTools;
import com.braintribe.web.multipart.impl.FormDataMultipartConstants;

public class Dechunker implements FormDataMultipartConstants {

	private final InputStream delegate;
	private final byte[] ignoreBefore;
	private final byte[] ignoreAfter;
	
	private final byte[] chunkSizeSizeBuffer;
	private final byte[] chunkSizeBuffer;
	
	private boolean lastChunkAnnounced;
	
	private static final int MAX_CHUNK_SIZE_DECLARATION_SIZE = 26;
	
	public static final int NORMALIZED_CHUNK_SIZE_BASE = 0x2000;
	public static final byte LAST_CHUNK_ANNOUNCEMENT = (byte)'!';
	public static final byte NORMALIZED_CHUNK_SIZE_INTRODUCTION = (byte)'^';

	public Dechunker(InputStream delegate, String ignoreBefore, String ignoreAfter) {
		this(delegate, ignoreBefore.getBytes(), ignoreAfter.getBytes());
	}
	
	public Dechunker(InputStream delegate, byte[] ignoreBefore, byte[] ignoreAfter) {
		this.delegate = delegate;
		this.ignoreBefore = ignoreBefore;
		this.ignoreAfter = ignoreAfter;
		
		chunkSizeSizeBuffer = new byte[ignoreBefore.length + 2 + ignoreAfter.length]; // +2: chunk size size declaration byte + 1 more byte that can always be read at the same time
		chunkSizeBuffer = new byte[MAX_CHUNK_SIZE_DECLARATION_SIZE -1];
	}

	private static int hexCharByteToInt(int x) {
		if (x >= 48 && x <= 57) {
			return x - 48;
		} else if (x >= 65 && x <= 70) {
			return x - 55;
		} else if (x >= 97 && x <= 102) {
			return x - 87;
		}

		throw new IllegalArgumentException("Byte " + x + " does not match a hex digit character");
	}

	private byte[] readFully(byte[] buffer) throws IOException {
		return readFully(buffer, buffer.length);
	}
	
	private byte[] readFully(byte[] buffer, int len) throws IOException {
		int read = IOTools.readFully(delegate, buffer, 0, len);
		if (read != len)
			throw new EOFException("Unexpected end of file during multipart reading. Expected " + len + " bytes, but read only: " + read);
		
		return buffer;
	}
	
	public int readChunkSize() throws IOException {
		if (lastChunkAnnounced)
			return 0;
		
		readFully(chunkSizeSizeBuffer);
		
		// Assert delimiter was correct
		for (int i=0; i<ignoreBefore.length; i++) {
			if (ignoreBefore[i] != chunkSizeSizeBuffer[i]) {
				throw new IOException("Expected '" + Arrays.toString(ignoreBefore) + "' followed by the size declaration for the chunk size but got: '" + Arrays.toString(chunkSizeSizeBuffer) + "'.");
			}
		}
		
		byte chunkSizeSizeAsCharByte = chunkSizeSizeBuffer[ignoreBefore.length];
		
		if (chunkSizeSizeAsCharByte == LF) {
			throw new NotImplementedException("Ending a chunked stream with LF is not supported any more.");
		}
		
		byte byteAfterChunkSizeSize = chunkSizeSizeBuffer[ignoreBefore.length + 1];
		
		int chunkSize;
		int chunkSizeSize;
		if (chunkSizeSizeAsCharByte == NORMALIZED_CHUNK_SIZE_INTRODUCTION) {
			int normalizedSizeFactor = hexCharByteToInt(byteAfterChunkSizeSize);
			chunkSizeSize = 1;
			chunkSize = NORMALIZED_CHUNK_SIZE_BASE << normalizedSizeFactor;
			System.arraycopy(chunkSizeSizeBuffer, ignoreBefore.length+chunkSizeSize+1, chunkSizeBuffer, chunkSizeSize, ignoreAfter.length);

		} else {
		
			chunkSizeSize = chunkSizeSizeAsCharByte - (byte)'a' + 1;
			
			if (chunkSizeSize < 1 || chunkSizeSize > 26) {
				throw new IllegalStateException("Chunk size size should be a lower case char but was: " + chunkSizeSizeAsCharByte);
			}
			
			int bytesUsedFromFirstRead = ignoreBefore.length + 1; // +1 = chunk size size declaration
			int additionalBytesToRead = chunkSizeSize-1; // one of these bytes was already read before
			
			readFully(chunkSizeBuffer, additionalBytesToRead); 
			
			chunkSize = 0;
			
			if (byteAfterChunkSizeSize == LAST_CHUNK_ANNOUNCEMENT) {
				lastChunkAnnounced = true;
				bytesUsedFromFirstRead++;
				chunkSizeSize --;
			} 
	
			// We need a buffer that starts at
			// chunkSizeSizeBuffer[ignoreBefore.length + 1]
			// and ends at
			// chunkSizeBuffer[remainingChunkSizeSize - ignoreAfter.length]
			int remainingBytesFirstReadBuffer = chunkSizeSizeBuffer.length - bytesUsedFromFirstRead;
			System.arraycopy(chunkSizeBuffer, 0, chunkSizeBuffer, remainingBytesFirstReadBuffer, additionalBytesToRead);
			System.arraycopy(chunkSizeSizeBuffer, bytesUsedFromFirstRead, chunkSizeBuffer, 0, remainingBytesFirstReadBuffer);
			
			for (int i=0; i<chunkSizeSize; i++) {
				int digit = chunkSizeBuffer[i];
	
				// 0000 0010
				chunkSize = chunkSize << 4;
				// 0010 0000
				chunkSize = chunkSize | hexCharByteToInt(digit);
				// 0010 1001
			}
		}
		
		// Assert delimiter was correct
		for (int i=0; i<ignoreAfter.length; i++) {
			if (ignoreAfter[i] != chunkSizeBuffer[i + chunkSizeSize]) {
				throw new IOException("Expected chunk size declaration followed by '" + Arrays.toString(ignoreAfter) + "' but got: '" + Arrays.toString(chunkSizeBuffer) + "'.");
			}
		}
			
		return chunkSize;
	}


	
	public byte[] readChunkInMemory(int chunkSize) throws IOException {
		if (chunkSize == 0) {
			return new byte[0];
		}

		byte[] buffer = new byte[chunkSize];
		readFully(buffer);
		
		return Arrays.copyOfRange(buffer, 0, chunkSize);
	}
}
