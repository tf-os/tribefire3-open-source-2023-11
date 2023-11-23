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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.braintribe.logging.Logger;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.archives.zip.ZipContext;
import com.braintribe.utils.archives.zip.ZipContextEntry;

/**
 * an implementation of the {@link ZipContext} interface
 * 
 * @author pit
 *
 */
public class ZipContextImpl implements ZipContext {
	private static Logger log = Logger.getLogger(ZipContextImpl.class);

	private List<ZipContextEntryImpl> entries = new ArrayList<ZipContextEntryImpl>();

	private Set<ZipFile> zipFilesToClose = new HashSet<ZipFile>();
	private Set<File> filesToDelete = new HashSet<File>();
	private Set<InputStream> streamsToClose = new HashSet<InputStream>();
	
	private void closeZipFiles() {
		for (ZipFile file : zipFilesToClose) {
			try {
				file.close();
			} catch (IOException e) {
				String msg = String.format( "Cannot close zip file [%s]", file.getName());
				log.warn( msg, e);
			}
		}
		zipFilesToClose.clear();
	}
	
	private void closeStreams() {
		for (InputStream stream : streamsToClose) {
			try {
				stream.close();
			} catch (IOException e) {
				String msg = String.format( "Cannot close stream [%s]", stream);
				log.warn( msg, e);
			}
		}
		streamsToClose.clear();
	}
	
	private void deleteFiles() {
		for (File file : filesToDelete) {
			if (file.delete() == false)
				file.deleteOnExit();
		}
		filesToDelete.clear();
	}
	
	private void transfer( ZipContextImpl sibling) {
		zipFilesToClose.addAll( sibling.zipFilesToClose);
		sibling.zipFilesToClose.clear();
		
		streamsToClose.addAll( sibling.streamsToClose);
		sibling.streamsToClose.clear();
		
		filesToDelete.addAll( sibling.filesToDelete);
		sibling.filesToDelete.clear();
	}
	
	@Override
	public ZipContext to(File file) throws ArchivesException{
		if (file == null) {
			String msg = String.format("a file must be specified");
			throw new ArchivesException(msg);
		}
		OutputStream outstream = null;
		try {
			 outstream = new FileOutputStream(file);		
		} catch (FileNotFoundException e) {
			String msg= String.format("Cannot create output stream to [%s]", file.getAbsolutePath());
			throw new ArchivesException(msg, e);
		}
		ZipOutputStream zipOutputStream = new ZipOutputStream( outstream);
		write( zipOutputStream);
		try {
			zipOutputStream.close();
		} catch (IOException e) {
			String msg= String.format("cannot close output stream");
			log.error( msg, e);
		}
		return this;
	}
	
	@Override
	public ZipContext to(OutputStream stream) throws ArchivesException {
		ZipOutputStream zipOutputStream = new ZipOutputStream( stream);
		write( zipOutputStream);
		return this;
	}

	@Override
	public ZipContext to(ZipOutputStream stream) throws ArchivesException{
		write( stream);
		return this;
	}
	
	private void write( ZipOutputStream zipOutputStream)  throws ArchivesException {
		if (zipOutputStream == null){
			String msg = String.format("an output target must be specified");
			throw new ArchivesException(msg);
		}
		for (ZipContextEntryImpl zipContextEntry: entries) {
			zipContextEntry.write(zipOutputStream);
		}
		try {
			zipOutputStream.flush();
		} catch (IOException e) {
			String msg= String.format("cannot flush output stream");
			log.error( msg, e);
		}
		try {
			zipOutputStream.finish();
		} catch (IOException e) {
			String msg= String.format("cannot finish output stream");
			log.error( msg, e);
		}
	}
	

	@Override
	public ZipContext from(File file) throws ArchivesException {
		if (file == null) {
			String msg = String.format("an file must be specified");
			throw new ArchivesException(msg);
		}
		try {
			ZipFile zipFile = new ZipFile( file);
			Enumeration<? extends ZipEntry> entriesEnumeration = zipFile.entries();
			while (entriesEnumeration.hasMoreElements()) {
				ZipEntry zipEntry = entriesEnumeration.nextElement();
				ZipContextEntryImpl entry = new ZipContextEntryImpl( zipEntry, zipFile.getInputStream(zipEntry));
				entries.add( entry);
			}
			zipFilesToClose.add(zipFile);
		} catch (Exception e1) {
			String msg = String.format("cannot create zip file from [%s]", file.getAbsolutePath());
			throw new ArchivesException(msg, e1);
		}
		
		return this;
	}

