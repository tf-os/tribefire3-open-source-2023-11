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
package com.braintribe.utils.stream.blocks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import com.braintribe.logging.Logger;

/**
 * A {@link Block} that persists in a {@link File} and thus can in theory grow to unlimited size.
 * {@link #destroy()} deletes that file.
 * 
 * @author Neidhart.Orlich
 *
 */
public class FileBlock extends Block {
	private static final Logger logger = Logger.getLogger(FileBlock.class);

	private final File file;
	private int inputBufferSize = 0;

	/**
	 * @param file File to be used as buffer file for this block. Parent folder MUST exist.
	 */
	public FileBlock(File file) {
		this.file = file;
	}

	@Override
	public InputStream openRawInputStream() {
		InputStream inputStream;

		try {
			ensureParentDir();
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new UncheckedIOException(e);
		}
		
		if (inputBufferSize > 0) {
			inputStream = new BufferedInputStream(inputStream, inputBufferSize);
		}
		
		return inputStream;
	}

	private void ensureParentDir() {
		File parentDir = file.getParentFile();
		if (parentDir != null && !parentDir.exists()) {
			logger.warn("Parent directory of the FileBlock's buffer file does not exist: " + parentDir.getAbsolutePath() + "\nMaybe it was deleted by a cleanup job? If so please make sure that this folder will be excluded from now on from any clean up jobs. This is not critical in this case but might result in slight performance loss or in other cases even in data loss. Creating parent directory now.");
			parentDir.mkdirs();
		}
	}

	@Override
	public OutputStream openOutputStream() {
		OutputStream outputStream;
		
		try {
			ensureParentDir();
			outputStream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new UncheckedIOException(e);
		}
		
		return outputStream;
	}

	@Override
	public int getTreshold() {
		return -1;
	}

	@Override
	public void destroy() {
		file.delete();
	}
	
	@Override
	public void autoBufferInputStreams(int bufferSize) {
		this.inputBufferSize = bufferSize;
	}
	
	public File getFile() {
		return file;
	}

	@Override
	public boolean isAutoBuffered() {
		return true;
	}

	@Override
	public long getBytesAllocated() {
		return file.length();
	}
}