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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import com.braintribe.utils.IOTools;

public class BackedUpChunkedOutputStream extends ChunkedOutputStream {
	private final File backupFile;
	private final OutputStream backupOut;
	private final InputStream backupIn;

	public BackedUpChunkedOutputStream(OutputStream delegate, boolean proprietaryMode) {
		this(delegate, IOTools.SIZE_64K, proprietaryMode);
	}

	public BackedUpChunkedOutputStream(OutputStream delegate, int chunkSize, boolean proprietaryMode) {
		super(delegate, chunkSize, proprietaryMode);

		try {
			backupFile = Files.createTempFile("chunked_backup", null).toFile();
			backupOut = new BufferedOutputStream(new FileOutputStream(backupFile));
			backupIn = new BufferedInputStream(new FileInputStream(backupFile));
		} catch (IOException e) {
			throw new UncheckedIOException("Could not create backup file for chunked output stream", e);
		}
	}

	@Override
	protected void addBytesToCurrentChunk(byte[] b, int off, int numOfBytesToWriteInTotal) {
		try {
			backupOut.write(b, off, numOfBytesToWriteInTotal);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not write to backup of chunked output stream", e);
		}
	}

	@Override
	protected void writeOutCurrentChunkContent() throws IOException {
		backupOut.flush();
		
		IOTools.transferBytes(backupIn, delegate, IOTools.BUFFER_SUPPLIER_64K);
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		backupIn.close();
		backupOut.close();
		backupFile.delete();
	}
}
