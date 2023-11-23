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
package com.braintribe.model.processing.logs.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.stream.DeleteOnCloseFileInputStream;

public class ZippingInputStreamProvider implements InputStreamProvider {

	private String name;
	private int logFilesCount;
	private Collection<File> files;

	public ZippingInputStreamProvider(String name, MultiMap<String, File> logFiles, int logFilesCount) {
		this.name = name;
		this.logFilesCount = logFilesCount;

		files = new ArrayList<>();
		for (String logFileKey : logFiles.keySet()) {
			files.addAll(logFiles.getAll(logFileKey));
		}
	}
	public ZippingInputStreamProvider(String name, Collection<File> logFiles, int logFilesCount) {
		this.name = name;
		this.logFilesCount = logFilesCount;
		files = new ArrayList<>(logFiles);
	}

	@Override
	public InputStream openInputStream() throws IOException {

		File tempFile = File.createTempFile(name, ".zip");
		//It is a conscious decision that this file is not registered with deleteOnExit
		//The reason is that deleteOnExit creates a permanent reference to the File object
		//that is not removed until the JVM shuts down. This is a potential memory leak.
		//tempFile.deleteOnExit();
		FileTools.deleteFileWhenOrphaned(tempFile);

		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tempFile))) {
			int addedLogFiles = 0;
			for (File logFile : files) {
				if (addedLogFiles < logFilesCount) {
					try (FileInputStream in = new FileInputStream(logFile)) {
						out.putNextEntry(new ZipEntry(logFile.getName()));
						IOTools.pump(in, out, 0xffff);
					} finally {
						out.closeEntry();
						addedLogFiles++;
					}
				} else {
					break;
				}
			}

			out.finish();
		}

		return new DeleteOnCloseFileInputStream(tempFile);
	}

}
