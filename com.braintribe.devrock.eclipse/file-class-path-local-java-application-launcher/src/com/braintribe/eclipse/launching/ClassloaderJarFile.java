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
package com.braintribe.eclipse.launching;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class ClassloaderJarFile {
	private static File jarFile;
	private static final String jarName = "FileClasspathClassloader-1.0.jar";
	
	protected static synchronized File getJarFile() throws IOException {
		if (jarFile == null) {
			File directory = Activator.getDefault().getStateLocation().toFile();
			File newJarFile = new File(directory, jarName);

			if (!newJarFile.exists()) {
				URL url = ClassloaderJarFile.class
						.getResource(jarName);
				InputStream in = url.openStream();
				OutputStream out = new FileOutputStream(newJarFile);
				try {
					byte buffer[] = new byte[32768];
					for (int len = 0; (len = in.read(buffer)) != -1;)
						out.write(buffer, 0, len);

					out.flush();
				} finally {
					in.close();
					out.close();
				}
			}

			jarFile = newJarFile;
		}

		return jarFile;
	}


}
