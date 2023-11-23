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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.braintribe.logging.Logger;

public class DeleteOnCloseFileInputStream extends FileInputStream {

	private static Logger logger = Logger.getLogger(DeleteOnCloseFileInputStream.class);

	protected File theFile;
	protected boolean deleteParentFolder = false;

	public DeleteOnCloseFileInputStream(File file) throws FileNotFoundException {
		super(file);
		this.theFile = file;
	}

	public DeleteOnCloseFileInputStream(File file, boolean deleteParentFolderIfEmpty) throws FileNotFoundException {
		super(file);
		this.theFile = file;
		this.deleteParentFolder = deleteParentFolderIfEmpty;
	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
		} catch (Exception e) {
			throw new IOException("Error while closing input stream from file " + theFile, e);
		} finally {
			deleteOnClose();
		}
	}

	private void deleteOnClose() {
		try {
			if (theFile != null && theFile.exists()) {

				int retries = 3;
				boolean trace = logger.isTraceEnabled();

				boolean deleteSuccess = this.deleteFile(this.theFile);

				if (!deleteSuccess) {
					logger.warn("Could not delete file: " + theFile.getAbsolutePath() + " after " + retries + " tries. Must be cleaned manually.");
				} else {
					if (this.deleteParentFolder) {
						if (trace) {
							logger.trace("Configured to delete containing folder if it is empty.");
						}

						File parent = this.theFile.getParentFile();
						if (parent != null) {
							File[] content = parent.listFiles();
							if (content != null && content.length > 0) {
								if (trace) {
									logger.trace("The parent folder " + parent.getAbsolutePath() + " is not empty");
								}
							} else {
								deleteSuccess = this.deleteFile(parent);
								if (!deleteSuccess) {
									logger.warn("Could not delete the parent folder: " + parent.getAbsolutePath() + " after " + retries
											+ " tries. Must be cleaned manually.");
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while trying to delete " + theFile, e);
		}
	}

	protected boolean deleteFile(File file) {
		int retries = 3;
		boolean trace = logger.isTraceEnabled();

		boolean fileExists = true;
		for (int i = 0; i < retries; i++) {
			file.delete();
			if (!file.exists()) {
				if (trace) {
					logger.trace("Successfully deleted file: " + file.getAbsolutePath());
				}
				fileExists = false;
				break;
			} else {
				if (trace) {
					logger.trace("Could not delete file: " + file.getAbsolutePath() + " in try: " + (i + 1));
				}
			}
		}
		return !fileExists;
	}
}
