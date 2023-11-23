// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.zip.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.braintribe.zip.api.ZipUtilException;

public class ZipContextEntryImpl {

	private ZipEntry zipEntry;
	private byte [] data;
	
	public ZipContextEntryImpl() {	
	}
	
	public ZipContextEntryImpl(ZipEntry zipEntry, byte [] data) {
		this.zipEntry = zipEntry;
		this.data = data;
	}

	public ZipEntry getZipEntry() {
		return zipEntry;
	}

	public void setZipEntry(ZipEntry zipEntry) {
		this.zipEntry = zipEntry;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
		
	
	/**
	 * write an existing entry to the stream 
	 * @param outstream - the {@link ZipOutputStream} to write to 
	 * @throws ZipUtilException - if it cannot be written 
	 */
	public void write( ZipOutputStream outstream) throws ZipUtilException{
		try {
			outstream.putNextEntry(zipEntry);
			outstream.write( data, 0, data.length);
			outstream.closeEntry();
		} catch (IOException e) {
			String msg=String.format("cannot write zip entry [%s], [%s] to stream",zipEntry.getName(), zipEntry.getComment());
			throw new ZipUtilException(msg);
		}
	}
	
	/**
	 * reads an entry from an input stream, and returns it 
	 * @param instream - the {@link ZipInputStream} to read from 
	 * @return - the {@link ZipContextEntryImpl} as reflecting the {@link ZipEntry}
	 * @throws ZipUtilException
	 */
	public static ZipContextEntryImpl read( ZipInputStream instream) throws ZipUtilException {
		ZipEntry entry;
		try {
			entry = instream.getNextEntry();
		} catch (IOException e1) {
			String msg=String.format("cannot read zip entry header from stream");
			throw new ZipUtilException(msg, e1);
		}
		if (entry == null)
			return null;
		
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte [] buffer = new byte[2048];
		int count  = 0;
		try {
			while ((count = instream.read( buffer)) != -1) {
				byteArrayOutputStream.write( buffer, 0, count);
			}
		} catch (IOException e) {
			String msg=String.format("cannot read zip entry data from stream");
			throw new ZipUtilException(msg, e);
		}
		byte [] payload = byteArrayOutputStream.toByteArray();
		
		ZipContextEntryImpl zipContextEntry = new ZipContextEntryImpl();
		zipContextEntry.setZipEntry(entry);
		zipContextEntry.setData(payload);
		return zipContextEntry;
	}
	
}
