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
package com.braintribe.model.processing.platformreflection.tf;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.packaging.Dependency;
import com.braintribe.model.packaging.Packaging;

/**
 * Best effort packaging supplier that requires a path to the WEB-INF/lib folder
 * 
 */
public class FileBasedPackagingProvider implements Supplier<Packaging> {

	private static Logger logger = Logger.getLogger(FileBasedPackagingProvider.class);
	
	private File libFolder;
	
	@Override
	public Packaging get() {
		if (libFolder == null) {
			logger.trace(() -> "No lib folder configured.");
			return null;
		}
		
		Packaging packaging = Packaging.T.create();
		packaging.setTimestamp(new Date());
		List<Dependency> dependencies = packaging.getDependencies();
		
		File[] files = libFolder.listFiles(f -> f.isFile() && f.getName().toLowerCase().endsWith(".jar"));
		
		if (files != null) {
			Arrays.sort(files);
			for (File f : files) {
				
				Dependency dep = Dependency.T.create();
				
				String name = f.getName();
				//Get rid of extension
				name = name.substring(0, name.length()-4);
				
				int index = name.lastIndexOf("-");
				if (index > 0 && index < (name.length()-1)) {

					String artifactId = name.substring(0, index);
					String version = name.substring(index+1);
					
					dep.setArtifactId(artifactId);
					dep.setVersion(version);
					
				} else {
					dep.setArtifactId(name);
				}
				
				dependencies.add(dep);				
			}
		}
		
		
		return packaging;
	}

	@Required
	public void setLibFolder(File libFolder) {
		if (libFolder == null) {
			logger.warn("The library folder is set to null.");
			return;
		}
		if (libFolder.exists() && libFolder.isDirectory()) {
			logger.info(() -> "Accepting library folder: "+libFolder.getAbsolutePath());
			this.libFolder = libFolder;
		} else {
			logger.info(() -> "Not accepting library folder: "+libFolder.getAbsolutePath());
		}
	}


}
