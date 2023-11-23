// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.utils.zip;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * the zip stuff ... 
 * @author pit
 *
 */
public interface ZipContext {

	
	/**
	 * close all open streams, close all zip files, delete all temporary files 
	 * this is also called by finalize, but you should do it eventually 
	 */
	void close();
	
	/**
	 * creates the {@link ZipContext} from the zip passed as file
	 * @param file - the {@link File} that represents the zip
	 * @return - the {@link ZipContext}
	 * @throws ZipUtilException
	 */
	ZipContext from( File file) throws ZipUtilException;
	/**
	 * creates the {@link ZipContext} from the zip passed as {@link InputStream}
	 * @param file - the {@link InputStream} that represents the zip
	 * @return - the {@link ZipContext}
	 * @throws ZipUtilException
	 */
	ZipContext from( InputStream stream) throws ZipUtilException;
	/**
	 * creates the {@link ZipContext} from the zip passed as {@link ZipInputStream}
	 * @param file - the {@link ZipInputStream} that represents the zip
	 * @return - the {@link ZipContext}
	 * @throws ZipUtilException
	 */
	ZipContext from( ZipInputStream stream) throws ZipUtilException;

	/**
	 * writes the {@link ZipContext} to a {@link File} 
	 * @param file - the {@link File} to write to 
	 * @return - the {@link ZipContext}
	 * @throws ZipUtilException
	 */
	ZipContext to( File file) throws ZipUtilException;	
	/**
	 * writes the {@link ZipContext} to an {@link OutputStream}
	 * @param stream - the {@link OutputStream}
	 * @return - the {@link ZipContext}
	 * @throws ZipUtilException
	 */
	ZipContext to( OutputStream stream) throws ZipUtilException;
	
	/**
	 * writes the {@link ZipContext} to a {@link ZipOutputStream}
	 * @param stream - the {@link ZipOutputStream} to write to 
	 * @return - the {@link ZipContext}
	 * @throws ZipUtilException
	 */
	ZipContext to( ZipOutputStream stream) throws ZipUtilException;
		
	/**
	 * packs a directory into a {@link ZipContext}
	 * @param directory - the {@link File} that represents the directory 
	 * @return - the {@link ZipContext}
	 * @throws ZipUtilException
	 */
	ZipContext pack( File directory) throws ZipUtilException;
	
	/**
	 * packs an array of {@link File} to a {@link ZipContext}
	 * @param base - if not null, the names are created relative to it, otherwise, the full name's used
	 * @param files - the Array of {@link File}
	 * @return - the {@link ZipContext}
	 * @throws ZipUtilException
	 */
	ZipContext pack (URI base, File... files) throws ZipUtilException;
	
	/**
	 * unpacks the {@link ZipContext} into a directory 
	 * @param directory - the File that represents the directory 
	 * @return - the {@link ZipContext}
	 * @throws ZipUtilException
	 */
	ZipContext unpack( File directory) throws ZipUtilException;
	
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
	 * gives the names of the entries in the {@link ZipContext}
	 * @return
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
	 * @throws ZipUtilException
	 */
	ZipContext merge( File file) throws ZipUtilException;
	
	/**
	 * merge (aka import) all contents of the ZIP {@link InputStream} into the {@link ZipContext}
	 * @param stream - the {@link InputStream} that accesses the ZIP
	 * @return - the {@link ZipContext}
	 * @throws ZipUtilException
	 */
	ZipContext merge( InputStream stream) throws ZipUtilException;
	
	/**
	 * merge (aka import) all contents of the ZIP {@link ZipInputStream} into the {@link ZipContext}
	 * @param stream - the {@link ZipInputStream} that accesses the ZIP
	 * @return - the {@link ZipContext}
	 * @throws ZipUtilException
	 */
	ZipContext merge( ZipInputStream stream) throws ZipUtilException;
	
	/**
	 * merge (aka import) all contents of a {@link ZipContext}} into the {@link ZipContext}
	 * @param context - the {@link ZipContext} that contains the zip
	 * @return - the {@link ZipContext}
	 * @throws ZipUtilException
	 */
	ZipContext merge( ZipContext context);
	
	
	ZipContext add( String name, File file) throws ZipUtilException;
	ZipContext add( String name, InputStream stream) throws ZipUtilException;	

	ZipContext add( ZipContextEntry contextEntry);
	
	
}
