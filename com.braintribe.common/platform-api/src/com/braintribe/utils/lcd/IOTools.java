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
package com.braintribe.utils.lcd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

/**
 * This class provides utility methods related to I/O.
 *
 * @author michael.lafite
 */
public class IOTools {

	public static final int SIZE_2M = 1 << 21;
	public static final int SIZE_1M = 1 << 20;

	public static final int SIZE_64K = 1 << 16;
	public static final int SIZE_32K = 1 << 15;
	public static final int SIZE_16K = 1 << 14;
	public static final int SIZE_8K = 1 << 13;
	public static final int SIZE_4K = 1 << 12;

	public static final Supplier<byte[]> BUFFER_SUPPLIER_64K = () -> new byte[SIZE_64K];
	public static final Supplier<byte[]> BUFFER_SUPPLIER_32K = () -> new byte[SIZE_32K];
	public static final Supplier<byte[]> BUFFER_SUPPLIER_16K = () -> new byte[SIZE_16K];
	public static final Supplier<byte[]> BUFFER_SUPPLIER_8K = () -> new byte[SIZE_8K];
	public static final Supplier<byte[]> BUFFER_SUPPLIER_4K = () -> new byte[SIZE_4K];

	protected IOTools() {
		// nothing to do
	}

	// Be careful: code that you add here must be GWT compatible!

	/**
	 * pumps all data up to EOF with a configurable buffer size from input to output stream (no output flushing here)
	 *
	 * @return the number of transferred bytes
	 */
	public static long pump(final InputStream inputStream, final OutputStream outputStream, int bufferSize) throws IOException {
		final byte[] buffer = new byte[bufferSize];

		int count;
		long totalCount = 0;

		while ((count = inputStream.read(buffer)) != -1) {
			try {
				outputStream.write(buffer, 0, count);
			} catch (Exception e) {
				throw new IOException("Error while transfering data. Data transferred so far: " + totalCount + ". Current buffer size: " + count, e);
			}
			totalCount += count;
		}

		return totalCount;
	}

	/**
	 * {@link #pump(InputStream, OutputStream, int) pumps} with a buffer size of 4096 bytes and flushes the output stream afterwards
	 */
	public static long pump(final InputStream inputStream, final OutputStream outputStream) throws IOException {
		long totalCount = pump(inputStream, outputStream, SIZE_64K);
		outputStream.flush();

		return totalCount;
	}

}
