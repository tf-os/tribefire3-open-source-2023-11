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
package com.braintribe.utils.classloader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.braintribe.logging.Logger;

/**
 *
 * This tools class provides utility methods that deal with ClassLoader issues.
 *
 */
public class ClassLoaderTools {

	private static Logger logger = Logger.getLogger(ClassLoaderTools.class);

	/**
	 * Provides a list of all classes reachable by the provided ClassLoader.
	 *
	 * @param classloader
	 *            The ClassLoader
	 * @return A Set of all classes that are reachable via the provided ClassLoader.
	 * @throws IOException
	 *             Thrown if an I/O error occured.
	 */
	public static Set<String> getAllClasses(ClassLoader classloader) throws IOException {

		Set<Resource> resources = getAllResources(classloader);

		Set<String> result = new HashSet<>();
		for (Resource resource : resources) {
			if (resource instanceof ClassResource) {
				ClassResource cr = (ClassResource) resource;
				result.add(cr.getName());
			}
		}
		return result;
	}

	/**
	 * Returns all resources available via the provided ClassLoader.
	 *
	 * @param classloader
	 *            The ClassLoader
	 * @return A Set of all {@link Resource}s available.
	 * @throws IOException
	 *             Thrown in the event of an I/O error.
	 */
	public static Set<Resource> getAllResources(ClassLoader classloader) throws IOException {

		Set<URI> scannedUris = new HashSet<>();
		Map<URI, ClassLoader> classpathEntries = getClassPathEntries(classloader);
		Set<Resource> resources = new HashSet<>();

		for (Map.Entry<URI, ClassLoader> entry : classpathEntries.entrySet()) {
			URI uri = entry.getKey();
			ClassLoader loader = entry.getValue();
			scan(uri, loader, scannedUris, resources);
		}

		return resources;

	}

	protected static void scan(URI uri, ClassLoader classloader, Set<URI> scannedUris, Set<Resource> resources) throws IOException {

		if (uri.getScheme().equals("file") && scannedUris.add(uri)) {
			File file = new File(uri);

			if (file.exists()) {

				if (file.isDirectory()) {
					scanDirectory(file, classloader, "", new HashSet<File>(), scannedUris, resources);
				} else {
					scanJar(file, classloader, scannedUris, resources);
				}
			}
		}
	}

	protected static void scanDirectory(File directory, ClassLoader classloader, String packagePrefix, Set<File> ancestors, Set<URI> scannedUris,
			Set<Resource> resources) throws IOException {

		File canonical = directory.getCanonicalFile();
		if (ancestors.contains(canonical)) {
			// A cycle in the filesystem, for example due to a symbolic link.
			return;
		}
		File[] files = directory.listFiles();
		if (files == null) {
			logger.warn("Cannot read directory " + directory);
			// IO error, just skip the directory
			return;
		}

		ancestors.add(canonical);

		for (File f : files) {
			String name = f.getName();
			if (f.isDirectory()) {
				scanDirectory(f, classloader, packagePrefix + name + "/", ancestors, scannedUris, resources);
			} else {
				String resourceName = packagePrefix + name;
				if (!resourceName.equals(JarFile.MANIFEST_NAME)) {
					resources.add(Resource.create(resourceName, classloader));
				}
			}
		}
	}

