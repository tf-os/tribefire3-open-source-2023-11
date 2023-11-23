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
import java.io.Reader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

public class ReaderInputStream extends InputStream {
	private static final int DEFAULT_BUFFER_SIZE = 1024;

	private final Reader reader;
	private final CharsetEncoder encoder;

	/**
	 * CharBuffer used as input for the decoder. It should be reasonably large as we read data from the underlying Reader into this buffer.
	 */
	private final CharBuffer encoderIn;

	/**
	 * ByteBuffer used as output for the decoder. This buffer can be small as it is only used to transfer data from the decoder to the buffer provided
	 * by the caller.
	 */
	private final ByteBuffer encoderOut;

	private CoderResult lastCoderResult;
	private boolean endOfInput;

	/**
	 * Construct a new {@link ReaderInputStream}.
	 *
	 * @param reader
	 *            the target {@link Reader}
	 * @param encoder
	 *            the charset encoder
	 * @since 2.1
	 */
	public ReaderInputStream(Reader reader, CharsetEncoder encoder) {
		this(reader, encoder, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Construct a new {@link ReaderInputStream}.
	 *
	 * @param reader
	 *            the target {@link Reader}
	 * @param encoder
	 *            the charset encoder
	 * @param bufferSize
	 *            the size of the input buffer in number of characters
	 * @since 2.1
	 */
	public ReaderInputStream(Reader reader, CharsetEncoder encoder, int bufferSize) {
		this.reader = reader;
		this.encoder = encoder;
		this.encoderIn = CharBuffer.allocate(bufferSize);
		((Buffer) this.encoderIn).flip();
		this.encoderOut = ByteBuffer.allocate(128);
		((Buffer) this.encoderOut).flip();
	}

	/**
	 * Construct a new {@link ReaderInputStream}.
	 *
	 * @param reader
	 *            the target {@link Reader}
	 * @param charset
	 *            the charset encoding
	 * @param bufferSize
	 *            the size of the input buffer in number of characters
	 */
	public ReaderInputStream(Reader reader, Charset charset, int bufferSize) {
		this(reader, charset.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE), bufferSize);
	}

	/**
	 * Construct a new {@link ReaderInputStream} with a default input buffer size of 1024 characters.
	 *
	 * @param reader
	 *            the target {@link Reader}
	 * @param charset
	 *            the charset encoding
	 */
	public ReaderInputStream(Reader reader, Charset charset) {
		this(reader, charset, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Construct a new {@link ReaderInputStream}.
	 *
	 * @param reader
	 *            the target {@link Reader}
	 * @param charsetName
	 *            the name of the charset encoding
	 * @param bufferSize
	 *            the size of the input buffer in number of characters
	 */
	public ReaderInputStream(Reader reader, String charsetName, int bufferSize) {
		this(reader, Charset.forName(charsetName), bufferSize);
	}

	/**
	 * Construct a new {@link ReaderInputStream} with a default input buffer size of 1024 characters.
	 *
	 * @param reader
	 *            the target {@link Reader}
	 * @param charsetName
	 *            the name of the charset encoding
	 */
	public ReaderInputStream(Reader reader, String charsetName) {
		this(reader, charsetName, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Construct a new {@link ReaderInputStream} that uses the default character encoding with a default input buffer size of 1024 characters.
	 *
	 * @param reader
	 *            the target {@link Reader}
	 */
	public ReaderInputStream(Reader reader) {
		this(reader, Charset.defaultCharset());
	}

	/**
	 * Fills the internal char buffer from the reader.
	 *
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	private void fillBuffer() throws IOException {
		if (!endOfInput && (lastCoderResult == null || lastCoderResult.isUnderflow())) {
			encoderIn.compact();
			int position = encoderIn.position();
			// We don't use Reader#read(CharBuffer) here because it is more efficient
			// to write directly to the underlying char array (the default implementation
			// copies data to a temporary char array).
			int c = reader.read(encoderIn.array(), position, encoderIn.remaining());
			if (c == -1) {
				endOfInput = true;
			} else {
				encoderIn.position(position + c);
			}
			((Buffer) this.encoderIn).flip();
		}
		encoderOut.compact();
		lastCoderResult = encoder.encode(encoderIn, encoderOut, endOfInput);
		((Buffer) this.encoderOut).flip();
	}

	/**
	 * Read the specified number of bytes into an array.
	 *
	 * @param b
	 *            the byte array to read into
	 * @param off
	 *            the offset to start reading bytes into
	 * @param len
	 *            the number of bytes to read
	 * @return the number of bytes read or <code>-1</code> if the end of the stream has been reached
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("Byte array must not be null");
		}
		if (len < 0 || off < 0 || (off + len) > b.length) {
			throw new IndexOutOfBoundsException("Array Size=" + b.length + ", offset=" + off + ", length=" + len);
		}
		int read = 0;
		if (len == 0) {
			return 0; // Always return 0 if len == 0
		}
		while (len > 0) {
			if (encoderOut.hasRemaining()) {
				int c = Math.min(encoderOut.remaining(), len);
				encoderOut.get(b, off, c);
				off += c;
				len -= c;
				read += c;
			} else {
				fillBuffer();
				if (endOfInput && !encoderOut.hasRemaining()) {
					break;
				}
			}
		}
		return read == 0 && endOfInput ? -1 : read;
	}

	/**
	 * Read the specified number of bytes into an array.
	 *
	 * @param b
	 *            the byte array to read into
	 * @return the number of bytes read or <code>-1</code> if the end of the stream has been reached
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * Read a single byte.
	 *
	 * @return either the byte read or <code>-1</code> if the end of the stream has been reached
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public int read() throws IOException {
		for (;;) {
			if (encoderOut.hasRemaining()) {
				return encoderOut.get() & 0xFF;
			} else {
				fillBuffer();
				if (endOfInput && !encoderOut.hasRemaining()) {
					return -1;
				}
			}
		}
	}

	/**
	 * Close the stream. This method will cause the underlying {@link Reader} to be closed.
	 *
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public void close() throws IOException {
		reader.close();
	}
}
