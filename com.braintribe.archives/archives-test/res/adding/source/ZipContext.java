// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.zip.api;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public interface ZipContext {

	ZipContext from( File file) throws ZipUtilException;
	ZipContext from( InputStream stream) throws ZipUtilException;
	ZipContext from( ZipInputStream stream) throws ZipUtilException;

	ZipContext to( File file) throws ZipUtilException;	
	ZipContext to( OutputStream stream) throws ZipUtilException;
	ZipContext to( ZipOutputStream stream) throws ZipUtilException;
		
	ZipContext pack( File directory) throws ZipUtilException;
	ZipContext pack (URI base, File... files) throws ZipUtilException;		
	ZipContext unpack( File directory) throws ZipUtilException;
	
	byte [] get( String name);
	List<String> get();
	Map<String, byte []> dump();
		
	ZipContext finish() throws ZipUtilException;
	ZipContext close() throws ZipUtilException;
	
	ZipContext add( File file) throws ZipUtilException;
	ZipContext add( InputStream stream) throws ZipUtilException;		
	ZipContext add( ZipInputStream stream) throws ZipUtilException;
	
	ZipContext add( String name, File file) throws ZipUtilException;
	ZipContext add( String name, InputStream stream) throws ZipUtilException;
	ZipContext add( String name, byte [] bytes);
	ZipContext add( String name, String comment, byte [] bytes);
	ZipContext add( ZipContext context);
	ZipContext add( ZipContextEntry contextEntry);
	
	
}
