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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.archives.zip.ZipContextEntry;

/**
 * an implementation of {@link ZipContextEntry}
 * 
 * TODO : change implementation NOT to keep the input streams for each entry, but rather note where to get the input
 * stream.<br/>
 * CAVEAT: take into account that some zip entries have NO input stream (for instance directory entries) and therefore
 * none can be provided for it
 * 
 * @author pit
 *
 */
public class ZipContextEntryImpl implements ZipContextEntry {

	private ZipEntry zipEntry;
	private InputStream stream;

	public ZipContextEntryImpl() {
	}

	public ZipContextEntryImpl(ZipEntry zipEntry, InputStream stream) {
		this.zipEntry = zipEntry;
		this.stream = stream;
	}

	@Override
	public ZipEntry getZipEntry() {
		return zipEntry;
	}

	@Override
	public void setZipEntry(ZipEntry zipEntry) {
		this.zipEntry = zipEntry;
	}

	@Override
	public InputStream getPayload() {
		return stream;
	}

	public void setPayload(InputStream stream) {
		this.stream = stream;
	}
	/**
	 * write an existing entry to the stream
	 * 
	 * @param outstream
	 *            - the {@link ZipOutputStream} to write to
	 * @throws ArchivesException
	 *             - if it cannot be written
	 */
	public void write(ZipOutputStream outstream) throws ArchivesException {
		try {
			outstream.putNextEntry(zipEntry);
			if (stream != null) {
				ArchivesHelper.pump(stream, outstream);
			}
			outstream.closeEntry();
		} catch (IOException e) {
			String msg = String.format("cannot write zip entry name: '[%s]', comment: '[%s]' size: '[%d]' to stream", zipEntry.getName(),
					zipEntry.getComment(), zipEntry.getSize());
			throw new ArchivesException(msg);
		}
	}
}
