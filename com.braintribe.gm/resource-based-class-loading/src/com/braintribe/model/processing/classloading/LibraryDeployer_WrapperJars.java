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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

/**
 * @author peter.gazdik
 */
public class LibraryDeployer_WrapperJars extends AbstractLibraryDeployer {

	/**
	 * Deploys library, which is given by an index file - a text file containing a list of load-able jars, with one entry per line.
	 * 
	 * @param resourceAwareClassLoader
	 *            {@link ClassLoader} which is able to load given resource (zip file containing jars), BUT ALSO the parent for the returned
	 *            ClassLoader. We assume we can use the same ClassLoade for both. The parent is important as the one to load all the classes
	 *            that we might have accessible in the context which invokes this method (e.g. GenericEntity).
	 * @param libFolder
	 *            folder to contain the jars
	 *
	 * @param libIndexFileName
	 *            name of the index file which can be loaded with our {@link ClassLoader}
	 * 
	 * @return instance of {@link DeployedLibrary} with a {@link URLClassLoader} for given jars
	 * 
	 * @throws LibraryDeploymentException
	 */
	public static ClassLoader deployLibrary(ClassLoader resourceAwareClassLoader, File libFolder, String libIndexFileName,
			ClassFilter classFilter) throws LibraryDeploymentException {

		return new LibraryDeployer_WrapperJars(resourceAwareClassLoader, libFolder, libIndexFileName, classFilter).deploy();
	}

	// ############################################
	// ## . . . . . . Implementation . . . . . . ##
	// ############################################

	private final boolean removeHashFromClassName = false;

	public LibraryDeployer_WrapperJars(ClassLoader resourceAwareClassLoader, File libFolder, String libIndexFileName,
			ClassFilter classFilter) {

		super(resourceAwareClassLoader, libFolder, libIndexFileName, classFilter);
	}

	@Override
	protected void deploy(File outputFile, String libFileName) throws LibraryDeploymentException {
		String className = getClassNameFromJarName(libFileName);

		Class<?> providerClass = getProviderClass(libFileName, className);

		try (InputStream is = getLibraryInputStream(providerClass)) {
			writeMissingLibFile(is, outputFile);

		} catch (IOException e) {
			throw new LibraryDeploymentException("Error whhile getting resource as stream. Resource name: " + libIndexFileName, e);
		}
	}

	private Class<?> getProviderClass(String libFileName, String className) throws LibraryDeploymentException {
		try {
			return Class.forName(className, true, resourceAwareClassLoader);

		} catch (ClassNotFoundException e) {
			throw new LibraryDeploymentException("Could not load jar-providing class '" + className + "', for library file: " + libFileName,
					e);
		}
	}

	private InputStream getLibraryInputStream(Class<?> providerClass) {
		try {
			return tryGetLibraryInputStream(providerClass);

		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Error while getting library jar input stream for class: " + providerClass.getName(), e);
		}
	}

	private InputStream tryGetLibraryInputStream(Class<?> providerClass) throws ReflectiveOperationException {
		Object provider = providerClass.newInstance();
		Method provideMethod = providerClass.getMethod("provide");

		return (InputStream) provideMethod.invoke(provider);
	}

	private String getClassNameFromJarName(String jarName) {
		if (removeHashFromClassName) {
			return escape(removeMd5HashAndJarExtension(jarName));
		} else {
			return escape(removeJarExtension(jarName));
		}
	}

	private static String removeJarExtension(String jarName) {
		return jarName.substring(0, jarName.length() - ".jar".length());
	}

	private String removeMd5HashAndJarExtension(String jarName) {
		int i = jarName.lastIndexOf('-');
		return jarName.substring(0, i);
	}

	private static String escape(String s) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if ((i == 0 && !Character.isJavaIdentifierStart(c)) || (i > 0 && !Character.isJavaIdentifierPart(c)) || c == '$') {
				sb.append('$');
				sb.append((int) c);
				sb.append('$');

			} else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

}
