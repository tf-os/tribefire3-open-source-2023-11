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
package com.braintribe.devrock.greyface.process.retrieval;

import java.io.File;
import java.io.IOException;

import com.braintribe.devrock.greyface.GreyfacePlugin;
import com.braintribe.logging.Logger;

/**
 * helper for temporary file creation 
 * @author pit
 *
 */
public class TempFileHelper {
	private static Logger log = Logger.getLogger(TempFileHelper.class);
	public static final String gf_tempfile_marker = ".gf.";
	private static File tmpDirectory;
	
	/**
	 * create a temporary file in the directory specified in the preferences
	 * @param name - the of the file to generate a respective temporary file 
	 * @return - the temporary {@link File}
	 * @throws IOException - arrgh
	 */
	public static File createTempFileFromFilename( String name) throws IOException {		
		File gfDirectory = getTempDirectory();		
		return File.createTempFile(name + gf_tempfile_marker, null, gfDirectory);
	}

	private static File getTempDirectory() {
		if (tmpDirectory != null)
			return tmpDirectory;
		
		GreyfacePlugin plugin = GreyfacePlugin.getInstance();
		String tempVar = plugin.getGreyfacePreferences(false).getTempDirectory();
		String tempDir = plugin.getVirtualPropertyResolver().resolve( tempVar);
		File directory = new File( tempDir);		
		directory.mkdirs();
		
		// 
		File gfDirectory = new File( directory, "devrock.gf");
		gfDirectory.mkdirs();
		
		tmpDirectory = gfDirectory;
		return tmpDirectory;
	}
	
	public static void purge() {
		File [] files = getTempDirectory().listFiles();
		int i = 0;
		if (files == null)
			return;
		
		for (File file : files) {
			boolean deleted = file.delete();
			if (deleted) {
				i++;
			}
		}		
		log.debug("purged [" + i + "] files from [" + getTempDirectory() + "]");
	}
	
	/**
	 * extracts the nucleus name of the temporary file (inverse to the wrapping of the {@link #createTempFileFromFilename(String)} function 
	 * @param tempfile - the name of the temporary file 
	 * @return - the original name 
	 */
	public static String extractFilenameFromTempFile( String tempfile){
		int p = tempfile.lastIndexOf( gf_tempfile_marker);
		if (p < 0)
			return tempfile;
		return tempfile.substring(0, p);
	}
	
	public static boolean isATempFile( String tempfile){
		if (tempfile.contains( gf_tempfile_marker) && tempfile.endsWith( ".tmp")) {
			return true;
		}
		return false;
	}
}