	@Override
	public ZipContext from(InputStream stream) throws ArchivesException {
		File tempZipFile;
		try {
			 tempZipFile = File.createTempFile("zipTool-", ".zip");
		} catch (IOException e) {
			String msg ="cannot create temporary file";
			throw new ArchivesException(msg, e);
		}
		FileOutputStream outstream = null;
		try {
			outstream = new FileOutputStream( tempZipFile);
			ArchivesHelper.pump(stream, outstream);
		} catch (FileNotFoundException e) {
			String msg="cannot generate output stream to temporary file";
			throw new ArchivesException(msg,e);
		} catch (IOException e) {
			String msg="cannot write to output stream to temporary file";
			throw new ArchivesException(msg,e);
		}
		finally {
			try {
				outstream.close();
			} catch (IOException e) {
				log.warn("cannot close output stream", e);
			}
		}
		from( tempZipFile);
		
		return this;
	}

	@Override
	public ZipContext from(ZipInputStream zipInStream) throws ArchivesException {
		File tempZipFile;
		try {
			 tempZipFile = File.createTempFile("zipTool-", ".zip");
		} catch (IOException e) {
			String msg ="cannot create temporary file";
			throw new ArchivesException(msg, e);
		}
		try {
			FileOutputStream outstream = new FileOutputStream( tempZipFile);
			ZipOutputStream zipOutStream = new ZipOutputStream( outstream);
			ArchivesHelper.pump(zipInStream, zipOutStream);
			zipOutStream.close();
		} catch (FileNotFoundException e) {
			String msg="cannot create output stream to temp file";
			throw new ArchivesException(msg, e);
		} catch (IOException e) {
			String msg="cannot pump streams";
			throw new ArchivesException(msg, e);
		}
		return this;
	}
	
	@Override
	public ZipContextEntry getEntry(String name) {		
		for (ZipContextEntryImpl zipContextEntry : entries) {
			ZipEntry zipEntry = zipContextEntry.getZipEntry();
			if (zipEntry.getName().equalsIgnoreCase(name)) 
				return zipContextEntry;
		}
		return null;
	}

	
	@Override
	public List<ZipContextEntry> getEntries(String match) {
		List<ZipContextEntry> result = new ArrayList<ZipContextEntry>();
		for (ZipContextEntryImpl zipContextEntry : entries) {
			ZipEntry zipEntry = zipContextEntry.getZipEntry();
			if (zipEntry.getName().matches( match)) {
				result.add(zipContextEntry);
			}				
		}
		return result;		
	}

	
	@Override
	public List<ZipContextEntry> getEntries(Predicate<ZipContextEntry> filter) {
		List<ZipContextEntry> result = new ArrayList<ZipContextEntry>();
		for (ZipContextEntryImpl zipContextEntry : entries) {			
			if (filter.test(zipContextEntry)) {
				result.add(zipContextEntry);
			}				
		}
		return result;
	}

	@Override
	public List<String> getHeaders() {
		List<String> headers = new ArrayList<String>( entries.size());
		for (ZipContextEntryImpl zipContextEntry : entries) {
			ZipEntry zipEntry = zipContextEntry.getZipEntry();
			headers.add( zipEntry.getName());
		}
		return headers;
	}
	
	@Override
	public ZipContext merge(File file) throws ArchivesException {
		ZipContextImpl sibling = (ZipContextImpl) Archives.zip().from(file);
		entries.addAll( sibling.entries);
		transfer( sibling);
		return this;
	}

	@Override
	public ZipContext merge(InputStream stream) throws ArchivesException{
		ZipContextImpl sibling = (ZipContextImpl) Archives.zip().from( stream);
		entries.addAll( sibling.entries);
		transfer( sibling);
		return this;
	}

	@Override
	public ZipContext merge(ZipInputStream stream) throws ArchivesException {		
		ZipContextImpl sibling = (ZipContextImpl) Archives.zip().from( stream);
		entries.addAll( sibling.entries);
		transfer( sibling);	
		return this;
	}
	
	

	@Override
	public ZipContext add(String name, File file) throws ArchivesException {
		if (file.isDirectory()) {
			String msg = String.format("File [%s] is a directory and cannot be added to the zip. Use pack", file.getAbsolutePath());
			throw new ArchivesException(msg);
		}
		try {
			FileInputStream stream = new FileInputStream( file);
			ZipContextEntryImpl entry = new ZipContextEntryImpl();
			ZipEntry zipEntry = new ZipEntry(name);
			entry.setZipEntry(zipEntry);
			entry.setPayload(stream);
			streamsToClose.add( stream);
			entries.add( entry);
		} catch (FileNotFoundException e) {
			String msg = String.format("cannot import entry [%s] from file [%s]", name, file.getAbsolutePath());
			throw new ArchivesException(msg, e);
		}
		return this;
	}

	@Override
	public ZipContext add(String name, InputStream stream) throws ArchivesException {
		try {
			File tempFile = File.createTempFile( "zipTool-", ".zip");
			FileOutputStream outstream = new FileOutputStream(tempFile);
			ArchivesHelper.pump(stream, outstream);
			outstream.close();
			filesToDelete.add( tempFile);
			add( name, tempFile);
		} catch (IOException e) {
			String msg=String.format("Entry [%s]'s bytes cannot be read, so it won't be packed", name);
			throw new ArchivesException(msg, e);		
		}		
		return this;
	}

	
	
	

