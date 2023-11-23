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
package com.braintribe.utils.archives.zip.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.braintribe.utils.IOTools;


/**
 * simple helper functions 
 * 
 * @author Pit
 *
 */
public class ArchivesHelper {
	/**
	 * pump from one stream to the other 
	 * @param inputStream - the {@link InputStream} to read from 
	 * @param outputStream - the {@link OutputStream} to write to 
	 * @throws IOException - thrown if anything goes wrong
	 */
	public static void pump(InputStream inputStream, OutputStream outputStream) throws IOException {
		int bufferSize = IOTools.SIZE_64K;
		byte[] buffer = new byte[bufferSize];

		int count;
		while ((count = inputStream.read(buffer)) != -1) { 
			outputStream.write(buffer, 0, count);
		}
		outputStream.flush();
	}
	
	/**
	 * pump from one ZIP stream to the other 
	 * @param inputStream - the {@link ZipInputStream} to read from 
	 * @param outputStream - the {@link ZipOutputStream} to write to 
	 * @throws IOException - thrown if anything goes wrong 
	 */
	public static void pump( ZipInputStream inputStream, ZipOutputStream outputStream) throws IOException {
		ZipEntry zipEntry = null;
		while ((zipEntry = inputStream.getNextEntry()) != null) {
			outputStream.putNextEntry(zipEntry);
			pump( (InputStream) inputStream, (OutputStream) outputStream);
		}
		outputStream.flush();
		outputStream.finish();
	}
}
