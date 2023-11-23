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
package com.braintribe.model.io.metamodel.render;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * 
 */
public class SerializingUtils {

	public static void copySourceToDestination(File source, File destination) {
		destination = getFileInExistingFolder(destination);
		try {
			InputStream in = new FileInputStream(source);
			try {
				OutputStream out = new FileOutputStream(destination);

				try {
					copyInToOut(in, out);
				} finally {
					out.close();
				}
			} finally {
				in.close();
			}

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static File getFileInExistingFolder(File file) {
		File parentFile = file.getParentFile();
		if (parentFile.isDirectory() || parentFile.mkdirs()) {
			return file;
		}

		throw new IllegalArgumentException("Failed to create folders to store file: " + file.getAbsolutePath());
	}

	private static void copyInToOut(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
	}

}
