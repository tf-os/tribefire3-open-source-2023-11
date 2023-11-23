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
package com.braintribe.model.processing.classloading;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;

/**
 * @author peter.gazdik
 */
public abstract class AbstractLibraryDeployer {

	private static final Logger log = Logger.getLogger(AbstractLibraryDeployer.class);

	protected final ClassLoader resourceAwareClassLoader;
	protected final File libFolder;
	protected final String libIndexFileName;
	protected final ClassFilter classFilter;

	protected Set<String> libFileNames;
	protected Set<String> existingFileNames;
	protected URL[] urls;


	public AbstractLibraryDeployer(ClassLoader resourceAwareClassLoader, File libFolder, String libIndexFileName, ClassFilter classFilter) {
		this.resourceAwareClassLoader = resourceAwareClassLoader;
		this.libFolder = libFolder;
		this.libIndexFileName = libIndexFileName;
		this.classFilter = classFilter;
	}

	protected ClassLoader deploy() throws LibraryDeploymentException {
		ensureLibFolder();
		readLibFileNamesFromIndexFile();
		findExistingJarFileNamesInLibFolder();
		copyMissingJarsToLibFolder();
		removeObsoleteJarsFromLibFolderIfPossible();

		return new ParentLastUrlClassLoader(urls, resourceAwareClassLoader, classFilter);
	}

	protected void ensureLibFolder() throws LibraryDeploymentException {
		if (libFolder.exists()) {
			if (!libFolder.isDirectory()) {
				throw new LibraryDeploymentException(
						"Given lib folder is actually an existing file that is not a directory: " + libFolder.getAbsolutePath());
			}
			return;
		}

		if (!libFolder.mkdirs()) {
			throw new LibraryDeploymentException("Could not create lib folder for given path: " + libFolder.getAbsolutePath());
		}
	}

	protected void readLibFileNamesFromIndexFile() throws LibraryDeploymentException {
		libFileNames = newSet();

		try (InputStream is = resourceAwareClassLoader.getResourceAsStream(libIndexFileName)) {
			if (is == null) {
				throw new LibraryDeploymentException("LibIndex file not found by given classloader. Given file name: " + libIndexFileName);
			}

			BufferedReader libIndexFileReader = new BufferedReader(new InputStreamReader(is));

			while (true) {
				String fileName = libIndexFileReader.readLine();
				if (fileName == null) {
					return;
				}
				libFileNames.add(fileName);
			}

		} catch (IOException e) {
			throw new LibraryDeploymentException("Error while getting resource as stream. Resource name: " + libIndexFileName, e);
		}
	}

	protected void findExistingJarFileNamesInLibFolder() {
		existingFileNames = newSet();
		for (File libFile: libFolder.listFiles()) {
			existingFileNames.add(libFile.getName());
		}
	}

	protected void copyMissingJarsToLibFolder() throws LibraryDeploymentException {
		urls = new URL[libFileNames.size()];

		int index = 0;
		for (String libFileName: libFileNames) {
			File outputFile = new File(libFolder, libFileName);

			if (!existingFileNames.contains(libFileName)) {
				deploy(outputFile, libFileName);
			}

			urls[index++] = toUrl(outputFile);
		}
	}

	private URL toUrl(File file) throws LibraryDeploymentException {
		try {
			return file.toURI().toURL();

		} catch (MalformedURLException e) {
			throw new LibraryDeploymentException("Problem getting URL for file: " + file.getAbsolutePath(), e);
		}
	}

	protected abstract void deploy(File outputFile, String libFileName) throws LibraryDeploymentException;

	protected void removeObsoleteJarsFromLibFolderIfPossible() {
		for (String existingFileName: existingFileNames) {
			if (!libFileNames.contains(existingFileName)) {
				deleteIfPossible(libFolder, existingFileName);
			}
		}
	}

	private void deleteIfPossible(File libFolder, String existingFileName) {
		File existingFile = new File(libFolder, existingFileName);
		if (existingFile.exists()) {
			try {
				if (!existingFile.delete()) {

					if (existingFile.exists()) {
						log.warn("Unable to delete jar file: " + existingFile.getAbsolutePath() +
								". Delete method returned false, but the file still exists.");
					}
				}

			} catch (Exception e) {
				log.warn("Unable to delete jar file: " + existingFile.getAbsolutePath(), e);
			}
		}
	}

	/**
	 * This method writes a newly deployed lib file. The library content is given by the {@link InputStream} ant the output location is
	 * given by the {@link File}.
	 * 
	 * This file was chosen because it doesn't exist (i.e. needs to be deployed), however, if we are deploying concurrently (e.g. multiple
	 * JVMs on one server), it might be deployed by somebody else too and this method handles that. First it checks whether the file exists,
	 * and proceeds only if it doesn't. But even then, if we were not able to write, maybe it was because the file was being written to at
	 * that moment, so we check after as well.
	 */
	protected void writeMissingLibFile(InputStream inputStream, File file) throws LibraryDeploymentException {
		if (file.exists()) {
			// if it was written in the meantime by another (concurrently running) deployer, we can skip writing
			return;
		}

		try {
			IOTools.inputToFile(inputStream, file);

		} catch (IOException e) {
			if (!file.exists()) {
				throw new LibraryDeploymentException("Error while storing the resource stream to file: " + file.getAbsolutePath(), e);
			}
		}
	}

}