	@Override
	public ZipContext merge(ZipContext context) {
		entries.addAll( ((ZipContextImpl) context).entries);
		return this;
	}

	@Override
	public ZipContext add(ZipContextEntry contextEntry) {
		entries.add( (ZipContextEntryImpl) contextEntry);
		return this;
	}

	@Override
	public ZipContext pack(File directory) throws ArchivesException {
		File [] files = directory.listFiles();
		return pack( directory.toURI(), files);
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.utils.zip.ZipContext#pack(java.net.URI, java.io.File[])
	 */
	@Override
	public ZipContext pack(URI base, File... files) throws ArchivesException {
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
				entries.add( entry);
				File [] subFiles = file.listFiles();
				pack( base, subFiles);
				continue;
			}
			
			FileInputStream inputStream = null;
	
			try {
				inputStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				String msg = String.format("cannot create input stream to file [%s]", file.getAbsolutePath());
				log.warn( msg, e);
				continue;
			}

			ZipContextEntryImpl entry = new ZipContextEntryImpl();
			ZipEntry zipEntry = new ZipEntry(name);
			entry.setZipEntry(zipEntry);
			entry.setPayload(inputStream);
			streamsToClose.add( inputStream);
			entries.add( entry);
		}
		return this;
	}
	
	

	@Override
	public ZipContext pack(File directory, Predicate<File> filter) throws ArchivesException {		
		File [] files = directory.listFiles();
		return pack( directory.toURI(), filter, files);
	}

	@Override
	public ZipContext pack(URI base, Predicate<File> filter, File... files) throws ArchivesException {
		for (File file : files) {
			if (!file.exists()) {
				String msg=String.format("File [%s] doesn't exist and won't be packed", file.getAbsoluteFile());
				log.error( msg);
				continue;
			}
			if (!filter.test(file)) {
				continue;
			}
			String name = base != null ? base.relativize( file.toURI()).getPath() : file.getName();
			
			if (file.isDirectory()) {
				// 
				ZipContextEntryImpl entry = new ZipContextEntryImpl();				
				ZipEntry zipEntry = new ZipEntry( name.endsWith("/") ? name : name + "/");
				entry.setZipEntry(zipEntry);
				entries.add( entry);
				File [] subFiles = file.listFiles();
				pack( base, filter, subFiles);
				continue;
			}
			
			FileInputStream inputStream = null;
	
			try {
				inputStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				String msg = String.format("cannot create input stream to file [%s]", file.getAbsolutePath());
				log.warn( msg, e);
				continue;
			}

			ZipContextEntryImpl entry = new ZipContextEntryImpl();
			ZipEntry zipEntry = new ZipEntry(name);
			entry.setZipEntry(zipEntry);
			entry.setPayload(inputStream);
			streamsToClose.add( inputStream);
			entries.add( entry);
		}
		return this;
	}

	@Override
	public ZipContext unpack(File directory) throws ArchivesException {
		for (ZipContextEntryImpl entry : entries) {
			String name = entry.getZipEntry().getName();
			File file = new File( directory, name);
			
			if (!FileTools.isInSubDirectory(directory, file)) {
				throw new RuntimeException("The target file "+file.getAbsolutePath()+" is not within the target folder "+directory.getAbsolutePath()+" (entry name: "+name+"). This is not allowed.");
			}
			
			if (name.endsWith("/")) {
				file.mkdirs();
				continue;
			} else {
				File parent = file.getParentFile();
				parent.mkdirs();
			}
			InputStream inputStream = entry.getPayload();
			try {
				FileOutputStream outstream = new FileOutputStream(file);
				ArchivesHelper.pump(inputStream, outstream);
				outstream.close();
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

	@Override
	public InputStream get(String name) {
		for (ZipContextEntryImpl entry : entries) {
			if (entry.getZipEntry().getName().equals( name)) {
				return entry.getPayload();
			}
		}
		return null;
	}

	@Override
	public Map<String, InputStream> plainDump() {
		Map<String, InputStream> result = new HashMap<String, InputStream>();
		for (ZipContextEntryImpl entry : entries) {
			result.put( entry.getZipEntry().getName(), entry.getPayload());
		}
		return result;
	}

	@Override
	public Map<ZipEntry, InputStream> dump() {
		Map<ZipEntry, InputStream> result = new HashMap<ZipEntry, InputStream>();
		for (ZipContextEntryImpl entry : entries) {
			result.put( entry.getZipEntry(), entry.getPayload());
		}
		return result;
	}
	

	@Override
	public void close() {
		closeZipFiles();
		closeStreams();
		deleteFiles();
	}


}
