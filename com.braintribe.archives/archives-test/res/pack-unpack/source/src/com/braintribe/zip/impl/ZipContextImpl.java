// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.zip.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.braintribe.logging.Logger;
import com.braintribe.zip.api.ZipContext;
import com.braintribe.zip.api.ZipContextEntry;
import com.braintribe.zip.api.ZipUtil;
import com.braintribe.zip.api.ZipUtilException;

public class ZipContextImpl implements ZipContext {
	private static Logger log = Logger.getLogger(ZipContextImpl.class);
	
	private ZipInputStream zipInputStream;
	private ZipOutputStream zipOutputStream;
	private List<ZipContextEntryImpl> entries = new ArrayList<ZipContextEntryImpl>();
	
	public ZipContextImpl() {	
	}

	@Override
	public ZipContext to(File file) throws ZipUtilException{
		if (file == null) {
			String msg = String.format("an file must be specified");
			throw new ZipUtilException(msg);
		}
		OutputStream outstream = null;
		try {
			 outstream = new FileOutputStream(file);		
		} catch (FileNotFoundException e) {
			String msg= String.format("Cannot create output stream to [%s]", file.getAbsolutePath());
			throw new ZipUtilException(msg);
		}
		zipOutputStream = new ZipOutputStream( outstream);
		write();
		try {
			zipOutputStream.flush();
		} catch (IOException e) {
			String msg= String.format("cannot flush output stream");
			log.error( msg);
		}
		return this;
	}
	
	@Override
	public ZipContext to(OutputStream stream) throws ZipUtilException {
		zipOutputStream = new ZipOutputStream( stream);
		write();
		return this;
	}

	@Override
	public ZipContext to(ZipOutputStream stream) throws ZipUtilException{
		zipOutputStream = stream;
		write();
		try {
			zipOutputStream.flush();
		} catch (IOException e) {
			String msg= String.format("cannot flush output stream");
			log.error( msg);
		}
		return this;
	}
	
	private void write()  throws ZipUtilException {
		if (zipOutputStream == null){
			String msg = String.format("an output target must be specified");
			throw new ZipUtilException(msg);
		}
		for (ZipContextEntryImpl zipContextEntry: entries) {
			zipContextEntry.write(zipOutputStream);
		}
		try {
			zipOutputStream.flush();
		} catch (IOException e) {
			String msg= String.format("cannot flush output stream");
			log.error( msg);
		}
	}
	