	protected static void scanJar(File file, ClassLoader classloader, Set<URI> scannedUris, Set<Resource> resources) throws IOException {
		JarFile jarFile;
		try {
			jarFile = new JarFile(file);
		} catch (IOException e) {
			// Not a jar file
			return;
		}
		try {
			for (URI uri : getClassPathFromManifest(file, jarFile.getManifest())) {
				scan(uri, classloader, scannedUris, resources);
			}
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (entry.isDirectory() || entry.getName().equals(JarFile.MANIFEST_NAME)) {
					continue;
				}
				resources.add(Resource.create(entry.getName(), classloader));
			}
		} finally {
			try {
				jarFile.close();
			} catch (IOException ignored) {
				// Ignore
			}
		}
	}

	protected static Set<URI> getClassPathFromManifest(File jarFile, Manifest manifest) {
		Set<URI> result = new HashSet<>();
		if (manifest == null) {
			return result;
		}

		String classpathAttribute = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH.toString());

		if (classpathAttribute != null) {
			for (String path : classpathAttribute.split(" ")) {
				URI uri;
				try {
					uri = getClassPathEntry(jarFile, path);
				} catch (URISyntaxException e) {
					// Ignore bad entry
					logger.warn("Invalid Class-Path entry: " + path);
					continue;
				}
				result.add(uri);
			}
		}
		return result;
	}

	protected static URI getClassPathEntry(File jarFile, String path) throws URISyntaxException {
		URI uri = new URI(path);
		if (uri.isAbsolute()) {
			return uri;
		} else {
			return new File(jarFile.getParentFile(), path.replace('/', File.separatorChar)).toURI();
		}
	}

	public static Map<URI, ClassLoader> getClassPathEntries(ClassLoader classloader) {
		LinkedHashMap<URI, ClassLoader> entries = new LinkedHashMap<>();
		// Search parent first, since it's the order ClassLoader#loadClass() uses.
		ClassLoader parent = classloader.getParent();
		if (parent != null) {
			entries.putAll(getClassPathEntries(parent));
		}
		if (classloader instanceof URLClassLoader) {
			URLClassLoader urlClassLoader = (URLClassLoader) classloader;
			for (URL entry : urlClassLoader.getURLs()) {
				URI uri;
				try {
					uri = entry.toURI();
				} catch (URISyntaxException e) {
					throw new IllegalArgumentException(e);
				}
				if (!entries.containsKey(uri)) {
					entries.put(uri, classloader);
				}
			}
		}
		return entries;
	}

	protected static URLClassLoader getUrlClassloader(ClassLoader classloader) {
		if (classloader == null) {
			return null;
		}
		if (classloader instanceof URLClassLoader) {
			return (URLClassLoader) classloader;
		}
		return getUrlClassloader(classloader.getParent());
	}

	/**
	 * Given a Class object, attempts to find its .class location [returns null if no such definition can be found]. Use for testing/debugging only.
	 *
	 * @return URL that points to the class definition [null if not found].
	 */
	public static URL getClassLocation(final Class<?> cls) {
		if (cls == null) {
			throw new IllegalArgumentException("null input: cls");
		}

		URL result = null;
		final String clsAsResource = cls.getName().replace('.', '/').concat(".class");

		final ProtectionDomain pd = cls.getProtectionDomain();
		// java.lang.Class contract does not specify if 'pd' can ever be null;
		// it is not the case for Sun's implementations, but guard against null
		// just in case:
		if (pd != null) {
			final CodeSource cs = pd.getCodeSource();
			// 'cs' can be null depending on the classloader behavior:
			if (cs != null) {
				result = cs.getLocation();
			}

			if (result != null) {
				// Convert a code source location into a full class file location
				// for some common cases:
				if ("file".equals(result.getProtocol())) {
					try {
						if (result.toExternalForm().endsWith(".jar") || result.toExternalForm().endsWith(".zip")) {
							result = new URL("jar:".concat(result.toExternalForm()).concat("!/").concat(clsAsResource));
						} else if (new File(result.getFile()).isDirectory()) {
							result = new URL(result, clsAsResource);
						}
					} catch (MalformedURLException ignore) {
						// ignore
					}
				}
			}
		}

		if (result == null) {
			// Try to find 'cls' definition as a resource; this is not
			// documented to be legal, but Sun's implementations seem to allow this:
			final ClassLoader clsLoader = cls.getClassLoader();

			result = clsLoader != null ? clsLoader.getResource(clsAsResource) : ClassLoader.getSystemResource(clsAsResource);
		}

		return result;
	}

}
