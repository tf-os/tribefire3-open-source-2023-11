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
package com.braintribe.devrock.artifactcontainer.control.container;

import java.io.File;
import java.io.IOException;

import com.braintribe.logging.Logger;

public class TemporaryFilestorage {
	private static Logger log = Logger.getLogger(TemporaryFilestorage.class);
	private static File tmpDirectory;
	
	private static File getTmpDirectory() {
		if (tmpDirectory != null) {
			return tmpDirectory;
		}
		File javaTmpDirectory = new File( System.getProperty( "java.io.tmpdir"));
		tmpDirectory = new File( javaTmpDirectory, "devrock.ac");
		tmpDirectory.mkdirs();
		return tmpDirectory;
	}
	
	public static File createTempFile( String prefix, String suffix) throws IOException {
		File tf = File.createTempFile(prefix, suffix, getTmpDirectory());
		return tf;
	}
	
	public static void purge() {
		File [] files = getTmpDirectory().listFiles();
		int i = 0;
		if (files == null)
			return;
		
		for (File file : files) {
			boolean deleted = file.delete();
			if (deleted) {
				i++;
			}
		}
		
		log.debug("purged [" + i + "] files from [" + tmpDirectory + "]");
	}
}