	@Override
	public ZipContext from(File file) throws ZipUtilException {
		if (file == null) {
			String msg = String.format("an file must be specified");
			throw new ZipUtilException(msg);
		}
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			String msg = String.format("Cannot create input stream from [%s]", file);
			throw new ZipUtilException(msg);
		}
		zipInputStream = new ZipInputStream(inputStream);
		read();
		return this;
	}

	@Override
	public ZipContext from(InputStream stream) throws ZipUtilException {
		zipInputStream = new ZipInputStream(stream);
		read();
		return this;
	}

	@Override
	public ZipContext from(ZipInputStream stream) throws ZipUtilException {
		zipInputStream = stream;
		read();
		return this;
	}
	
	private void read() throws ZipUtilException {
		if (zipInputStream == null) {
			String msg = String.format("an input source must be specified");
			throw new ZipUtilException(msg);
		}
		ZipContextEntryImpl entry = null;
		while ((entry = ZipContextEntryImpl.read(zipInputStream)) != null) {
			entries.add( entry);
		}
	}
	
	@Override
	public byte[] get(String name) {
		for (ZipContextEntryImpl zipContextEntry : entries) {
			ZipEntry zipEntry = zipContextEntry.getZipEntry();
			if (zipEntry.getName().equalsIgnoreCase(name)) 
				return zipContextEntry.getData();
		}
		return null;
	}
	
	
	@Override
	public List<String> get() {
		List<String> headers = new ArrayList<>( entries.size());
		for (ZipContextEntryImpl zipContextEntry : entries) {
			ZipEntry zipEntry = zipContextEntry.getZipEntry();
			headers.add( zipEntry.getName());
		}
		return headers;
	}

	@Override
	public Map<String, byte[]> dump() {
		Map<String, byte[]> result = new HashMap<String, byte[]>( entries.size());
		for (ZipContextEntryImpl zipContextEntry : entries) {
			ZipEntry zipEntry = zipContextEntry.getZipEntry();
			result.put( zipEntry.getName(), zipContextEntry.getData());
		}
		return result;
	}

	@Override
	public ZipContext finish() throws ZipUtilException {
		if (zipOutputStream != null) {
			try {
				zipOutputStream.finish();
			} catch (IOException e) {
				String msg="cannot finish zip output stream";
				log.error(msg, e);
			}
		} else {
			String msg="no zip output stream active, cannot finish";
			throw new ZipUtilException(msg);
		}
		return this;
	}

	@Override
	public ZipContext close() {
		if (zipOutputStream != null) {
			try {					
				zipOutputStream.close();
			} catch (IOException e) {
				String msg="cannot close zip output stream";
				log.error(msg, e);
				
			}
		}
		if (zipInputStream != null) {
			try {
				zipInputStream.close();
			} catch (IOException e) {
				String msg="cannot close zip input stream";
				log.error(msg, e);
			}
		}
		return this;
	}

	@Override
	public ZipContext add(File file) throws ZipUtilException {
		ZipContextImpl imported = (ZipContextImpl) new ZipContextImpl().from(file).close();
		entries.addAll( imported.entries);
		return this;
	}

	@Override
	public ZipContext add(InputStream stream) throws ZipUtilException{
		ZipContextImpl imported = (ZipContextImpl) new ZipContextImpl().from( stream).close();
		entries.addAll( imported.entries);
		return this;
	}

	@Override
	public ZipContext add(ZipInputStream stream) throws ZipUtilException {		
		ZipContextImpl imported = (ZipContextImpl) new ZipContextImpl().from( stream).close();
		entries.addAll( imported.entries);
		return this;
	}
	
	

	@Override
	public ZipContext add(String name, File file) throws ZipUtilException {
		if (file.isDirectory()) {
			String msg = String.format("File [%s] is a directory and cannot be added to the zip. Use pack", file.getAbsolutePath());
			throw new ZipUtilException(msg);
		}
		byte[] bytes;
		try {
			bytes = Files.readAllBytes( Paths.get( file.toURI())); 
		} catch (IOException e) {
			String msg=String.format("File [%s]'s bytes cannot be read, so it won't be packed to [%s]", file.getAbsoluteFile(), name);
			throw new ZipUtilException(msg, e);			
		}
		ZipContextEntryImpl entry = new ZipContextEntryImpl();
		ZipEntry zipEntry = new ZipEntry(name);
		entry.setZipEntry(zipEntry);
		entry.setData(bytes);
		entries.add( entry);
		
		return this;
	}

	@Override
	public ZipContext add(String name, InputStream stream) throws ZipUtilException {
		try {
			// read .. 
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byte [] buffer = new byte[2048];
			int count  = 0;
			while ((count = stream.read( buffer)) != -1) {
				byteArrayOutputStream.write( buffer, 0, count);
			}
			byte [] payload = byteArrayOutputStream.toByteArray();		
			
			ZipContextEntryImpl entry = new ZipContextEntryImpl();
			ZipEntry zipEntry = new ZipEntry(name);
			entry.setZipEntry(zipEntry);
			entry.setData( payload);
			entries.add( entry);
		} catch (IOException e) {
			String msg=String.format("Entry [%s]'s bytes cannot be read, so it won't be packed", name);
			throw new ZipUtilException(msg, e);		
		}		
		return this;
	}

	@Override
	public ZipContext add(String name, byte[] bytes) {
		add( name, null, bytes);
		return this;
	}
	
	@Override
	public ZipContext add(String name, String comment, byte[] bytes) {
		ZipEntry zipEntry = new ZipEntry(name);
		if (comment != null) {
			zipEntry.setComment(comment);			
		}
		zipEntry.setSize( bytes.length);
		ZipContextEntryImpl entry = new ZipContextEntryImpl( zipEntry, bytes);
		entries.add( entry);
		return this;
	}

	@Override
	public ZipContext add(ZipContext context) {
		entries.addAll( ((ZipContextImpl) context).entries);
		return this;
	}

	@Override
	public ZipContext add(ZipContextEntry contextEntry) {
		entries.add( (ZipContextEntryImpl) contextEntry);
		return this;
	}

	@Override
	public ZipContext pack(File directory) throws ZipUtilException {
		File [] files = directory.listFiles();
		return pack( directory.toURI(), files);
	}
	
	@Override
	public ZipContext pack(URI base, File... files) throws ZipUtilException {
		for (File file : files) {
			if (!file.exists()) {
				String msg=String.format("File [%s] doesn't exist and won't be packed", file.getAbsoluteFile());
				log.error( msg);
				continue;
			}
			String name = base != null ? base.relativize( file.toURI()).getPath() : file.getName();
			
			if (file.isDirectory()) {
				// 
				ZipContextEntryImpl entry = new ZipContextEntryImpl();				
				ZipEntry zipEntry = new ZipEntry( name.endsWith("/") ? name : name + "/");
				entry.setZipEntry(zipEntry);
				
				File [] subFiles = file.listFiles();
				//pack( file.toURI(), subFiles);
				pack( base, subFiles);
				continue;
			}

			byte[] bytes;
			try {
				bytes = Files.readAllBytes( Paths.get( file.toURI())); 
			} catch (IOException e) {
				String msg=String.format("File [%s]'s bytes cannot be read, so it won't be packed", file.getAbsoluteFile());
				log.error( msg);
				continue;
			}
			ZipContextEntryImpl entry = new ZipContextEntryImpl();
			ZipEntry zipEntry = new ZipEntry(name);
			entry.setZipEntry(zipEntry);
			entry.setData(bytes);
			entries.add( entry);
		}
		return this;
	}

	@Override
	public ZipContext unpack(File directory) throws ZipUtilException {
		for (ZipContextEntryImpl entry : entries) {
			String name = entry.getZipEntry().getName();
			File file = new File( directory, name);
			if (file.isDirectory()) {
				file.mkdirs();
				continue;
			}			
			byte [] payload = entry.getData();
			try {
				FileOutputStream stream = new FileOutputStream(file);
				stream.write(payload);
				stream.flush();
				stream.close();
			} catch (FileNotFoundException e) {
				String msg = String.format("cannot create output stream to [%s]", file.getAbsolutePath());
				log.error( msg, e);
			} catch (IOException e) {
				String msg = String.format("cannot write to output stream targeting [%s]", file.getAbsolutePath());
				log.error( msg, e);
			}			
		}
		return this;
	}
	
	

}
