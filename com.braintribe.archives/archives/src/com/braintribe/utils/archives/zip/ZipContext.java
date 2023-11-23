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
package com.braintribe.utils.archives.zip;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.braintribe.utils.archives.ArchivesException;

/**
 * a representation of a Zip archive<br/>
 * Please note that in order to minimize memory consumption, the {@link ZipContext} will generate temporary files, ie. all streams you pass are INSTANTLY read, and their content
 * stored in temporary files.<br/>
 * CAVEAT: the current implementation will INTERNALLY open input streams to the respective sources (real files or temporary ones) and keep 
 * them open as long as the {@link ZipContext} isn't closed. The streams, files are closed when you call {@link #close()} (which is mandatory). The finalize method is no longer used to clean up resources.<br/>
 * Test artifact - which also acts as example - is in com.braintribe.utils.archives:ArchivesTest#1.0
 * 
 * @author pit
 *
 */
public interface ZipContext extends AutoCloseable {

	
	/**
	 * close all open streams, close all zip files, delete all temporary files 
	 * this is also called by finalize, but you should do it eventually 
	 */
	@Override
	void close();
	
	/**
	 * creates the {@link ZipContext} from the zip passed as file
	 * @param file - the {@link File} that represents the zip
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext from( File file) throws ArchivesException;
	/**
	 * creates the {@link ZipContext} from the zip passed as {@link InputStream}
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext from( InputStream stream) throws ArchivesException;
	/**
	 * creates the {@link ZipContext} from the zip passed as {@link ZipInputStream}
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext from( ZipInputStream stream) throws ArchivesException;

	/**
	 * writes the {@link ZipContext} to a {@link File} 
	 * @param file - the {@link File} to write to 
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext to( File file) throws ArchivesException;	
	/**
	 * writes the {@link ZipContext} to an {@link OutputStream}
	 * @param stream - the {@link OutputStream}
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext to( OutputStream stream) throws ArchivesException;
	
	/**
	 * writes the {@link ZipContext} to a {@link ZipOutputStream}
	 * @param stream - the {@link ZipOutputStream} to write to 
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext to( ZipOutputStream stream) throws ArchivesException;
		
	/**
	 * packs a directory into a {@link ZipContext}
	 * @param directory - the {@link File} that represents the directory 
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext pack( File directory) throws ArchivesException;
	
	/**
	 * packs a directory into a {@link ZipContext}, but any files are tested by the filter. 
	 * @param directory - the {@link File} that represents the directory
	 * @param filter - the {@link Predicate} that tests the pack 
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext pack( File directory, Predicate<File> filter) throws ArchivesException;
	
	/**
	 * packs an array of {@link File} to a {@link ZipContext}
	 * @param base - if not null, the names are created relative to it, otherwise, the full name's used
	 * @param files - the Array of {@link File}
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext pack (URI base, File... files) throws ArchivesException;
	
	/**
	 * packs an array of {@link File} to a {@link ZipContext}, but tests any files with the filter
	 * @param base - if not null, the names are created relative to it, otherwise, the full name's used
	 * @param files - the Array of {@link File}
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext pack (URI base, Predicate<File> filter, File... files) throws ArchivesException;
	
	/**
	 * unpacks the {@link ZipContext} into a directory 
	 * @param directory - the File that represents the directory 
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext unpack( File directory) throws ArchivesException;
	
	/**
	 * returns the byte array (the payload) of the entry with that name 
	 * @param name - the name of the entry 
	 * @return - the byte array or null, if the {@link ZipContext} has no such entry 
	 */
	InputStream get( String name);
	
	/**
	 * returns the {@link ZipContextEntry} with that name if any 
	 * @param name - the name of the {@link ZipContextEntry}
	 * @return - the {@link ZipContextEntry} or null if none's found
	 */
	ZipContextEntry getEntry( String name);
	
	/**
	 * returns a {@link List} of {@link ZipContextEntry} whose names match the passed regular expression 
	 * @param match - a {@link String} with the regular expression 
	 * @return - a {@link List} of {@link ZipContextEntry} that match 
	 */
	List<ZipContextEntry> getEntries( String match);
	
	/**
	 * returns a {@link List} of {@link ZipContextEntry} that pass the specified filter 
	 * @param filter - a {@link Predicate}  
	 * @return - a {@link List} of {@link ZipContextEntry} that match 
	 */
	List<ZipContextEntry> getEntries( Predicate<ZipContextEntry> filter);
		
	/**
	 * gives the names of the entries in the {@link ZipContext}
	 * @return A list of the headers
	 */
	List<String> getHeaders();
	
	/**
	 * dumps the full contents of the {@link ZipContext}
	 * @return - a {@link Map} of {@link String} to byte array (entry name to payload)
	 */
	Map<String, InputStream> plainDump();
	
	/**
	 * dumps te full contents of the {@link ZipContext}
	 * @return - a {@link Map} of {@link ZipEntry} to byte array (ZipEntry to payload)
	 */
	Map<ZipEntry, InputStream> dump();
		

	/**
	 * merge (aka import) all contents of the ZIP {@link File} into the {@link ZipContext}
	 * @param file - the {@link File} that points to the ZIP 
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext merge( File file) throws ArchivesException;
	
	/**
	 * merge (aka import) all contents of the ZIP {@link InputStream} into the {@link ZipContext}
	 * @param stream - the {@link InputStream} that accesses the ZIP
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext merge( InputStream stream) throws ArchivesException;
	
	/**
	 * merge (aka import) all contents of the ZIP {@link ZipInputStream} into the {@link ZipContext}
	 * @param stream - the {@link ZipInputStream} that accesses the ZIP
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext merge( ZipInputStream stream) throws ArchivesException;
	
	/**
	 * merge (aka import) all contents of a {@link ZipContext}} into the {@link ZipContext}
	 * @param context - the {@link ZipContext} that contains the zip
	 * @return - the {@link ZipContext}
	 */
	ZipContext merge( ZipContext context);
	
	
	/**
	 * add a file to a zip
	 * @param name - the name of the entry in the zip
	 * @param file - the file to use as source for the entry 
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext add( String name, File file) throws ArchivesException;
	
	/**
	 * add data from a input stream to the zip
	 * @param name - the name of the entry 
	 * @param stream - the stream that delivers the data 
	 * @return - the {@link ZipContext}
	 * @throws ArchivesException Thrown in the event of an error.
	 */
	ZipContext add( String name, InputStream stream) throws ArchivesException;	

	/**
	 * add a {@link ZipContextEntry} to an exiting {@link ZipContext}
	 * @param contextEntry - the {@link ZipContextEntry} to add 
	 * @return - the {@link ZipContext}
	 */
	ZipContext add( ZipContextEntry contextEntry);
	
	
}
